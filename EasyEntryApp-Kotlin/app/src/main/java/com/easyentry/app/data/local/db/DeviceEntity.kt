package com.easyentry.app.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "devices",
    foreignKeys = [
        ForeignKey(
            entity = DeviceGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceGroupId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deviceGroupId")]
)
data class DeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val statusValue: Int,
    val deviceUrl: String,
    val isOpened: Boolean,
    val deviceGroupId: Int,
    val position: Int = 0
)
