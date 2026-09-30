package com.easyentry.app.data.remote

/**
 * Netzwerk-Parameter für die ESP-Kommunikation. Als injizierbarer Wert gehalten,
 * damit Timeouts und Backoff an einer Stelle stehen und nachjustierbar sind.
 */
data class EspNetworkConfig(
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
    val writeTimeoutMs: Long,
    val callTimeoutMs: Long,
    /** Anzahl Versuche pro Erreichbarkeitsprüfung (nicht pro Steuerbefehl). */
    val probeAttempts: Int,
    /**
     * Wartezeit VOR Versuch n. `backoffMs[0]` wird nie benutzt.
     *
     * Bewusst nicht exponentiell, sondern am DTIM-Intervall orientiert: der Access Point
     * puffert Unicast-Pakete für einen ESP32 im Modem-Sleep bis zum nächsten DTIM-Beacon
     * (Default ca. 300 ms). Der zweite Versuch landet damit garantiert in einem anderen
     * Beacon-Fenster als der erste.
     */
    val backoffMs: LongArray,
) {
    init {
        require(probeAttempts >= 1) { "probeAttempts muss >= 1 sein" }
        require(backoffMs.size >= probeAttempts) { "backoffMs braucht einen Eintrag pro Versuch" }
    }

    // LongArray hat keine strukturelle equals/hashCode — data class deshalb nachziehen.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EspNetworkConfig) return false
        return connectTimeoutMs == other.connectTimeoutMs &&
            readTimeoutMs == other.readTimeoutMs &&
            writeTimeoutMs == other.writeTimeoutMs &&
            callTimeoutMs == other.callTimeoutMs &&
            probeAttempts == other.probeAttempts &&
            backoffMs.contentEquals(other.backoffMs)
    }

    override fun hashCode(): Int {
        var result = connectTimeoutMs.hashCode()
        result = 31 * result + readTimeoutMs.hashCode()
        result = 31 * result + writeTimeoutMs.hashCode()
        result = 31 * result + callTimeoutMs.hashCode()
        result = 31 * result + probeAttempts
        result = 31 * result + backoffMs.contentHashCode()
        return result
    }
}
