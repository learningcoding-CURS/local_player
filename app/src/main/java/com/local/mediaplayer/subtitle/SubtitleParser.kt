package com.local.mediaplayer.subtitle

import android.net.Uri
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * 字幕条目
 */
data class SubtitleEntry(
    val index: Int,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val text: String
)

/**
 * 字幕解析器
 * 支持 SRT 和 WebVTT 格式
 */
class SubtitleParser {
    
    /**
     * 解析 SRT 格式字幕
     * 格式示例：
     * 1
     * 00:00:01,500 --> 00:00:04,000
     * 字幕文本
     */
    fun parseSrt(inputStream: InputStream): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        
        var index = 0
        var timeRange = ""
        val textLines = mutableListOf<String>()
        
        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                
                when {
                    // 序号行
                    trimmed.matches(Regex("\\d+")) -> {
                        index = trimmed.toIntOrNull() ?: 0
                    }
                    // 时间戳行
                    trimmed.contains("-->") -> {
                        timeRange = trimmed
                    }
                    // 空行，表示一条字幕结束
                    trimmed.isEmpty() -> {
                        if (index > 0 && timeRange.isNotEmpty() && textLines.isNotEmpty()) {
                            val (start, end) = parseTimeRange(timeRange)
                            entries.add(
                                SubtitleEntry(
                                    index = index,
                                    startTimeMs = start,
                                    endTimeMs = end,
                                    text = textLines.joinToString("\n")
                                )
                            )
                        }
                        // 重置
                        index = 0
                        timeRange = ""
                        textLines.clear()
                    }
                    // 文本行
                    else -> {
                        textLines.add(trimmed)
                    }
                }
            }
        }
        
        // 处理最后一条字幕（如果文件末尾没有空行）
        if (index > 0 && timeRange.isNotEmpty() && textLines.isNotEmpty()) {
            val (start, end) = parseTimeRange(timeRange)
            entries.add(
                SubtitleEntry(
                    index = index,
                    startTimeMs = start,
                    endTimeMs = end,
                    text = textLines.joinToString("\n")
                )
            )
        }
        
        return entries
    }
    
    /**
     * 解析 WebVTT 格式字幕
     * 格式示例：
     * WEBVTT
     * 
     * 00:00:01.500 --> 00:00:04.000
     * 字幕文本
     */
    fun parseWebVtt(inputStream: InputStream): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        
        var index = 0
        var timeRange = ""
        val textLines = mutableListOf<String>()
        var isHeader = true
        
        reader.useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                
                // 跳过 WEBVTT 头部
                if (isHeader) {
                    if (trimmed.startsWith("WEBVTT")) {
                        isHeader = false
                    }
                    return@forEach
                }
                
                when {
                    // 时间戳行
                    trimmed.contains("-->") -> {
                        timeRange = trimmed
                        index++
                    }
                    // 空行，表示一条字幕结束
                    trimmed.isEmpty() -> {
                        if (timeRange.isNotEmpty() && textLines.isNotEmpty()) {
                            val (start, end) = parseTimeRange(timeRange, isWebVtt = true)
                            entries.add(
                                SubtitleEntry(
                                    index = index,
                                    startTimeMs = start,
                                    endTimeMs = end,
                                    text = textLines.joinToString("\n")
                                )
                            )
                        }
                        // 重置
                        timeRange = ""
                        textLines.clear()
                    }
                    // 跳过提示行（如 NOTE）
                    trimmed.startsWith("NOTE") -> {}
                    // 文本行
                    else -> {
                        textLines.add(trimmed)
                    }
                }
            }
        }
        
        // 处理最后一条字幕
        if (timeRange.isNotEmpty() && textLines.isNotEmpty()) {
            val (start, end) = parseTimeRange(timeRange, isWebVtt = true)
            entries.add(
                SubtitleEntry(
                    index = index,
                    startTimeMs = start,
                    endTimeMs = end,
                    text = textLines.joinToString("\n")
                )
            )
        }
        
        return entries
    }
    
    /**
     * 解析时间范围
     * SRT: 00:00:01,500 --> 00:00:04,000
     * WebVTT: 00:00:01.500 --> 00:00:04.000
     */
    private fun parseTimeRange(timeRange: String, isWebVtt: Boolean = false): Pair<Long, Long> {
        val parts = timeRange.split("-->").map { it.trim() }
        if (parts.size != 2) return 0L to 0L
        
        val startMs = parseTimestamp(parts[0], isWebVtt)
        val endMs = parseTimestamp(parts[1], isWebVtt)
        
        return startMs to endMs
    }
    
    /**
     * 解析时间戳为毫秒
     * SRT: 00:01:23,500 (小时:分钟:秒,毫秒)
     * WebVTT: 00:01:23.500 (小时:分钟:秒.毫秒)
     */
    private fun parseTimestamp(timestamp: String, isWebVtt: Boolean): Long {
        val separator = if (isWebVtt) '.' else ','
        val parts = timestamp.split(':', separator)
        
        return when (parts.size) {
            4 -> {
                // 00:01:23,500
                val hours = parts[0].toLongOrNull() ?: 0
                val minutes = parts[1].toLongOrNull() ?: 0
                val seconds = parts[2].toLongOrNull() ?: 0
                val millis = parts[3].toLongOrNull() ?: 0
                hours * 3600000 + minutes * 60000 + seconds * 1000 + millis
            }
            3 -> {
                // 01:23,500 (无小时)
                val minutes = parts[0].toLongOrNull() ?: 0
                val seconds = parts[1].toLongOrNull() ?: 0
                val millis = parts[2].toLongOrNull() ?: 0
                minutes * 60000 + seconds * 1000 + millis
            }
            else -> 0L
        }
    }
    
    /**
     * 根据当前时间获取应该显示的字幕
     */
    fun getCurrentSubtitle(entries: List<SubtitleEntry>, currentTimeMs: Long): SubtitleEntry? {
        return entries.firstOrNull { 
            currentTimeMs >= it.startTimeMs && currentTimeMs <= it.endTimeMs 
        }
    }
}

