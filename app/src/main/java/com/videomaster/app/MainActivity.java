package com.videomaster.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.videomaster.app.model.MediaList;
import com.videomaster.app.model.VideoItem;
import com.videomaster.app.playlist.MediaListManager;
import com.videomaster.app.playlist.PlaylistAdapter;
import com.videomaster.app.stats.PlayStats;
import com.videomaster.app.ui.VideoAdapter;
import com.videomaster.app.util.BuiltinMediaProvider;
import com.videomaster.app.util.PermissionUtils;
import com.videomaster.app.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME              = SettingsActivity.PREFS_NAME;
    private static final String PREF_DEFAULT_TAB        = SettingsActivity.PREF_DEFAULT_TAB;
    private static final String PREF_TAB_ORDER          = SettingsActivity.PREF_TAB_ORDER;
    private static final String PREF_VIEW_MODE          = SettingsActivity.PREF_VIEW_MODE;
    private static final String PREF_GRID_CELL_WIDTH_LIBRARY  = SettingsActivity.PREF_GRID_CELL_WIDTH_LIBRARY;
    private static final String PREF_GRID_CELL_WIDTH_BUILTIN   = SettingsActivity.PREF_GRID_CELL_WIDTH_BUILTIN;
    private static final String PREF_GRID_CELL_WIDTH_PLAYLIST      = SettingsActivity.PREF_GRID_CELL_WIDTH_PLAYLIST;
    private static final String PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS = SettingsActivity.PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS;
    private static final String DEFAULT_TAB_ORDER       = SettingsActivity.DEFAULT_TAB_ORDER;
    private static final String PREF_HOME_SHORTCUT_ORDER   = SettingsActivity.PREF_HOME_SHORTCUT_ORDER;
    private static final String DEFAULT_HOME_SHORTCUT_ORDER = SettingsActivity.DEFAULT_HOME_SHORTCUT_ORDER;

    // Toolbar action buttons
    private LinearLayout toolbarActions;
    private String activeShortcutId = null;
    private ImageButton btnToggleView;

    // Library tab views
    private RecyclerView  recyclerView;
    private VideoAdapter  adapter;
    private TextView      emptyView;
    private View          loadingView;
    private FloatingActionButton fabOpen;

    // Built-in tab views
    private RecyclerView  recyclerBuiltin;
    private TextView      tvBuiltinEmpty;
    private VideoAdapter  builtinAdapter;

    // Playlist tab views (inline)
    private View          tabPlaylist;
    private View          playlistListView;
    private View          playlistDetailView;
    private RecyclerView  recyclerPlaylists;
    private TextView      tvPlaylistEmpty;
    private FloatingActionButton fabCreatePlaylist;
    private RecyclerView  recyclerPlaylistItems;
    private TextView      tvPlaylistDetailEmpty;
    private FloatingActionButton fabAddToPlaylist;
    private PlaylistAdapter playlistAdapter;
    private VideoAdapter  playlistItemAdapter;
    private MediaList     currentOpenList;

    // MediaList thumbnail picker
    private String pendingCustomThumbListId = null;
    private static final String PLAYLIST_THUMB_PREFS = "playlist_thumbnails";

    // Tab containers
    private View          tabLibrary;
    private View          tabBuiltin;
    private View          tabStats;

    // Stats tab views
    private RecyclerView  recyclerStats;
    private TextView      tvStatsEmpty;
    private TextView      tvStatsSummary;

    private BottomNavigationView bottomNav;
    private MediaListManager     mediaListManager;

    private final List<VideoItem> videoList   = new ArrayList<>();
    private final List<VideoItem> builtinList = new ArrayList<>();
    private final List<MediaList> allLists    = new ArrayList<>();
    private final List<VideoItem> currentListItems = new ArrayList<>();

    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    // Currently visible tab identifier
    private String currentTabId = "builtin";

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) openPlayer(uri);
                }
            });

    private final ActivityResultLauncher<Intent> addToPlaylistLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    processAddToPlaylistResult(result.getData());
                }
            });

    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                applyTabOrderAndDefault();
                applyViewMode();
            });

    private final SharedPreferences.OnSharedPreferenceChangeListener prefsChangeListener =
            (p, key) -> {
                if (PREF_VIEW_MODE.equals(key) || PREF_GRID_CELL_WIDTH_LIBRARY.equals(key)
                        || PREF_GRID_CELL_WIDTH_BUILTIN.equals(key) || PREF_GRID_CELL_WIDTH_PLAYLIST.equals(key)
                        || PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS.equals(key)) {
                    runOnUiThread(this::applyViewMode);
                }
                if (SettingsActivity.PREF_BUILTIN_VISIBLE.equals(key) || SettingsActivity.PREF_PLAYLIST_VISIBLE.equals(key)) {
                    runOnUiThread(this::applyTabOrderAndDefault);
                }
            };

    // Stats export/import launchers
    private final ActivityResultLauncher<Intent> statsExportLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) writeStatsExport(uri);
                }
            });

    private final ActivityResultLauncher<Intent> statsImportLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) readStatsImport(uri);
                }
            });

    // URI string of the playlist item whose thumbnail is being set
    private String pendingCustomThumbUri = null;

    private final ActivityResultLauncher<Intent> thumbnailPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null
                        && pendingCustomThumbUri != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) saveCustomThumbnail(pendingCustomThumbUri, imageUri);
                    pendingCustomThumbUri = null;
                }
            });

    // Launcher for picking a thumbnail image for a MediaList (playlist)
    private final ActivityResultLauncher<Intent> playlistThumbPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null
                        && pendingCustomThumbListId != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) savePlaylistThumbnail(pendingCustomThumbListId, imageUri);
                    pendingCustomThumbListId = null;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mediaListManager = MediaListManager.getInstance(this);

        // Toolbar action buttons container
        toolbarActions = findViewById(R.id.toolbarActions);

        // Tab containers
        tabLibrary  = findViewById(R.id.tabLibrary);
        tabBuiltin  = findViewById(R.id.tabBuiltin);
        tabPlaylist = findViewById(R.id.tabPlaylist);
        tabStats    = findViewById(R.id.tabStats);

        // Library tab
        recyclerView = findViewById(R.id.recyclerView);
        emptyView    = findViewById(R.id.tvEmpty);
        loadingView  = findViewById(R.id.loadingView);
        fabOpen      = findViewById(R.id.fabOpen);
        fabOpen.setOnClickListener(v -> pickVideoFile());
        adapter = new VideoAdapter(videoList, item -> openPlayerWithList(item, videoList));
        recyclerView.setAdapter(adapter);

        // Built-in tab
        recyclerBuiltin = findViewById(R.id.recyclerBuiltin);
        tvBuiltinEmpty  = findViewById(R.id.tvBuiltinEmpty);
        builtinAdapter  = new VideoAdapter(builtinList, item -> openPlayerWithList(item, builtinList));
        recyclerBuiltin.setAdapter(builtinAdapter);

        // Stats tab (inline)
        recyclerStats   = findViewById(R.id.recyclerStats);
        tvStatsEmpty    = findViewById(R.id.tvStatsEmpty);
        tvStatsSummary  = findViewById(R.id.tvStatsSummary);
        recyclerStats.setLayoutManager(new LinearLayoutManager(this));
        findViewById(R.id.btnClearStats).setOnClickListener(v -> confirmClearStats());
        findViewById(R.id.btnExportStats).setOnClickListener(v -> startStatsExport());
        findViewById(R.id.btnImportStats).setOnClickListener(v -> startStatsImport());

        // Playlist tab (inline)
        playlistListView   = findViewById(R.id.playlistListView);
        playlistDetailView = findViewById(R.id.playlistDetailView);
        recyclerPlaylists  = findViewById(R.id.recyclerPlaylists);
        tvPlaylistEmpty    = findViewById(R.id.tvPlaylistEmpty);
        fabCreatePlaylist  = findViewById(R.id.fabCreatePlaylist);
        recyclerPlaylistItems = findViewById(R.id.recyclerPlaylistItems);
        tvPlaylistDetailEmpty = findViewById(R.id.tvPlaylistDetailEmpty);
        fabAddToPlaylist      = findViewById(R.id.fabAddToPlaylist);

        fabCreatePlaylist.setOnClickListener(v -> showCreatePlaylistDialog(null));
        fabAddToPlaylist.setOnClickListener(v -> pickMediaForPlaylist());

        setupPlaylistAdapter();
        recyclerPlaylists.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerPlaylists.setAdapter(playlistAdapter);

        applyViewMode();

        // 监听方格/列表模式变化，设置页透明时实时更新背景网格
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(prefsChangeListener);

        // Bottom navigation
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            highlightToolbarAction(null);
            int id = item.getItemId();
            if (id == R.id.nav_playlist) {
                showTab("playlist");
                refreshPlaylistList();
                return true;
            } else if (id == R.id.nav_builtin) {
                showTab("builtin");
                loadBuiltinMedia();
                return true;
            }
            return false;
        });

        // Handle VIEW intent from other apps
        if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
            openPlayer(getIntent().getData());
            return;
        }

        applyTabOrderAndDefault();
    }

    // ── Back navigation within playlist tab ────────────────────────────────

    @Override
    public void onBackPressed() {
        if ("playlist".equals(currentTabId) && playlistDetailView.getVisibility() == View.VISIBLE) {
            showPlaylistList();
        } else {
            super.onBackPressed();
        }
    }

    // ── Toolbar actions ────────────────────────────────────────────────────

    // ── Tab management ─────────────────────────────────────────────────────

    private void applyTabOrderAndDefault() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String orderStr   = prefs.getString(PREF_TAB_ORDER, DEFAULT_TAB_ORDER);
        String defaultTab = prefs.getString(PREF_DEFAULT_TAB, "builtin");

        // Migration: extract stats/library from tab order into shortcut order
        if (orderStr.contains("stats") || orderStr.contains("library")) {
            String[] tokens = orderStr.split(",");
            StringBuilder tabSb = new StringBuilder();
            StringBuilder shortcutSb = new StringBuilder();
            for (String t : tokens) {
                String trimmed = t.trim();
                if ("library".equals(trimmed) || "stats".equals(trimmed)) {
                    if (shortcutSb.length() > 0) shortcutSb.append(",");
                    shortcutSb.append(trimmed);
                } else {
                    if (tabSb.length() > 0) tabSb.append(",");
                    tabSb.append(trimmed);
                }
            }
            orderStr = tabSb.length() > 0 ? tabSb.toString() : DEFAULT_TAB_ORDER;
            if (!prefs.contains(PREF_HOME_SHORTCUT_ORDER)) {
                String shortcutStr = shortcutSb.length() > 0 ? shortcutSb.toString() : DEFAULT_HOME_SHORTCUT_ORDER;
                prefs.edit().putString(PREF_HOME_SHORTCUT_ORDER, shortcutStr).apply();
            }
            prefs.edit().putString(PREF_TAB_ORDER, orderStr).apply();
        }

        String[] order = orderStr.split(",");
        boolean builtinVisible  = prefs.getBoolean(SettingsActivity.PREF_BUILTIN_VISIBLE, true);
        boolean playlistVisible = prefs.getBoolean(SettingsActivity.PREF_PLAYLIST_VISIBLE, true);
        rebuildBottomNav(order, builtinVisible, playlistVisible);
        setupToolbarActions(prefs);

        // 若默认标签被隐藏，则切换到第一个可见标签
        String effectiveDefault = defaultTab;
        if ("builtin".equals(defaultTab) && !builtinVisible) effectiveDefault = null;
        if ("playlist".equals(defaultTab) && !playlistVisible) effectiveDefault = null;
        if (effectiveDefault == null) {
            for (String t : order) {
                String id = t.trim();
                if ("builtin".equals(id) && builtinVisible) { effectiveDefault = "builtin"; break; }
                if ("playlist".equals(id) && playlistVisible) { effectiveDefault = "playlist"; break; }
            }
        }

        switch (effectiveDefault != null ? effectiveDefault : "") {
            case "library":
                showTab("library");
                checkPermissionsAndLoad();
                highlightToolbarAction("library");
                break;
            case "playlist": bottomNav.setSelectedItemId(R.id.nav_playlist); break;
            case "builtin":  bottomNav.setSelectedItemId(R.id.nav_builtin);  break;
            default:
                if (builtinVisible) bottomNav.setSelectedItemId(R.id.nav_builtin);
                else if (playlistVisible) bottomNav.setSelectedItemId(R.id.nav_playlist);
                else { showTab("library"); checkPermissionsAndLoad(); highlightToolbarAction("library"); }
                break;
        }
    }

    private void rebuildBottomNav(String[] order, boolean builtinVisible, boolean playlistVisible) {
        Menu menu = bottomNav.getMenu();
        menu.clear();
        for (String tabId : order) {
            String id = tabId.trim();
            if ("playlist".equals(id) && !playlistVisible) continue;
            if ("builtin".equals(id) && !builtinVisible) continue;
            switch (id) {
                case "playlist":
                    menu.add(Menu.NONE, R.id.nav_playlist, Menu.NONE, R.string.tab_playlist)
                            .setIcon(R.drawable.ic_playlist);
                    break;
                case "builtin":
                    menu.add(Menu.NONE, R.id.nav_builtin, Menu.NONE, R.string.tab_builtin)
                            .setIcon(R.drawable.ic_builtin);
                    break;
            }
        }
    }

    // ── Toolbar action buttons ─────────────────────────────────────────────

    private void setupToolbarActions(SharedPreferences prefs) {
        toolbarActions.removeAllViews();
        btnToggleView = null;
        String shortcutStr = prefs.getString(PREF_HOME_SHORTCUT_ORDER, DEFAULT_HOME_SHORTCUT_ORDER);
        String[] shortcuts = migrateShortcutOrder(shortcutStr, prefs);

        for (String s : shortcuts) {
            String id = s.trim();
            if (!isToolbarActionVisible(prefs, id)) continue;
            ImageButton btn = createToolbarAction(id);
            if (btn != null) {
                int size = dp(40);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                lp.setMarginEnd(dp(2));
                btn.setLayoutParams(lp);
                btn.setPadding(dp(8), dp(8), dp(8), dp(8));
                btn.setScaleType(ImageButton.ScaleType.FIT_CENTER);
                btn.setBackgroundResource(android.R.drawable.list_selector_background);
                btn.setTag(id);
                toolbarActions.addView(btn);
            }
        }
        updateToggleViewIcon();
        highlightToolbarAction(activeShortcutId);
    }

    private boolean isToolbarActionVisible(SharedPreferences prefs, String id) {
        switch (id) {
            case "stats":       return prefs.getBoolean(SettingsActivity.PREF_HOME_BTN_STATS_VISIBLE, true);
            case "library":     return prefs.getBoolean(SettingsActivity.PREF_HOME_BTN_LIBRARY_VISIBLE, true);
            case "toggle_view": return prefs.getBoolean(SettingsActivity.PREF_HOME_BTN_TOGGLE_VIEW_VISIBLE, true);
            default:            return true;
        }
    }

    private ImageButton createToolbarAction(String id) {
        int iconRes;
        switch (id) {
            case "library":    iconRes = R.drawable.ic_home;      break;
            case "stats":      iconRes = R.drawable.ic_stats;     break;
            case "toggle_view": iconRes = R.drawable.ic_view_list; break;
            case "settings":   iconRes = R.drawable.ic_settings;  break;
            default: return null;
        }

        ImageButton btn = new ImageButton(this);
        btn.setImageResource(iconRes);
        btn.setColorFilter(getResources().getColor(R.color.textPrimary, null));

        if ("toggle_view".equals(id)) {
            btnToggleView = btn;
        }

        btn.setOnClickListener(v -> onToolbarActionClick(id));
        return btn;
    }

    private void onToolbarActionClick(String id) {
        switch (id) {
            case "library":
                showTab("library");
                checkPermissionsAndLoad();
                highlightToolbarAction("library");
                break;
            case "stats":
                showTab("stats");
                loadStats();
                highlightToolbarAction("stats");
                break;
            case "toggle_view":
                toggleViewMode();
                break;
            case "settings":
                settingsLauncher.launch(new Intent(this, SettingsActivity.class));
                break;
        }
    }

    private void highlightToolbarAction(String activeId) {
        this.activeShortcutId = activeId;
        for (int i = 0; i < toolbarActions.getChildCount(); i++) {
            View child = toolbarActions.getChildAt(i);
            String tagId = (String) child.getTag();
            boolean active = activeId != null && activeId.equals(tagId);
            if (child instanceof ImageButton) {
                ((ImageButton) child).setColorFilter(
                        getResources().getColor(active ? R.color.colorAccent : R.color.textPrimary, null));
            }
        }
    }

    private void updateToggleViewIcon() {
        if (btnToggleView == null) return;
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(PREF_VIEW_MODE, SettingsActivity.VIEW_MODE_GRID);
        boolean isGrid = SettingsActivity.VIEW_MODE_GRID.equals(mode);
        btnToggleView.setImageResource(isGrid ? R.drawable.ic_view_list : R.drawable.ic_view_grid);
    }

    private String[] migrateShortcutOrder(String orderStr, SharedPreferences prefs) {
        String[] ALL = {"library", "stats", "toggle_view", "settings"};
        java.util.List<String> current = new java.util.ArrayList<>();
        for (String s : orderStr.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) current.add(t);
        }
        boolean changed = false;
        for (String key : ALL) {
            if (!current.contains(key)) {
                current.add(key);
                changed = true;
            }
        }
        if (changed) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < current.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(current.get(i));
            }
            prefs.edit().putString(PREF_HOME_SHORTCUT_ORDER, sb.toString()).apply();
        }
        return current.toArray(new String[0]);
    }

    private void showTab(String tabId) {
        currentTabId = tabId;
        tabLibrary.setVisibility("library".equals(tabId)  ? View.VISIBLE : View.GONE);
        tabBuiltin.setVisibility("builtin".equals(tabId)  ? View.VISIBLE : View.GONE);
        tabPlaylist.setVisibility("playlist".equals(tabId) ? View.VISIBLE : View.GONE);
        tabStats.setVisibility("stats".equals(tabId)      ? View.VISIBLE : View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if ("library".equals(tabId))       toolbar.setTitle(R.string.library_title);
        else if ("builtin".equals(tabId))  toolbar.setTitle(R.string.tab_builtin);
        else if ("playlist".equals(tabId)) toolbar.setTitle(R.string.tab_playlist);
        else if ("stats".equals(tabId))    toolbar.setTitle(R.string.stats_title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ("library".equals(currentTabId))  loadVideos();
        if ("playlist".equals(currentTabId)) refreshPlaylistList();
        if ("stats".equals(currentTabId))    loadStats();
    }

    @Override
    protected void onDestroy() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(prefsChangeListener);
        super.onDestroy();
    }

    // ── View mode (grid / list) ────────────────────────────────────────────

    private void applyViewMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(PREF_VIEW_MODE, SettingsActivity.VIEW_MODE_GRID);
        boolean isGrid = SettingsActivity.VIEW_MODE_GRID.equals(mode);

        int spanLibrary       = isGrid ? computeGridSpanCount(prefs, PREF_GRID_CELL_WIDTH_LIBRARY) : 1;
        int spanBuiltin       = isGrid ? computeGridSpanCount(prefs, PREF_GRID_CELL_WIDTH_BUILTIN) : 1;
        int spanPlaylist      = isGrid ? computeGridSpanCount(prefs, PREF_GRID_CELL_WIDTH_PLAYLIST) : 1;
        int spanPlaylistItems = isGrid ? computeGridSpanCount(prefs, PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS) : 1;

        adapter.setViewMode(isGrid ? VideoAdapter.ViewMode.GRID : VideoAdapter.ViewMode.LIST);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanLibrary));

        builtinAdapter.setViewMode(isGrid ? VideoAdapter.ViewMode.GRID : VideoAdapter.ViewMode.LIST);
        recyclerBuiltin.setLayoutManager(new GridLayoutManager(this, spanBuiltin));

        if (playlistItemAdapter != null) {
            playlistItemAdapter.setViewMode(isGrid ? VideoAdapter.ViewMode.GRID : VideoAdapter.ViewMode.LIST);
        }
        recyclerPlaylistItems.setLayoutManager(new GridLayoutManager(this, spanPlaylistItems));

        if (playlistAdapter != null) {
            playlistAdapter.setViewMode(
                    isGrid ? PlaylistAdapter.ViewMode.GRID : PlaylistAdapter.ViewMode.LIST);
        }
        recyclerPlaylists.setLayoutManager(
                isGrid ? new GridLayoutManager(this, spanPlaylist) : new LinearLayoutManager(this));

        updateToggleViewIcon();
    }

    /** 根据方格目标宽度（dp）和屏幕宽度计算列数 */
    private int computeGridSpanCount(SharedPreferences prefs, String prefKey) {
        int cellWidthDp = prefs.getInt(prefKey, SettingsActivity.DEFAULT_GRID_CELL_WIDTH_DP);
        cellWidthDp = Math.max(SettingsActivity.MIN_GRID_CELL_WIDTH_DP,
                Math.min(SettingsActivity.MAX_GRID_CELL_WIDTH_DP, cellWidthDp));
        float density = getResources().getDisplayMetrics().density;
        int screenWidthPx = getResources().getDisplayMetrics().widthPixels;
        int paddingPx = (int) (32 * density);
        int cellWidthPx = (int) (cellWidthDp * density);
        if (cellWidthPx <= 0) cellWidthPx = 1;
        return Math.max(2, Math.min(6, (screenWidthPx - paddingPx) / cellWidthPx));
    }

    private void toggleViewMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String current = prefs.getString(PREF_VIEW_MODE, SettingsActivity.VIEW_MODE_GRID);
        String next = SettingsActivity.VIEW_MODE_GRID.equals(current)
                ? SettingsActivity.VIEW_MODE_LIST
                : SettingsActivity.VIEW_MODE_GRID;
        prefs.edit().putString(PREF_VIEW_MODE, next).apply();
        applyViewMode();
        updateToggleViewIcon();
        if ("playlist".equals(currentTabId) && playlistDetailView.getVisibility() == View.VISIBLE) {
            refreshPlaylistDetail(currentOpenList);
        }
    }

    // ── Stats tab ─────────────────────────────────────────────────────────

    private void loadStats() {
        PlayStats stats = PlayStats.getInstance(this);
        java.util.List<PlayStats.StatEntry> entries = stats.getStats();

        if (entries.isEmpty()) {
            recyclerStats.setVisibility(View.GONE);
            tvStatsEmpty.setVisibility(View.VISIBLE);
            tvStatsSummary.setText(R.string.stats_empty);
            return;
        }

        recyclerStats.setVisibility(View.VISIBLE);
        tvStatsEmpty.setVisibility(View.GONE);

        long totalMs = stats.getTotalMs();
        tvStatsSummary.setText(getString(R.string.stats_summary,
                entries.size(), TimeUtils.formatDuration(totalMs)));

        long maxMs = entries.get(0).totalMs;
        recyclerStats.setAdapter(new StatsInlineAdapter(entries, maxMs));
    }

    private void confirmClearStats() {
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.stats_clear_title)
                .setMessage(R.string.stats_clear_message)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    PlayStats.getInstance(this).clearAll();
                    loadStats();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void startStatsExport() {
        PlayStats stats = PlayStats.getInstance(this);
        if (stats.getStats().isEmpty()) {
            Toast.makeText(this, R.string.stats_no_data, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "videomaster_stats.json");
        statsExportLauncher.launch(intent);
    }

    private void writeStatsExport(Uri uri) {
        try (java.io.OutputStream out = getContentResolver().openOutputStream(uri)) {
            if (out == null) throw new Exception("null stream");
            String json = PlayStats.getInstance(this).exportToJson();
            out.write(json.getBytes("UTF-8"));
            out.flush();
            Toast.makeText(this, R.string.stats_export_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.stats_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void startStatsImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/json", "text/plain", "application/octet-stream"});
        statsImportLauncher.launch(intent);
    }

    private void readStatsImport(Uri uri) {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(getContentResolver().openInputStream(uri), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            int count = PlayStats.getInstance(this).importFromJson(sb.toString());
            if (count >= 0) {
                Toast.makeText(this, getString(R.string.stats_import_success, count),
                        Toast.LENGTH_SHORT).show();
                loadStats();
            } else {
                Toast.makeText(this, R.string.stats_import_failed, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.stats_import_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private static class StatsInlineAdapter extends RecyclerView.Adapter<StatsInlineAdapter.VH> {
        private final java.util.List<PlayStats.StatEntry> entries;
        private final long maxMs;

        StatsInlineAdapter(java.util.List<PlayStats.StatEntry> entries, long maxMs) {
            this.entries = entries;
            this.maxMs   = maxMs;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stats_bar, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            PlayStats.StatEntry e = entries.get(pos);
            h.tvName.setText(String.format(java.util.Locale.getDefault(), "%d. %s", pos + 1, e.name));
            h.tvTime.setText(TimeUtils.formatDuration(e.totalMs));
            int progress = maxMs > 0 ? (int) (e.totalMs * 1000L / maxMs) : 0;
            h.progressBar.setProgress(progress);
        }

        @Override public int getItemCount() { return entries.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView tvName;
            final TextView tvTime;
            final android.widget.ProgressBar progressBar;

            VH(@NonNull View v) {
                super(v);
                tvName      = v.findViewById(R.id.tvStatName);
                tvTime      = v.findViewById(R.id.tvStatTime);
                progressBar = v.findViewById(R.id.pbStatBar);
            }
        }
    }

    // ── Permissions ────────────────────────────────────────────────────────

    private void checkPermissionsAndLoad() {
        if (!PermissionUtils.ensureStoragePermissions(this)) {
            // waiting for callback
        } else {
            loadVideos();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionUtils.REQUEST_STORAGE) {
            if (PermissionUtils.isGranted(grantResults)) {
                loadVideos();
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show();
            }
        }
    }

    // ── Video loading (MediaStore) ─────────────────────────────────────────

    private void loadVideos() {
        loadingView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);

        executor.execute(() -> {
            List<VideoItem> items = queryMediaStore();
            mainHandler.post(() -> {
                loadingView.setVisibility(View.GONE);
                videoList.clear();
                videoList.addAll(items);
                adapter.notifyDataSetChanged();
                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private List<VideoItem> queryMediaStore() {
        List<VideoItem> items = new ArrayList<>();
        items.addAll(queryMediaStoreUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION, MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE, MediaStore.Video.Media.DATE_MODIFIED));
        items.addAll(queryMediaStoreUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.DATE_MODIFIED));
        items.sort((a, b) -> Long.compare(b.getLastModified(), a.getLastModified()));
        return items;
    }

    private List<VideoItem> queryMediaStoreUri(android.net.Uri contentUri,
            String colId, String colName, String colDuration,
            String colSize, String colMime, String colDate) {
        List<VideoItem> items = new ArrayList<>();
        String[] projection = {colId, colName, colDuration, colSize, colMime, colDate};
        try (Cursor cursor = getContentResolver().query(
                contentUri, projection, null, null, colDate + " DESC")) {
            if (cursor == null) return items;
            int idIdx   = cursor.getColumnIndexOrThrow(colId);
            int nameIdx = cursor.getColumnIndexOrThrow(colName);
            int durIdx  = cursor.getColumnIndexOrThrow(colDuration);
            int sizeIdx = cursor.getColumnIndexOrThrow(colSize);
            int mimeIdx = cursor.getColumnIndexOrThrow(colMime);
            int dateIdx = cursor.getColumnIndexOrThrow(colDate);
            while (cursor.moveToNext()) {
                long id  = cursor.getLong(idIdx);
                Uri  uri = Uri.withAppendedPath(contentUri, String.valueOf(id));
                items.add(new VideoItem(
                        cursor.getString(nameIdx), uri,
                        cursor.getLong(durIdx), cursor.getLong(sizeIdx),
                        cursor.getString(mimeIdx), cursor.getLong(dateIdx) * 1000L));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    // ── Built-in media loading ─────────────────────────────────────────────

    private void loadBuiltinMedia() {
        executor.execute(() -> {
            List<VideoItem> items = BuiltinMediaProvider.getBuiltinMedia(this);
            mainHandler.post(() -> {
                builtinList.clear();
                builtinList.addAll(items);
                builtinAdapter.notifyDataSetChanged();
                tvBuiltinEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerBuiltin.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
            });
        });
    }

    // ── Inline Playlist Tab ────────────────────────────────────────────────

    private void setupPlaylistAdapter() {
        allLists.clear();
        allLists.addAll(mediaListManager.getLists());
        playlistAdapter = new PlaylistAdapter(allLists, new PlaylistAdapter.OnItemListener() {
            @Override public void onItemClick(MediaList list) {
                openPlaylistDetail(list);
            }
            @Override public void onItemLongClick(MediaList list) {
                showPlaylistListOptions(list);
            }
        });
        // Use the same view-mode preference as the rest of the app
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isGrid = SettingsActivity.VIEW_MODE_GRID.equals(
                prefs.getString(PREF_VIEW_MODE, SettingsActivity.VIEW_MODE_GRID));
        playlistAdapter.setViewMode(isGrid ? PlaylistAdapter.ViewMode.GRID : PlaylistAdapter.ViewMode.LIST);
    }

    private void refreshPlaylistList() {
        allLists.clear();
        allLists.addAll(mediaListManager.getLists());
        if (playlistAdapter == null) {
            setupPlaylistAdapter();
            applyViewMode();
            recyclerPlaylists.setAdapter(playlistAdapter);
        } else {
            playlistAdapter.notifyDataSetChanged();
        }
        tvPlaylistEmpty.setVisibility(allLists.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerPlaylists.setVisibility(allLists.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showPlaylistList() {
        currentOpenList = null;
        playlistListView.setVisibility(View.VISIBLE);
        playlistDetailView.setVisibility(View.GONE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.tab_playlist);
        refreshPlaylistList();
    }

    private void openPlaylistDetail(MediaList list) {
        currentOpenList = list;
        playlistListView.setVisibility(View.GONE);
        playlistDetailView.setVisibility(View.VISIBLE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(list.getName());
        refreshPlaylistDetail(list);
    }

    private void refreshPlaylistDetail(MediaList list) {
        if (list == null) return;
        List<String> uris = list.getItemUris();
        currentListItems.clear();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(PREF_VIEW_MODE, SettingsActivity.VIEW_MODE_GRID);
        boolean isGrid = SettingsActivity.VIEW_MODE_GRID.equals(mode);
        int spanCount = isGrid ? computeGridSpanCount(prefs, PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS) : 1;

        for (String uriStr : uris) {
            Uri uri = Uri.parse(uriStr);
            String title = uri.getLastPathSegment();
            if (title == null) title = uriStr;
            try { title = java.net.URLDecoder.decode(title, "UTF-8"); } catch (Exception ignored) {}
            // Detect mime type from extension for thumbnail loading
            String lower = title.toLowerCase();
            String mime = null;
            if (lower.endsWith(".mp3") || lower.endsWith(".aac") || lower.endsWith(".flac")
                    || lower.endsWith(".wav") || lower.endsWith(".ogg") || lower.endsWith(".m4a")
                    || lower.endsWith(".opus") || lower.endsWith(".wma")) {
                mime = "audio/";
            }
            currentListItems.add(new VideoItem(title, uri, 0, 0, mime, 0));
        }

        playlistItemAdapter = new VideoAdapter(currentListItems, item -> {
            // Build a snapshot of the current list to avoid index drift issues
            ArrayList<String> uriStrs = new ArrayList<>();
            for (VideoItem vi : currentListItems) uriStrs.add(vi.getUri().toString());
            int idx = uriStrs.indexOf(item.getUri().toString());
            if (idx < 0) return;
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra(PlayerActivity.EXTRA_VIDEO_URI, uriStrs.get(idx));
            intent.putStringArrayListExtra(PlayerActivity.EXTRA_PLAYLIST_URIS, uriStrs);
            intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_INDEX, idx);
            intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_ID, list.getId());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, R.string.error_no_video, Toast.LENGTH_SHORT).show();
            }
        });
        playlistItemAdapter.setOnItemLongClickListener(this::showPlaylistItemOptions);
        playlistItemAdapter.setViewMode(isGrid ? VideoAdapter.ViewMode.GRID : VideoAdapter.ViewMode.LIST);
        recyclerPlaylistItems.setLayoutManager(new GridLayoutManager(this, spanCount));
        recyclerPlaylistItems.setAdapter(playlistItemAdapter);

        tvPlaylistDetailEmpty.setVisibility(uris.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerPlaylistItems.setVisibility(uris.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /** Long-press options for a MediaList item in the playlist list view (NOT inside a list). */
    private void showPlaylistListOptions(MediaList list) {
        String[] opts = {
                getString(R.string.playlist_thumb_set),
                getString(R.string.playlist_thumb_clear),
                getString(R.string.playlist_rename),
                getString(R.string.playlist_delete)
        };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(list.getName())
                .setItems(opts, (d, which) -> {
                    if (which == 0) pickPlaylistThumbnail(list.getId());
                    else if (which == 1) clearPlaylistThumbnail(list.getId());
                    else if (which == 2) showCreatePlaylistDialog(list);
                    else confirmDeletePlaylist(list);
                })
                .show();
    }

    private void pickPlaylistThumbnail(String listId) {
        pendingCustomThumbListId = listId;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        playlistThumbPickerLauncher.launch(intent);
    }

    private void clearPlaylistThumbnail(String listId) {
        getSharedPreferences(PLAYLIST_THUMB_PREFS, MODE_PRIVATE)
                .edit().remove(listId).apply();
        refreshPlaylistList();
        Toast.makeText(this, R.string.custom_thumb_clear, Toast.LENGTH_SHORT).show();
    }

    private void savePlaylistThumbnail(String listId, Uri imageUri) {
        java.io.File thumbDir = new java.io.File(getFilesDir(), "playlist_thumbs");
        if (!thumbDir.exists()) thumbDir.mkdirs();
        String hash = String.valueOf(Math.abs(listId.hashCode()));
        java.io.File destFile = new java.io.File(thumbDir, hash + ".jpg");

        // Run I/O on background thread to avoid blocking the UI
        executor.execute(() -> {
            try {
                try (java.io.InputStream in = getContentResolver().openInputStream(imageUri);
                     java.io.FileOutputStream out = new java.io.FileOutputStream(destFile)) {
                    if (in == null) {
                        mainHandler.post(() -> Toast.makeText(this,
                                R.string.playlist_thumb_failed, Toast.LENGTH_SHORT).show());
                        return;
                    }
                    android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                    if (bmp == null) {
                        mainHandler.post(() -> Toast.makeText(this,
                                R.string.playlist_thumb_failed, Toast.LENGTH_SHORT).show());
                        return;
                    }
                    bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out);
                }
                getSharedPreferences(PLAYLIST_THUMB_PREFS, MODE_PRIVATE)
                        .edit().putString(listId, destFile.getAbsolutePath()).apply();
                mainHandler.post(() -> {
                    refreshPlaylistList();
                    Toast.makeText(this, R.string.playlist_thumb_saved, Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this,
                        R.string.playlist_thumb_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void showCreatePlaylistDialog(MediaList existing) {
        EditText etName     = new EditText(this);
        EditText etCategory = new EditText(this);
        etName.setHint(R.string.playlist_name_hint);
        etCategory.setHint(R.string.playlist_category_hint);
        etName.setTextColor(getResources().getColor(R.color.textPrimary, null));
        etCategory.setTextColor(getResources().getColor(R.color.textPrimary, null));
        int pad = dp(16);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(pad, pad / 2, pad, pad / 2);
        layout.addView(etName);
        layout.addView(etCategory);

        if (existing != null) {
            etName.setText(existing.getName());
            etCategory.setText(existing.getCategory());
        }

        int titleRes = existing == null ? R.string.playlist_create : R.string.playlist_rename;
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(titleRes)
                .setView(layout)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    String cat  = etCategory.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.playlist_name_empty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (existing == null) {
                        mediaListManager.createList(name, cat);
                    } else {
                        mediaListManager.renameList(existing.getId(), name, cat);
                    }
                    refreshPlaylistList();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmDeletePlaylist(MediaList list) {
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.playlist_delete)
                .setMessage(getString(R.string.playlist_delete_confirm, list.getName()))
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    mediaListManager.deleteList(list.getId());
                    refreshPlaylistList();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void pickMediaForPlaylist() {
        if (currentOpenList == null) return;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "audio/*"});
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        addToPlaylistLauncher.launch(intent);
    }

    private void processAddToPlaylistResult(Intent data) {
        if (currentOpenList == null) return;
        List<Uri> uris = new ArrayList<>();
        ClipData clipData = data.getClipData();
        if (clipData != null) {
            for (int i = 0; i < clipData.getItemCount(); i++) {
                Uri u = clipData.getItemAt(i).getUri();
                if (u != null) uris.add(u);
            }
        } else if (data.getData() != null) {
            uris.add(data.getData());
        }
        int count = 0;
        for (Uri uri : uris) {
            try {
                getContentResolver().takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored) {}
            mediaListManager.addItemToList(currentOpenList.getId(), uri.toString());
            count++;
        }
        if (count > 0) {
            String msg = count == 1
                    ? getString(R.string.playlist_add_media)
                    : getString(R.string.playlist_added_count, count);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            refreshPlaylistDetail(currentOpenList);
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    private void pickVideoFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "audio/*"});
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        filePickerLauncher.launch(intent);
    }

    private void openPlayer(Uri uri) {
        ArrayList<String> uris = new ArrayList<>();
        uris.add(uri.toString());
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URI, uri.toString());
        intent.putStringArrayListExtra(PlayerActivity.EXTRA_PLAYLIST_URIS, uris);
        intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_INDEX, 0);
        startActivity(intent);
    }

    private void openPlayerWithList(VideoItem item, List<VideoItem> list) {
        int idx = list.indexOf(item);
        ArrayList<String> uris = new ArrayList<>();
        for (VideoItem vi : list) uris.add(vi.getUri().toString());
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URI, item.getUri().toString());
        intent.putStringArrayListExtra(PlayerActivity.EXTRA_PLAYLIST_URIS, uris);
        intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_INDEX, Math.max(0, idx));
        startActivity(intent);
    }

    // ── Custom thumbnail ───────────────────────────────────────────────────

    private void pickCustomThumbnail(String mediaUriStr) {
        pendingCustomThumbUri = mediaUriStr;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        thumbnailPickerLauncher.launch(intent);
    }

    private void saveCustomThumbnail(String mediaUriStr, Uri imageUri) {
        try {
            java.io.File thumbDir = new java.io.File(getFilesDir(), "thumbs");
            if (!thumbDir.exists()) thumbDir.mkdirs();
            // Use a hash of the URI as filename to avoid path collisions
            String hash = String.valueOf(Math.abs(mediaUriStr.hashCode()));
            java.io.File destFile = new java.io.File(thumbDir, hash + ".jpg");

            // Copy the selected image to internal storage
            try (java.io.InputStream in = getContentResolver().openInputStream(imageUri);
                 java.io.FileOutputStream out = new java.io.FileOutputStream(destFile)) {
                if (in == null) return;
                android.graphics.Bitmap bmp = android.graphics.BitmapFactory.decodeStream(in);
                if (bmp != null) {
                    bmp.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out);
                }
            }

            // Persist the mapping
            getSharedPreferences("custom_thumbnails", MODE_PRIVATE)
                    .edit().putString(mediaUriStr, destFile.getAbsolutePath()).apply();

            // Refresh the adapter so the new thumbnail shows
            if (currentOpenList != null) refreshPlaylistDetail(currentOpenList);
            Toast.makeText(this, R.string.custom_thumb_saved, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.custom_thumb_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void showPlaylistItemOptions(VideoItem item) {
        String uriStr = item.getUri().toString();
        String[] opts = {
                getString(R.string.custom_thumb_set),
                getString(R.string.custom_thumb_clear),
                getString(R.string.playlist_remove_item)
        };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(item.getTitle())
                .setItems(opts, (d, which) -> {
                    if (which == 0) {
                        pickCustomThumbnail(uriStr);
                    } else if (which == 1) {
                        getSharedPreferences("custom_thumbnails", MODE_PRIVATE)
                                .edit().remove(uriStr).apply();
                        if (currentOpenList != null) refreshPlaylistDetail(currentOpenList);
                    } else if (which == 2 && currentOpenList != null) {
                        mediaListManager.removeItemFromList(currentOpenList.getId(), uriStr);
                        refreshPlaylistDetail(currentOpenList);
                    }
                })
                .show();
    }

    private int dp(int dpVal) {
        return (int) (dpVal * getResources().getDisplayMetrics().density);
    }

    private String getTabLabel(String tabId) {
        switch (tabId) {
            case "library":  return getString(R.string.tab_library);
            case "playlist": return getString(R.string.tab_playlist);
            case "builtin":  return getString(R.string.tab_builtin);
            case "stats":    return getString(R.string.tab_stats);
            default:         return tabId;
        }
    }
}
