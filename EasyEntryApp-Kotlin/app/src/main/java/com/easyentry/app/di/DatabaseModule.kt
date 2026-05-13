package com.easyentry.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.easyentry.app.data.local.db.AppDatabase
import com.easyentry.app.data.local.db.DeviceDao
import com.easyentry.app.data.local.db.DeviceGroupDao
import com.easyentry.app.data.local.db.MIGRATION_1_2
import com.easyentry.app.data.local.db.MIGRATION_2_3
import com.easyentry.app.data.local.db.MIGRATION_3_4
import com.easyentry.app.data.local.db.MIGRATION_4_5
import com.easyentry.app.data.local.db.ScheduledActionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "easyentry.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()

    @Provides
    fun provideDeviceGroupDao(db: AppDatabase): DeviceGroupDao = db.deviceGroupDao()

    @Provides
    fun provideDeviceDao(db: AppDatabase): DeviceDao = db.deviceDao()

    @Provides
    fun provideScheduledActionDao(db: AppDatabase): ScheduledActionDao = db.scheduledActionDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore
}
