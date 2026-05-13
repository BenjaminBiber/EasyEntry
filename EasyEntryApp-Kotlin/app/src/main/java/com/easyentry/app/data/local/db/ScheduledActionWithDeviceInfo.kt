package com.easyentry.app.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class ScheduledActionWithDeviceInfo(
    @Embedded val action: ScheduledActionEntity,
    @ColumnInfo(name = "deviceName") val deviceName: String,
    @ColumnInfo(name = "deviceUrl") val deviceUrl: String
)
