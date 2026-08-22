package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        GoalEntity::class,
        GoalSubtaskEntity::class,
        GoalCompletionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BrieflyDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: BrieflyDatabase? = null

        fun getDatabase(context: Context): BrieflyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrieflyDatabase::class.java,
                    "briefly_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
