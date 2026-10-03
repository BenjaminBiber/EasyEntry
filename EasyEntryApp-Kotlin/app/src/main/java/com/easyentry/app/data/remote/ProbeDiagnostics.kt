package com.easyentry.app.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ringpuffer der letzten Probe-Fehlversuche. Zweck: die Ursache sporadischer
 * "nicht erreichbar"-Meldungen im Feld bestimmen, wo kein `adb logcat` verfügbar ist.
 *
 * Entscheidend ist [ProbeEvent.msSinceLastSuccess]: veraltete gepoolte Verbindungen
 * zeigen sich als Reset/EOF bei Versuch 0 mit Erfolg bei Versuch 1, korreliert mit einem
 * Abstand zwischen ca. 5 s und 5 min zum letzten Erfolg. Paketverlust durch Modem-Sleep
 * zeigt sich dagegen als Timeout UNABHÄNGIG von diesem Abstand.
 */
@Singleton
class ProbeDiagnostics @Inject constructor() {

    companion object {
        private const val CAPACITY = 200
        // DateTimeFormatter statt SimpleDateFormat: format() laeuft ausserhalb des Locks und
        // parallel aus mehreren Probes, SimpleDateFormat ist dafuer nicht thread-safe.
        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS", Locale.GERMANY)
            .withZone(ZoneId.systemDefault())
    }

    data class ProbeEvent(
        val atMs: Long,
        val host: String,
        /** 0-basiert: 0 ist der erste Versuch. */
        val attempt: Int,
        val errorName: String,
        val errorMessage: String?,
        val durationMs: Long,
        /** Abstand zum letzten erfolgreichen Request an denselben Host, oder null. */
        val msSinceLastSuccess: Long?,
    )

    private val lock = Any()
    private val buffer = ArrayDeque<ProbeEvent>(CAPACITY)
    private val lastSuccessPerHost = HashMap<String, Long>()

    private val _events = MutableStateFlow<List<ProbeEvent>>(emptyList())
    val events: StateFlow<List<ProbeEvent>> = _events.asStateFlow()

    fun recordFailure(
        host: String,
        attempt: Int,
        error: Throwable,
        durationMs: Long,
        atMs: Long = System.currentTimeMillis(),
    ) {
        val event = synchronized(lock) {
            val event = ProbeEvent(
                atMs = atMs,
                host = host,
                attempt = attempt,
                errorName = error.javaClass.simpleName,
                errorMessage = error.message?.take(120),
                durationMs = durationMs,
                msSinceLastSuccess = lastSuccessPerHost[host]?.let { atMs - it },
            )
            if (buffer.size >= CAPACITY) buffer.removeFirst()
            buffer.addLast(event)
            event
        }
        _events.value = snapshot()
        // Immer auch nach logcat, für den Fall dass ein Gerät angeschlossen ist.
        android.util.Log.w("ProbeDiagnostics", format(event))
    }

    fun recordSuccess(host: String, atMs: Long = System.currentTimeMillis()) {
        synchronized(lock) { lastSuccessPerHost[host] = atMs }
    }

    fun clear() {
        synchronized(lock) {
            buffer.clear()
            lastSuccessPerHost.clear()
        }
        _events.value = emptyList()
    }

    private fun snapshot(): List<ProbeEvent> = synchronized(lock) { buffer.toList() }

    /** Aufbereitet zum Teilen per Share-Intent. Neueste Zeile zuletzt. */
    fun asShareText(): String {
        val events = snapshot()
        if (events.isEmpty()) return "Keine Probe-Fehler aufgezeichnet."
        return buildString {
            appendLine("EasyEntry Probe-Diagnose (${events.size} Einträge)")
            appendLine("Zeit | Host | Versuch | Fehler | Dauer | seit letztem Erfolg")
            events.forEach { appendLine(format(it)) }
        }
    }

    private fun format(e: ProbeEvent): String = buildString {
        append(TIME_FORMAT.format(Instant.ofEpochMilli(e.atMs)))
        append(" | ").append(e.host)
        append(" | #").append(e.attempt)
        append(" | ").append(e.errorName)
        e.errorMessage?.let { append(": ").append(it) }
        append(" | ").append(e.durationMs).append("ms")
        append(" | ").append(e.msSinceLastSuccess?.let { "${it}ms" } ?: "nie")
    }
}
