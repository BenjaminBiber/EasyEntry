package com.easyentry.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE device_groups ADD COLUMN color INTEGER NOT NULL DEFAULT 4284518345")
        db.execSQL("ALTER TABLE device_groups ADD COLUMN icon TEXT NOT NULL DEFAULT 'HOME'")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE devices ADD COLUMN position INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [DeviceGroupEntity::class, DeviceEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceGroupDao(): DeviceGroupDao
    abstract fun deviceDao(): DeviceDao
}
