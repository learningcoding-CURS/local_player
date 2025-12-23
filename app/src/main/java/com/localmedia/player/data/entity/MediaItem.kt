package com.localmedia.player.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 媒体条目实体
 * 存储本地音视频文件的元数据和播放状态
 */
@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 文件 URI（使用 SAF 持久化权限）
    val uri: String,
    
    // 显示标题
    val title: String,
    
    // 时长（毫秒）
    val duration: Long = 0,
    
    // 媒体类型：VIDEO 或 AUDIO
    val type: MediaType,
    
    // 封面图 URI（可选）
    val coverUri: String? = null,
    
    // 上次播放位置（毫秒）
    val lastPosition: Long = 0,
    
    // 所属分类 ID
    val categoryId: Long? = null,
    
    // 添加时间
    val addedTime: Long = System.currentTimeMillis(),
    
    // 最后播放时间
    val lastPlayedTime: Long = 0,
    
    // 播放次数
    val playCount: Int = 0,
    
    // 视频宽度（仅视频）
    val videoWidth: Int = 0,
    
    // 视频高度（仅视频）
    val videoHeight: Int = 0,
    
    // 字幕文件 URI（可选）
    val subtitleUri: String? = null,
    
    // 文稿文件 URI（可选，用于时间点跳转）
    val transcriptUri: String? = null
)

enum class MediaType {
    VIDEO,
    AUDIO
}

