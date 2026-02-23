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
 * Parser for WebVTT (.vtt) subtitle files.
 */
public class VttParser implements ISubtitleParser {

    @Override
    public String getFormatName() { return "VTT"; }

    @Override
    public String[] getSupportedExtensions() { return new String[]{".vtt"}; }

    @Override
    public List<SubtitleEntry> parse(InputStream inputStream, String charset) throws Exception {
        List<SubtitleEntry> entries = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        String line;
        boolean headerPassed = false;
        int index = 0;
        long startMs = 0, endMs = 0;
        StringBuilder textBuf = new StringBuilder();
        boolean inCue = false;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("\uFEFF")) line = line.substring(1);
            line = line.trim();

            if (!headerPassed) {
                if (line.startsWith("WEBVTT")) { headerPassed = true; }
                continue;
            }

            if (line.contains("-->")) {
                // Save previous cue if any
                if (inCue && textBuf.length() > 0) {
                    entries.add(new SubtitleEntry(index, startMs, endMs,
                            textBuf.toString().trim()));
                    textBuf.setLength(0);
                }
                // Parse new cue timestamp (strip positioning hints)
                String[] parts = line.split("-->");
                startMs = TimeUtils.parseTimestamp(parts[0].trim());
                String endPart = parts[1].trim().split("\\s+")[0];
                endMs = TimeUtils.parseTimestamp(endPart);
                index++;
                inCue = true;
            } else if (inCue) {
                if (line.isEmpty()) {
                    if (textBuf.length() > 0) {
                        entries.add(new SubtitleEntry(index, startMs, endMs,
                                textBuf.toString().trim()));
                        textBuf.setLength(0);
                    }
                    inCue = false;
                } else {
                    if (textBuf.length() > 0) textBuf.append('\n');
                    textBuf.append(line);
                }
            }
        }
        // Flush last cue
        if (inCue && textBuf.length() > 0) {
            entries.add(new SubtitleEntry(index, startMs, endMs, textBuf.toString().trim()));
        }

        Collections.sort(entries);
        return entries;
    }
}
