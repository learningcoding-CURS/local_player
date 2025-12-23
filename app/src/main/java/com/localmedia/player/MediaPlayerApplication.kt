package com.localmedia.player

import android.app.Application
import com.localmedia.player.data.MediaDatabase

/**
 * Application 类
 * 初始化全局组件
 */
class MediaPlayerApplication : Application() {
    
    // 数据库实例
    val database: MediaDatabase by lazy {
        MediaDatabase.getInstance(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        // 初始化其他组件
    }
}

