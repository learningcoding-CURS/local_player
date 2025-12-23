package com.local.mediaplayer.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.local.mediaplayer.data.entity.MediaItem
import com.local.mediaplayer.data.entity.MediaType
import com.local.mediaplayer.ui.player.formatTime

/**
 * 媒体库屏幕
 * 显示媒体列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onMediaItemClick: (MediaItem) -> Unit,
    onAddMediaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mediaItems by viewModel.mediaItems.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("媒体库") },
                actions = {
                    IconButton(onClick = onAddMediaClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加媒体"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mediaItems) { item ->
                MediaItemCard(
                    mediaItem = item,
                    onClick = { onMediaItemClick(item) }
                )
            }
        }
    }
}

/**
 * 媒体项卡片
 */
@Composable
fun MediaItemCard(
    mediaItem: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Icon(
                imageVector = if (mediaItem.type == MediaType.VIDEO) {
                    Icons.Default.VideoLibrary
                } else {
                    Icons.Default.MusicNote
                },
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mediaItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatTime(mediaItem.duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (mediaItem.lastPosition > 0 && mediaItem.duration > 0) {
                        val progress = (mediaItem.lastPosition * 100 / mediaItem.duration).toInt()
                        Text(
                            text = "已播放 $progress%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

