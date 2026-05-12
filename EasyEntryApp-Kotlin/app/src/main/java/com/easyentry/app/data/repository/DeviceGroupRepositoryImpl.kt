package com.easyentry.app.data.repository

import com.easyentry.app.data.local.db.DeviceDao
import com.easyentry.app.data.local.db.DeviceEntity
import com.easyentry.app.data.local.db.DeviceGroupDao
import com.easyentry.app.data.local.db.DeviceGroupEntity
import com.easyentry.app.data.local.db.DeviceGroupWithDevices
import com.easyentry.app.domain.model.Device
import com.easyentry.app.domain.model.DeviceGroup
import com.easyentry.app.domain.model.DeviceStatus
import com.easyentry.app.domain.model.GroupIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceGroupRepositoryImpl @Inject constructor(
    private val deviceGroupDao: DeviceGroupDao,
    private val deviceDao: DeviceDao
) : DeviceGroupRepository {

    override fun getGroupsWithDevices(): Flow<List<DeviceGroup>> =
        deviceGroupDao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addGroup(groupName: String, color: Long, icon: GroupIcon): Result<Unit> = runCatching {
        deviceGroupDao.insert(DeviceGroupEntity(groupName = groupName, color = color, icon = icon.name))
    }

    override suspend fun renameGroup(groupId: Int, newName: String): Result<Unit> = runCatching {
        val existing = deviceGroupDao.getById(groupId)?.group ?: return@runCatching
        deviceGroupDao.update(existing.copy(groupName = newName))
    }

    override suspend fun updateGroup(groupId: Int, name: String, color: Long, icon: GroupIcon): Result<Unit> = runCatching {
        val existing = deviceGroupDao.getById(groupId)?.group ?: return@runCatching
        deviceGroupDao.update(existing.copy(groupName = name, color = color, icon = icon.name))
    }

    override suspend fun deleteGroup(groupId: Int): Result<Unit> = runCatching {
        val entity = deviceGroupDao.getById(groupId)?.group ?: return@runCatching
        deviceGroupDao.delete(entity)
    }

    override suspend fun addDevice(deviceUrl: String, deviceName: String): Result<Unit> = runCatching {
        val ungroupedName = "ungruppiert"
        val existingGroup = deviceGroupDao.findByName(ungroupedName)
        val groupId = existingGroup?.id
            ?: deviceGroupDao.insert(DeviceGroupEntity(groupName = ungroupedName)).toInt()
        val nextPosition = (deviceDao.getMaxPositionInGroup(groupId) ?: -1) + 1
        deviceDao.insert(
            DeviceEntity(
                name = deviceName,
                statusValue = DeviceStatus.CLOSED.value,
                deviceUrl = deviceUrl,
                isOpened = false,
                deviceGroupId = groupId,
                position = nextPosition
            )
        )
    }

    override suspend fun deleteDevice(deviceId: Int): Result<Unit> = runCatching {
        deviceDao.deleteById(deviceId)
    }

    override suspend fun moveDevice(deviceId: Int, newGroupId: Int): Result<Unit> = runCatching {
        val device = deviceDao.getById(deviceId) ?: return@runCatching
        deviceDao.update(device.copy(deviceGroupId = newGroupId))
    }

    override suspend fun deviceUrlExists(url: String): Boolean =
        deviceDao.existsByUrl(url)

    override suspend fun groupNameExists(name: String): Boolean =
        deviceGroupDao.findByName(name) != null

    override suspend fun deleteAllGroupsAndDevices(): Result<Unit> = runCatching {
        deviceGroupDao.deleteAll()
    }

    override suspend fun reorderDevices(orderedDeviceIds: List<Int>): Result<Unit> = runCatching {
        orderedDeviceIds.forEachIndexed { index, id ->
            deviceDao.updatePosition(id, index)
        }
    }
}

private fun DeviceGroupWithDevices.toDomain() = DeviceGroup(
    id = group.id,
    groupName = group.groupName,
    color = group.color,
    icon = runCatching { GroupIcon.valueOf(group.icon) }.getOrDefault(GroupIcon.HOME),
    devices = devices.sortedBy { it.position }.map { it.toDomain() }
)

private fun DeviceEntity.toDomain() = Device(
    id = id,
    name = name,
    status = DeviceStatus.fromValue(statusValue),
    deviceUrl = deviceUrl,
    isOpened = isOpened,
    deviceGroupId = deviceGroupId
)
