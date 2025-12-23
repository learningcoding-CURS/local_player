package com.localmedia.player.utils

import android.content.Context
import android.media.AudioManager
import android.view.WindowManager
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

/**
 * 手势处理器
 * 处理播放器的各种手势交互
 */
class GestureHandler(private val context: Context) {
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    
    // 最大音量
    private val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
    
    // 手势类型
    enum class GestureType {
        NONE,
        BRIGHTNESS,    // 调节亮度
        VOLUME,        // 调节音量
        SEEK,          // 快进/快退
        DOUBLE_TAP     // 双击
    }
    
    /**
     * 判断是否在左半屏
     */
    fun isLeftHalf(offset: Offset, screenWidth: Float): Boolean {
        return offset.x < screenWidth / 2
    }
    
    /**
     * 判断是否在右半屏
     */
    fun isRightHalf(offset: Offset, screenWidth: Float): Boolean {
        return offset.x >= screenWidth / 2
    }
    
    /**
     * 计算亮度变化
     * @param dragAmount 拖动距离（负数为向上，正数为向下）
     * @param screenHeight 屏幕高度
     * @return 新的亮度值 (0.0 - 1.0)
     */
    fun calculateBrightnessChange(
        currentBrightness: Float,
        dragAmount: Float,
        screenHeight: Float
    ): Float {
        // 将拖动距离转换为亮度变化（屏幕高度的 1/3 为完整范围）
        val delta = -dragAmount / (screenHeight / 3f)
        val newBrightness = (currentBrightness + delta).coerceIn(0.01f, 1.0f)
        return newBrightness
    }
    
    /**
     * 计算音量变化
     * @param dragAmount 拖动距离（负数为向上，正数为向下）
     * @param screenHeight 屏幕高度
     * @return 新的音量值 (0 - maxVolume)
     */
    fun calculateVolumeChange(
        currentVolume: Int,
        dragAmount: Float,
        screenHeight: Float
    ): Int {
        // 将拖动距离转换为音量变化（屏幕高度的 1/3 为完整范围）
        val delta = (-dragAmount / (screenHeight / 3f) * maxVolume).toInt()
        val newVolume = (currentVolume + delta).coerceIn(0, maxVolume)
        return newVolume
    }
    
    /**
     * 设置系统音量
     */
    fun setVolume(volume: Int) {
        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume.coerceIn(0, maxVolume),
            0
        )
    }
    
    /**
     * 获取当前音量
     */
    fun getCurrentVolume(): Int {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
    }
    
    /**
     * 获取音量百分比
     */
    fun getVolumePercentage(): Float {
        return getCurrentVolume().toFloat() / maxVolume.toFloat()
    }
    
    /**
     * 计算快进/快退时间
     * @param dragAmount 拖动距离
     * @param screenWidth 屏幕宽度
     * @return 快进/快退的毫秒数（负数为后退）
     */
    fun calculateSeekTime(dragAmount: Float, screenWidth: Float): Long {
        // 屏幕宽度的 1/4 为 10 秒
        val seconds = (dragAmount / (screenWidth / 4f)) * 10f
        return (seconds * 1000).toLong()
    }
    
    /**
     * 判断是否为垂直滑动
     */
    fun isVerticalDrag(offset: Offset, startOffset: Offset): Boolean {
        val dx = abs(offset.x - startOffset.x)
        val dy = abs(offset.y - startOffset.y)
        return dy > dx && dy > 20f // 垂直滑动且移动距离 > 20dp
    }
    
    /**
     * 判断是否为水平滑动
     */
    fun isHorizontalDrag(offset: Offset, startOffset: Offset): Boolean {
        val dx = abs(offset.x - startOffset.x)
        val dy = abs(offset.y - startOffset.y)
        return dx > dy && dx > 20f // 水平滑动且移动距离 > 20dp
    }
}

/**
 * 亮度管理器
 */
class BrightnessManager(private val window: android.view.Window) {
    
    /**
     * 设置窗口亮度（不需要 WRITE_SETTINGS 权限）
     * @param brightness 0.0 - 1.0
     */
    fun setScreenBrightness(brightness: Float) {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = brightness.coerceIn(0.01f, 1.0f)
        window.attributes = layoutParams
    }
    
    /**
     * 获取当前窗口亮度
     */
    fun getScreenBrightness(): Float {
        val brightness = window.attributes.screenBrightness
        // -1.0 表示使用系统亮度
        return if (brightness < 0) 0.5f else brightness
    }
    
    /**
     * 恢复系统默认亮度
     */
    fun resetScreenBrightness() {
        val layoutParams = window.attributes
        layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = layoutParams
    }
}

