package com.videomaster.app.subtitle;

import com.videomaster.app.interfaces.ISubtitleParser;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses plain-text (.txt) files as subtitles.
 *
 * Strategy:
 *   - Blank-line-separated paragraphs → each paragraph = one subtitle entry.
 *   - If the file has no blank lines, each non-empty line = one entry.
 *   - Timing is auto-generated based on character count (reading-speed model):
 *       durationMs = max(2000, chars * 60_000 / CHARS_PER_MINUTE)
 *       capped at 8000 ms per entry.
 *   - A 300 ms gap is inserted between consecutive entries.
 */
public class TxtParser implements ISubtitleParser {

    /** Assumed reading speed in characters per minute. */
    private static final int  CHARS_PER_MINUTE = 300;
    private static final long MIN_DURATION_MS  = 2_000;
    private static final long MAX_DURATION_MS  = 8_000;
    private static final long GAP_MS           = 300;

    @Override
    public String getFormatName() { return "TXT"; }

    @Override
    public String[] getSupportedExtensions() { return new String[]{".txt"}; }

    @Override
    public List<SubtitleEntry> parse(InputStream inputStream, String charset) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));

        List<String> paragraphs = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        String line;
        boolean hadBlankLine = false;

        while ((line = reader.readLine()) != null) {
            // Strip BOM
            if (buf.length() == 0 && line.startsWith("\uFEFF")) line = line.substring(1);
            if (line.trim().isEmpty()) {
                if (buf.length() > 0) {
                    paragraphs.add(buf.toString().trim());
                    buf.setLength(0);
                    hadBlankLine = true;
                }
            } else {
                if (hadBlankLine || buf.length() == 0) {
                    buf.append(line.trim());
                } else {
                    buf.append(' ').append(line.trim());
                }
            }
        }
        if (buf.length() > 0) paragraphs.add(buf.toString().trim());

        // If no blank lines found, re-split by individual lines
        if (!hadBlankLine && paragraphs.size() == 1) {
            String full = paragraphs.get(0);
            paragraphs.clear();
            for (String s : full.split("\\s{2,}|(?<=。)|(?<=！)|(?<=？)|(?<=\\.)")) {
                String t = s.trim();
                if (!t.isEmpty()) paragraphs.add(t);
            }
            if (paragraphs.isEmpty()) paragraphs.add(full);
        }

        return buildEntries(paragraphs);
    }

    private List<SubtitleEntry> buildEntries(List<String> paragraphs) {
        List<SubtitleEntry> entries = new ArrayList<>();
        long cursor = 0;
        for (int i = 0; i < paragraphs.size(); i++) {
            String text = paragraphs.get(i);
            long dur = durationFor(text);
            entries.add(new SubtitleEntry(i + 1, cursor, cursor + dur, text));
            cursor += dur + GAP_MS;
        }
        return entries;
    }

    private long durationFor(String text) {
        long d = (long) text.length() * 60_000L / CHARS_PER_MINUTE;
        return Math.max(MIN_DURATION_MS, Math.min(MAX_DURATION_MS, d));
    }
}
