package com.local.mediaplayer.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.local.mediaplayer.data.dao.CategoryDao
import com.local.mediaplayer.data.dao.MediaItemDao
import com.local.mediaplayer.data.entity.Category
import com.local.mediaplayer.data.entity.MediaItem

/**
 * 应用数据库
 */
@Database(
    entities = [MediaItem::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        private const val TAG = "AppDatabase"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            Log.d(TAG, "Getting database instance...")
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    Log.d(TAG, "Creating new database instance")
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "media_player_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    Log.d(TAG, "Database instance created successfully")
                    instance
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to create database", e)
                    throw e
                }
            }
        }
    }
}

