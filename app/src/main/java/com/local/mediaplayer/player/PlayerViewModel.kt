package com.local.mediaplayer.player

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.local.mediaplayer.data.entity.MediaItem
import com.local.mediaplayer.data.repository.MediaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 播放器 ViewModel
 * 管理播放逻辑和 UI 状态
 */
class PlayerViewModel(
    private val playerManager: ExoPlayerManager,
    private val repository: MediaRepository
) : ViewModel() {
    
    // 当前播放的媒体项
    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem.asStateFlow()
    
    // 播放列表
    private val _playlist = MutableStateFlow<List<MediaItem>>(emptyList())
    val playlist: StateFlow<List<MediaItem>> = _playlist.asStateFlow()
    
    // 当前播放索引
    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    
    // UI 控制显示
    private val _showControls = MutableStateFlow(true)
    val showControls: StateFlow<Boolean> = _showControls.asStateFlow()
    
    // 锁屏模式
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()
    
    // 进度更新任务
    private var progressJob: Job? = null
    
    // 位置保存任务
    private var savePositionJob: Job? = null
    
    init {
        // 监听播放状态，自动开始/停止进度更新
        viewModelScope.launch {
            playerManager.playbackState.collect { state ->
                when (state) {
                    is PlaybackState.Playing -> {
                        startProgressUpdate()
                        startAutoSavePosition()
                    }
                    is PlaybackState.Paused, is PlaybackState.Ended -> {
                        saveCurrentPosition()
                    }
                    else -> {}
                }
            }
        }
    }
    
    /**
     * 加载媒体项并播放
     */
    fun loadAndPlay(mediaItem: MediaItem) {
        _currentMediaItem.value = mediaItem
        val uri = Uri.parse(mediaItem.uri)
        playerManager.loadMedia(uri, mediaItem.lastPosition)
        playerManager.play()
        
        // 更新播放统计
        viewModelScope.launch {
            repository.updatePlayStats(mediaItem.id)
        }
    }
    
    /**
     * 设置播放列表
     */
    fun setPlaylist(items: List<MediaItem>, startIndex: Int = 0) {
        _playlist.value = items
        _currentIndex.value = startIndex
        if (items.isNotEmpty()) {
            loadAndPlay(items[startIndex])
        }
    }
    
    /**
     * 播放下一个
     */
    fun playNext() {
        val items = _playlist.value
        if (items.isEmpty()) return
        
        saveCurrentPosition()
        val nextIndex = (_currentIndex.value + 1) % items.size
        _currentIndex.value = nextIndex
        loadAndPlay(items[nextIndex])
    }
    
    /**
     * 播放上一个
     */
    fun playPrevious() {
        val items = _playlist.value
        if (items.isEmpty()) return
        
        saveCurrentPosition()
        val prevIndex = if (_currentIndex.value == 0) {
            items.size - 1
        } else {
            _currentIndex.value - 1
        }
        _currentIndex.value = prevIndex
        loadAndPlay(items[prevIndex])
    }
    
    /**
     * 切换播放/暂停
     */
    fun togglePlayPause() {
        playerManager.togglePlayPause()
    }
    
    /**
     * Seek 到指定位置
     */
    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }
    
    /**
     * 前进 15 秒
     */
    fun seekForward() {
        playerManager.seekForward()
    }
    
    /**
     * 后退 15 秒
     */
    fun seekBackward() {
        playerManager.seekBackward()
    }
    
    /**
     * 设置播放速度
     */
    fun setPlaybackSpeed(speed: Float) {
        playerManager.setPlaybackSpeed(speed)
    }
    
    /**
     * 长按开始
     */
    fun onLongPressStart() {
        playerManager.onLongPressStart()
    }
    
    /**
     * 长按结束
     */
    fun onLongPressEnd() {
        playerManager.onLongPressEnd()
    }
    
    /**
     * 切换锁定状态
     */
    fun toggleLock() {
        _isLocked.value = !_isLocked.value
    }
    
    /**
     * 显示控制
     */
    fun showControls() {
        _showControls.value = true
    }
    
    /**
     * 隐藏控制
     */
    fun hideControls() {
        if (!_isLocked.value) {
            _showControls.value = false
        }
    }
    
    /**
     * 开始进度更新
     */
    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                playerManager.updateProgress()
                delay(200) // 每 200ms 更新一次
            }
        }
    }
    
    /**
     * 开始自动保存位置（每 5 秒）
     */
    private fun startAutoSavePosition() {
        savePositionJob?.cancel()
        savePositionJob = viewModelScope.launch {
            while (true) {
                delay(5000) // 每 5 秒保存一次
                saveCurrentPosition()
            }
        }
    }
    
    /**
     * 保存当前播放位置
     */
    private fun saveCurrentPosition() {
        val mediaItem = _currentMediaItem.value ?: return
        val position = playerManager.getCurrentPosition()
        
        viewModelScope.launch {
            repository.updatePosition(mediaItem.id, position)
        }
    }
    
    /**
     * 暂停播放并保存位置（用于后台或Activity暂停）
     */
    fun onPause() {
        playerManager.pause()
        saveCurrentPosition()
    }
    
    /**
     * 恢复播放（从后台返回）
     */
    fun onResume() {
        // 不自动播放，让用户决定是否继续
    }
    
    /**
     * 停止播放（Activity停止时调用）
     */
    fun onStop() {
        playerManager.pause()
        saveCurrentPosition()
        progressJob?.cancel()
        savePositionJob?.cancel()
    }
    
    /**
     * 释放资源
     */
    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        savePositionJob?.cancel()
        saveCurrentPosition()
        // 释放播放器资源
        playerManager.release()
    }
}

