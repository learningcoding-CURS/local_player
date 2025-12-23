package com.local.mediaplayer.data.dao

import androidx.room.*
import com.local.mediaplayer.data.entity.MediaItem
import com.local.mediaplayer.data.entity.MediaType
import kotlinx.coroutines.flow.Flow

/**
 * 媒体项 DAO
 * 提供数据库操作接口
 */
@Dao
interface MediaItemDao {
    
    /**
     * 插入单个媒体项
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItem): Long
    
    /**
     * 插入多个媒体项
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MediaItem>)
    
    /**
     * 更新媒体项
     */
    @Update
    suspend fun update(item: MediaItem)
    
    /**
     * 删除媒体项
     */
    @Delete
    suspend fun delete(item: MediaItem)
    
    /**
     * 根据 ID 删除
     */
    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    /**
     * 获取所有媒体项（Flow 实时更新）
     */
    @Query("SELECT * FROM media_items ORDER BY addedTime DESC")
    fun getAllItems(): Flow<List<MediaItem>>
    
    /**
     * 根据 ID 获取媒体项
     */
    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun getItemById(id: Long): MediaItem?
    
    /**
     * 根据类型获取媒体项
     */
    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY addedTime DESC")
    fun getItemsByType(type: MediaType): Flow<List<MediaItem>>
    
    /**
     * 根据分类获取媒体项
     */
    @Query("SELECT * FROM media_items WHERE categoryId = :categoryId ORDER BY addedTime DESC")
    fun getItemsByCategory(categoryId: Long): Flow<List<MediaItem>>
    
    /**
     * 获取最近播放的媒体项
     */
    @Query("SELECT * FROM media_items WHERE lastPlayedTime > 0 ORDER BY lastPlayedTime DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<MediaItem>>
    
    /**
     * 更新播放位置
     */
    @Query("UPDATE media_items SET lastPosition = :position WHERE id = :id")
    suspend fun updatePosition(id: Long, position: Long)
    
    /**
     * 更新播放统计
     */
    @Query("UPDATE media_items SET lastPlayedTime = :time, playCount = playCount + 1 WHERE id = :id")
    suspend fun updatePlayStats(id: Long, time: Long = System.currentTimeMillis())
    
    /**
     * 更新视频尺寸
     */
    @Query("UPDATE media_items SET videoWidth = :width, videoHeight = :height WHERE id = :id")
    suspend fun updateVideoSize(id: Long, width: Int, height: Int)
    
    /**
     * 搜索媒体项
     */
    @Query("SELECT * FROM media_items WHERE title LIKE '%' || :query || '%' ORDER BY addedTime DESC")
    fun searchItems(query: String): Flow<List<MediaItem>>
}

