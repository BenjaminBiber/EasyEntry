package com.easyentry.app.data.repository

import com.easyentry.app.domain.model.DeviceGroup
import com.easyentry.app.domain.model.GroupIcon
import kotlinx.coroutines.flow.Flow

interface DeviceGroupRepository {
    fun getGroupsWithDevices(): Flow<List<DeviceGroup>>
    suspend fun addGroup(groupName: String, color: Long, icon: GroupIcon): Result<Unit>
    suspend fun renameGroup(groupId: Int, newName: String): Result<Unit>
    suspend fun updateGroup(groupId: Int, name: String, color: Long, icon: GroupIcon): Result<Unit>
    suspend fun deleteGroup(groupId: Int): Result<Unit>
    suspend fun addDevice(deviceUrl: String, deviceName: String): Result<Unit>
    suspend fun deleteDevice(deviceId: Int): Result<Unit>
    suspend fun moveDevice(deviceId: Int, newGroupId: Int): Result<Unit>
    suspend fun deviceUrlExists(url: String): Boolean
    suspend fun groupNameExists(name: String): Boolean
    suspend fun deleteAllGroupsAndDevices(): Result<Unit>
    suspend fun reorderDevices(orderedDeviceIds: List<Int>): Result<Unit>
}
