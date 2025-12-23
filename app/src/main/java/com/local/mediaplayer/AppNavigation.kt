package com.local.mediaplayer

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.local.mediaplayer.data.database.AppDatabase
import com.local.mediaplayer.data.repository.MediaRepository
import com.local.mediaplayer.player.ExoPlayerManager
import com.local.mediaplayer.player.PlayerViewModel
import com.local.mediaplayer.ui.library.LibraryScreen
import com.local.mediaplayer.ui.library.LibraryViewModel
import com.local.mediaplayer.ui.player.PlayerScreen

private const val TAG = "AppNavigation"

/**
 * 应用导航
 */
@Composable
fun AppNavigation() {
    Log.d(TAG, "AppNavigation composing...")
    val navController = rememberNavController()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // 初始化数据库和仓库
    val database = remember { 
        try {
            Log.d(TAG, "Initializing database...")
            AppDatabase.getInstance(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize database", e)
            throw e
        }
    }
    
    val repository = remember { 
        try {
            Log.d(TAG, "Creating repository...")
            MediaRepository(database.mediaItemDao(), database.categoryDao())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create repository", e)
            throw e
        }
    }
    
    // 初始化播放器管理器
    val playerManager = remember { 
        try {
            Log.d(TAG, "Creating player manager...")
            ExoPlayerManager(context).apply { initializePlayer() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create player manager", e)
            throw e
        }
    }
    
    // 初始化 ViewModel
    val libraryViewModel = remember { 
        try {
            Log.d(TAG, "Creating library viewModel...")
            LibraryViewModel(repository, context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create library viewModel", e)
            throw e
        }
    }
    
    val playerViewModel = remember { 
        try {
            Log.d(TAG, "Creating player viewModel...")
            PlayerViewModel(playerManager, repository)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create player viewModel", e)
            throw e
        }
    }
    
    // 监听生命周期事件
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Activity 暂停时暂停播放
                    playerViewModel.onPause()
                }
                Lifecycle.Event.ON_STOP -> {
                    // Activity 停止时停止播放
                    playerViewModel.onStop()
                }
                Lifecycle.Event.ON_RESUME -> {
                    // Activity 恢复时（可选择是否自动播放）
                    playerViewModel.onResume()
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // 清理资源会由 ViewModel.onCleared() 处理
        }
    }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { libraryViewModel.importMediaFile(it) }
    }
    
    NavHost(navController = navController, startDestination = "library") {
        // 媒体库屏幕
        composable("library") {
            LibraryScreen(
                viewModel = libraryViewModel,
                onMediaItemClick = { mediaItem ->
                    playerViewModel.loadAndPlay(mediaItem)
                    navController.navigate("player")
                },
                onAddMediaClick = {
                    // 打开文件选择器
                    filePickerLauncher.launch(arrayOf("video/*", "audio/*"))
                }
            )
        }
        
        // 播放器屏幕
        composable("player") {
            PlayerScreen(
                viewModel = playerViewModel,
                playerManager = playerManager
            )
        }
    }
}

