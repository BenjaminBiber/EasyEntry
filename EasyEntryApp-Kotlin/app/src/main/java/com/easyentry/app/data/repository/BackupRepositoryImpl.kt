package com.easyentry.app.data.repository

import com.easyentry.app.data.local.backup.BackupData
import com.easyentry.app.data.local.backup.BackupDeviceData
import com.easyentry.app.data.local.backup.BackupGroupData
import com.easyentry.app.data.local.db.DeviceDao
import com.easyentry.app.data.local.db.DeviceEntity
import com.easyentry.app.data.local.db.DeviceGroupDao
import com.easyentry.app.data.local.db.DeviceGroupEntity
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    private val deviceGroupDao: DeviceGroupDao,
    private val deviceDao: DeviceDao,
    private val moshi: Moshi
) : BackupRepository {

    private val adapter = moshi.adapter(BackupData::class.java)

    override suspend fun createBackupJson(): Result<String> {
        return try {
            val groupsWithDevices = deviceGroupDao.getAll().first()
            val backup = BackupData(
                timestamp = System.currentTimeMillis(),
                groups = groupsWithDevices.map { gwd ->
                    BackupGroupData(
                        id = gwd.group.id,
                        groupName = gwd.group.groupName,
                        color = gwd.group.color,
                        icon = gwd.group.icon
                    )
                },
                devices = groupsWithDevices.flatMap { gwd ->
                    gwd.devices.map { d ->
                        BackupDeviceData(
                            id = d.id,
                            name = d.name,
                            statusValue = d.statusValue,
                            deviceUrl = d.deviceUrl,
                            isOpened = d.isOpened,
                            deviceGroupId = d.deviceGroupId,
                            position = d.position
                        )
                    }
                }
            )
            Result.success(adapter.toJson(backup))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreFromJson(json: String): Result<Unit> {
        return try {
            val backup = adapter.fromJson(json)
                ?: return Result.failure(IllegalArgumentException("Ungültiges Backup-Format"))

            // CASCADE delete: deleting all groups removes all devices automatically
            deviceGroupDao.deleteAll()

            deviceGroupDao.insertAll(
                backup.groups.map { g ->
                    DeviceGroupEntity(
                        id = g.id,
                        groupName = g.groupName,
                        color = g.color,
                        icon = g.icon
                    )
                }
            )

            deviceDao.insertAll(
                backup.devices.map { d ->
                    DeviceEntity(
                        id = d.id,
                        name = d.name,
                        statusValue = d.statusValue,
                        deviceUrl = d.deviceUrl,
                        isOpened = d.isOpened,
                        deviceGroupId = d.deviceGroupId,
                        position = d.position
                    )
                }
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
