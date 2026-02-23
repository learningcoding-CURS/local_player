package com.videomaster.app.subtitle;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Manages subtitle files stored in the app's internal storage.
 * Library directory: filesDir/subtitle_lib/
 */
public class SubtitleLibraryManager {

    private static final String LIB_DIR_NAME = "subtitle_lib";

    private static SubtitleLibraryManager instance;

    private final File libraryDir;

    public static SubtitleLibraryManager getInstance(Context ctx) {
        if (instance == null) instance = new SubtitleLibraryManager(ctx.getApplicationContext());
        return instance;
    }

    private SubtitleLibraryManager(Context context) {
        libraryDir = new File(context.getFilesDir(), LIB_DIR_NAME);
        if (!libraryDir.exists()) libraryDir.mkdirs();
    }

    /** Returns all subtitle files in the library, sorted by name. */
    public List<SubtitleFileInfo> getAll() {
        List<SubtitleFileInfo> list = new ArrayList<>();
        File[] files = libraryDir.listFiles();
        if (files != null) {
            Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            for (File f : files) {
                if (f.isFile()) {
                    list.add(new SubtitleFileInfo(f.getName(), f.getAbsolutePath()));
                }
            }
        }
        return list;
    }

    /**
     * Copies a subtitle from the given URI into the library.
     *
     * @param context     context for ContentResolver
     * @param uri         source URI
     * @param displayName desired filename (may be altered to avoid collision)
     * @return the saved File, or null on failure
     */
    public File importFromUri(Context context, Uri uri, String displayName) {
        if (displayName == null || displayName.isEmpty()) displayName = "subtitle.srt";
        File dest = uniqueFile(displayName);
        try (InputStream in = context.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dest)) {
            if (in == null) return null;
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            return dest;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Renames a file in the library.
     *
     * @param oldName current filename
     * @param newName desired new filename
     * @return true on success
     */
    public boolean rename(String oldName, String newName) {
        if (newName == null || newName.isEmpty()) return false;
        File old = new File(libraryDir, oldName);
        if (!old.exists()) return false;
        File nw = uniqueFile(newName);
        return old.renameTo(nw);
    }

    /** Deletes a file from the library. */
    public boolean delete(String fileName) {
        return new File(libraryDir, fileName).delete();
    }

    /** Returns the File object for a given library filename. */
    public File getFile(String fileName) {
        return new File(libraryDir, fileName);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    private File uniqueFile(String desired) {
        File f = new File(libraryDir, desired);
        if (!f.exists()) return f;
        String base = desired.contains(".")
                ? desired.substring(0, desired.lastIndexOf('.'))
                : desired;
        String ext = desired.contains(".")
                ? desired.substring(desired.lastIndexOf('.'))
                : "";
        int n = 1;
        while (f.exists()) {
            f = new File(libraryDir, base + "_" + n + ext);
            n++;
        }
        return f;
    }
}
