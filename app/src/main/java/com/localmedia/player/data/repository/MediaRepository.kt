package com.localmedia.player.data.repository

import com.localmedia.player.data.dao.CategoryDao
import com.localmedia.player.data.dao.MediaItemDao
import com.localmedia.player.data.dao.PlaybackHistoryDao
import com.localmedia.player.data.entity.Category
import com.localmedia.player.data.entity.MediaItem
import com.localmedia.player.data.entity.MediaType
import com.localmedia.player.data.entity.PlaybackHistory
import kotlinx.coroutines.flow.Flow

/**
 * 媒体数据仓库
 * 统一管理数据访问
 */
class MediaRepository(
    private val mediaItemDao: MediaItemDao,
    private val categoryDao: CategoryDao,
    private val playbackHistoryDao: PlaybackHistoryDao
) {
    
    // ========== MediaItem 操作 ==========
    
    fun getAllMediaItems(): Flow<List<MediaItem>> = mediaItemDao.getAllItems()
    
    fun getMediaItemsByType(type: MediaType): Flow<List<MediaItem>> = 
        mediaItemDao.getItemsByType(type)
    
    fun getMediaItemsByCategory(categoryId: Long): Flow<List<MediaItem>> = 
        mediaItemDao.getItemsByCategory(categoryId)
    
    fun getMediaItemById(id: Long): Flow<MediaItem?> = 
        mediaItemDao.getItemByIdFlow(id)
    
    suspend fun getMediaItemByIdSync(id: Long): MediaItem? = 
        mediaItemDao.getItemById(id)
    
    suspend fun getMediaItemByUri(uri: String): MediaItem? = 
        mediaItemDao.getItemByUri(uri)
    
    suspend fun insertMediaItem(item: MediaItem): Long = 
        mediaItemDao.insert(item)
    
    suspend fun insertMediaItems(items: List<MediaItem>): List<Long> = 
        mediaItemDao.insertAll(items)
    
    suspend fun updateMediaItem(item: MediaItem) = 
        mediaItemDao.update(item)
    
    suspend fun deleteMediaItem(item: MediaItem) = 
        mediaItemDao.delete(item)
    
    suspend fun deleteMediaItemById(id: Long) = 
        mediaItemDao.deleteById(id)
    
    suspend fun updatePlaybackPosition(id: Long, position: Long) = 
        mediaItemDao.updatePlaybackPosition(id, position)
    
    suspend fun incrementPlayCount(id: Long) = 
        mediaItemDao.incrementPlayCount(id)
    
    suspend fun updateVideoSize(id: Long, width: Int, height: Int) = 
        mediaItemDao.updateVideoSize(id, width, height)
    
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<MediaItem>> = 
        mediaItemDao.getRecentlyPlayed(limit)
    
    // ========== Category 操作 ==========
    
    fun getAllCategories(): Flow<List<Category>> = 
        categoryDao.getAllCategories()
    
    suspend fun getCategoryById(id: Long): Category? = 
        categoryDao.getCategoryById(id)
    
    suspend fun insertCategory(category: Category): Long = 
        categoryDao.insert(category)
    
    suspend fun updateCategory(category: Category) = 
        categoryDao.update(category)
    
    suspend fun deleteCategory(category: Category) = 
        categoryDao.delete(category)
    
    // ========== PlaybackHistory 操作 ==========
    
    fun getPlaybackHistory(mediaId: Long): Flow<List<PlaybackHistory>> = 
        playbackHistoryDao.getHistoryForMedia(mediaId)
    
    suspend fun getLatestPlaybackHistory(mediaId: Long): PlaybackHistory? = 
        playbackHistoryDao.getLatestHistoryForMedia(mediaId)
    
    suspend fun insertPlaybackHistory(history: PlaybackHistory): Long = 
        playbackHistoryDao.insert(history)
    
    suspend fun deletePlaybackHistory(mediaId: Long) = 
        playbackHistoryDao.deleteHistoryForMedia(mediaId)
    
    suspend fun cleanOldHistory(daysToKeep: Int = 30) {
        val threshold = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        playbackHistoryDao.deleteOldHistory(threshold)
    }
}

