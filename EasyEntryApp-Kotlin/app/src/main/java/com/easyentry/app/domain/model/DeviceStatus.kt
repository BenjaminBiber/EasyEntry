package com.easyentry.app.domain.model

enum class DeviceStatus(val value: Int, val label: String) {
    OPENED(1, "geöffnet"),
    CLOSED(2, "geschlossen"),
    NEUTRAL(3, "Stopp");

    companion object {
        fun fromValue(v: Int) = entries.firstOrNull { it.value == v } ?: NEUTRAL
    }
}
