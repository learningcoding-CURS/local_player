package com.videomaster.app.subtitle;

/**
 * Represents a single subtitle cue.
 */
public class SubtitleEntry implements Comparable<SubtitleEntry> {
    private long startTimeMs;
    private long endTimeMs;
    private String text;
    private int index;

    public SubtitleEntry() {}

    public SubtitleEntry(int index, long startTimeMs, long endTimeMs, String text) {
        this.index = index;
        this.startTimeMs = startTimeMs;
        this.endTimeMs = endTimeMs;
        this.text = text;
    }

    public long getStartTimeMs() { return startTimeMs; }
    public void setStartTimeMs(long startTimeMs) { this.startTimeMs = startTimeMs; }

    public long getEndTimeMs() { return endTimeMs; }
    public void setEndTimeMs(long endTimeMs) { this.endTimeMs = endTimeMs; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }

    /** Returns the plain text without any markup tags. */
    public String getPlainText() {
        if (text == null) return "";
        return text.replaceAll("<[^>]+>", "").trim();
    }

    @Override
    public int compareTo(SubtitleEntry other) {
        return Long.compare(this.startTimeMs, other.startTimeMs);
    }
}
