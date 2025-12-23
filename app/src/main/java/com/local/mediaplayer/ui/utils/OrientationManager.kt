package com.local.mediaplayer.ui.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.media3.common.VideoSize

/**
 * 屏幕方向管理器
 * 根据视频尺寸自动切换横竖屏
 */
class OrientationManager(private val activity: Activity) {
    
    /**
     * 根据视频尺寸设置屏幕方向
     * @param videoSize 视频尺寸，null 表示音频
     */
    fun setOrientation(videoSize: VideoSize?) {
        if (videoSize == null) {
            // 音频：竖屏
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            // 视频：根据宽高比决定
            if (videoSize.width > videoSize.height) {
                // 横屏视频
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                // 竖屏视频
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }
    
    /**
     * 锁定当前方向
     */
    fun lockOrientation() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
    }
    
    /**
     * 解锁方向
     */
    fun unlockOrientation() {
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }
}

