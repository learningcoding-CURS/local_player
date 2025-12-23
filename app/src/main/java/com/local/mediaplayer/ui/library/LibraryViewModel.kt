package com.local.mediaplayer.ui.library

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.local.mediaplayer.data.entity.MediaItem
import com.local.mediaplayer.data.entity.MediaType
import com.local.mediaplayer.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 媒体库 ViewModel
 */
class LibraryViewModel(
    private val repository: MediaRepository,
    private val context: Context
) : ViewModel() {
    
    companion object {
        private const val TAG = "LibraryViewModel"
    }
    
    init {
        Log.d(TAG, "LibraryViewModel initialized")
    }
    
    // 媒体项列表
    val mediaItems: StateFlow<List<MediaItem>> = try {
        repository.getAllItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize mediaItems flow", e)
        throw e
    }
    
    /**
     * 导入媒体文件
     */
    fun importMediaFile(uri: Uri) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Importing media file: $uri")
                // 持久化 URI 权限
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                val documentFile = DocumentFile.fromSingleUri(context, uri)
                if (documentFile != null && documentFile.exists()) {
                    val mediaItem = createMediaItem(uri, documentFile)
                    repository.insertItem(mediaItem)
                    Log.d(TAG, "Media file imported successfully: ${mediaItem.title}")
                } else {
                    Log.w(TAG, "Document file is null or doesn't exist")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to import media file", e)
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 导入媒体文件夹
     */
    fun importMediaFolder(uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                val documentFile = DocumentFile.fromTreeUri(context, uri)
                if (documentFile != null && documentFile.isDirectory) {
                    val mediaItems = mutableListOf<MediaItem>()
                    processDirectory(documentFile, mediaItems)
                    repository.insertItems(mediaItems)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * 递归处理目录
     */
    private fun processDirectory(directory: DocumentFile, mediaItems: MutableList<MediaItem>) {
        directory.listFiles().forEach { file ->
            if (file.isDirectory) {
                processDirectory(file, mediaItems)
            } else if (file.isFile && isMediaFile(file)) {
                val mediaItem = createMediaItem(file.uri, file)
                mediaItems.add(mediaItem)
            }
        }
    }
    
    /**
     * 判断是否为媒体文件
     */
    private fun isMediaFile(file: DocumentFile): Boolean {
        val mimeType = file.type ?: return false
        return mimeType.startsWith("video/") || mimeType.startsWith("audio/")
    }
    
    /**
     * 创建媒体项
     */
    private fun createMediaItem(uri: Uri, documentFile: DocumentFile): MediaItem {
        val mimeType = documentFile.type ?: ""
        val type = if (mimeType.startsWith("video/")) {
            MediaType.VIDEO
        } else {
            MediaType.AUDIO
        }
        
        return MediaItem(
            uri = uri.toString(),
            title = documentFile.name ?: "未知",
            type = type,
            duration = 0 // 将在播放时更新
        )
    }
    
    /**
     * 删除媒体项
     */
    fun deleteMediaItem(item: MediaItem) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }
}

