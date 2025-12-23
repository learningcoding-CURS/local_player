package com.localmedia.player.utils

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * 字幕解析器
 * 支持 SRT 和 WebVTT 格式
 */
class SubtitleParser(private val context: Context) {
    
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
     * 解析字幕文件
     */
    fun parseSubtitle(uri: Uri): List<SubtitleEntry> {
        val content = readFileContent(uri)
        
        return when {
            content.startsWith("WEBVTT") -> parseWebVTT(content)
            else -> parseSRT(content)
        }
    }
    
    /**
     * 读取文件内容
     */
    private fun readFileContent(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        return BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { it.readText() }
    }
    
    /**
     * 解析 SRT 格式
     * 格式示例：
     * 1
     * 00:00:01,000 --> 00:00:03,000
     * 字幕文本
     */
    private fun parseSRT(content: String): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        val lines = content.lines()
        
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            
            // 跳过空行
            if (line.isEmpty()) {
                i++
                continue
            }
            
            // 读取索引
            val index = line.toIntOrNull()
            if (index == null) {
                i++
                continue
            }
            
            i++
            if (i >= lines.size) break
            
            // 读取时间范围
            val timeLine = lines[i].trim()
            val timeMatch = """(\d{2}):(\d{2}):(\d{2}),(\d{3})\s*-->\s*(\d{2}):(\d{2}):(\d{2}),(\d{3})"""
                .toRegex()
                .find(timeLine)
            
            if (timeMatch == null) {
                i++
                continue
            }
            
            val (h1, m1, s1, ms1, h2, m2, s2, ms2) = timeMatch.destructured
            val startTimeMs = parseTime(h1, m1, s1, ms1)
            val endTimeMs = parseTime(h2, m2, s2, ms2)
            
            i++
            
            // 读取字幕文本（可能多行）
            val textLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().isNotEmpty()) {
                textLines.add(lines[i].trim())
                i++
            }
            
            if (textLines.isNotEmpty()) {
                entries.add(
                    SubtitleEntry(
                        index = index,
                        startTimeMs = startTimeMs,
                        endTimeMs = endTimeMs,
                        text = textLines.joinToString("\n")
                    )
                )
            }
            
            i++
        }
        
        return entries
    }
    
    /**
     * 解析 WebVTT 格式
     * 格式示例：
     * WEBVTT
     *
     * 00:00:01.000 --> 00:00:03.000
     * 字幕文本
     */
    private fun parseWebVTT(content: String): List<SubtitleEntry> {
        val entries = mutableListOf<SubtitleEntry>()
        val lines = content.lines()
        
        var i = 0
        var index = 1
        
        // 跳过 WEBVTT 头
        while (i < lines.size && !lines[i].contains("-->")) {
            i++
        }
        
        while (i < lines.size) {
            val line = lines[i].trim()
            
            // 跳过空行
            if (line.isEmpty()) {
                i++
                continue
            }
            
            // 跳过注释和样式
            if (line.startsWith("NOTE") || line.startsWith("STYLE")) {
                i++
                continue
            }
            
            // 读取时间范围
            val timeMatch = """(\d{2}):(\d{2}):(\d{2})\.(\d{3})\s*-->\s*(\d{2}):(\d{2}):(\d{2})\.(\d{3})"""
                .toRegex()
                .find(line)
            
            if (timeMatch == null) {
                i++
                continue
            }
            
            val (h1, m1, s1, ms1, h2, m2, s2, ms2) = timeMatch.destructured
            val startTimeMs = parseTime(h1, m1, s1, ms1)
            val endTimeMs = parseTime(h2, m2, s2, ms2)
            
            i++
            
            // 读取字幕文本
            val textLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().isNotEmpty() && !lines[i].contains("-->")) {
                textLines.add(lines[i].trim())
                i++
            }
            
            if (textLines.isNotEmpty()) {
                entries.add(
                    SubtitleEntry(
                        index = index++,
                        startTimeMs = startTimeMs,
                        endTimeMs = endTimeMs,
                        text = textLines.joinToString("\n")
                    )
                )
            }
        }
        
        return entries
    }
    
    /**
     * 解析时间为毫秒
     */
    private fun parseTime(hours: String, minutes: String, seconds: String, milliseconds: String): Long {
        return hours.toLong() * 3600000 +
                minutes.toLong() * 60000 +
                seconds.toLong() * 1000 +
                milliseconds.toLong()
    }
    
    /**
     * 根据当前播放位置获取应显示的字幕
     */
    fun getCurrentSubtitle(entries: List<SubtitleEntry>, positionMs: Long): SubtitleEntry? {
        return entries.find { entry ->
            positionMs >= entry.startTimeMs && positionMs <= entry.endTimeMs
        }
    }
}

