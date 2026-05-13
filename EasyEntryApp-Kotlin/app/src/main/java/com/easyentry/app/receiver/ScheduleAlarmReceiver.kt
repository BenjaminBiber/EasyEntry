package com.easyentry.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.easyentry.app.alarm.ScheduleAlarmManager
import com.easyentry.app.data.local.db.DeviceDao
import com.easyentry.app.data.local.db.ScheduledActionDao
import com.easyentry.app.data.remote.api.EspApi
import com.easyentry.app.data.remote.dto.EspControlDto
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduledActionDao: ScheduledActionDao
    @Inject lateinit var deviceDao: DeviceDao
    @Inject lateinit var espApi: EspApi
    @Inject lateinit var scheduleAlarmManager: ScheduleAlarmManager

    companion object {
        const val KEY_ACTION_ID = "action_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val actionId = intent.getIntExtra(KEY_ACTION_ID, -1).takeIf { it != -1 } ?: return
        val asyncResult = goAsync()

        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val action = scheduledActionDao.getById(actionId) ?: return@launch
                if (!action.isEnabled) return@launch

                val ids = if (action.deviceIds.isNotBlank()) {
                    action.deviceIds.split(",").mapNotNull { it.trim().toIntOrNull() }
                } else {
                    listOf(action.deviceId)
                }

                for (id in ids) {
                    val device = deviceDao.getById(id) ?: continue
                    try {
                        espApi.controlDoor("http://${device.deviceUrl}/", EspControlDto(action.actionStatusValue))
                    } catch (_: Exception) {
                        // Gerät nicht erreichbar — nächste Ausführung wie geplant
                    }
                }

                if (action.isRecurring) {
                    scheduleAlarmManager.schedule(action)
                }
            } finally {
                asyncResult.finish()
            }
        }
    }
}
