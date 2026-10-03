package com.easyentry.app.data.remote

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.util.Collections
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

class ProbeDiagnosticsTest {

    /**
     * recordFailure wird von parallel laufenden Probes aufgerufen. Die Formatierung darf dabei
     * weder Zeitstempel verfaelschen noch werfen: sie liegt im catch-Block von DeviceProbe, eine
     * Exception dort wuerde ein Geraet ohne weitere Versuche als offline markieren.
     */
    @Test
    fun `concurrent formatting keeps every timestamp intact`() {
        val diagnostics = ProbeDiagnostics()
        val base = 1_790_000_000_000L
        repeat(50) { i ->
            // Ueber Stunden und Tage verteilt, damit ein zerschossener Kalender auffaellt.
            diagnostics.recordFailure(
                host = "10.0.0.$i",
                attempt = i % 3,
                error = IOException("timeout"),
                durationMs = 100L + i,
                atMs = base + i * 3_723_456L,
            )
        }
        val expected = diagnostics.asShareText()

        val mismatches = AtomicInteger()
        val errors = Collections.synchronizedList(mutableListOf<Throwable>())
        val start = CountDownLatch(1)
        val workers = List(8) {
            thread {
                start.await()
                repeat(500) {
                    try {
                        if (diagnostics.asShareText() != expected) mismatches.incrementAndGet()
                    } catch (t: Throwable) {
                        errors += t
                    }
                }
            }
        }
        start.countDown()
        workers.forEach { it.join() }

        assertEquals("Exceptions beim Formatieren: $errors", 0, errors.size)
        assertEquals("Ausgaben mit falschen Zeitstempeln", 0, mismatches.get())
    }
}
