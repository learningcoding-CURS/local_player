package com.local.mediaplayer.ui.utils

import android.content.Context
import android.media.AudioManager

/**
 * 音量管理器
 */
class VolumeManager(private val context: Context) {
    
    private val audioManager: AudioManager = 
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    /**
     * 获取当前音量（0-100）
     */
    fun getCurrentVolume(): Int {
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        return (current * 100f / max).toInt()
    }
    
    /**
     * 设置音量（0-100）
     */
    fun setVolume(volumePercent: Int) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volume = (volumePercent * max / 100f).toInt().coerceIn(0, max)
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume,
            0 // 不显示系统音量面板
        )
    }
    
    /**
     * 调整音量（增量）
     */
    fun adjustVolume(delta: Int) {
        val current = getCurrentVolume()
        setVolume((current + delta).coerceIn(0, 100))
    }
}

