package com.local.mediaplayer.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ExoPlayer 管理器
 * 封装 ExoPlayer 核心功能：播放控制、倍速、跳转等
 */
class ExoPlayerManager(private val context: Context) {
    
    private var player: ExoPlayer? = null
    private var isReleased = false
    
    // 播放状态
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
    
    // 当前播放位置（毫秒）
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    // 总时长（毫秒）
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    // 视频尺寸
    private val _videoSize = MutableStateFlow<VideoSize?>(null)
    val videoSize: StateFlow<VideoSize?> = _videoSize.asStateFlow()
    
    // 当前倍速
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    // 播放错误
    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()
    
    // 倍速记忆（用于长按临时切换）
    private var savedSpeed = 1.0f
    
    // 是否正在长按加速
    private var isLongPressing = false
    
    companion object {
        private const val TAG = "ExoPlayerManager"
    }
    
    /**
     * 初始化播放器
     */
    fun initializePlayer() {
        if (player == null && !isReleased) {
            try {
                player = ExoPlayer.Builder(context).build().apply {
                    // 添加播放器监听器
                    addListener(playerListener)
                }
                Log.d(TAG, "Player initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize player", e)
                _playbackError.value = "初始化播放器失败: ${e.message}"
            }
        }
    }
    
    /**
     * 播放器事件监听
     */
    private val playerListener = object : Player.Listener {
        
        override fun onPlaybackStateChanged(state: Int) {
            _playbackState.value = when (state) {
                Player.STATE_IDLE -> PlaybackState.Idle
                Player.STATE_BUFFERING -> PlaybackState.Buffering
                Player.STATE_READY -> {
                    if (player?.isPlaying == true) PlaybackState.Playing
                    else PlaybackState.Paused
                }
                Player.STATE_ENDED -> PlaybackState.Ended
                else -> PlaybackState.Idle
            }
        }
        
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playbackState.value = if (isPlaying) {
                PlaybackState.Playing
            } else {
                PlaybackState.Paused
            }
        }
        
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            _videoSize.value = videoSize
        }
        
        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Player error: ${error.message}", error)
            _playbackError.value = "播放错误: ${error.message}"
            _playbackState.value = PlaybackState.Error(error.message ?: "未知错误")
        }
    }
    
    /**
     * 加载媒体
     */
    fun loadMedia(uri: Uri, startPosition: Long = 0) {
        if (isReleased) {
            Log.w(TAG, "Cannot load media: player is released")
            return
        }
        
        try {
            player?.apply {
                val mediaItem = MediaItem.fromUri(uri)
                setMediaItem(mediaItem)
                prepare()
                if (startPosition > 0) {
                    seekTo(startPosition)
                }
                _playbackError.value = null // 清除之前的错误
                Log.d(TAG, "Media loaded: $uri")
            } ?: run {
                Log.w(TAG, "Player is null, reinitializing...")
                initializePlayer()
                loadMedia(uri, startPosition)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load media", e)
            _playbackError.value = "加载媒体失败: ${e.message}"
        }
    }
    
    /**
     * 播放
     */
    fun play() {
        if (isReleased) return
        try {
            player?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play", e)
            _playbackError.value = "播放失败: ${e.message}"
        }
    }
    
    /**
     * 暂停
     */
    fun pause() {
        if (isReleased) return
        try {
            player?.pause()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pause", e)
        }
    }
    
    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        if (isReleased) return
        try {
            player?.let {
                if (it.isPlaying) {
                    pause()
                } else {
                    play()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle play/pause", e)
        }
    }
    
    /**
     * Seek 到指定位置（毫秒）
     */
    fun seekTo(positionMs: Long) {
        if (isReleased) return
        try {
            player?.seekTo(positionMs)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek", e)
        }
    }
    
    /**
     * 前进指定时长（默认 15 秒）
     */
    fun seekForward(deltaMs: Long = 15000) {
        if (isReleased) return
        try {
            player?.let {
                val duration = it.duration
                if (duration > 0) {
                    val newPosition = (it.currentPosition + deltaMs).coerceAtMost(duration)
                    it.seekTo(newPosition)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek forward", e)
        }
    }
    
    /**
     * 后退指定时长（默认 15 秒）
     */
    fun seekBackward(deltaMs: Long = 15000) {
        if (isReleased) return
        try {
            player?.let {
                val newPosition = (it.currentPosition - deltaMs).coerceAtLeast(0)
                it.seekTo(newPosition)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seek backward", e)
        }
    }
    
    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        if (isReleased) return
        try {
            player?.let {
                val params = PlaybackParameters(speed)
                it.playbackParameters = params
                _playbackSpeed.value = speed
                
                // 如果不是长按状态，保存当前倍速
                if (!isLongPressing) {
                    savedSpeed = speed
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set playback speed", e)
        }
    }
    
    /**
     * 长按开始：临时切换到 2.5x
     */
    fun onLongPressStart() {
        if (!isLongPressing && !isReleased) {
            isLongPressing = true
            savedSpeed = _playbackSpeed.value
            setPlaybackSpeed(2.5f)
        }
    }
    
    /**
     * 长按结束：恢复原速
     */
    fun onLongPressEnd() {
        if (isLongPressing && !isReleased) {
            isLongPressing = false
            setPlaybackSpeed(savedSpeed)
        }
    }
    
    /**
     * 获取当前播放位置
     */
    fun getCurrentPosition(): Long {
        if (isReleased) return 0
        return try {
            player?.currentPosition ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current position", e)
            0
        }
    }
    
    /**
     * 获取总时长
     */
    fun getDuration(): Long {
        if (isReleased) return 0
        return try {
            player?.duration?.takeIf { it > 0 } ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get duration", e)
            0
        }
    }
    
    /**
     * 获取播放器实例（用于绑定 UI）
     */
    fun getPlayer(): ExoPlayer? {
        return if (isReleased) null else player
    }
    
    /**
     * 检查播放器是否已释放
     */
    fun isPlayerReleased(): Boolean = isReleased
    
    /**
     * 释放播放器
     */
    fun release() {
        if (isReleased) return
        
        Log.d(TAG, "Releasing player...")
        try {
            player?.let {
                it.removeListener(playerListener)
                it.stop()
                it.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during player release", e)
        } finally {
            player = null
            isReleased = true
            _playbackState.value = PlaybackState.Idle
            Log.d(TAG, "Player released")
        }
    }
    
    /**
     * 更新播放状态（需要定期调用）
     */
    fun updateProgress() {
        if (isReleased) return
        try {
            player?.let {
                _currentPosition.value = it.currentPosition
                _duration.value = it.duration.takeIf { d -> d > 0 } ?: 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update progress", e)
        }
    }
}

/**
 * 播放状态枚举
 */
sealed class PlaybackState {
    object Idle : PlaybackState()
    object Buffering : PlaybackState()
    object Playing : PlaybackState()
    object Paused : PlaybackState()
    object Ended : PlaybackState()
    data class Error(val message: String) : PlaybackState()
}

