package com.easyentry.app.data.local.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scheduled_actions",
    foreignKeys = [
        ForeignKey(
            entity = DeviceEntity::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deviceId")]
)
data class ScheduledActionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: Int,
    val actionStatusValue: Int,
    val isRecurring: Boolean,
    val dayOfWeekBitmask: Int,
    val hourOfDay: Int,
    val minuteOfHour: Int,
    val delayMinutes: Int,
    val workManagerTagId: String,
    val isEnabled: Boolean = true,
    val label: String = "",
    val deviceIds: String = ""
)
