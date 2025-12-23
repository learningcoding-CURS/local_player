package com.local.mediaplayer.ui.utils

import android.app.Activity
import android.view.WindowManager

/**
 * 亮度管理器
 * 管理应用内窗口亮度（不需要 WRITE_SETTINGS 权限）
 */
class BrightnessManager(private val activity: Activity) {
    
    companion object {
        private const val MIN_BRIGHTNESS = 0.01f
        private const val MAX_BRIGHTNESS = 1.0f
    }
    
    /**
     * 获取当前亮度
     */
    fun getCurrentBrightness(): Float {
        val layoutParams = activity.window.attributes
        return if (layoutParams.screenBrightness == WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) {
            0.5f // 默认值
        } else {
            layoutParams.screenBrightness
        }
    }
    
    /**
     * 设置亮度（0.0 - 1.0）
     */
    fun setBrightness(brightness: Float) {
        val clampedBrightness = brightness.coerceIn(MIN_BRIGHTNESS, MAX_BRIGHTNESS)
        val layoutParams = activity.window.attributes
        layoutParams.screenBrightness = clampedBrightness
        activity.window.attributes = layoutParams
    }
    
    /**
     * 调整亮度（增量）
     */
    fun adjustBrightness(delta: Float) {
        val current = getCurrentBrightness()
        setBrightness(current + delta)
    }
}

