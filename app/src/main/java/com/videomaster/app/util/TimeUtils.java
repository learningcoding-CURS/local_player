package com.videomaster.app.util;

import java.util.Locale;

/** Utility methods for time formatting. */
public final class TimeUtils {
    private TimeUtils() {}

    /**
     * Formats milliseconds as HH:MM:SS or MM:SS when under an hour.
     */
    public static String formatDuration(long ms) {
        if (ms < 0) ms = 0;
        long totalSeconds = ms / 1000;
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        if (hours > 0) {
            return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format(Locale.US, "%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Formats milliseconds in SRT timestamp format: HH:MM:SS,mmm
     */
    public static String toSrtTimestamp(long ms) {
        if (ms < 0) ms = 0;
        long millis  = ms % 1000;
        long totalSec = ms / 1000;
        long hours   = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;
        long seconds = totalSec % 60;
        return String.format(Locale.US, "%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
    }

    /**
     * Formats milliseconds in VTT timestamp format: HH:MM:SS.mmm
     */
    public static String toVttTimestamp(long ms) {
        if (ms < 0) ms = 0;
        long millis   = ms % 1000;
        long totalSec = ms / 1000;
        long hours    = totalSec / 3600;
        long minutes  = (totalSec % 3600) / 60;
        long seconds  = totalSec % 60;
        return String.format(Locale.US, "%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }

    /**
     * Parses an SRT/VTT timestamp string to milliseconds.
     * Accepts: HH:MM:SS,mmm  HH:MM:SS.mmm  MM:SS,mmm  MM:SS.mmm
     */
    public static long parseTimestamp(String ts) {
        if (ts == null) return 0;
        ts = ts.trim().replace(',', '.');
        String[] parts = ts.split(":");
        try {
            if (parts.length == 3) {
                long h = Long.parseLong(parts[0].trim());
                long m = Long.parseLong(parts[1].trim());
                String[] secMs = parts[2].trim().split("\\.");
                long s = Long.parseLong(secMs[0]);
                long ms = secMs.length > 1 ? Long.parseLong(String.format("%-3s", secMs[1]).replace(' ', '0').substring(0, 3)) : 0;
                return ((h * 3600 + m * 60 + s) * 1000) + ms;
            } else if (parts.length == 2) {
                long m = Long.parseLong(parts[0].trim());
                String[] secMs = parts[1].trim().split("\\.");
                long s = Long.parseLong(secMs[0]);
                long ms = secMs.length > 1 ? Long.parseLong(String.format("%-3s", secMs[1]).replace(' ', '0').substring(0, 3)) : 0;
                return ((m * 60 + s) * 1000) + ms;
            }
        } catch (NumberFormatException ignored) {}
        return 0;
    }
}
