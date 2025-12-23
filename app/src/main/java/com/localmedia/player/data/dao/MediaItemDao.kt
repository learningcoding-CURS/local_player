package com.localmedia.player.data.dao

import androidx.room.*
import com.localmedia.player.data.entity.MediaItem
import com.localmedia.player.data.entity.MediaType
import kotlinx.coroutines.flow.Flow

/**
 * 媒体条目数据访问对象
 */
@Dao
interface MediaItemDao {
    
    @Query("SELECT * FROM media_items ORDER BY addedTime DESC")
    fun getAllItems(): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY addedTime DESC")
    fun getItemsByType(type: MediaType): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items WHERE categoryId = :categoryId ORDER BY addedTime DESC")
    fun getItemsByCategory(categoryId: Long): Flow<List<MediaItem>>
    
    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getItemById(id: Long): MediaItem?
    
    @Query("SELECT * FROM media_items WHERE id = :id")
    fun getItemByIdFlow(id: Long): Flow<MediaItem?>
    
    @Query("SELECT * FROM media_items WHERE uri = :uri LIMIT 1")
    suspend fun getItemByUri(uri: String): MediaItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>): List<Long>
    
    @Update
    suspend fun update(item: MediaItem)
    
    @Delete
    suspend fun delete(item: MediaItem)
    
    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("UPDATE media_items SET lastPosition = :position, lastPlayedTime = :time WHERE id = :id")
    suspend fun updatePlaybackPosition(id: Long, position: Long, time: Long = System.currentTimeMillis())
    
    @Query("UPDATE media_items SET playCount = playCount + 1, lastPlayedTime = :time WHERE id = :id")
    suspend fun incrementPlayCount(id: Long, time: Long = System.currentTimeMillis())
    
    @Query("UPDATE media_items SET videoWidth = :width, videoHeight = :height WHERE id = :id")
    suspend fun updateVideoSize(id: Long, width: Int, height: Int)
    
    @Query("SELECT * FROM media_items WHERE lastPlayedTime > 0 ORDER BY lastPlayedTime DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<MediaItem>>
}

