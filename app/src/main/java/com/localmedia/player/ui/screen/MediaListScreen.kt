package com.localmedia.player.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.localmedia.player.data.entity.MediaItem
import com.localmedia.player.data.entity.MediaType

/**
 * 媒体列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaListScreen(
    onMediaItemClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    // 示例数据，实际使用需要从 ViewModel 获取
    var mediaItems by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            // 处理选择的文件
            // 实际使用需要保存到数据库
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("本地播放器") },
                actions = {
                    IconButton(onClick = {
                        // 打开文件选择器
                        filePickerLauncher.launch(arrayOf("video/*", "audio/*"))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加媒体"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(arrayOf("video/*", "audio/*"))
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加"
                )
            }
        }
    ) { paddingValues ->
        if (mediaItems.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "没有媒体文件",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "点击 + 按钮添加视频或音频",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // 媒体列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mediaItems) { item ->
                    MediaItemCard(
                        item = item,
                        onClick = { onMediaItemClick(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun MediaItemCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (item.type == MediaType.VIDEO) {
                    Icons.Default.VideoFile
                } else {
                    Icons.Default.AudioFile
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = formatDuration(item.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (item.lastPosition > 0) {
                    LinearProgressIndicator(
                        progress = item.lastPosition.toFloat() / item.duration.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

