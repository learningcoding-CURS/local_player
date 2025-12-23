package com.localmedia.player.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.media3.common.VideoSize

/**
 * 屏幕方向管理器
 * 根据视频宽高比自动切换横竖屏
 */
class OrientationManager(private val activity: Activity) {
    
    /**
     * 根据视频尺寸设置屏幕方向
     */
    fun setOrientationForVideo(videoSize: VideoSize?) {
        if (videoSize == null || videoSize.width == 0 || videoSize.height == 0) {
            // 无视频信息时保持竖屏
            setPortrait()
            return
        }
        
        val aspectRatio = videoSize.width.toFloat() / videoSize.height.toFloat()
        
        if (aspectRatio > 1.0f) {
            // 横屏视频
            setLandscape()
        } else {
            // 竖屏视频
            setPortrait()
        }
    }
    
    /**
     * 音频播放时始终竖屏
     */
    fun setOrientationForAudio() {
        setPortrait()
    }
    
    /**
     * 设置横屏
     */
    fun setLandscape() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }
    
    /**
     * 设置竖屏
     */
    fun setPortrait() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    
    /**
     * 设置自动旋转
     */
    fun setAutoRotate() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
    
    /**
     * 锁定当前方向
     */
    fun lockCurrentOrientation() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
    
    /**
     * 解锁方向
     */
    fun unlockOrientation() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}

