package com.local.mediaplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.local.mediaplayer.MainActivity
import com.local.mediaplayer.R

/**
 * 后台播放服务
 * 使用 Media3 的 MediaSessionService 实现后台播放和通知栏控制
 * 
 * 注意：此服务当前未激活使用。
 * 在当前实现中，播放器由 ExoPlayerManager 直接管理，避免多个 Player 实例冲突。
 * 如需启用后台播放服务，需要重构为单例模式或使用依赖注入来共享 Player 实例。
 */
class PlaybackService : MediaSessionService() {
    
    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    
    companion object {
        private const val TAG = "PlaybackService"
        private const val NOTIFICATION_CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_ID = 1001
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "PlaybackService onCreate")
        
        // 创建通知渠道
        createNotificationChannel()
        
        // 注意：当前实现创建了独立的 player 实例
        // 这可能与主界面的 player 冲突
        // TODO: 重构为使用共享的 player 实例
        try {
            player = ExoPlayer.Builder(this).build()
            
            // 创建 MediaSession
            mediaSession = MediaSession.Builder(this, player!!)
                .setCallback(MediaSessionCallback())
                .build()
            
            Log.d(TAG, "Player and MediaSession initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize player", e)
        }
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        Log.d(TAG, "PlaybackService onDestroy")
        try {
            mediaSession?.let { session ->
                player?.let { p ->
                    p.stop()
                    p.release()
                }
                session.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        } finally {
            mediaSession = null
            player = null
        }
        super.onDestroy()
    }
    
    /**
     * 创建通知渠道（Android 8.0+）
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "媒体播放",  // 硬编码字符串，避免资源缺失导致崩溃
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "媒体播放控制通知"
                    setShowBadge(false)
                }
                
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create notification channel", e)
            }
        }
    }
    
    /**
     * MediaSession 回调
     * 处理播放控制命令
     * 
     * 注意：Media3 的 MediaSession.Callback 会自动处理播放控制，
     * 不需要显式覆盖 onPlay/onPause/onStop 方法。
     * 如需自定义行为，可以通过 Player.Listener 监听播放状态变化。
     */
    private inner class MediaSessionCallback : MediaSession.Callback {
        // MediaSession.Callback 会自动将播放控制命令转发给 player
        // 这里保留空实现，如需扩展可以添加自定义逻辑
    }
}

