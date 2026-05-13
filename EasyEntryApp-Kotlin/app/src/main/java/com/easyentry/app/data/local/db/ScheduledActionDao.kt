package com.easyentry.app.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledActionDao {

    @Query("""
        SELECT sa.*, d.name as deviceName, d.deviceUrl as deviceUrl
        FROM scheduled_actions sa
        INNER JOIN devices d ON sa.deviceId = d.id
        ORDER BY sa.hourOfDay ASC, sa.minuteOfHour ASC
    """)
    fun getAllWithDeviceInfo(): Flow<List<ScheduledActionWithDeviceInfo>>

    @Query("SELECT * FROM scheduled_actions WHERE id = :id")
    suspend fun getById(id: Int): ScheduledActionEntity?

    @Query("SELECT * FROM scheduled_actions WHERE isEnabled = 1")
    suspend fun getAllEnabled(): List<ScheduledActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: ScheduledActionEntity): Long

    @Update
    suspend fun update(action: ScheduledActionEntity)

    @Query("DELETE FROM scheduled_actions WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE scheduled_actions SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Int, enabled: Boolean)
}
