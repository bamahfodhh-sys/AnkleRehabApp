package com.sanad.anklerehab.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SessionRecordEntity::class, ExerciseCheckEntity::class],
    version = 1,
    exportSchema = true
)
abstract class RehabDatabase : RoomDatabase() {
    abstract fun rehabDao(): RehabDao

    companion object {
        @Volatile private var INSTANCE: RehabDatabase? = null

        fun get(context: Context): RehabDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                RehabDatabase::class.java,
                "rehab.db"
            ).build().also { INSTANCE = it }
        }
    }
}
