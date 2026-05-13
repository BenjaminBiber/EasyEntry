package com.easyentry.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.EntryPointAccessors

class ScheduleWidgetToggleCallback : ActionCallback {

    companion object {
        val KEY_SCHEDULE_ID = ActionParameters.Key<Int>("sw_schedule_id")
        val KEY_NEW_ENABLED = ActionParameters.Key<Boolean>("sw_new_enabled")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val scheduleId = parameters[KEY_SCHEDULE_ID] ?: return
        val newEnabled  = parameters[KEY_NEW_ENABLED]  ?: return

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ScheduleWidgetEntryPoint::class.java
        )
        val dao          = entryPoint.scheduledActionDao()
        val alarmManager = entryPoint.scheduleAlarmManager()

        dao.setEnabled(scheduleId, newEnabled)
        if (newEnabled) {
            dao.getById(scheduleId)?.let { alarmManager.schedule(it) }
        } else {
            alarmManager.cancel(scheduleId)
        }

        ScheduleWidget().updateAll(context)
    }
}
