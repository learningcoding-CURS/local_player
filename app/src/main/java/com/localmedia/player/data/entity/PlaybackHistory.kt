package com.localmedia.player.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 播放历史记录
 * 用于追踪播放进度和历史
 */
@Entity(
    tableName = "playback_history",
    foreignKeys = [
        ForeignKey(
            entity = MediaItem::class,
            parentColumns = ["id"],
            childColumns = ["mediaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("mediaId"), Index("timestamp")]
)
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 关联的媒体 ID
    val mediaId: Long,
    
    // 播放位置（毫秒）
    val position: Long,
    
    // 记录时间戳
    val timestamp: Long = System.currentTimeMillis(),
    
    // 播放速度
    val playbackSpeed: Float = 1.0f
)

