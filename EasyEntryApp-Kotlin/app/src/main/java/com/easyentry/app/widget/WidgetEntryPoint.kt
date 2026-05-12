package com.easyentry.app.widget

import com.easyentry.app.data.local.db.DeviceDao
import com.easyentry.app.data.remote.api.EspApi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun deviceDao(): DeviceDao
    fun espApi(): EspApi
    fun widgetPreferencesDataStore(): WidgetPreferencesDataStore
}
