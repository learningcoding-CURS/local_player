package com.local.mediaplayer

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
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
import kotlinx.coroutines.launch

private const val TAG = "AppNavigation"

/**
 * 应用导航
 * 管理应用内的页面跳转和状态
 */
@Composable
fun AppNavigation() {
    Log.d(TAG, "AppNavigation composing...")
    
    val context = LocalContext.current
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    // 初始化数据库和仓库
    val database = remember {
        Log.d(TAG, "Initializing database...")
        AppDatabase.getInstance(context)
    }
    val repository = remember {
        Log.d(TAG, "Creating repository...")
        MediaRepository(
            mediaItemDao = database.mediaItemDao(),
            categoryDao = database.categoryDao()
        )
    }
    
    // 初始化播放器管理器
    val playerManager = remember {
        Log.d(TAG, "Creating player manager...")
        ExoPlayerManager(context)
    }
    
    // 创建 ViewModels（手动创建，因为没有专用的 Factory）
    val libraryViewModel = remember {
        LibraryViewModel(repository, context)
    }
    
    val playerViewModel = remember {
        PlayerViewModel(playerManager, repository)
    }
    
    // 监听 Activity 生命周期，管理播放器
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Log.d(TAG, "Lifecycle ON_PAUSE - pausing playback")
                    playerViewModel.onPause()
                }
                Lifecycle.Event.ON_STOP -> {
                    Log.d(TAG, "Lifecycle ON_STOP - stopping playback")
                    playerViewModel.onStop()
                }
                Lifecycle.Event.ON_RESUME -> {
                    Log.d(TAG, "Lifecycle ON_RESUME - resuming if needed")
                    playerViewModel.onResume()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            Log.d(TAG, "Removing lifecycle observer")
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // 文件选择器
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d(TAG, "File selected: $uri")
            libraryViewModel.importMediaFile(uri)
        }
    }
    
    NavHost(navController = navController, startDestination = "library") {
        // 媒体库页面
        composable("library") {
            LibraryScreen(
                viewModel = libraryViewModel,
                onMediaItemClick = { mediaItem ->
                    Log.d(TAG, "Media item clicked: ${mediaItem.title}")
                    navController.navigate("player/${mediaItem.id}")
                },
                onAddMediaClick = {
                    Log.d(TAG, "Add media clicked")
                    fileLauncher.launch("video/*")
                }
            )
        }
        
        // 播放器页面
        composable(
            route = "player/{mediaItemId}",
            arguments = listOf(navArgument("mediaItemId") { type = NavType.LongType })
        ) { backStackEntry ->
            val mediaItemId = backStackEntry.arguments?.getLong("mediaItemId") ?: return@composable
            
            LaunchedEffect(mediaItemId) {
                Log.d(TAG, "Loading media item: $mediaItemId")
                val mediaItem = repository.getItemById(mediaItemId)
                mediaItem?.let { 
                    playerViewModel.loadAndPlay(it)
                }
            }
            
            PlayerScreen(
                viewModel = playerViewModel,
                playerManager = playerManager
            )
        }
    }
    
    Log.d(TAG, "AppNavigation composed successfully")
}
