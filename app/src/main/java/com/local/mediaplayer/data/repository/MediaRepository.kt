package com.local.mediaplayer.data.repository

import com.local.mediaplayer.data.dao.CategoryDao
import com.local.mediaplayer.data.dao.MediaItemDao
import com.local.mediaplayer.data.entity.Category
import com.local.mediaplayer.data.entity.MediaItem
import com.local.mediaplayer.data.entity.MediaType
import kotlinx.coroutines.flow.Flow

/**
 * 媒体数据仓库
 * 提供统一的数据访问接口
 */
class MediaRepository(
    private val mediaItemDao: MediaItemDao,
    private val categoryDao: CategoryDao
) {
    
    // ============ MediaItem 操作 ============
    
    fun getAllItems(): Flow<List<MediaItem>> = mediaItemDao.getAllItems()
    
    fun getItemsByType(type: MediaType): Flow<List<MediaItem>> = 
        mediaItemDao.getItemsByType(type)
    
    fun getItemsByCategory(categoryId: Long): Flow<List<MediaItem>> = 
        mediaItemDao.getItemsByCategory(categoryId)
    
    fun getRecentlyPlayed(limit: Int = 20): Flow<List<MediaItem>> = 
        mediaItemDao.getRecentlyPlayed(limit)
    
    suspend fun getItemById(id: Long): MediaItem? = 
        mediaItemDao.getItemById(id)
    
    suspend fun insertItem(item: MediaItem): Long = 
        mediaItemDao.insert(item)
    
    suspend fun insertItems(items: List<MediaItem>) = 
        mediaItemDao.insertAll(items)
    
    suspend fun updateItem(item: MediaItem) = 
        mediaItemDao.update(item)
    
    suspend fun deleteItem(item: MediaItem) = 
        mediaItemDao.delete(item)
    
    suspend fun deleteItemById(id: Long) = 
        mediaItemDao.deleteById(id)
    
    suspend fun updatePosition(id: Long, position: Long) = 
        mediaItemDao.updatePosition(id, position)
    
    suspend fun updatePlayStats(id: Long) = 
        mediaItemDao.updatePlayStats(id)
    
    suspend fun updateVideoSize(id: Long, width: Int, height: Int) = 
        mediaItemDao.updateVideoSize(id, width, height)
    
    fun searchItems(query: String): Flow<List<MediaItem>> = 
        mediaItemDao.searchItems(query)
    
    // ============ Category 操作 ============
    
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
}

