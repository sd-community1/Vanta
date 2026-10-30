package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.EpisodeDao
import com.example.data.dao.MediaDao
import com.example.data.dao.SeasonDao
import com.example.data.model.EpisodeEntity
import com.example.data.model.MediaEntity
import com.example.data.model.SeasonEntity

@Database(
    entities = [
        MediaEntity::class,
        SeasonEntity::class,
        EpisodeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VantaDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun seasonDao(): SeasonDao
    abstract fun episodeDao(): EpisodeDao

    companion object {
        @Volatile
        private var INSTANCE: VantaDatabase? = null

        fun getInstance(context: Context): VantaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VantaDatabase::class.java,
                    "vanta_streaming.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
