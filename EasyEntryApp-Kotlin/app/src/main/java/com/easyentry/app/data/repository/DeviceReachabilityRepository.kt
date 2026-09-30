package com.easyentry.app.data.repository

import com.easyentry.app.data.remote.DeviceProbe
import com.easyentry.app.data.remote.ProbeResult
import com.easyentry.app.data.remote.ProbeTarget
import com.easyentry.app.di.ApplicationScope
import com.easyentry.app.domain.model.Reachability
import com.easyentry.app.domain.model.ReachabilityEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Einzige Stelle in der App, die Geraete-Erreichbarkeit prueft, und einziger Besitzer des
 * Ergebnis-Caches. Bewusst als Singleton: vorher hatten HomeViewModel und
 * GroupDetailViewModel je eigene Probe-Schleifen, die sich gegenseitig ueberschrieben haben.
 *
 * Die drei Regeln, die den urspruenglichen Fehler unmoeglich machen:
 *
 * 1. Nur die neueste Runde zaehlt. mapLatest bricht eine laufende Runde ab, sobald ein neuer
 *    Ausloeser kommt. Ein Mutex waere hier falsch: er wuerde die Runden nur aneinander reihen,
 *    die veraltete liefe durch und wuerde am Ende trotzdem schreiben.
 * 2. Nie die ganze Map ersetzen. Jedes Ergebnis wird einzeln gemerged, sofort wenn es vorliegt.
 *    Die UI fuellt sich dadurch fortlaufend statt erst nach dem langsamsten Geraet.
 * 3. Ein aelteres Ergebnis ueberschreibt kein neueres. Siehe [publish].
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@Singleton
class DeviceReachabilityRepository @Inject constructor(
    deviceGroupRepository: DeviceGroupRepository,
    private val probe: DeviceProbe,
    @ApplicationScope private val scope: CoroutineScope,
) {

    private data class ProbeRequest(
        val targets: Set<ProbeTarget>,
        /** Gesetzt bei manuellem Neuladen, damit der Aufrufer auf das Ende warten kann. */
        val completion: CompletableDeferred<Unit>? = null,
    )

    private val cache = MutableStateFlow<Map<Int, ReachabilityEntry>>(emptyMap())

    /** Kanonischer Erreichbarkeits-Zustand. Fehlender Eintrag bedeutet UNKNOWN, nicht OFFLINE. */
    val status: StateFlow<Map<Int, ReachabilityEntry>> = cache.asStateFlow()

    /**
     * replay = 1, damit ein Refresh nicht verloren geht, der emittiert wird bevor der
     * Collector unten subscribed hat -- sonst wuerde das zugehoerige CompletableDeferred nie
     * completen und der Ladeindikator haengen bleiben.
     *
     * Bewusst kein DROP_OLDEST: ein verworfener Request laesst seinen Aufrufer haengen.
     * Der Default SUSPEND kann nicht stallen, weil mapLatest Elemente sofort annimmt.
     */
    private val manualRefresh = MutableSharedFlow<CompletableDeferred<Unit>>(
        replay = 1,
        extraBufferCapacity = 1,
    )

    /** Letzter bekannter Stand, um einen URL-Wechsel eines Geraets zu erkennen. */
    private var lastUrlById: Map<Int, String> = emptyMap()

    /**
     * Die zu pruefenden Geraete, entkoppelt von allem, was keine Pruefung rechtfertigt.
     *
     * - Set statt List: die Repository-Abbildung sortiert Geraete nach position, ein Reorder
     *   aendert also die Listenreihenfolge. Als Set ist ein Reorder unsichtbar.
     * - Nur id und url im Schluessel: Rename, isOpened und Gruppenwechsel loesen keine
     *   Pruefung aus.
     * - debounce VOR distinctUntilChanged: danach waere es fuer Bursts gleicher Schluessel
     *   wirkungslos. Davor fasst es einen Burst unterschiedlicher Schluessel zusammen, wie er
     *   beim Backup-Restore entsteht.
     */
    private val targets: StateFlow<Set<ProbeTarget>> =
        deviceGroupRepository.getGroupsWithDevices()
            .map { groups ->
                groups.asSequence()
                    .flatMap { it.devices.asSequence() }
                    .map { ProbeTarget(id = it.id, url = it.deviceUrl) }
                    .toSet()
            }
            .debounce(TARGET_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { syncCacheWith(it) }
            .stateIn(scope, SharingStarted.Eagerly, emptySet())

    init {
        merge(
            targets.map { ProbeRequest(it) },
            manualRefresh.map { done -> ProbeRequest(targets.value, done) },
        ).mapLatest { request ->
            try {
                probeRound(request.targets)
            } finally {
                // Auch bei Abbruch completen: ein ueberholtes Neuladen soll sofort
                // zurueckkehren statt den Spinner haengen zu lassen.
                request.completion?.complete(Unit)
            }
        }.catch { e ->
            // Eine durchgeschlagene Exception wuerde die Collection endgueltig beenden und
            // die Erreichbarkeitspruefung bis zum App-Neustart totlegen.
            android.util.Log.e(TAG, "Probe-Pipeline abgebrochen", e)
        }.launchIn(scope)
    }

    /**
     * Loest eine Runde ueber alle aktuellen Geraete aus und suspendiert, bis diese fertig oder
     * von einer neueren Runde ueberholt ist.
     */
    suspend fun refreshNow() {
        val done = CompletableDeferred<Unit>()
        manualRefresh.emit(done)
        done.await()
    }

    /** Einzelnes Geraet neu pruefen, etwa im Nachlauf eines Steuerbefehls. */
    suspend fun refreshOne(target: ProbeTarget) {
        val startedAtMs = System.currentTimeMillis()
        publish(target.id, probe.probe(target.url), startedAtMs)
    }

    private suspend fun probeRound(targets: Set<ProbeTarget>) {
        if (targets.isEmpty()) return
        coroutineScope {
            targets.forEach { target ->
                launch {
                    val startedAtMs = System.currentTimeMillis()
                    try {
                        // Pro Geraet sofort veroeffentlichen statt am Rundenende gesammelt.
                        publish(target.id, probe.probe(target.url), startedAtMs)
                    } catch (ce: CancellationException) {
                        throw ce
                    } catch (e: Exception) {
                        // Ein einzelnes Geraet darf die Runde fuer die anderen nicht killen:
                        // coroutineScope wuerde sonst alle Geschwister abbrechen.
                        android.util.Log.e(TAG, "Probe fehlgeschlagen: ${target.url}", e)
                        publish(
                            target.id,
                            ProbeResult.Unreachable(e.javaClass.simpleName, attempts = 1),
                            startedAtMs,
                        )
                    }
                }
            }
        }
    }

    /**
     * Schreibt ein Ergebnis, sofern nicht bereits ein juengeres vorliegt.
     *
     * Der Zeitstempel-Vergleich schuetzt gegen Schreiber ausserhalb der mapLatest-Pipeline,
     * insbesondere [refreshOne] aus dem Nachlauf eines Steuerbefehls.
     */
    private fun publish(deviceId: Int, result: ProbeResult, startedAtMs: Long) {
        val finishedAtMs = System.currentTimeMillis()
        cache.update { old ->
            val existing = old[deviceId]
            if (existing != null && existing.checkedAtMs > startedAtMs) {
                old
            } else {
                old + (deviceId to ReachabilityEntry(
                    state = when (result) {
                        is ProbeResult.Reachable -> Reachability.ONLINE
                        is ProbeResult.Unreachable -> Reachability.OFFLINE
                    },
                    checkedAtMs = finishedAtMs,
                    lastErrorName = (result as? ProbeResult.Unreachable)?.errorName,
                ))
            }
        }
    }

    /**
     * Haelt den Cache deckungsgleich mit den vorhandenen Geraeten: geloeschte fliegen raus, und
     * ein Geraet mit geaenderter URL faellt auf UNKNOWN zurueck, statt den Status der alten
     * Adresse weiterzuzeigen.
     */
    private fun syncCacheWith(targets: Set<ProbeTarget>) {
        val currentUrlById = targets.associate { it.id to it.url }
        val previousUrlById = lastUrlById
        lastUrlById = currentUrlById

        cache.update { old ->
            val kept = old.filterKeys { id ->
                currentUrlById.containsKey(id) && currentUrlById[id] == previousUrlById[id]
            }
            if (kept.size == old.size) old else kept
        }
    }

    private companion object {
        const val TAG = "DeviceReachability"
        const val TARGET_DEBOUNCE_MS = 200L
    }
}
