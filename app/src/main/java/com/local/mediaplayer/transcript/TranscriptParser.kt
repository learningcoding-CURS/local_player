package com.local.mediaplayer.transcript

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

/**
 * 稿件时间点条目
 */
data class TranscriptEntry(
    val timeMs: Long,
    val text: String,
    val lineNumber: Int
)

/**
 * 稿件/文稿解析器
 * 解析包含时间点标记的文本文件
 * 
 * 支持格式：
 * [00:01:23.500] 这里是文本内容
 * [00:01:30] 这里是另一段文本
 */
class TranscriptParser {
    
    // 时间戳正则表达式
    // 匹配: [00:01:23.500] 或 [00:01:23] 或 [01:23]
    private val timestampRegex = Regex("""^\[(\d{1,2}):(\d{2})(?::(\d{2}))?(?:[.,](\d{1,3}))?\]\s*(.*)$""")
    
    /**
     * 解析稿件文件
     */
    fun parse(inputStream: InputStream): List<TranscriptEntry> {
        val entries = mutableListOf<TranscriptEntry>()
        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        
        var lineNumber = 0
        reader.useLines { lines ->
            lines.forEach { line ->
                lineNumber++
                val trimmed = line.trim()
                
                if (trimmed.isEmpty()) return@forEach
                
                // 匹配时间戳行
                val match = timestampRegex.find(trimmed)
                if (match != null) {
                    val timeMs = parseTimestamp(match)
                    val text = match.groupValues[5].trim()
                    
                    entries.add(
                        TranscriptEntry(
                            timeMs = timeMs,
                            text = text,
                            lineNumber = lineNumber
                        )
                    )
                }
            }
        }
        
        return entries.sortedBy { it.timeMs }
    }
    
    /**
     * 从匹配结果中解析时间戳
     */
    private fun parseTimestamp(match: MatchResult): Long {
        val part1 = match.groupValues[1].toLongOrNull() ?: 0
        val part2 = match.groupValues[2].toLongOrNull() ?: 0
        val part3 = match.groupValues[3].takeIf { it.isNotEmpty() }?.toLongOrNull()
        val millis = match.groupValues[4].takeIf { it.isNotEmpty() }?.toLongOrNull() ?: 0
        
        return if (part3 != null) {
            // 格式: [HH:MM:SS.mmm] 或 [HH:MM:SS]
            part1 * 3600000 + part2 * 60000 + part3 * 1000 + millis
        } else {
            // 格式: [MM:SS.mmm] 或 [MM:SS]
            part1 * 60000 + part2 * 1000 + millis
        }
    }
    
    /**
     * 根据当前播放时间获取当前应该高亮的条目
     */
    fun getCurrentEntry(entries: List<TranscriptEntry>, currentTimeMs: Long): TranscriptEntry? {
        if (entries.isEmpty()) return null
        
        // 找到最后一个小于等于当前时间的条目
        return entries.lastOrNull { it.timeMs <= currentTimeMs }
    }
    
    /**
     * 根据当前播放时间获取当前条目的索引
     */
    fun getCurrentIndex(entries: List<TranscriptEntry>, currentTimeMs: Long): Int {
        if (entries.isEmpty()) return -1
        
        return entries.indexOfLast { it.timeMs <= currentTimeMs }
    }
}

/**
 * 稿件管理器
 * 管理稿件的加载和时间点跳转
 */
class TranscriptManager {
    
    private val parser = TranscriptParser()
    private var entries: List<TranscriptEntry> = emptyList()
    
    /**
     * 加载稿件
     */
    fun load(inputStream: InputStream) {
        entries = parser.parse(inputStream)
    }
    
    /**
     * 获取所有条目
     */
    fun getEntries(): List<TranscriptEntry> = entries
    
    /**
     * 根据索引获取条目
     */
    fun getEntry(index: Int): TranscriptEntry? {
        return entries.getOrNull(index)
    }
    
    /**
     * 获取当前高亮的条目索引
     */
    fun getCurrentIndex(currentTimeMs: Long): Int {
        return parser.getCurrentIndex(entries, currentTimeMs)
    }
    
    /**
     * 获取当前高亮的条目
     */
    fun getCurrentEntry(currentTimeMs: Long): TranscriptEntry? {
        return parser.getCurrentEntry(entries, currentTimeMs)
    }
    
    /**
     * 清除稿件
     */
    fun clear() {
        entries = emptyList()
    }
}

