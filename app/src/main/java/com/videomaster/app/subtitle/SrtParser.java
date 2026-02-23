package com.videomaster.app.subtitle;

import com.videomaster.app.interfaces.ISubtitleParser;
import com.videomaster.app.util.TimeUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parser for SRT (SubRip Text) subtitle files.
 * Handles UTF-8 with or without BOM, and common Windows line endings.
 */
public class SrtParser implements ISubtitleParser {

    @Override
    public String getFormatName() { return "SRT"; }

    @Override
    public String[] getSupportedExtensions() { return new String[]{".srt"}; }

    @Override
    public List<SubtitleEntry> parse(InputStream inputStream, String charset) throws Exception {
        List<SubtitleEntry> entries = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        String line;
        int state = 0; // 0=seek index, 1=seek timestamp, 2=read text
        int index = 0;
        long startMs = 0, endMs = 0;
        StringBuilder textBuf = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            // Strip BOM from first line
            if (line.startsWith("\uFEFF")) line = line.substring(1);
            line = line.trim();

            switch (state) {
                case 0: // expecting sequence number
                    if (!line.isEmpty() && line.matches("\\d+")) {
                        try { index = Integer.parseInt(line); } catch (NumberFormatException e) { index = entries.size() + 1; }
                        state = 1;
                    }
                    break;

                case 1: // expecting timestamp line
                    if (line.contains("-->")) {
                        String[] parts = line.split("-->");
                        if (parts.length >= 2) {
                            startMs = TimeUtils.parseTimestamp(parts[0].trim());
                            // VTT may include position hints after the end time
                            String endPart = parts[1].trim().split("\\s+")[0];
                            endMs = TimeUtils.parseTimestamp(endPart);
                        }
                        textBuf.setLength(0);
                        state = 2;
                    }
                    break;

                case 2: // reading text lines
                    if (line.isEmpty()) {
                        if (textBuf.length() > 0) {
                            entries.add(new SubtitleEntry(index, startMs, endMs,
                                    textBuf.toString().trim()));
                            textBuf.setLength(0);
                        }
                        state = 0;
                    } else {
                        if (textBuf.length() > 0) textBuf.append('\n');
                        textBuf.append(line);
                    }
                    break;
            }
        }
        // Flush last entry (file may not end with blank line)
        if (state == 2 && textBuf.length() > 0) {
            entries.add(new SubtitleEntry(index, startMs, endMs, textBuf.toString().trim()));
        }

        Collections.sort(entries);
        return entries;
    }
}
