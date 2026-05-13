package com.easyentry.app.widget

import com.easyentry.app.alarm.ScheduleAlarmManager
import com.easyentry.app.data.local.db.DeviceDao
import com.easyentry.app.data.local.db.ScheduledActionDao
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ScheduleWidgetEntryPoint {
    fun scheduledActionDao(): ScheduledActionDao
    fun scheduleAlarmManager(): ScheduleAlarmManager
    fun deviceDao(): DeviceDao
}
