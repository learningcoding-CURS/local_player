package com.videomaster.app.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** File and URI utility helpers. */
public final class FileUtils {
    private FileUtils() {}

    /** Copies an InputStream to a File. */
    public static void copyStreamToFile(InputStream in, File dest) throws IOException {
        try (OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
        }
    }

    /**
     * Returns the display name of a Uri (file name with extension).
     * Falls back to the last path segment if the query fails.
     */
    public static String getFileName(Context ctx, Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor c = ctx.getContentResolver()
                    .query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) return c.getString(idx);
                }
            }
        }
        String path = uri.getLastPathSegment();
        return path != null ? path : "unknown";
    }

    /**
     * Returns the file extension (including dot) from a filename or Uri display name.
     * Returns empty string if no extension found.
     */
    public static String getExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot) : "";
    }

    /**
     * Returns the file size in bytes for a content Uri.
     * Returns -1 if unavailable.
     */
    public static long getFileSize(Context ctx, Uri uri) {
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            try (Cursor c = ctx.getContentResolver()
                    .query(uri, new String[]{OpenableColumns.SIZE}, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    int idx = c.getColumnIndex(OpenableColumns.SIZE);
                    if (idx >= 0) return c.getLong(idx);
                }
            }
        }
        return -1;
    }

    /** Formats a file size (bytes) to a human-readable string. */
    public static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * Returns the MIME type for a file extension.
     * Covers common subtitle and video formats.
     */
    public static String getMimeType(String ext) {
        if (ext == null) return "*/*";
        switch (ext.toLowerCase()) {
            case ".srt": return "application/x-subrip";
            case ".vtt": return "text/vtt";
            case ".ass":
            case ".ssa": return "text/x-ssa";
            case ".mp4": return "video/mp4";
            case ".mkv": return "video/x-matroska";
            case ".avi": return "video/x-msvideo";
            case ".mov": return "video/quicktime";
            case ".webm": return "video/webm";
            default: return "*/*";
        }
    }

    /** Returns the video duration via MediaStore. Returns 0 if unavailable. */
    public static long getVideoDuration(Context ctx, Uri uri) {
        try (Cursor c = ctx.getContentResolver().query(
                uri,
                new String[]{MediaStore.Video.Media.DURATION},
                null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(MediaStore.Video.Media.DURATION);
                if (idx >= 0) return c.getLong(idx);
            }
        }
        return 0;
    }
}
