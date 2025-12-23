package com.localmedia.player.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.localmedia.player.data.dao.CategoryDao
import com.localmedia.player.data.dao.MediaItemDao
import com.localmedia.player.data.dao.PlaybackHistoryDao
import com.localmedia.player.data.entity.Category
import com.localmedia.player.data.entity.MediaItem
import com.localmedia.player.data.entity.PlaybackHistory

/**
 * Room 数据库实例
 */
@Database(
    entities = [
        MediaItem::class,
        Category::class,
        PlaybackHistory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MediaDatabase : RoomDatabase() {
    
    abstract fun mediaItemDao(): MediaItemDao
    abstract fun categoryDao(): CategoryDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: MediaDatabase? = null
        
        fun getInstance(context: Context): MediaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MediaDatabase::class.java,
                    "media_player_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

