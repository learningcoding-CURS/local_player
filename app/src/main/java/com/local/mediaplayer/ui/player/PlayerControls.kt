package com.local.mediaplayer.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 播放器控制栏
 */
@Composable
fun PlayerControls(
    currentPosition: Long,
    duration: Long,
    playbackSpeed: Float,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onLock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSpeedMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(16.dp)
    ) {
        // 进度条
        Column {
            Slider(
                value = if (duration > 0) currentPosition.toFloat() else 0f,
                valueRange = 0f..(duration.toFloat()),
                onValueChange = { onSeek(it.toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
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
            // 后退 15 秒
            IconButton(onClick = onSeekBackward) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "后退 15 秒",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 播放/暂停
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // 前进 15 秒
            IconButton(onClick = onSeekForward) {
                Icon(
                    imageVector = Icons.Default.Forward,
                    contentDescription = "前进 15 秒",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // 倍速菜单
            Box {
                TextButton(onClick = { showSpeedMenu = true }) {
                    Text(
                        text = "${playbackSpeed}x",
                        color = Color.White
                    )
                }
                
                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = { showSpeedMenu = false }
                ) {
                    val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f)
                    speeds.forEach { speed ->
                        DropdownMenuItem(
                            text = { Text("${speed}x") },
                            onClick = {
                                onSpeedChange(speed)
                                showSpeedMenu = false
                            }
                        )
                    }
                }
            }
            
            // 锁定按钮
            IconButton(onClick = onLock) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "锁定",
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * 格式化时间（毫秒 -> HH:MM:SS 或 MM:SS）
 */
fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

