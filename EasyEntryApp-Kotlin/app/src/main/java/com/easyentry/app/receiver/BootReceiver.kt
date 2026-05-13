package com.easyentry.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.easyentry.app.alarm.ScheduleAlarmManager
import com.easyentry.app.data.local.db.ScheduledActionDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduledActionDao: ScheduledActionDao
    @Inject lateinit var scheduleAlarmManager: ScheduleAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in listOf(
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON"
            )
        ) return

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            scheduledActionDao.getAllEnabled().forEach { action ->
                scheduleAlarmManager.schedule(action)
            }
        }
    }
}
