package com.local.mediaplayer.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.local.mediaplayer.player.ExoPlayerManager
import com.local.mediaplayer.player.PlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 播放器屏幕
 * 包含视频播放、手势控制、UI 控制等功能
 */
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    playerManager: ExoPlayerManager,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    
    // 收集状态
    val showControls by viewModel.showControls.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val currentPosition by playerManager.currentPosition.collectAsState()
    val duration by playerManager.duration.collectAsState()
    val playbackSpeed by playerManager.playbackSpeed.collectAsState()
    
    // 手势状态
    var gestureState by remember { mutableStateOf<GestureState>(GestureState.None) }
    
    // 自动隐藏控制栏
    LaunchedEffect(showControls) {
        if (showControls && !isLocked) {
            delay(3000)
            viewModel.hideControls()
        }
    }
    
    // 管理 PlayerView 的生命周期
    DisposableEffect(Unit) {
        onDispose {
            // PlayerView 销毁时不需要释放 player，因为 player 由 playerManager 管理
            // playerManager 会在 ViewModel.onCleared() 时释放
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ExoPlayer 视图
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.getPlayer()
                    useController = false // 使用自定义控制
                }
            },
            update = { playerView ->
                // 确保 player 正确绑定（处理配置变化）
                val currentPlayer = playerManager.getPlayer()
                if (playerView.player != currentPlayer) {
                    playerView.player = currentPlayer
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (!isLocked) {
                                if (showControls) {
                                    viewModel.hideControls()
                                } else {
                                    viewModel.showControls()
                                }
                            }
                        },
                        onDoubleTap = { offset ->
                            if (!isLocked) {
                                // 双击左侧：后退 15 秒
                                // 双击右侧：前进 15 秒
                                val screenWidth = size.width
                                if (offset.x < screenWidth / 2) {
                                    viewModel.seekBackward()
                                    gestureState = GestureState.SeekBackward
                                    scope.launch {
                                        delay(500)
                                        gestureState = GestureState.None
                                    }
                                } else {
                                    viewModel.seekForward()
                                    gestureState = GestureState.SeekForward
                                    scope.launch {
                                        delay(500)
                                        gestureState = GestureState.None
                                    }
                                }
                            }
                        },
                        onLongPress = {
                            if (!isLocked) {
                                viewModel.onLongPressStart()
                                gestureState = GestureState.SpeedUp
                            }
                        }
                    )
                }
                .pointerInput(Unit) {
                    // 处理长按释放
                    detectTapGestures(
                        onPress = {
                            val pressed = tryAwaitRelease()
                            if (!pressed && gestureState == GestureState.SpeedUp) {
                                viewModel.onLongPressEnd()
                                gestureState = GestureState.None
                            }
                        }
                    )
                }
        )
        
        // 手势反馈提示
        GestureFeedback(gestureState = gestureState)
        
        // 控制栏
        if (showControls && !isLocked) {
            PlayerControls(
                currentPosition = currentPosition,
                duration = duration,
                playbackSpeed = playbackSpeed,
                isPlaying = playerManager.playbackState.collectAsState().value is com.local.mediaplayer.player.PlaybackState.Playing,
                onPlayPause = { viewModel.togglePlayPause() },
                onSeek = { viewModel.seekTo(it) },
                onSeekBackward = { viewModel.seekBackward() },
                onSeekForward = { viewModel.seekForward() },
                onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                onLock = { viewModel.toggleLock() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // 锁定按钮（始终显示）
        if (isLocked) {
            IconButton(
                onClick = { viewModel.toggleLock() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "解锁",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 手势状态
 */
sealed class GestureState {
    object None : GestureState()
    object SeekBackward : GestureState()
    object SeekForward : GestureState()
    object SpeedUp : GestureState()
    data class BrightnessAdjust(val value: Float) : GestureState()
    data class VolumeAdjust(val value: Int) : GestureState()
}

/**
 * 手势反馈提示
 */
@Composable
fun GestureFeedback(gestureState: GestureState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        when (gestureState) {
            is GestureState.SeekBackward -> {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "后退 15 秒",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is GestureState.SeekForward -> {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "前进 15 秒",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            is GestureState.SpeedUp -> {
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "2.5x 加速中",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {}
        }
    }
}

