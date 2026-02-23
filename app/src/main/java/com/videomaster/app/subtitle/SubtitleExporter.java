package com.videomaster.app.subtitle;

import com.videomaster.app.interfaces.ISubtitleExporter;
import com.videomaster.app.util.TimeUtils;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

/** Provides SRT and VTT subtitle exporters. */
public final class SubtitleExporter {
    private SubtitleExporter() {}

    // ──────────────────────────── SRT ────────────────────────────

    public static class SrtExporter implements ISubtitleExporter {
        @Override public String getFormatName()    { return "SRT"; }
        @Override public String getFileExtension() { return ".srt"; }

        @Override
        public void export(List<SubtitleEntry> entries, OutputStream outputStream,
                           String charset) throws Exception {
            PrintWriter writer = new PrintWriter(outputStream, false,
                    java.nio.charset.Charset.forName(charset));
            int seq = 1;
            for (SubtitleEntry e : entries) {
                writer.println(seq++);
                writer.println(TimeUtils.toSrtTimestamp(e.getStartTimeMs())
                        + " --> " + TimeUtils.toSrtTimestamp(e.getEndTimeMs()));
                writer.println(e.getText());
                writer.println();
            }
            writer.flush();
        }
    }

    // ──────────────────────────── VTT ────────────────────────────

    public static class VttExporter implements ISubtitleExporter {
        @Override public String getFormatName()    { return "VTT"; }
        @Override public String getFileExtension() { return ".vtt"; }

        @Override
        public void export(List<SubtitleEntry> entries, OutputStream outputStream,
                           String charset) throws Exception {
            PrintWriter writer = new PrintWriter(outputStream, false,
                    java.nio.charset.Charset.forName(charset));
            writer.println("WEBVTT");
            writer.println();
            int seq = 1;
            for (SubtitleEntry e : entries) {
                writer.println(seq++);
                writer.println(TimeUtils.toVttTimestamp(e.getStartTimeMs())
                        + " --> " + TimeUtils.toVttTimestamp(e.getEndTimeMs()));
                writer.println(e.getText());
                writer.println();
            }
            writer.flush();
        }
    }
}
