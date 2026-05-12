package com.easyentry.app.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DeviceDao {

    @Query("SELECT EXISTS(SELECT 1 FROM devices WHERE deviceUrl = :url)")
    suspend fun existsByUrl(url: String): Boolean

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: Int): DeviceEntity?

    @Insert
    suspend fun insert(device: DeviceEntity): Long

    @Update
    suspend fun update(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE devices SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Int, position: Int)

    @Query("SELECT MAX(position) FROM devices WHERE deviceGroupId = :groupId")
    suspend fun getMaxPositionInGroup(groupId: Int): Int?
}
