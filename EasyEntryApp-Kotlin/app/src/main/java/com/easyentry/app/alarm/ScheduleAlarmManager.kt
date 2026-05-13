package com.easyentry.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.easyentry.app.data.local.db.ScheduledActionEntity
import com.easyentry.app.receiver.ScheduleAlarmReceiver
import com.easyentry.app.worker.computeDelayMillis
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleAlarmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms()
        else true

    fun schedule(action: ScheduledActionEntity) {
        val delayMs = computeDelayMillis(
            action.hourOfDay, action.minuteOfHour,
            action.dayOfWeekBitmask, action.isRecurring, action.delayMinutes
        )
        val triggerAt = System.currentTimeMillis() + delayMs
        val pending = buildPendingIntent(action.id)

        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
        }
    }

    fun cancel(actionId: Int) {
        alarmManager.cancel(buildPendingIntent(actionId))
    }

    private fun buildPendingIntent(actionId: Int): PendingIntent {
        val intent = Intent(context, ScheduleAlarmReceiver::class.java).apply {
            putExtra(ScheduleAlarmReceiver.KEY_ACTION_ID, actionId)
        }
        return PendingIntent.getBroadcast(
            context,
            actionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
