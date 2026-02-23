package com.videomaster.app.subtitle;

import android.content.Context;
import android.net.Uri;

import com.videomaster.app.interfaces.ISubtitleExporter;
import com.videomaster.app.interfaces.ISubtitleParser;
import com.videomaster.app.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central registry for subtitle parsers and exporters.
 * Add new parsers/exporters by calling registerParser() / registerExporter().
 */
public class SubtitleManager {

    private final List<ISubtitleParser>   parsers   = new ArrayList<>();
    private final List<ISubtitleExporter> exporters = new ArrayList<>();

    private List<SubtitleEntry> currentSubtitles = new ArrayList<>();
    private int lastFoundIndex = 0;

    // Singleton
    private static SubtitleManager instance;
    public static SubtitleManager getInstance() {
        if (instance == null) instance = new SubtitleManager();
        return instance;
    }

    private SubtitleManager() {
        // Register built-in parsers
        parsers.add(new SrtParser());
        parsers.add(new VttParser());
        parsers.add(new AssParser());
        parsers.add(new TxtParser());
        parsers.add(new MarkdownParser());
        // Register built-in exporters
        exporters.add(new SubtitleExporter.SrtExporter());
        exporters.add(new SubtitleExporter.VttExporter());
    }

    /** Registers an external subtitle parser (extensibility point). */
    public void registerParser(ISubtitleParser parser) {
        parsers.add(0, parser);
    }

    /** Registers an external subtitle exporter (extensibility point). */
    public void registerExporter(ISubtitleExporter exporter) {
        exporters.add(exporter);
    }

    /** Returns a copy of all registered parsers. */
    public List<ISubtitleParser> getParsers() { return Collections.unmodifiableList(parsers); }

    /** Returns a copy of all registered exporters. */
    public List<ISubtitleExporter> getExporters() { return Collections.unmodifiableList(exporters); }

    /**
     * Loads subtitles from a content Uri.
     * Auto-detects format from file extension.
     *
     * @param ctx     application context
     * @param uri     subtitle file URI
     * @param charset encoding, e.g. "UTF-8"
     * @return true on success
     */
    public boolean loadSubtitles(Context ctx, Uri uri, String charset) {
        String name = FileUtils.getFileName(ctx, uri);
        String ext  = FileUtils.getExtension(name);
        ISubtitleParser parser = findParserForExtension(ext);
        if (parser == null) return false;
        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            if (in == null) return false;
            currentSubtitles = parser.parse(in, charset);
            lastFoundIndex = 0;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Loads subtitles from a File (e.g. internal storage library).
     *
     * @param file    subtitle file
     * @param charset encoding, e.g. "UTF-8"
     * @return true on success
     */
    public boolean loadSubtitlesFromFile(File file, String charset) {
        String name = file.getName();
        String ext  = FileUtils.getExtension(name);
        ISubtitleParser parser = findParserForExtension(ext);
        if (parser == null) return false;
        try (InputStream in = new FileInputStream(file)) {
            currentSubtitles = parser.parse(in, charset);
            lastFoundIndex = 0;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Exports current subtitles to an output stream.
     *
     * @param formatName  e.g. "SRT", "VTT"
     * @param out         target output stream
     * @param charset     encoding
     * @return true on success
     */
    public boolean exportSubtitles(String formatName, OutputStream out, String charset) {
        ISubtitleExporter exporter = findExporterByName(formatName);
        if (exporter == null || currentSubtitles.isEmpty()) return false;
        try {
            exporter.export(currentSubtitles, out, charset);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Returns the subtitle entry active at the given playback position, or null.
     * Uses sequential search with remembered last index for efficiency.
     */
    public SubtitleEntry getSubtitleAt(long positionMs) {
        if (currentSubtitles.isEmpty()) return null;
        // Search forward from last known position
        for (int i = lastFoundIndex; i < currentSubtitles.size(); i++) {
            SubtitleEntry e = currentSubtitles.get(i);
            if (positionMs < e.getStartTimeMs()) {
                lastFoundIndex = i > 0 ? i - 1 : 0;
                return null;
            }
            if (positionMs >= e.getStartTimeMs() && positionMs < e.getEndTimeMs()) {
                lastFoundIndex = i;
                return e;
            }
        }
        // Seek happened — full scan
        for (int i = 0; i < currentSubtitles.size(); i++) {
            SubtitleEntry e = currentSubtitles.get(i);
            if (positionMs >= e.getStartTimeMs() && positionMs < e.getEndTimeMs()) {
                lastFoundIndex = i;
                return e;
            }
        }
        lastFoundIndex = 0;
        return null;
    }

    public void clearSubtitles() {
        currentSubtitles.clear();
        lastFoundIndex = 0;
    }

    public List<SubtitleEntry> getCurrentSubtitles() {
        return Collections.unmodifiableList(currentSubtitles);
    }

    public boolean hasSubtitles() { return !currentSubtitles.isEmpty(); }

    private ISubtitleParser findParserForExtension(String ext) {
        for (ISubtitleParser p : parsers) {
            if (p.supportsExtension(ext)) return p;
        }
        return null;
    }

    private ISubtitleExporter findExporterByName(String name) {
        for (ISubtitleExporter e : exporters) {
            if (e.getFormatName().equalsIgnoreCase(name)) return e;
        }
        return null;
    }

    /** Resets seek optimisation (call after user seeks). */
    public void onSeek() { lastFoundIndex = 0; }
}
