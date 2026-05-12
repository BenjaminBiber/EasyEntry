package com.easyentry.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val SHOW_SNACKBAR = booleanPreferencesKey("show_snack_bar")
    }

    val showSnackBar: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[SHOW_SNACKBAR] ?: true }

    suspend fun setShowSnackBar(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_SNACKBAR] = value
        }
    }
}
