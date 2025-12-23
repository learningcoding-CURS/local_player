package com.local.mediaplayer.data.dao

import androidx.room.*
import com.local.mediaplayer.data.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * 分类 DAO
 */
@Dao
interface CategoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long
    
    @Update
    suspend fun update(category: Category)
    
    @Delete
    suspend fun delete(category: Category)
    
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?
}

