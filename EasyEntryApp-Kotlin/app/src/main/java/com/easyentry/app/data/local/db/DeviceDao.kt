package com.easyentry.app.data.local.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface DeviceDao {

    @Query("SELECT EXISTS(SELECT 1 FROM devices WHERE deviceUrl = :url)")
    suspend fun existsByUrl(url: String): Boolean

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: Int): DeviceEntity?

    @Insert
    suspend fun insert(device: DeviceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(devices: List<DeviceEntity>)

    @Update
    suspend fun update(device: DeviceEntity)

    @Delete
    suspend fun delete(device: DeviceEntity)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE devices SET position = :position WHERE id = :id")
    suspend fun updatePosition(id: Int, position: Int)

    /**
     * Setzt alle Positionen in EINER Transaktion.
     *
     * Wichtig fuer die Erreichbarkeitsanzeige: als Einzel-Updates invalidiert Room den
     * getAll()-Flow N mal, was N konkurrierende Probe-Runden ausgeloest hat. Room invalidiert
     * hier erst beim Commit, aus N Emissionen wird eine.
     */
    @Transaction
    suspend fun updatePositions(orderedIds: List<Int>) {
        orderedIds.forEachIndexed { index, id -> updatePosition(id, index) }
    }

    @Query("SELECT MAX(position) FROM devices WHERE deviceGroupId = :groupId")
    suspend fun getMaxPositionInGroup(groupId: Int): Int?
}
