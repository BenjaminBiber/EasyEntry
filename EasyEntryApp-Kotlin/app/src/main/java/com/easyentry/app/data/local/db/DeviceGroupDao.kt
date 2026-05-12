package com.easyentry.app.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceGroupDao {

    @Transaction
    @Query("SELECT * FROM device_groups ORDER BY groupName ASC")
    fun getAll(): Flow<List<DeviceGroupWithDevices>>

    @Transaction
    @Query("SELECT * FROM device_groups WHERE id = :id")
    suspend fun getById(id: Int): DeviceGroupWithDevices?

    @Query("SELECT * FROM device_groups WHERE groupName = :name LIMIT 1")
    suspend fun findByName(name: String): DeviceGroupEntity?

    @Insert
    suspend fun insert(group: DeviceGroupEntity): Long

    @Update
    suspend fun update(group: DeviceGroupEntity)

    @Delete
    suspend fun delete(group: DeviceGroupEntity)

    @Query("DELETE FROM device_groups")
    suspend fun deleteAll()
}
