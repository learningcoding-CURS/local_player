package com.videomaster.app.util;

import android.content.Context;
import android.net.Uri;

import com.videomaster.app.model.VideoItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads media files bundled inside assets/builtin_media/.
 *
 * Auto-discovery mode (no index.json required):
 *   Just place audio/video files in assets/builtin_media/ and they will
 *   be listed automatically by scanning all files in the directory.
 *   index.json is still supported as an override for custom titles/order.
 *
 * Supported extensions: mp4, mkv, avi, mov, wmv, flv, webm, ts, m4v,
 *                        mp3, aac, flac, wav, ogg, m4a, opus, wma
 */
public class BuiltinMediaProvider {

    private static final String INDEX_PATH = "builtin_media/index.json";
    private static final String ASSET_DIR  = "builtin_media";
    private static final String ASSET_BASE = "file:///android_asset/builtin_media/";

    private static final Set<String> MEDIA_EXTENSIONS = new HashSet<>(Arrays.asList(
            "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "ts", "m4v", "3gp",
            "mp3", "aac", "flac", "wav", "ogg", "m4a", "opus", "wma"
    ));
    private static final Set<String> AUDIO_EXTENSIONS = new HashSet<>(Arrays.asList(
            "mp3", "aac", "flac", "wav", "ogg", "m4a", "opus", "wma"
    ));

    private BuiltinMediaProvider() {}

    /**
     * Returns a list of {@link VideoItem} representing all built-in media.
     * First tries index.json for custom titles; falls back to auto-discovery.
     */
    public static List<VideoItem> getBuiltinMedia(Context ctx) {
        // 1. Try index.json (custom titles/order)
        List<VideoItem> fromIndex = loadFromIndex(ctx);
        if (!fromIndex.isEmpty()) return fromIndex;

        // 2. Auto-discover all media files in assets/builtin_media/
        return autoDiscover(ctx);
    }

    private static List<VideoItem> loadFromIndex(Context ctx) {
        List<VideoItem> result = new ArrayList<>();
        try {
            String json = readAssetText(ctx, INDEX_PATH);
            if (json == null) return result;

            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String filename = obj.optString("filename");
                if (filename.isEmpty()) continue;
                String title    = obj.optString("title", filename);
                long   duration = obj.optLong("durationMs", 0);

                if (!assetExists(ctx, ASSET_DIR + "/" + filename)) continue;

                String ext  = getExtension(filename);
                String mime = AUDIO_EXTENSIONS.contains(ext) ? "audio/" + ext : "video/" + ext;
                Uri uri = Uri.parse(ASSET_BASE + filename);
                result.add(new VideoItem(title, uri, duration, 0, mime, 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static List<VideoItem> autoDiscover(Context ctx) {
        List<VideoItem> result = new ArrayList<>();
        try {
            String[] files = ctx.getAssets().list(ASSET_DIR);
            if (files == null) return result;
            Arrays.sort(files);
            for (String filename : files) {
                String ext = getExtension(filename);
                if (!MEDIA_EXTENSIONS.contains(ext)) continue;

                String mime = AUDIO_EXTENSIONS.contains(ext) ? "audio/" + ext : "video/" + ext;
                Uri    uri  = Uri.parse(ASSET_BASE + filename);
                // Strip extension for display title
                String title = filename.contains(".")
                        ? filename.substring(0, filename.lastIndexOf('.'))
                        : filename;
                result.add(new VideoItem(title, uri, 0, 0, mime, 0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }

    private static String readAssetText(Context ctx, String path) {
        try (InputStream is = ctx.getAssets().open(path);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (IOException e) {
            return null;
        }
    }

    private static boolean assetExists(Context ctx, String path) {
        try (InputStream is = ctx.getAssets().open(path)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
