package com.easyentry.app.data.remote

import com.easyentry.app.data.remote.api.EspApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

data class ProbeTarget(val id: Int, val url: String)

sealed interface ProbeResult {
    /** Das Gerät hat geantwortet. Auch ein 404 oder 500 beweist, dass der Server läuft. */
    data class Reachable(val httpCode: Int) : ProbeResult

    data class Unreachable(val errorName: String, val attempts: Int) : ProbeResult
}

/**
 * Erreichbarkeitsprüfung eines einzelnen Geräts.
 *
 * Zwei bewusste Entscheidungen:
 *
 * 1. **Retry hier statt als OkHttp-Interceptor.** Der `OkHttpClient` ist ein Singleton und
 *    wird von `controlDoor` (PUT) mitbenutzt; der ESP pulst bei jedem PUT das Relais. Ein
 *    Interceptor-Retry würde ein Tor doppelt ansteuern. Ausserdem braucht die Diagnose die
 *    Versuchsnummer, die nur hier bekannt ist.
 *
 * 2. **Ein Mutex pro Host.** Der ESP-Sketch nutzt `WebServer` mit `handleClient()` in `loop()`
 *    und bedient damit strikt eine Verbindung zur Zeit. Zwei gleichzeitige Requests an dasselbe
 *    Gerät führen dazu, dass einer wartet oder verworfen wird.
 */
@Singleton
class DeviceProbe @Inject constructor(
    private val espApi: EspApi,
    private val config: EspNetworkConfig,
    private val diagnostics: ProbeDiagnostics,
) {

    private val hostLocks = ConcurrentHashMap<String, Mutex>()

    suspend fun probe(url: String): ProbeResult =
        hostLocks.getOrPut(url) { Mutex() }.withLock {
            var lastError: Throwable? = null

            repeat(config.probeAttempts) { attempt ->
                if (attempt > 0) delay(config.backoffMs[attempt])

                val startedAt = System.currentTimeMillis()
                try {
                    val response = espApi.ping("http://$url/")
                    diagnostics.recordSuccess(url)
                    return@withLock ProbeResult.Reachable(response.code())
                } catch (ce: CancellationException) {
                    // Abbruch ist kein Geräte-Fehler und darf nie als "offline" gedeutet werden.
                    throw ce
                } catch (e: IOException) {
                    lastError = e
                    diagnostics.recordFailure(url, attempt, e, System.currentTimeMillis() - startedAt)
                } catch (e: Exception) {
                    // Etwa eine unbrauchbare URL — ein Retry würde daran nichts ändern.
                    diagnostics.recordFailure(url, attempt, e, System.currentTimeMillis() - startedAt)
                    return@withLock ProbeResult.Unreachable(e.javaClass.simpleName, attempt + 1)
                }
            }

            ProbeResult.Unreachable(
                errorName = lastError?.javaClass?.simpleName ?: "Unknown",
                attempts = config.probeAttempts,
            )
        }
}
