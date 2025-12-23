package com.localmedia.player.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ExoPlayer 管理器
 * 封装播放器核心功能：播放控制、倍速、跳转等
 */
class ExoPlayerManager(private val context: Context) {
    
    private var _player: ExoPlayer? = null
    val player: ExoPlayer?
        get() = _player
    
    // 播放状态
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    // 当前播放位置（毫秒）
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    // 总时长（毫秒）
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    // 当前倍速
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    // 视频尺寸
    private val _videoSize = MutableStateFlow<VideoSize?>(null)
    val videoSize: StateFlow<VideoSize?> = _videoSize.asStateFlow()
    
    // 是否准备好
    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()
    
    // 预设倍速列表
    val availableSpeeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)
    
    // 长按时的临时倍速
    private val longPressSpeed = 2.5f
    private var savedSpeedBeforeLongPress: Float? = null
    
    // 跳转间隔（毫秒）
    private val seekInterval = 15_000L // 15 秒
    
    /**
     * 初始化播放器
     */
    fun initialize() {
        if (_player != null) return
        
        _player = ExoPlayer.Builder(context)
            .build()
            .apply {
                // 设置监听器
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                    }
                    
                    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                        _playbackSpeed.value = playbackParameters.speed
                    }
                    
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        _videoSize.value = videoSize
                    }
                    
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                _isReady.value = true
                                _duration.value = duration
                            }
                            Player.STATE_ENDED -> {
                                _isPlaying.value = false
                            }
                            else -> {}
                        }
                    }
                })
                
                // 启用自动播放
                playWhenReady = true
            }
    }
    
    /**
     * 准备并播放媒体
     */
    fun prepareAndPlay(uri: String, startPosition: Long = 0) {
        val player = _player ?: run {
            initialize()
            _player ?: return
        }
        
        val mediaItem = MediaItem.fromUri(uri)
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
        
        player.apply {
            setMediaSource(mediaSource)
            prepare()
            if (startPosition > 0) {
                seekTo(startPosition)
            }
            playWhenReady = true
        }
        
        _isReady.value = false
    }
    
    /**
     * 播放/暂停
     */
    fun togglePlayPause() {
        _player?.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }
    
    /**
     * 播放
     */
    fun play() {
        _player?.play()
    }
    
    /**
     * 暂停
     */
    fun pause() {
        _player?.pause()
    }
    
    /**
     * 跳转到指定位置
     */
    fun seekTo(positionMs: Long) {
        _player?.seekTo(positionMs.coerceIn(0, _duration.value))
    }
    
    /**
     * 前进 15 秒
     */
    fun seekForward() {
        _player?.let { player ->
            val newPosition = (player.currentPosition + seekInterval).coerceAtMost(player.duration)
            player.seekTo(newPosition)
        }
    }
    
    /**
     * 后退 15 秒
     */
    fun seekBackward() {
        _player?.let { player ->
            val newPosition = (player.currentPosition - seekInterval).coerceAtLeast(0)
            player.seekTo(newPosition)
        }
    }
    
    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        _player?.setPlaybackSpeed(speed)
        _playbackSpeed.value = speed
    }
    
    /**
     * 切换到下一个倍速
     */
    fun cyclePlaybackSpeed() {
        val currentSpeed = _playbackSpeed.value
        val currentIndex = availableSpeeds.indexOf(currentSpeed)
        val nextIndex = (currentIndex + 1) % availableSpeeds.size
        setPlaybackSpeed(availableSpeeds[nextIndex])
    }
    
    /**
     * 开始长按（临时切换到 2.5x）
     */
    fun startLongPress() {
        val currentSpeed = _playbackSpeed.value
        if (currentSpeed != longPressSpeed) {
            savedSpeedBeforeLongPress = currentSpeed
            setPlaybackSpeed(longPressSpeed)
        }
    }
    
    /**
     * 结束长按（恢复原速度）
     */
    fun endLongPress() {
        savedSpeedBeforeLongPress?.let { savedSpeed ->
            setPlaybackSpeed(savedSpeed)
            savedSpeedBeforeLongPress = null
        }
    }
    
    /**
     * 获取当前播放位置
     */
    fun getCurrentPositionMs(): Long {
        return _player?.currentPosition ?: 0L
    }
    
    /**
     * 获取总时长
     */
    fun getDurationMs(): Long {
        return _player?.duration ?: 0L
    }
    
    /**
     * 更新播放状态（用于 UI 更新）
     */
    fun updateProgress() {
        _player?.let { player ->
            _currentPosition.value = player.currentPosition
            if (player.duration > 0) {
                _duration.value = player.duration
            }
        }
    }
    
    /**
     * 停止播放
     */
    fun stop() {
        _player?.stop()
        _isReady.value = false
    }
    
    /**
     * 释放播放器资源
     */
    fun release() {
        _player?.release()
        _player = null
        _isPlaying.value = false
        _isReady.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }
    
    /**
     * 设置音量
     */
    fun setVolume(volume: Float) {
        _player?.volume = volume.coerceIn(0f, 1f)
    }
    
    /**
     * 获取视频宽高比
     */
    fun getVideoAspectRatio(): Float {
        val videoSize = _videoSize.value ?: return 0f
        if (videoSize.height == 0) return 0f
        return videoSize.width.toFloat() / videoSize.height.toFloat()
    }
    
    /**
     * 判断是否为横屏视频
     */
    fun isLandscapeVideo(): Boolean {
        val aspectRatio = getVideoAspectRatio()
        return aspectRatio > 1.0f
    }
}

