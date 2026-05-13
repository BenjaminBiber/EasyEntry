package com.easyentry.app.data.local.backup

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val version: Int = 1,
    val timestamp: Long,
    val groups: List<BackupGroupData>,
    val devices: List<BackupDeviceData>
)

@JsonClass(generateAdapter = true)
data class BackupGroupData(
    val id: Int,
    val groupName: String,
    val color: Long,
    val icon: String
)

@JsonClass(generateAdapter = true)
data class BackupDeviceData(
    val id: Int,
    val name: String,
    val statusValue: Int,
    val deviceUrl: String,
    val isOpened: Boolean,
    val deviceGroupId: Int,
    val position: Int
)
