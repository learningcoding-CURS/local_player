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
import java.util.List;

/**
 * Loads the list of media files bundled inside assets/builtin_media/.
 * Developers can place audio/video files in that folder and register them
 * in index.json.  At runtime the files are referenced via
 * "file:///android_asset/builtin_media/<filename>" URIs, which ExoPlayer
 * resolves through its default DataSource.Factory.
 */
public class BuiltinMediaProvider {

    private static final String INDEX_PATH = "builtin_media/index.json";
    private static final String ASSET_BASE = "file:///android_asset/builtin_media/";

    private BuiltinMediaProvider() {}

    /**
     * Returns a list of {@link VideoItem} objects representing all built-in media.
     * Items whose asset file does not exist in the APK are silently skipped.
     */
    public static List<VideoItem> getBuiltinMedia(Context ctx) {
        List<VideoItem> result = new ArrayList<>();
        try {
            String json = readAssetText(ctx, INDEX_PATH);
            if (json == null) return result;

            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String filename = obj.optString("filename");
                String title    = obj.optString("title", filename);
                long   duration = obj.optLong("durationMs", 0);

                // Skip if file doesn't exist in assets
                if (!assetExists(ctx, "builtin_media/" + filename)) continue;

                Uri uri = Uri.parse(ASSET_BASE + filename);
                result.add(new VideoItem(title, uri, duration, 0, "builtin", 0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
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
