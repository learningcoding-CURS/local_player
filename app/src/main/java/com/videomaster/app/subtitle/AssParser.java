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
 * Parser for ASS/SSA (Advanced SubStation Alpha) subtitle files.
 * Extracts dialogue lines and strips ASS override tags.
 */
public class AssParser implements ISubtitleParser {

    @Override
    public String getFormatName() { return "ASS/SSA"; }

    @Override
    public String[] getSupportedExtensions() { return new String[]{".ass", ".ssa"}; }

    @Override
    public List<SubtitleEntry> parse(InputStream inputStream, String charset) throws Exception {
        List<SubtitleEntry> entries = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        String line;
        boolean inEvents = false;
        int startCol = -1, endCol = -1, textCol = -1;
        int index = 0;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("\uFEFF")) line = line.substring(1);
            String trimmed = line.trim();

            if (trimmed.equalsIgnoreCase("[Events]")) {
                inEvents = true;
                startCol = endCol = textCol = -1;
                continue;
            }
            if (trimmed.startsWith("[") && !trimmed.equalsIgnoreCase("[Events]")) {
                inEvents = false;
            }
            if (!inEvents) continue;

            if (trimmed.startsWith("Format:")) {
                String[] cols = trimmed.substring(7).split(",");
                for (int i = 0; i < cols.length; i++) {
                    String col = cols[i].trim();
                    if (col.equalsIgnoreCase("Start")) startCol = i;
                    else if (col.equalsIgnoreCase("End")) endCol = i;
                    else if (col.equalsIgnoreCase("Text")) textCol = i;
                }
            } else if (trimmed.startsWith("Dialogue:")) {
                if (startCol < 0 || endCol < 0 || textCol < 0) continue;
                // Split only up to textCol+1 parts so the text isn't split on commas
                String content = trimmed.substring(9); // remove "Dialogue:"
                String[] parts = content.split(",", textCol + 1);
                if (parts.length <= textCol) continue;
                long startMs = parseAssTime(parts[startCol].trim());
                long endMs   = parseAssTime(parts[endCol].trim());
                String text  = stripAssTags(parts[textCol]);
                if (!text.isEmpty()) {
                    entries.add(new SubtitleEntry(++index, startMs, endMs, text));
                }
            }
        }

        Collections.sort(entries);
        return entries;
    }

    /** Parses ASS time format H:MM:SS.cc to milliseconds. */
    private long parseAssTime(String t) {
        try {
            String[] parts = t.split(":");
            long h = Long.parseLong(parts[0]);
            long m = Long.parseLong(parts[1]);
            String[] sc = parts[2].split("\\.");
            long s  = Long.parseLong(sc[0]);
            // centiseconds
            long cs = sc.length > 1 ? Long.parseLong(sc[1]) : 0;
            return (h * 3600 + m * 60 + s) * 1000 + cs * 10;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Removes ASS override tags like {\an8}, {\pos(x,y)}, {\b1}, etc. */
    private String stripAssTags(String text) {
        if (text == null) return "";
        // Remove override blocks {}
        text = text.replaceAll("\\{[^}]*\\}", "");
        // Replace soft line breaks \N and \n
        text = text.replace("\\N", "\n").replace("\\n", "\n").replace("\\h", " ");
        return text.trim();
    }
}
