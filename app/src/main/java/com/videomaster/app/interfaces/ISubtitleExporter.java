package com.videomaster.app.interfaces;

import com.videomaster.app.subtitle.SubtitleEntry;
import java.io.OutputStream;
import java.util.List;

/**
 * Subtitle exporter interface — implement this to add new export format support.
 */
public interface ISubtitleExporter {

    /**
     * Returns the export format name, e.g. "SRT"
     */
    String getFormatName();

    /**
     * Returns the default file extension including dot, e.g. ".srt"
     */
    String getFileExtension();

    /**
     * Export subtitle entries to an output stream.
     *
     * @param entries     ordered list of subtitle entries to export
     * @param outputStream target output stream
     * @param charset     character encoding (e.g. "UTF-8")
     * @throws Exception  on write error
     */
    void export(List<SubtitleEntry> entries, OutputStream outputStream, String charset) throws Exception;
}
