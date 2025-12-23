package com.local.mediaplayer.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 媒体项实体
 * 存储音视频文件的元数据和播放进度
 */
@Entity(tableName = "media_items")
data class MediaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 文件 URI（通过 SAF 获取）
    val uri: String,
    
    // 文件标题
    val title: String,
    
    // 媒体时长（毫秒）
    val duration: Long = 0,
    
    // 媒体类型：VIDEO 或 AUDIO
    val type: MediaType,
    
    // 封面 URI（可选）
    val coverUri: String? = null,
    
    // 上次播放位置（毫秒）
    val lastPosition: Long = 0,
    
    // 分类 ID
    val categoryId: Long? = null,
    
    // 添加时间
    val addedTime: Long = System.currentTimeMillis(),
    
    // 上次播放时间
    val lastPlayedTime: Long = 0,
    
    // 播放次数
    val playCount: Int = 0,
    
    // 视频宽度（仅视频）
    val videoWidth: Int = 0,
    
    // 视频高度（仅视频）
    val videoHeight: Int = 0,
    
    // 字幕文件 URI（可选）
    val subtitleUri: String? = null,
    
    // 稿件文本 URI（可选）
    val transcriptUri: String? = null
)

/**
 * 媒体类型枚举
 */
enum class MediaType {
    VIDEO,
    AUDIO
}

