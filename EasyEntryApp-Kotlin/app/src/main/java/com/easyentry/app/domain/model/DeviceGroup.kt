package com.easyentry.app.domain.model

data class DeviceGroup(
    val id: Int,
    val groupName: String,
    val color: Long,
    val icon: GroupIcon,
    val devices: List<Device> = emptyList()
)
