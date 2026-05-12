package com.easyentry.app.data.local.db

import androidx.room.Embedded
import androidx.room.Relation

data class DeviceGroupWithDevices(
    @Embedded val group: DeviceGroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "deviceGroupId"
    )
    val devices: List<DeviceEntity>
)
