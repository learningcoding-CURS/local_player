package com.videomaster.app.interfaces;

import com.videomaster.app.subtitle.SubtitleEntry;
import java.io.InputStream;
import java.util.List;

/**
 * Subtitle parser interface — implement this to add new subtitle format support.
 */
public interface ISubtitleParser {

    /**
     * Returns the format name, e.g. "SRT", "ASS", "VTT"
     */
    String getFormatName();

    /**
     * Returns supported file extensions, e.g. {".srt"}
     */
    String[] getSupportedExtensions();

    /**
     * Parse subtitle from input stream.
     *
     * @param inputStream  raw byte stream of the subtitle file
     * @param charset      character encoding (e.g. "UTF-8", "GBK")
     * @return             ordered list of subtitle entries
     * @throws Exception   on parse error
     */
    List<SubtitleEntry> parse(InputStream inputStream, String charset) throws Exception;

    /**
     * Quick check whether a file extension is supported by this parser.
     */
    default boolean supportsExtension(String ext) {
        if (ext == null) return false;
        String lower = ext.toLowerCase();
        for (String s : getSupportedExtensions()) {
            if (s.equalsIgnoreCase(lower)) return true;
        }
        return false;
    }
}
