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

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS scheduled_actions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                deviceId INTEGER NOT NULL,
                actionStatusValue INTEGER NOT NULL,
                isRecurring INTEGER NOT NULL,
                dayOfWeekBitmask INTEGER NOT NULL,
                hourOfDay INTEGER NOT NULL,
                minuteOfHour INTEGER NOT NULL,
                delayMinutes INTEGER NOT NULL,
                workManagerTagId TEXT NOT NULL,
                isEnabled INTEGER NOT NULL DEFAULT 1,
                label TEXT NOT NULL DEFAULT '',
                FOREIGN KEY(deviceId) REFERENCES devices(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_scheduled_actions_deviceId ON scheduled_actions(deviceId)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE scheduled_actions ADD COLUMN deviceIds TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [DeviceGroupEntity::class, DeviceEntity::class, ScheduledActionEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceGroupDao(): DeviceGroupDao
    abstract fun deviceDao(): DeviceDao
    abstract fun scheduledActionDao(): ScheduledActionDao
}
