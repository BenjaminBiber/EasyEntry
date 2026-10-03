package com.easyentry.app.data.remote

/**
 * Monotone Uhr fuer Zeitvergleiche und Altersberechnungen.
 *
 * Bewusst nicht System.currentTimeMillis(): die Wanduhr springt bei NTP-Abgleich oder manueller
 * Umstellung, auch rueckwaerts. Ein Ruecksprung liesse jedes neue Ergebnis aelter aussehen als
 * das gespeicherte, und die Anzeige wuerde einfrieren. In Produktion SystemClock.elapsedRealtime().
 */
fun interface MonotonicClock {
    fun nowMs(): Long
}
