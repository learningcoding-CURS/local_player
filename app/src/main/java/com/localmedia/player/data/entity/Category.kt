package com.localmedia.player.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 媒体分类实体
 * 用于组织和管理媒体列表
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 分类名称
    val name: String,
    
    // 排序顺序
    val sortOrder: Int = 0,
    
    // 创建时间
    val createdTime: Long = System.currentTimeMillis()
)

