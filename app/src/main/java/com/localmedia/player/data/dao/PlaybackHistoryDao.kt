package com.localmedia.player.data.dao

import androidx.room.*
import com.localmedia.player.data.entity.PlaybackHistory
import kotlinx.coroutines.flow.Flow

/**
 * 播放历史数据访问对象
 */
@Dao
interface PlaybackHistoryDao {
    
    @Query("SELECT * FROM playback_history WHERE mediaId = :mediaId ORDER BY timestamp DESC")
    fun getHistoryForMedia(mediaId: Long): Flow<List<PlaybackHistory>>
    
    @Query("SELECT * FROM playback_history WHERE mediaId = :mediaId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestHistoryForMedia(mediaId: Long): PlaybackHistory?
    
    @Insert
    suspend fun insert(history: PlaybackHistory): Long
    
    @Query("DELETE FROM playback_history WHERE mediaId = :mediaId")
    suspend fun deleteHistoryForMedia(mediaId: Long)
    
    @Query("DELETE FROM playback_history WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldHistory(beforeTimestamp: Long)
}

