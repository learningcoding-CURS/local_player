package com.videomaster.app.subtitle;

/** Represents a subtitle file entry in the subtitle library. */
public class SubtitleFileInfo {
    private final String name;
    private final String path;

    public SubtitleFileInfo(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
}
