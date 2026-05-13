package com.easyentry.app.data.repository

interface BackupRepository {
    suspend fun createBackupJson(): Result<String>
    suspend fun restoreFromJson(json: String): Result<Unit>
}
