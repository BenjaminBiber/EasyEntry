package com.easyentry.app.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.easyentry.app.data.remote.dto.EspControlDto
import com.easyentry.app.domain.model.DeviceStatus
import dagger.hilt.android.EntryPointAccessors

class WidgetActionCallback : ActionCallback {

    companion object {
        val KEY_DEVICE_ID    = ActionParameters.Key<Int>("deviceId")
        val KEY_STATUS_VALUE = ActionParameters.Key<Int>("statusValue")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val deviceId    = parameters[KEY_DEVICE_ID]    ?: return
        val statusValue = parameters[KEY_STATUS_VALUE] ?: return

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val deviceDao = entryPoint.deviceDao()
        val espApi    = entryPoint.espApi()

        val device = deviceDao.getById(deviceId) ?: return

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[EasyEntryWidget.IS_LOADING_KEY]     = true
            prefs[EasyEntryWidget.LOADING_STATUS_KEY] = statusValue
            prefs[EasyEntryWidget.LAST_ERROR_KEY]     = false
        }
        EasyEntryWidget().update(context, glanceId)

        val success = try {
            espApi.controlDoor("http://${device.deviceUrl}/", EspControlDto(statusValue))
            true
        } catch (e: Exception) {
            false
        }

        val updatedDevice = deviceDao.getById(deviceId)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[EasyEntryWidget.IS_LOADING_KEY]     = false
            prefs[EasyEntryWidget.LOADING_STATUS_KEY] = -1
            prefs[EasyEntryWidget.LAST_ERROR_KEY]     = !success
            if (updatedDevice != null) {
                prefs[EasyEntryWidget.DEVICE_NAME_KEY] = updatedDevice.name
                prefs[EasyEntryWidget.IS_OPENED_KEY]   = updatedDevice.isOpened
            }
        }
        EasyEntryWidget().update(context, glanceId)
    }
}
