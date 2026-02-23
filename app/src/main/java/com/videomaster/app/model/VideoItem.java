package com.videomaster.app.model;

import android.net.Uri;

/**
 * Represents a single video file in the library.
 */
public class VideoItem {
    private String title;
    private Uri uri;
    private long durationMs;
    private long fileSizeBytes;
    private String mimeType;
    private long lastModified;

    public VideoItem() {}

    public VideoItem(String title, Uri uri, long durationMs, long fileSizeBytes,
                     String mimeType, long lastModified) {
        this.title = title;
        this.uri = uri;
        this.durationMs = durationMs;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.lastModified = lastModified;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Uri getUri() { return uri; }
    public void setUri(Uri uri) { this.uri = uri; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }
}
