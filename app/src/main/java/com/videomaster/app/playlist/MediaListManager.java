package com.videomaster.app.playlist;

import android.content.Context;

import com.videomaster.app.model.MediaList;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages custom media playlists and per-file playback progress.
 * Data is persisted as JSON in the app's private files directory.
 *
 * Thread safety: call from main thread only (or synchronize externally).
 */
public class MediaListManager {

    private static MediaListManager instance;

    private final File             dataFile;
    private final List<MediaList>  lists    = new ArrayList<>();
    /** uri → [positionMs, durationMs] */
    private final Map<String, long[]> progress = new LinkedHashMap<>();

    private MediaListManager(Context ctx) {
        dataFile = new File(ctx.getFilesDir(), "playlists.json");
        load();
    }

    public static synchronized MediaListManager getInstance(Context ctx) {
        if (instance == null) instance = new MediaListManager(ctx.getApplicationContext());
        return instance;
    }

    // ── CRUD ───────────────────────────────────────────────────────────────

    /** Returns an unmodifiable view of all playlists. */
    public List<MediaList> getLists() { return lists; }

    public MediaList getList(String id) {
        for (MediaList l : lists) if (l.getId().equals(id)) return l;
        return null;
    }

    public MediaList createList(String name, String category) {
        MediaList list = new MediaList(name, category != null ? category : "");
        lists.add(list);
        save();
        return list;
    }

    public void deleteList(String id) {
        lists.removeIf(l -> l.getId().equals(id));
        save();
    }

    public void renameList(String id, String newName, String newCategory) {
        MediaList l = getList(id);
        if (l != null) {
            l.setName(newName);
            l.setCategory(newCategory);
            save();
        }
    }

    public void addItemToList(String listId, String uri) {
        MediaList l = getList(listId);
        if (l != null) {
            l.addItem(uri);
            save();
        }
    }

    public void removeItemFromList(String listId, String uri) {
        MediaList l = getList(listId);
        if (l != null) {
            l.removeItem(uri);
            save();
        }
    }

    // ── Progress tracking ──────────────────────────────────────────────────

    public void saveProgress(String uri, long positionMs, long durationMs) {
        progress.put(uri, new long[]{positionMs, durationMs});
        save();
    }

    /** Returns [positionMs, durationMs], or null if not found. */
    public long[] getProgress(String uri) {
        return progress.get(uri);
    }

    /** Returns progress percentage (0–100), or 0 if unknown. */
    public int getProgressPercent(String uri) {
        long[] p = progress.get(uri);
        if (p == null || p[1] <= 0) return 0;
        return (int) (p[0] * 100L / p[1]);
    }

    // ── Persistence ────────────────────────────────────────────────────────

    private void save() {
        try {
            JSONObject root = new JSONObject();

            JSONArray listsArr = new JSONArray();
            for (MediaList ml : lists) {
                JSONObject o = new JSONObject();
                o.put("id",        ml.getId());
                o.put("name",      ml.getName());
                o.put("category",  ml.getCategory());
                o.put("createdAt", ml.getCreatedAt());
                JSONArray items = new JSONArray();
                for (String u : ml.getItemUris()) items.put(u);
                o.put("items", items);
                listsArr.put(o);
            }
            root.put("lists", listsArr);

            JSONObject prog = new JSONObject();
            for (Map.Entry<String, long[]> e : progress.entrySet()) {
                JSONArray arr = new JSONArray();
                arr.put(e.getValue()[0]);
                arr.put(e.getValue()[1]);
                prog.put(e.getKey(), arr);
            }
            root.put("progress", prog);

            try (FileWriter w = new FileWriter(dataFile)) {
                w.write(root.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!dataFile.exists()) return;
        try (FileReader r = new FileReader(dataFile)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[4096];
            int n;
            while ((n = r.read(buf)) != -1) sb.append(buf, 0, n);

            JSONObject root = new JSONObject(sb.toString());

            JSONArray listsArr = root.optJSONArray("lists");
            if (listsArr != null) {
                for (int i = 0; i < listsArr.length(); i++) {
                    JSONObject o  = listsArr.getJSONObject(i);
                    MediaList ml  = new MediaList();
                    ml.setId(o.optString("id"));
                    ml.setName(o.optString("name"));
                    ml.setCategory(o.optString("category"));
                    ml.setCreatedAt(o.optLong("createdAt"));
                    JSONArray items = o.optJSONArray("items");
                    if (items != null) {
                        for (int j = 0; j < items.length(); j++) ml.addItem(items.getString(j));
                    }
                    lists.add(ml);
                }
            }

            JSONObject prog = root.optJSONObject("progress");
            if (prog != null) {
                JSONArray keys = prog.names();
                if (keys != null) {
                    for (int i = 0; i < keys.length(); i++) {
                        String key  = keys.getString(i);
                        JSONArray v = prog.getJSONArray(key);
                        progress.put(key, new long[]{v.getLong(0), v.getLong(1)});
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
