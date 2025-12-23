package com.localmedia.player.utils

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 文稿解析器
 * 用于解析带时间点的文稿并支持跳转
 */
class TranscriptParser(private val context: Context) {
    
    /**
     * 文稿时间点条目
     */
    data class TranscriptEntry(
        val timeMs: Long,        // 时间戳（毫秒）
        val text: String,        // 文本内容
        val originalText: String // 原始文本（包含时间标记）
    )
    
    /**
     * 时间点格式：
     * [HH:MM:SS.mmm] 文本内容
     * [MM:SS.mmm] 文本内容
     * [MM:SS] 文本内容
     */
    private val timePatterns = listOf(
        // [HH:MM:SS.mmm]
        """^\[(\d{2}):(\d{2}):(\d{2})\.(\d{3})\]\s*(.*)$""".toRegex(),
        // [HH:MM:SS]
        """^\[(\d{2}):(\d{2}):(\d{2})\]\s*(.*)$""".toRegex(),
        // [MM:SS.mmm]
        """^\[(\d{2}):(\d{2})\.(\d{3})\]\s*(.*)$""".toRegex(),
        // [MM:SS]
        """^\[(\d{2}):(\d{2})\]\s*(.*)$""".toRegex()
    )
    
    /**
     * 解析文稿文件
     */
    fun parseTranscript(uri: Uri): List<TranscriptEntry> {
        val content = readFileContent(uri)
        return parseTranscriptContent(content)
    }
    
    /**
     * 解析文稿内容
     */
    fun parseTranscriptContent(content: String): List<TranscriptEntry> {
        val entries = mutableListOf<TranscriptEntry>()
        val lines = content.lines()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            val entry = parseTimestampLine(trimmedLine)
            if (entry != null) {
                entries.add(entry)
            }
        }
        
        return entries.sortedBy { it.timeMs }
    }
    
    /**
     * 解析单行时间戳
     */
    private fun parseTimestampLine(line: String): TranscriptEntry? {
        for (pattern in timePatterns) {
            val match = pattern.find(line) ?: continue
            
            val groups = match.groupValues
            return when (groups.size) {
                // [HH:MM:SS.mmm] 文本
                6 -> {
                    val hours = groups[1].toLongOrNull() ?: continue
                    val minutes = groups[2].toLongOrNull() ?: continue
                    val seconds = groups[3].toLongOrNull() ?: continue
                    val millis = groups[4].toLongOrNull() ?: continue
                    val text = groups[5]
                    
                    TranscriptEntry(
                        timeMs = hours * 3600000 + minutes * 60000 + seconds * 1000 + millis,
                        text = text,
                        originalText = line
                    )
                }
                // [HH:MM:SS] 文本
                5 -> {
                    if (groups[4].length > 10) { // 可能是 [MM:SS.mmm]
                        val minutes = groups[1].toLongOrNull() ?: continue
                        val seconds = groups[2].toLongOrNull() ?: continue
                        val millis = groups[3].toLongOrNull() ?: continue
                        val text = groups[4]
                        
                        TranscriptEntry(
                            timeMs = minutes * 60000 + seconds * 1000 + millis,
                            text = text,
                            originalText = line
                        )
                    } else { // [HH:MM:SS]
                        val hours = groups[1].toLongOrNull() ?: continue
                        val minutes = groups[2].toLongOrNull() ?: continue
                        val seconds = groups[3].toLongOrNull() ?: continue
                        val text = groups[4]
                        
                        TranscriptEntry(
                            timeMs = hours * 3600000 + minutes * 60000 + seconds * 1000,
                            text = text,
                            originalText = line
                        )
                    }
                }
                // [MM:SS] 文本
                4 -> {
                    val minutes = groups[1].toLongOrNull() ?: continue
                    val seconds = groups[2].toLongOrNull() ?: continue
                    val text = groups[3]
                    
                    TranscriptEntry(
                        timeMs = minutes * 60000 + seconds * 1000,
                        text = text,
                        originalText = line
                    )
                }
                else -> null
            }
        }
        
        return null
    }
    
    /**
     * 读取文件内容
     */
    private fun readFileContent(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        return BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
    }
    
    /**
     * 根据当前播放位置获取当前应高亮的文稿条目
     */
    fun getCurrentEntry(entries: List<TranscriptEntry>, positionMs: Long): TranscriptEntry? {
        // 找到最接近但不超过当前位置的条目
        return entries
            .filter { it.timeMs <= positionMs }
            .maxByOrNull { it.timeMs }
    }
    
    /**
     * 格式化时间（毫秒转为显示格式）
     */
    fun formatTime(timeMs: Long): String {
        val hours = timeMs / 3600000
        val minutes = (timeMs % 3600000) / 60000
        val seconds = (timeMs % 60000) / 1000
        val millis = timeMs % 1000
        
        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis)
        } else {
            String.format("%02d:%02d.%03d", minutes, seconds, millis)
        }
    }
}

