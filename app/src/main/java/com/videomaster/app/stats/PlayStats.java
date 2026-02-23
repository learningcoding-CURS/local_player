package com.videomaster.app.stats;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tracks cumulative playback time per playlist (MediaList).
 * Data is persisted in a dedicated SharedPreferences file.
 *
 * Key scheme:
 *   "time_<listId>"  → long   total milliseconds played
 *   "name_<listId>"  → String display name of the list (kept current)
 *
 * Special key "__single__" is used for files opened without a playlist context.
 */
public class PlayStats {

    public static final String SINGLE_FILE_ID = "__single__";

    private static final String PREFS_NAME  = "play_stats";
    private static final String PREFIX_TIME = "time_";
    private static final String PREFIX_NAME = "name_";

    private static PlayStats instance;

    private final SharedPreferences prefs;

    private PlayStats(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized PlayStats getInstance(Context ctx) {
        if (instance == null) {
            instance = new PlayStats(ctx.getApplicationContext());
        }
        return instance;
    }

    /**
     * Adds {@code ms} milliseconds of playback time to the given list.
     *
     * @param listId   MediaList ID (or {@link #SINGLE_FILE_ID} for stand-alone files)
     * @param listName Human-readable display name for the list
     * @param ms       Milliseconds to add (ignored if &le; 0)
     */
    public void addTime(String listId, String listName, long ms) {
        if (ms <= 0 || listId == null) return;
        long current = prefs.getLong(PREFIX_TIME + listId, 0);
        prefs.edit()
                .putLong(PREFIX_TIME + listId, current + ms)
                .putString(PREFIX_NAME + listId, listName != null ? listName : listId)
                .apply();
    }

    /**
     * Returns all entries sorted by total play time descending.
     */
    public List<StatEntry> getStats() {
        List<StatEntry> list = new ArrayList<>();
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(PREFIX_TIME)) {
                String id = key.substring(PREFIX_TIME.length());
                long totalMs = ((Number) entry.getValue()).longValue();
                String name = prefs.getString(PREFIX_NAME + id, id);
                list.add(new StatEntry(id, name, totalMs));
            }
        }
        list.sort((a, b) -> Long.compare(b.totalMs, a.totalMs));
        return list;
    }

    /** Returns the total recorded time across all lists, in milliseconds. */
    public long getTotalMs() {
        long total = 0;
        for (StatEntry e : getStats()) total += e.totalMs;
        return total;
    }

    /** Removes all recorded stats. */
    public void clearAll() {
        prefs.edit().clear().apply();
    }

    // ── Export / Import ──────────────────────────────────────────────────────

    /**
     * Serialises all stats entries to a JSON string.
     * Format: [ { "id":"...", "name":"...", "totalMs":12345 }, ... ]
     */
    public String exportToJson() {
        try {
            JSONArray arr = new JSONArray();
            for (StatEntry e : getStats()) {
                JSONObject obj = new JSONObject();
                obj.put("id", e.id);
                obj.put("name", e.name);
                obj.put("totalMs", e.totalMs);
                arr.put(obj);
            }
            return arr.toString(2);
        } catch (Exception e) {
            return "[]";
        }
    }

    /**
     * Imports stats from a JSON string. Existing entries with the same ID are
     * merged (times added together); new entries are created.
     *
     * @return number of entries imported / merged
     */
    public int importFromJson(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            int count = 0;
            SharedPreferences.Editor editor = prefs.edit();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String id   = obj.getString("id");
                String name = obj.optString("name", id);
                long   ms   = obj.optLong("totalMs", 0);
                if (ms <= 0 || id == null) continue;
                long existing = prefs.getLong(PREFIX_TIME + id, 0);
                editor.putLong(PREFIX_TIME + id, existing + ms);
                editor.putString(PREFIX_NAME + id, name);
                count++;
            }
            editor.apply();
            return count;
        } catch (Exception e) {
            return -1;
        }
    }

    // ── Data holder ──────────────────────────────────────────────────────────

    public static class StatEntry {
        public final String id;
        public final String name;
        public final long totalMs;

        StatEntry(String id, String name, long totalMs) {
            this.id = id;
            this.name = name;
            this.totalMs = totalMs;
        }
    }
}
