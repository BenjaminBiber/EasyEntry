package com.easyentry.app.data.repository

import com.easyentry.app.alarm.ScheduleAlarmManager
import com.easyentry.app.data.local.db.ScheduledActionDao
import com.easyentry.app.data.local.db.ScheduledActionEntity
import com.easyentry.app.data.local.db.ScheduledActionWithDeviceInfo
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduledActionRepositoryImpl @Inject constructor(
    private val dao: ScheduledActionDao,
    private val alarmManager: ScheduleAlarmManager
) : ScheduledActionRepository {

    override fun getAllActions(): Flow<List<ScheduledActionWithDeviceInfo>> =
        dao.getAllWithDeviceInfo()

    override suspend fun addAction(
        deviceIds: List<Int>,
        actionStatusValue: Int,
        isRecurring: Boolean,
        dayOfWeekBitmask: Int,
        hourOfDay: Int,
        minuteOfHour: Int,
        delayMinutes: Int,
        label: String
    ): Result<Unit> = runCatching {
        val entity = ScheduledActionEntity(
            deviceId = deviceIds.first(),
            deviceIds = deviceIds.joinToString(","),
            actionStatusValue = actionStatusValue,
            isRecurring = isRecurring,
            dayOfWeekBitmask = dayOfWeekBitmask,
            hourOfDay = hourOfDay,
            minuteOfHour = minuteOfHour,
            delayMinutes = delayMinutes,
            workManagerTagId = UUID.randomUUID().toString(),
            label = label
        )
        val id = dao.insert(entity).toInt()
        alarmManager.schedule(entity.copy(id = id))
    }

    override suspend fun updateAction(action: ScheduledActionEntity): Result<Unit> = runCatching {
        alarmManager.cancel(action.id)
        dao.update(action)
        if (action.isEnabled) {
            alarmManager.schedule(action)
        }
    }

    override suspend fun deleteAction(id: Int): Result<Unit> = runCatching {
        alarmManager.cancel(id)
        dao.deleteById(id)
    }

    override suspend fun setEnabled(id: Int, enabled: Boolean): Result<Unit> = runCatching {
        val action = dao.getById(id) ?: return@runCatching
        dao.setEnabled(id, enabled)
        if (enabled) {
            alarmManager.schedule(action.copy(isEnabled = true))
        } else {
            alarmManager.cancel(id)
        }
    }
}
