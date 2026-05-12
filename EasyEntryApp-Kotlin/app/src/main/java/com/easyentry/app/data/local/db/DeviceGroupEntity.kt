package com.easyentry.app.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_groups")
data class DeviceGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val color: Long = 0xFF60ADC9L,
    val icon: String = "HOME"
)
