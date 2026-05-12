package com.easyentry.app.widget

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.widgetDataStore by preferencesDataStore(name = "widget_prefs")

@Singleton
class WidgetPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private fun deviceKey(appWidgetId: Int) = intPreferencesKey("widget_device_$appWidgetId")
    }

    fun getDeviceId(appWidgetId: Int): Flow<Int?> =
        context.widgetDataStore.data.map { it[deviceKey(appWidgetId)] }

    suspend fun setDeviceId(appWidgetId: Int, deviceId: Int) {
        context.widgetDataStore.edit { it[deviceKey(appWidgetId)] = deviceId }
    }

    suspend fun removeDeviceId(appWidgetId: Int) {
        context.widgetDataStore.edit { it.remove(deviceKey(appWidgetId)) }
    }
}
