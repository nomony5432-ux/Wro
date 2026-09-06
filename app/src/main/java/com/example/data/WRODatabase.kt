package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WROScore::class], version = 1, exportSchema = false)
abstract class WRODatabase : RoomDatabase() {
    abstract fun scoreDao(): WROScoreDao

    companion object {
        @Volatile
        private var INSTANCE: WRODatabase? = null

        fun getDatabase(context: Context): WRODatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WRODatabase::class.java,
                    "wro_score_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
