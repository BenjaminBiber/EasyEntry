package com.easyentry.app.domain.model

data class Device(
    val id: Int,
    val name: String,
    val status: DeviceStatus,
    val deviceUrl: String,
    val isOpened: Boolean,
    val deviceGroupId: Int
)
