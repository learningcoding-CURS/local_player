package com.localmedia.player.ui.screen

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.localmedia.player.player.ExoPlayerManager
import com.localmedia.player.utils.BrightnessManager
import com.localmedia.player.utils.GestureHandler
import kotlinx.coroutines.delay
import kotlin.math.abs

/**
 * 播放器主界面
 * 包含手势控制、UI 控件、锁定模式等
 */
@Composable
fun PlayerScreen(
    playerManager: ExoPlayerManager,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as Activity
    val view = LocalView.current
    
    // 手势和亮度管理
    val gestureHandler = remember { GestureHandler(context) }
    val brightnessManager = remember { BrightnessManager(activity.window) }
    
    // UI 状态
    var showControls by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    
    // 手势状态
    var isDragging by remember { mutableStateOf(false) }
    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var currentDragOffset by remember { mutableStateOf(Offset.Zero) }
    var gestureType by remember { mutableStateOf(GestureHandler.GestureType.NONE) }
    var isLongPressing by remember { mutableStateOf(false) }
    
    // 调节显示
    var showBrightnessIndicator by remember { mutableStateOf(false) }
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var brightnessValue by remember { mutableStateOf(0.5f) }
    var volumeValue by remember { mutableStateOf(0.5f) }
    
    // 播放器状态
    val isPlaying by playerManager.isPlaying.collectAsState()
    val currentPosition by playerManager.currentPosition.collectAsState()
    val duration by playerManager.duration.collectAsState()
    val playbackSpeed by playerManager.playbackSpeed.collectAsState()
    
    // 自动隐藏控件
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying && !isLocked) {
            delay(5000)
            showControls = false
        }
    }
    
    // 定时更新进度
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            playerManager.updateProgress()
            delay(100)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                
                detectTapGestures(
                    onTap = { offset ->
                        showControls = !showControls
                    },
                    onDoubleTap = { offset ->
                        val screenWidth = size.width.toFloat()
                        if (gestureHandler.isLeftHalf(offset, screenWidth)) {
                            // 左侧双击：后退 15 秒
                            playerManager.seekBackward()
                        } else {
                            // 右侧双击：前进 15 秒
                            playerManager.seekForward()
                        }
                    },
                    onLongPress = {
                        // 长按：临时 2.5x 速度
                        isLongPressing = true
                        playerManager.startLongPress()
                    },
                    onPress = {
                        val press = tryAwaitRelease()
                        if (!press && isLongPressing) {
                            // 长按释放
                            isLongPressing = false
                            playerManager.endLongPress()
                        }
                    }
                )
            }
            .pointerInput(isLocked) {
                if (isLocked) return@pointerInput
                
                // 检测拖动手势
                androidx.compose.foundation.gestures.detectDragGestures(
                    onDragStart = { offset ->
                        dragStartOffset = offset
                        isDragging = true
                        gestureType = GestureHandler.GestureType.NONE
                    },
                    onDrag = { change, dragAmount ->
                        if (!isDragging) return@detectDragGestures
                        
                        currentDragOffset = change.position
                        val screenWidth = size.width.toFloat()
                        val screenHeight = size.height.toFloat()
                        
                        // 判断手势类型
                        if (gestureType == GestureHandler.GestureType.NONE) {
                            if (gestureHandler.isVerticalDrag(currentDragOffset, dragStartOffset)) {
                                gestureType = if (gestureHandler.isLeftHalf(dragStartOffset, screenWidth)) {
                                    GestureHandler.GestureType.BRIGHTNESS
                                } else {
                                    GestureHandler.GestureType.VOLUME
                                }
                            } else if (gestureHandler.isHorizontalDrag(currentDragOffset, dragStartOffset)) {
                                gestureType = GestureHandler.GestureType.SEEK
                            }
                        }
                        
                        // 处理手势
                        when (gestureType) {
                            GestureHandler.GestureType.BRIGHTNESS -> {
                                val currentBrightness = brightnessManager.getScreenBrightness()
                                val newBrightness = gestureHandler.calculateBrightnessChange(
                                    currentBrightness,
                                    dragAmount.y,
                                    screenHeight
                                )
                                brightnessManager.setScreenBrightness(newBrightness)
                                brightnessValue = newBrightness
                                showBrightnessIndicator = true
                            }
                            GestureHandler.GestureType.VOLUME -> {
                                val currentVolume = gestureHandler.getCurrentVolume()
                                val newVolume = gestureHandler.calculateVolumeChange(
                                    currentVolume,
                                    dragAmount.y,
                                    screenHeight
                                )
                                gestureHandler.setVolume(newVolume)
                                volumeValue = gestureHandler.getVolumePercentage()
                                showVolumeIndicator = true
                            }
                            GestureHandler.GestureType.SEEK -> {
                                // 快进/快退显示
                                // 实际跳转在 onDragEnd 执行
                            }
                            else -> {}
                        }
                    },
                    onDragEnd = {
                        if (gestureType == GestureHandler.GestureType.SEEK) {
                            val dragDistance = currentDragOffset.x - dragStartOffset.x
                            val seekTime = gestureHandler.calculateSeekTime(
                                dragDistance,
                                size.width.toFloat()
                            )
                            val newPosition = (currentPosition + seekTime).coerceIn(0, duration)
                            playerManager.seekTo(newPosition)
                        }
                        
                        isDragging = false
                        gestureType = GestureHandler.GestureType.NONE
                        
                        // 隐藏指示器
                        kotlinx.coroutines.GlobalScope.launch {
                            delay(1000)
                            showBrightnessIndicator = false
                            showVolumeIndicator = false
                        }
                    }
                )
            }
    ) {
        // ExoPlayer 视图
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = playerManager.player
                    useController = false // 使用自定义控件
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // 长按提示
        if (isLongPressing) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                    .padding(24.dp)
            ) {
                Text(
                    text = "2.5x",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        
        // 亮度指示器
        if (showBrightnessIndicator) {
            BrightnessIndicator(
                brightness = brightnessValue,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 音量指示器
        if (showVolumeIndicator) {
            VolumeIndicator(
                volume = volumeValue,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // 锁定模式
        if (isLocked) {
            IconButton(
                onClick = { isLocked = false },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "解锁",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // 控制界面
        if (showControls && !isLocked) {
            PlayerControls(
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                playbackSpeed = playbackSpeed,
                onPlayPause = { playerManager.togglePlayPause() },
                onSeekTo = { playerManager.seekTo(it) },
                onSeekForward = { playerManager.seekForward() },
                onSeekBackward = { playerManager.seekBackward() },
                onSpeedClick = { showSpeedMenu = true },
                onLock = { isLocked = true },
                onBack = onBack,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        
        // 倍速选择菜单
        if (showSpeedMenu) {
            SpeedSelectionMenu(
                currentSpeed = playbackSpeed,
                availableSpeeds = playerManager.availableSpeeds,
                onSpeedSelected = { speed ->
                    playerManager.setPlaybackSpeed(speed)
                    showSpeedMenu = false
                },
                onDismiss = { showSpeedMenu = false },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun PlayerControls(
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSpeedClick: () -> Unit,
    onLock: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        // 顶部栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }
            
            Row {
                IconButton(onClick = onLock) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = "锁定",
                        tint = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 进度条
        Column {
            Slider(
                value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                onValueChange = { progress ->
                    onSeekTo((progress * duration).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = formatTime(duration),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSeekBackward) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "后退 15 秒",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            IconButton(onClick = onSeekForward) {
                Icon(
                    imageVector = Icons.Default.Forward,
                    contentDescription = "前进 15 秒",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            TextButton(onClick = onSpeedClick) {
                Text(
                    text = "${playbackSpeed}x",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun SpeedSelectionMenu(
    currentSpeed: Float,
    availableSpeeds: List<Float>,
    onSpeedSelected: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(32.dp)
            .wrapContentSize(),
        shape = MaterialTheme.shapes.medium,
        color = Color.Black.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "播放速度",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            availableSpeeds.forEach { speed ->
                TextButton(
                    onClick = { onSpeedSelected(speed) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${speed}x",
                        color = if (speed == currentSpeed) MaterialTheme.colorScheme.primary else Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("取消", color = Color.White)
            }
        }
    }
}

@Composable
fun BrightnessIndicator(
    brightness: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Brightness6,
                contentDescription = "亮度",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(brightness * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun VolumeIndicator(
    volume: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = if (volume > 0) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                contentDescription = "音量",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(volume * 100).toInt()}%",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

