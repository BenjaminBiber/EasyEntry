package com.easyentry.app.domain.model

/**
 * Erreichbarkeit eines Geräts. Dreiwertig: "noch nicht geprüft" ist ausdrücklich
 * NICHT dasselbe wie "nicht erreichbar" und darf in der UI nicht als Fehler erscheinen.
 */
enum class Reachability { UNKNOWN, ONLINE, OFFLINE }

data class ReachabilityEntry(
    val state: Reachability,
    /** Zeitpunkt, zu dem dieses Ergebnis entstand — Basis für den Veraltungs-Schutz. */
    val checkedAtMs: Long,
    /** Klassenname der letzten Exception, nur zur Diagnose. */
    val lastErrorName: String? = null,
)
