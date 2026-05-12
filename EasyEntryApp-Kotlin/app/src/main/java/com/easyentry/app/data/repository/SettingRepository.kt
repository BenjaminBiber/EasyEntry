package com.easyentry.app.data.repository

import com.easyentry.app.data.local.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingRepository @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    val showSnackBar: Flow<Boolean> = settingsDataStore.showSnackBar

    suspend fun setShowSnackBar(value: Boolean) {
        settingsDataStore.setShowSnackBar(value)
    }
}
