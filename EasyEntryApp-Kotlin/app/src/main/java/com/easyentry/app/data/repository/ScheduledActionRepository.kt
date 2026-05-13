package com.easyentry.app.data.repository

import com.easyentry.app.data.local.db.ScheduledActionEntity
import com.easyentry.app.data.local.db.ScheduledActionWithDeviceInfo
import kotlinx.coroutines.flow.Flow

interface ScheduledActionRepository {
    fun getAllActions(): Flow<List<ScheduledActionWithDeviceInfo>>
    suspend fun addAction(
        deviceIds: List<Int>,
        actionStatusValue: Int,
        isRecurring: Boolean,
        dayOfWeekBitmask: Int,
        hourOfDay: Int,
        minuteOfHour: Int,
        delayMinutes: Int,
        label: String
    ): Result<Unit>
    suspend fun updateAction(action: ScheduledActionEntity): Result<Unit>
    suspend fun deleteAction(id: Int): Result<Unit>
    suspend fun setEnabled(id: Int, enabled: Boolean): Result<Unit>
}
