package com.videomaster.app;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.HashMap;
import java.util.Map;

/**
 * Full-screen Settings Activity.
 * Covers: default home tab, tab order, list display mode (grid/list),
 * portrait swipe direction, landscape swipe direction, player control button
 * visibility/color/position/order, and subtitle appearance.
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME            = "app_settings";
    public static final String PREF_DEFAULT_TAB      = "default_tab";
    public static final String PREF_TAB_ORDER        = "tab_order";
    public static final String PREF_BUILTIN_VISIBLE  = "builtin_visible";   // 内置媒体 有无
    public static final String PREF_PLAYLIST_VISIBLE = "playlist_visible";  // 我的列表 有无
    public static final String PREF_PORTRAIT_SWIPE   = "portrait_swipe";
    public static final String PREF_LANDSCAPE_SWIPE  = "landscape_swipe";
    public static final String PREF_VIEW_MODE        = "view_mode";
    public static final String PREF_GRID_SPAN_COUNT  = "grid_span_count";  // deprecated, migrated to PREF_GRID_CELL_WIDTH_DP
    public static final String PREF_GRID_CELL_WIDTH_DP     = "grid_cell_width_dp";  // deprecated, migrated to per-tab
    public static final String PREF_GRID_CELL_WIDTH_LIBRARY = "grid_cell_width_library";   // 媒体库
    public static final String PREF_GRID_CELL_WIDTH_BUILTIN = "grid_cell_width_builtin";   // 内置媒体
    public static final String PREF_GRID_CELL_WIDTH_PLAYLIST     = "grid_cell_width_playlist";      // 我的列表（卡片）
    public static final String PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS = "grid_cell_width_playlist_items";  // 列表内音视频（点进文件夹后）
    public static final String PREF_SUBTITLE_SIZE    = "subtitle_size";
    public static final String PREF_SUBTITLE_LINE_SP = "subtitle_line_spacing";

    // Seek button settings
    public static final String PREF_SEEK_ICON_SIZE         = "seek_icon_size";
    public static final String PREF_SEEK_SECONDS           = "seek_seconds";
    public static final String PREF_SEEK_ALPHA             = "seek_alpha";
    public static final String PREF_SEEK_OFFSET_Y           = "seek_offset_y";
    public static final int    DEFAULT_SEEK_OFFSET_Y       = 0;

    // Seekbar settings
    public static final String PREF_SEEKBAR_HEIGHT         = "seekbar_height";
    public static final String PREF_SEEKBAR_PROGRESS_ALPHA = "seekbar_progress_alpha";
    // Subtitle panel
    public static final String PREF_SUBTITLE_PANEL_ALPHA   = "subtitle_panel_alpha";
    // Subtitle text default visibility (whether subtitle text is shown when player starts)
    public static final String PREF_SUBTITLE_DEFAULT_VISIBLE = "subtitle_default_visible";

    // Playlist panel slide-in direction
    public static final String PREF_PANEL_DIR_PORTRAIT  = "panel_dir_portrait";
    public static final String PREF_PANEL_DIR_LANDSCAPE = "panel_dir_landscape";
    // Possible values: "TOP" | "BOTTOM" | "LEFT" | "RIGHT"

    // Playlist panel appearance (in-player)
    public static final String PREF_PLAYLIST_PANEL_BG_ALPHA    = "playlist_panel_bg_alpha";
    public static final String PREF_PLAYLIST_PANEL_WIDTH_DP    = "playlist_panel_width_dp";
    public static final String PREF_PLAYLIST_PANEL_HEIGHT_DP   = "playlist_panel_height_dp";
    public static final String PREF_PLAYLIST_PANEL_ORIENTATION = "playlist_panel_orientation";
    public static final int    DEFAULT_PLAYLIST_PANEL_BG_ALPHA  = 87;   // #DD ≈ 87%
    public static final int    DEFAULT_PLAYLIST_PANEL_WIDTH_DP   = 260;
    public static final int    DEFAULT_PLAYLIST_PANEL_HEIGHT_DP  = 300;
    public static final String PLAYLIST_PANEL_ORIENTATION_VERTICAL   = "VERTICAL";
    public static final String PLAYLIST_PANEL_ORIENTATION_HORIZONTAL = "HORIZONTAL";
    public static final String PANEL_DIR_TOP    = "TOP";
    public static final String PANEL_DIR_BOTTOM = "BOTTOM";
    public static final String PANEL_DIR_LEFT   = "LEFT";
    public static final String PANEL_DIR_RIGHT  = "RIGHT";

    // Player control button visibility (boolean)
    public static final String PREF_BTN_LOCK_VISIBLE            = "btn_lock_visible";
    public static final String PREF_BTN_PLAYMODE_VISIBLE        = "btn_playmode_visible";
    public static final String PREF_BTN_PLAYLIST_VISIBLE        = "btn_playlist_visible";
    public static final String PREF_BTN_SUBTITLE_VISIBLE        = "btn_subtitle_visible";
    public static final String PREF_BTN_SUBTITLE_TOGGLE_VISIBLE = "btn_subtitle_toggle_visible";
    public static final String PREF_BTN_SUBTLIST_VISIBLE        = "btn_subtlist_visible";
    public static final String PREF_BTN_ROTATE_VISIBLE          = "btn_rotate_visible";
    public static final String PREF_BTN_SPEED_VISIBLE          = "btn_speed_visible";

    // Skip buttons (prev/next video)
    public static final String PREF_BTN_SKIP_VISIBLE         = "btn_skip_visible";
    public static final String PREF_BTN_SKIP_COLOR           = "btn_skip_color";
    public static final String PREF_SKIP_BTN_SIZE            = "skip_btn_size";
    public static final int    DEFAULT_SKIP_BTN_SIZE         = 48;
    public static final String PREF_SKIP_BTN_ALPHA           = "skip_btn_alpha";
    public static final int    DEFAULT_SKIP_BTN_ALPHA        = 100;
    public static final String PREF_SKIP_BTN_OFFSET_Y        = "skip_btn_offset_y";
    public static final int    DEFAULT_SKIP_BTN_OFFSET_Y     = 0;

    // Player control button colors (string: "white","accent","orange","cyan","green","yellow")
    public static final String PREF_BTN_LOCK_COLOR           = "btn_lock_color";
    public static final String PREF_BTN_PLAYMODE_COLOR       = "btn_playmode_color";
    public static final String PREF_BTN_PLAYLIST_COLOR       = "btn_playlist_color";
    public static final String PREF_BTN_SUBTITLE_COLOR       = "btn_subtitle_color";
    public static final String PREF_BTN_SUBTITLE_TOGGLE_COLOR= "btn_subtitle_toggle_color";
    public static final String PREF_BTN_SUBTLIST_COLOR       = "btn_subtlist_color";
    public static final String PREF_BTN_ROTATE_COLOR         = "btn_rotate_color";
    public static final String PREF_BTN_SPEED_COLOR           = "btn_speed_color";
    public static final String PREF_BTN_SEEK_VISIBLE          = "btn_seek_visible";
    public static final String PREF_BTN_SEEK_COLOR           = "btn_seek_color";
    public static final String PREF_BTN_PLAYER_SETTINGS_VISIBLE = "btn_player_settings_visible";
    public static final String PREF_BTN_PLAYER_SETTINGS_COLOR   = "btn_player_settings_color";
    public static final String PREF_BTN_PLAYPAUSE_VISIBLE     = "btn_playpause_visible";
    public static final String PREF_BTN_PLAYPAUSE_COLOR      = "btn_playpause_color";
    public static final String PREF_PLAYPAUSE_BTN_SIZE        = "playpause_btn_size";
    public static final int    DEFAULT_PLAYPAUSE_BTN_SIZE     = 72;
    public static final String PREF_PLAYPAUSE_BTN_ALPHA       = "playpause_btn_alpha";
    public static final int    DEFAULT_PLAYPAUSE_BTN_ALPHA    = 100;
    public static final String PREF_PLAYPAUSE_BTN_OFFSET_Y    = "playpause_btn_offset_y";
    public static final int    DEFAULT_PLAYPAUSE_BTN_OFFSET_Y = 0;

    // Home toolbar button visibility
    public static final String PREF_HOME_BTN_STATS_VISIBLE       = "home_btn_stats_visible";
    public static final String PREF_HOME_BTN_LIBRARY_VISIBLE     = "home_btn_library_visible";
    public static final String PREF_HOME_BTN_TOGGLE_VIEW_VISIBLE = "home_btn_toggle_view_visible";

    // Button order preferences (comma-separated IDs within each bar)
    public static final String PREF_TOP_BTN_ORDER    = "top_btn_order";
    public static final String PREF_CENTER_BTN_ORDER = "center_btn_order";

    // Default button orders
    public static final String DEFAULT_TOP_BTN_ORDER =
            "lock,play-mode,playlist-panel,speed,subtitle-toggle,subtitle,subtitle-list,rotate,settings";
    public static final String DEFAULT_CENTER_BTN_ORDER = "rewind,play-pause,forward";

    public static final String DEFAULT_BTN_COLOR         = "white";
    public static final String DEFAULT_PLAYLIST_BTN_COLOR = "accent";

    public static final String VIEW_MODE_GRID = "GRID";
    public static final String VIEW_MODE_LIST = "LIST";

    public static final int DEFAULT_GRID_SPAN_COUNT = 2;
    /** 方格目标宽度（dp），用于计算列数。值越小方格越小、列数越多。 */
    public static final int DEFAULT_GRID_CELL_WIDTH_DP = 140;
    public static final int MIN_GRID_CELL_WIDTH_DP   = 80;
    public static final int MAX_GRID_CELL_WIDTH_DP   = 220;

    public static final float DEFAULT_SUBTITLE_SIZE    = 18f;
    public static final float DEFAULT_SUBTITLE_LINE_SP = 1.2f;

    public static final int DEFAULT_SEEK_ICON_SIZE         = 64;
    public static final int DEFAULT_SEEK_SECONDS           = 5;
    public static final int DEFAULT_SEEK_ALPHA             = 100;
    public static final int DEFAULT_SEEKBAR_HEIGHT         = 12;
    public static final int DEFAULT_SEEKBAR_PROGRESS_ALPHA = 80;
    public static final int DEFAULT_SUBTITLE_PANEL_ALPHA   = 60;

    public static final String PREF_SETTINGS_BG_ALPHA      = "settings_bg_alpha";
    public static final int    DEFAULT_SETTINGS_BG_ALPHA    = 100;

    static final String DEFAULT_TAB_ORDER = "builtin,playlist";

    // Home shortcut buttons (top row on main page)
    public static final String PREF_HOME_SHORTCUT_ORDER   = "home_shortcut_order";
    public static final String DEFAULT_HOME_SHORTCUT_ORDER = "library,stats,toggle_view,settings";

    /** 将旧的列数偏好迁移为方格宽度（dp）。2列≈180dp，3列≈120dp，4列≈90dp */
    private static int migrateGridSpanToCellWidth(int span) {
        if (span <= 2) return 180;
        if (span == 3) return 120;
        return 90;
    }

    // Available button color options
    private static final String[] COLOR_VALUES = {"white","accent","orange","cyan","green","yellow"};
    private static final int[]    COLOR_INT    = {0xB3FFFFFF,0xFFE94560,0xFFFF9800,0xFF00BCD4,0xFF4CAF50,0xFFFFEB3B};
    private static final String[] COLOR_LABELS = {"白","红","橙","青","绿","黄"};

    private String[] tabOrder;
    private String[] shortcutOrder;
    private String[] topBtnOrder;
    private String[] centerBtnOrder;

    // Tracks currently selected color per control row (index = control index)
    private String[] selectedColors;

    // SharedPreferences instance (instance field so all methods can access it)
    private SharedPreferences prefs;
    // Snapshot of preferences as they were when settings was opened (for Restore)
    private Map<String, Object> originalPrefs;
    // Control-to-pref mappings needed across methods
    private String[] ctrlPrefsColor;
    private String[] ctrlPrefsVis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Snapshot the state on entry — used by the Restore button
        originalPrefs = new HashMap<>(prefs.getAll());
        String savedOrderStr  = prefs.getString(PREF_TAB_ORDER, DEFAULT_TAB_ORDER);
        // Migration: extract "stats" and "library" from tab order into shortcut order
        if (savedOrderStr.contains("stats") || savedOrderStr.contains("library")) {
            String[] tokens = savedOrderStr.split(",");
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
            savedOrderStr = tabSb.length() > 0 ? tabSb.toString() : DEFAULT_TAB_ORDER;
            String shortcutStr = shortcutSb.length() > 0 ? shortcutSb.toString() : DEFAULT_HOME_SHORTCUT_ORDER;
            // Only write shortcut order if not already migrated
            if (!prefs.contains(PREF_HOME_SHORTCUT_ORDER)) {
                prefs.edit().putString(PREF_HOME_SHORTCUT_ORDER, shortcutStr).apply();
            }
            prefs.edit().putString(PREF_TAB_ORDER, savedOrderStr).apply();
        }
        String savedDefault   = prefs.getString(PREF_DEFAULT_TAB, "builtin");
        String savedPortrait  = prefs.getString(PREF_PORTRAIT_SWIPE, "VERTICAL");
        String savedLandscape = prefs.getString(PREF_LANDSCAPE_SWIPE, "HORIZONTAL");
        String savedPanelDirPortrait  = prefs.getString(PREF_PANEL_DIR_PORTRAIT,  PANEL_DIR_RIGHT);
        String savedPanelDirLandscape = prefs.getString(PREF_PANEL_DIR_LANDSCAPE, PANEL_DIR_RIGHT);
        String savedViewMode  = prefs.getString(PREF_VIEW_MODE, VIEW_MODE_GRID);
        int    savedGridSpan  = prefs.getInt(PREF_GRID_SPAN_COUNT, DEFAULT_GRID_SPAN_COUNT);
        int    legacyWidth    = prefs.getInt(PREF_GRID_CELL_WIDTH_DP, -1);
        if (legacyWidth < 0) legacyWidth = migrateGridSpanToCellWidth(savedGridSpan);
        legacyWidth = Math.max(MIN_GRID_CELL_WIDTH_DP, Math.min(MAX_GRID_CELL_WIDTH_DP, legacyWidth));
        int savedGridLibrary  = prefs.getInt(PREF_GRID_CELL_WIDTH_LIBRARY, -1);
        int savedGridBuiltin   = prefs.getInt(PREF_GRID_CELL_WIDTH_BUILTIN, -1);
        int savedGridPlaylist  = prefs.getInt(PREF_GRID_CELL_WIDTH_PLAYLIST, -1);
        if (savedGridLibrary < 0) { savedGridLibrary = legacyWidth; prefs.edit().putInt(PREF_GRID_CELL_WIDTH_LIBRARY, savedGridLibrary).apply(); }
        if (savedGridBuiltin < 0) { savedGridBuiltin  = legacyWidth; prefs.edit().putInt(PREF_GRID_CELL_WIDTH_BUILTIN, savedGridBuiltin).apply(); }
        int savedGridPlaylistItems = prefs.getInt(PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS, -1);
        if (savedGridPlaylist < 0) { savedGridPlaylist = legacyWidth; prefs.edit().putInt(PREF_GRID_CELL_WIDTH_PLAYLIST, savedGridPlaylist).apply(); }
        if (savedGridPlaylistItems < 0) { savedGridPlaylistItems = savedGridPlaylist; prefs.edit().putInt(PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS, savedGridPlaylistItems).apply(); }
        savedGridLibrary  = Math.max(MIN_GRID_CELL_WIDTH_DP, Math.min(MAX_GRID_CELL_WIDTH_DP, savedGridLibrary));
        savedGridBuiltin  = Math.max(MIN_GRID_CELL_WIDTH_DP, Math.min(MAX_GRID_CELL_WIDTH_DP, savedGridBuiltin));
        savedGridPlaylist  = Math.max(MIN_GRID_CELL_WIDTH_DP, Math.min(MAX_GRID_CELL_WIDTH_DP, savedGridPlaylist));
        savedGridPlaylistItems = Math.max(MIN_GRID_CELL_WIDTH_DP, Math.min(MAX_GRID_CELL_WIDTH_DP, savedGridPlaylistItems));
        float  savedSubSize   = prefs.getFloat(PREF_SUBTITLE_SIZE, DEFAULT_SUBTITLE_SIZE);
        float  savedLineSp    = prefs.getFloat(PREF_SUBTITLE_LINE_SP, DEFAULT_SUBTITLE_LINE_SP);
        int    savedSeekIconSize   = prefs.getInt(PREF_SEEK_ICON_SIZE,         DEFAULT_SEEK_ICON_SIZE);
        int    savedSeekSeconds    = prefs.getInt(PREF_SEEK_SECONDS,           DEFAULT_SEEK_SECONDS);
        int    savedSeekAlpha      = prefs.getInt(PREF_SEEK_ALPHA,             DEFAULT_SEEK_ALPHA);
        int    savedSeekOffset     = prefs.getInt(PREF_SEEK_OFFSET_Y,          DEFAULT_SEEK_OFFSET_Y);
        int    savedSeekbarHeight  = prefs.getInt(PREF_SEEKBAR_HEIGHT,         DEFAULT_SEEKBAR_HEIGHT);
        int    savedProgAlpha      = prefs.getInt(PREF_SEEKBAR_PROGRESS_ALPHA, DEFAULT_SEEKBAR_PROGRESS_ALPHA);
        int    savedPanelAlpha     = prefs.getInt(PREF_SUBTITLE_PANEL_ALPHA,   DEFAULT_SUBTITLE_PANEL_ALPHA);
        boolean savedSubDefaultVisible = prefs.getBoolean(PREF_SUBTITLE_DEFAULT_VISIBLE, true);
        int    savedPlaylistPanelBgAlpha = prefs.getInt(PREF_PLAYLIST_PANEL_BG_ALPHA, DEFAULT_PLAYLIST_PANEL_BG_ALPHA);
        int    savedPlaylistPanelWidth   = prefs.getInt(PREF_PLAYLIST_PANEL_WIDTH_DP,  DEFAULT_PLAYLIST_PANEL_WIDTH_DP);
        int    savedPlaylistPanelHeight  = prefs.getInt(PREF_PLAYLIST_PANEL_HEIGHT_DP, DEFAULT_PLAYLIST_PANEL_HEIGHT_DP);
        String savedPlaylistPanelOrientation = prefs.getString(PREF_PLAYLIST_PANEL_ORIENTATION, PLAYLIST_PANEL_ORIENTATION_VERTICAL);

        tabOrder       = savedOrderStr.split(",");
        shortcutOrder  = migrateShortcutOrder(
                prefs.getString(PREF_HOME_SHORTCUT_ORDER, DEFAULT_HOME_SHORTCUT_ORDER));
        topBtnOrder    = migrateTopBtnOrder(
                prefs.getString(PREF_TOP_BTN_ORDER, DEFAULT_TOP_BTN_ORDER));
        centerBtnOrder = prefs.getString(PREF_CENTER_BTN_ORDER, DEFAULT_CENTER_BTN_ORDER).split(",");

        // ── 默认主界面/底部标签/工具栏 标签切换 ─────────────────────────────────
        {
            View sectionDefaultHome  = findViewById(R.id.sectionDefaultHome);
            View sectionBottomOrder = findViewById(R.id.sectionBottomOrder);
            View sectionToolbarOrder = findViewById(R.id.sectionToolbarOrder);

            TextView tabDefaultHome  = findViewById(R.id.tabDefaultHome);
            TextView tabBottomOrder  = findViewById(R.id.tabBottomOrder);
            TextView tabToolbarOrder = findViewById(R.id.tabToolbarOrder);

            View.OnClickListener navTabToggle = v -> {
                View section;
                if (v == tabDefaultHome)   section = sectionDefaultHome;
                else if (v == tabBottomOrder)  section = sectionBottomOrder;
                else                           section = sectionToolbarOrder;

                boolean wasVisible = section.getVisibility() == View.VISIBLE;
                section.setVisibility(wasVisible ? View.GONE : View.VISIBLE);

                int activeColor = getResources().getColor(R.color.colorAccent, null);
                int normalColor = 0x00000000;
                ((TextView) v).setBackgroundColor(wasVisible ? normalColor : activeColor);
            };
            tabDefaultHome.setOnClickListener(navTabToggle);
            tabBottomOrder.setOnClickListener(navTabToggle);
            tabToolbarOrder.setOnClickListener(navTabToggle);
        }

        // ── Default tab RadioGroup ────────────────────────────────────────────
        RadioGroup rgDefault = findViewById(R.id.rgDefaultTab);
        String[] tabValues = { "builtin", "library", "playlist" };
        String[] tabLabels = {
            getString(R.string.tab_builtin),
            getString(R.string.tab_library),
            getString(R.string.tab_playlist)
        };
        for (int i = 0; i < tabValues.length; i++) {
            RadioButton rb = makeRadioButton(tabLabels[i]);
            rb.setChecked(tabValues[i].equals(savedDefault));
            RadioGroup.LayoutParams rlp = new RadioGroup.LayoutParams(
                    0, RadioGroup.LayoutParams.WRAP_CONTENT, 1f);
            rgDefault.addView(rb, rlp);
        }
        rgDefault.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < tabValues.length; i++) {
                RadioButton rb = (RadioButton) group.getChildAt(i);
                if (rb != null && rb.isChecked()) {
                    prefs.edit().putString(PREF_DEFAULT_TAB, tabValues[i]).apply();
                    break;
                }
            }
        });

        // ── 内置媒体/我的列表 有无 ─────────────────────────────────────────────
        Switch switchBuiltinVisible  = findViewById(R.id.switchBuiltinVisible);
        Switch switchPlaylistVisible = findViewById(R.id.switchPlaylistVisible);
        switchBuiltinVisible.setChecked(prefs.getBoolean(PREF_BUILTIN_VISIBLE, true));
        switchPlaylistVisible.setChecked(prefs.getBoolean(PREF_PLAYLIST_VISIBLE, true));
        switchBuiltinVisible.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(PREF_BUILTIN_VISIBLE, checked).apply());
        switchPlaylistVisible.setOnCheckedChangeListener((btn, checked) ->
                prefs.edit().putBoolean(PREF_PLAYLIST_VISIBLE, checked).apply());

        // ── Tab order ─────────────────────────────────────────────────────────
        LinearLayout tabOrderContainer = findViewById(R.id.tabOrderContainer);
        refreshTabOrderRows(tabOrderContainer);

        // ── Shortcut order ───────────────────────────────────────────────────
        LinearLayout shortcutOrderContainer = findViewById(R.id.shortcutOrderContainer);
        refreshShortcutOrderRows(shortcutOrderContainer);

        // ── 列表/手势/面板 标签切换 ───────────────────────────────────────────
        {
            View sectionViewMode       = findViewById(R.id.sectionViewMode);
            View sectionPortraitSwipe   = findViewById(R.id.sectionPortraitSwipe);
            View sectionLandscapeSwipe  = findViewById(R.id.sectionLandscapeSwipe);
            View sectionPanelPortrait  = findViewById(R.id.sectionPanelPortrait);
            View sectionPanelLandscape = findViewById(R.id.sectionPanelLandscape);
            View sectionPlaylistPanel  = findViewById(R.id.sectionPlaylistPanel);

            TextView tabViewMode       = findViewById(R.id.tabViewMode);
            TextView tabPortraitSwipe  = findViewById(R.id.tabPortraitSwipe);
            TextView tabLandscapeSwipe  = findViewById(R.id.tabLandscapeSwipe);
            TextView tabPanelPortrait  = findViewById(R.id.tabPanelPortrait);
            TextView tabPanelLandscape = findViewById(R.id.tabPanelLandscape);
            TextView tabPlaylistPanel  = findViewById(R.id.tabPlaylistPanel);

            View.OnClickListener generalTabToggle = v -> {
                View section;
                if (v == tabViewMode)        section = sectionViewMode;
                else if (v == tabPortraitSwipe)  section = sectionPortraitSwipe;
                else if (v == tabLandscapeSwipe) section = sectionLandscapeSwipe;
                else if (v == tabPanelPortrait)  section = sectionPanelPortrait;
                else if (v == tabPanelLandscape) section = sectionPanelLandscape;
                else                             section = sectionPlaylistPanel;

                boolean wasVisible = section.getVisibility() == View.VISIBLE;
                section.setVisibility(wasVisible ? View.GONE : View.VISIBLE);

                int activeColor = getResources().getColor(R.color.colorAccent, null);
                int normalColor = 0x00000000;
                ((TextView) v).setBackgroundColor(wasVisible ? normalColor : activeColor);
            };
            tabViewMode.setOnClickListener(generalTabToggle);
            tabPortraitSwipe.setOnClickListener(generalTabToggle);
            tabLandscapeSwipe.setOnClickListener(generalTabToggle);
            tabPanelPortrait.setOnClickListener(generalTabToggle);
            tabPanelLandscape.setOnClickListener(generalTabToggle);
            tabPlaylistPanel.setOnClickListener(generalTabToggle);
        }

        // ── View mode ─────────────────────────────────────────────────────────
        RadioButton rbGrid = findViewById(R.id.rbViewGrid);
        RadioButton rbList = findViewById(R.id.rbViewList);
        rbGrid.setChecked(VIEW_MODE_GRID.equals(savedViewMode));
        rbList.setChecked(VIEW_MODE_LIST.equals(savedViewMode));
        ((RadioGroup) findViewById(R.id.rgViewMode)).setOnCheckedChangeListener((g, id) ->
                prefs.edit().putString(PREF_VIEW_MODE,
                        id == R.id.rbViewGrid ? VIEW_MODE_GRID : VIEW_MODE_LIST).apply());

        // ── 方格大小（媒体库 / 内置媒体 / 我的列表 分别调节）────────────────────
        setupGridCellSeekBar(R.id.sbGridCellWidthLibrary, R.id.tvGridCellWidthLibrary,
                PREF_GRID_CELL_WIDTH_LIBRARY, savedGridLibrary, R.string.settings_grid_width_library);
        setupGridCellSeekBar(R.id.sbGridCellWidthBuiltin, R.id.tvGridCellWidthBuiltin,
                PREF_GRID_CELL_WIDTH_BUILTIN, savedGridBuiltin, R.string.settings_grid_width_builtin);
        setupGridCellSeekBar(R.id.sbGridCellWidthPlaylist, R.id.tvGridCellWidthPlaylist,
                PREF_GRID_CELL_WIDTH_PLAYLIST, savedGridPlaylist, R.string.settings_grid_width_playlist);
        setupGridCellSeekBar(R.id.sbGridCellWidthPlaylistItems, R.id.tvGridCellWidthPlaylistItems,
                PREF_GRID_CELL_WIDTH_PLAYLIST_ITEMS, savedGridPlaylistItems, R.string.settings_grid_width_playlist_items);

        // ── Portrait swipe ────────────────────────────────────────────────────
        RadioButton rbPortraitV = findViewById(R.id.rbPortraitVertical);
        RadioButton rbPortraitH = findViewById(R.id.rbPortraitHorizontal);
        rbPortraitV.setChecked("VERTICAL".equals(savedPortrait));
        rbPortraitH.setChecked("HORIZONTAL".equals(savedPortrait));
        ((RadioGroup) findViewById(R.id.rgPortraitSwipe)).setOnCheckedChangeListener((g, id) ->
                prefs.edit().putString(PREF_PORTRAIT_SWIPE,
                        id == R.id.rbPortraitHorizontal ? "HORIZONTAL" : "VERTICAL").apply());

        // ── Landscape swipe ───────────────────────────────────────────────────
        RadioButton rbLandH = findViewById(R.id.rbLandscapeHorizontal);
        RadioButton rbLandV = findViewById(R.id.rbLandscapeVertical);
        rbLandH.setChecked("HORIZONTAL".equals(savedLandscape));
        rbLandV.setChecked("VERTICAL".equals(savedLandscape));
        ((RadioGroup) findViewById(R.id.rgLandscapeSwipe)).setOnCheckedChangeListener((g, id) ->
                prefs.edit().putString(PREF_LANDSCAPE_SWIPE,
                        id == R.id.rbLandscapeVertical ? "VERTICAL" : "HORIZONTAL").apply());

        // ── 播放列表面板方向（竖屏） ────────────────────────────────────────────
        setupPanelDirGroup(
                new RadioButton[]{
                        findViewById(R.id.rbPanelDirPortraitTop),
                        findViewById(R.id.rbPanelDirPortraitBottom),
                        findViewById(R.id.rbPanelDirPortraitLeft),
                        findViewById(R.id.rbPanelDirPortraitRight)},
                new String[]{PANEL_DIR_TOP, PANEL_DIR_BOTTOM, PANEL_DIR_LEFT, PANEL_DIR_RIGHT},
                savedPanelDirPortrait, PREF_PANEL_DIR_PORTRAIT);

        setupPanelDirGroup(
                new RadioButton[]{
                        findViewById(R.id.rbPanelDirLandscapeTop),
                        findViewById(R.id.rbPanelDirLandscapeBottom),
                        findViewById(R.id.rbPanelDirLandscapeLeft),
                        findViewById(R.id.rbPanelDirLandscapeRight)},
                new String[]{PANEL_DIR_TOP, PANEL_DIR_BOTTOM, PANEL_DIR_LEFT, PANEL_DIR_RIGHT},
                savedPanelDirLandscape, PREF_PANEL_DIR_LANDSCAPE);

        // ── 播放列表面板（背景透明度、宽高、排列方向）────────────────────────────
        savedPlaylistPanelBgAlpha = Math.max(0, Math.min(100, savedPlaylistPanelBgAlpha));
        savedPlaylistPanelWidth   = Math.max(200, Math.min(450, savedPlaylistPanelWidth));
        savedPlaylistPanelHeight  = Math.max(200, Math.min(550, savedPlaylistPanelHeight));
        SeekBar sbPlaylistBgAlpha = findViewById(R.id.sbPlaylistPanelBgAlpha);
        TextView tvPlaylistBgAlphaLabel = findViewById(R.id.tvPlaylistPanelBgAlphaLabel);
        sbPlaylistBgAlpha.setMax(100);
        sbPlaylistBgAlpha.setProgress(savedPlaylistPanelBgAlpha);
        tvPlaylistBgAlphaLabel.setText(getString(R.string.settings_playlist_panel_bg_alpha, savedPlaylistPanelBgAlpha));
        sbPlaylistBgAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvPlaylistBgAlphaLabel.setText(getString(R.string.settings_playlist_panel_bg_alpha, p));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_PLAYLIST_PANEL_BG_ALPHA, sb.getProgress()).apply();
            }
        });
        SeekBar sbPlaylistWidth = findViewById(R.id.sbPlaylistPanelWidth);
        TextView tvPlaylistWidthLabel = findViewById(R.id.tvPlaylistPanelWidthLabel);
        sbPlaylistWidth.setMax(250);
        sbPlaylistWidth.setProgress(savedPlaylistPanelWidth - 200);
        tvPlaylistWidthLabel.setText(getString(R.string.settings_playlist_panel_width, savedPlaylistPanelWidth));
        sbPlaylistWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvPlaylistWidthLabel.setText(getString(R.string.settings_playlist_panel_width, 200 + p));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_PLAYLIST_PANEL_WIDTH_DP, 200 + sb.getProgress()).apply();
            }
        });
        SeekBar sbPlaylistHeight = findViewById(R.id.sbPlaylistPanelHeight);
        TextView tvPlaylistHeightLabel = findViewById(R.id.tvPlaylistPanelHeightLabel);
        sbPlaylistHeight.setMax(350);
        sbPlaylistHeight.setProgress(savedPlaylistPanelHeight - 200);
        tvPlaylistHeightLabel.setText(getString(R.string.settings_playlist_panel_height, savedPlaylistPanelHeight));
        sbPlaylistHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvPlaylistHeightLabel.setText(getString(R.string.settings_playlist_panel_height, 200 + p));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_PLAYLIST_PANEL_HEIGHT_DP, 200 + sb.getProgress()).apply();
            }
        });
        RadioButton rbPlaylistVertical   = findViewById(R.id.rbPlaylistPanelVertical);
        RadioButton rbPlaylistHorizontal  = findViewById(R.id.rbPlaylistPanelHorizontal);
        rbPlaylistVertical.setChecked(PLAYLIST_PANEL_ORIENTATION_VERTICAL.equals(savedPlaylistPanelOrientation));
        rbPlaylistHorizontal.setChecked(PLAYLIST_PANEL_ORIENTATION_HORIZONTAL.equals(savedPlaylistPanelOrientation));
        ((RadioGroup) findViewById(R.id.rgPlaylistPanelOrientation)).setOnCheckedChangeListener((g, id) ->
                prefs.edit().putString(PREF_PLAYLIST_PANEL_ORIENTATION,
                        id == R.id.rbPlaylistPanelHorizontal ? PLAYLIST_PANEL_ORIENTATION_HORIZONTAL : PLAYLIST_PANEL_ORIENTATION_VERTICAL).apply());

        // ── 字幕大小 ───────────────────────────────────────────────────────────
        SeekBar sbSubSize = findViewById(R.id.sbSubtitleSize);
        TextView tvSubSizeLabel = findViewById(R.id.tvSubtitleSizeLabel);
        sbSubSize.setMax(30);
        int initSubSizeProgress = Math.round(savedSubSize - 10f);
        sbSubSize.setProgress(Math.max(0, Math.min(30, initSubSizeProgress)));
        tvSubSizeLabel.setText(getString(R.string.settings_subtitle_size, (int) savedSubSize));
        sbSubSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvSubSizeLabel.setText(getString(R.string.settings_subtitle_size, p + 10));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putFloat(PREF_SUBTITLE_SIZE, sb.getProgress() + 10f).apply();
            }
        });

        // ── 字幕行间距 ─────────────────────────────────────────────────────────
        SeekBar sbLineSp = findViewById(R.id.sbSubtitleLineSpacing);
        TextView tvLineSpLabel = findViewById(R.id.tvSubtitleLineSpacingLabel);
        sbLineSp.setMax(20);
        int initLineSpProgress = Math.round((savedLineSp - 1.0f) * 10f);
        sbLineSp.setProgress(Math.max(0, Math.min(20, initLineSpProgress)));
        tvLineSpLabel.setText(getString(R.string.settings_subtitle_line_spacing,
                String.format("%.1f", savedLineSp)));
        sbLineSp.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                float val = 1.0f + p * 0.1f;
                tvLineSpLabel.setText(getString(R.string.settings_subtitle_line_spacing,
                        String.format("%.1f", val)));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putFloat(PREF_SUBTITLE_LINE_SP, 1.0f + sb.getProgress() * 0.1f).apply();
            }
        });

        // ── 按钮调试区标签切换 ─────────────────────────────────────────────────
        {
            View sectionSeek      = findViewById(R.id.sectionSeek);
            View sectionSkip      = findViewById(R.id.sectionSkip);
            View sectionPlayPause = findViewById(R.id.sectionPlayPause);
            View sectionProgress  = findViewById(R.id.sectionProgress);

            TextView tabSeek      = findViewById(R.id.tabSeek);
            TextView tabSkip      = findViewById(R.id.tabSkip);
            TextView tabPlayPause = findViewById(R.id.tabPlayPause);
            TextView tabProgress  = findViewById(R.id.tabProgress);

            View.OnClickListener tabToggle = v -> {
                View section;
                if (v == tabSeek)           section = sectionSeek;
                else if (v == tabSkip)      section = sectionSkip;
                else if (v == tabPlayPause) section = sectionPlayPause;
                else                        section = sectionProgress;

                boolean wasVisible = section.getVisibility() == View.VISIBLE;
                section.setVisibility(wasVisible ? View.GONE : View.VISIBLE);

                int activeColor = getResources().getColor(R.color.colorAccent, null);
                int normalColor = 0x00000000;
                ((TextView) v).setBackgroundColor(wasVisible ? normalColor : activeColor);
            };
            tabSeek.setOnClickListener(tabToggle);
            tabSkip.setOnClickListener(tabToggle);
            tabPlayPause.setOnClickListener(tabToggle);
            tabProgress.setOnClickListener(tabToggle);
        }

        // ── 跳转按钮大小 ──────────────────────────────────────────────────────
        SeekBar sbSeekIconSize = findViewById(R.id.sbSeekIconSize);
        TextView tvSeekIconSizeLabel = findViewById(R.id.tvSeekIconSizeLabel);
        sbSeekIconSize.setMax(128); // 32-160 dp
        sbSeekIconSize.setProgress(Math.max(0, Math.min(128, savedSeekIconSize - 32)));
        tvSeekIconSizeLabel.setText(getString(R.string.settings_seek_icon_size, savedSeekIconSize));
        sbSeekIconSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvSeekIconSizeLabel.setText(getString(R.string.settings_seek_icon_size, p + 32));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_SEEK_ICON_SIZE, sb.getProgress() + 32).apply();
            }
        });

        // ── 跳转时长 ───────────────────────────────────────────────────────
        SeekBar sbSeekSeconds = findViewById(R.id.sbSeekSeconds);
        TextView tvSeekSecondsLabel = findViewById(R.id.tvSeekSecondsLabel);
        sbSeekSeconds.setMax(59);
        sbSeekSeconds.setProgress(Math.max(0, Math.min(59, savedSeekSeconds - 1)));
        tvSeekSecondsLabel.setText(getString(R.string.settings_seek_seconds, savedSeekSeconds));
        sbSeekSeconds.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvSeekSecondsLabel.setText(getString(R.string.settings_seek_seconds, p + 1));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_SEEK_SECONDS, sb.getProgress() + 1).apply();
            }
        });

        // ── 跳转按钮透明度 ────────────────────────────────────────────────
        SeekBar sbSeekAlpha = findViewById(R.id.sbSeekAlpha);
        TextView tvSeekAlphaLabel = findViewById(R.id.tvSeekAlphaLabel);
        sbSeekAlpha.setMax(90);
        sbSeekAlpha.setProgress(Math.max(0, Math.min(90, savedSeekAlpha - 10)));
        tvSeekAlphaLabel.setText(getString(R.string.settings_seek_alpha, savedSeekAlpha));
        sbSeekAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvSeekAlphaLabel.setText(getString(R.string.settings_seek_alpha, p + 10));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_SEEK_ALPHA, sb.getProgress() + 10).apply();
            }
        });

        // ── 跳转按钮上下偏移（±800 dp，覆盖全屏任意位置）─────────────────────────────
        SeekBar sbSeekOffset = findViewById(R.id.sbSeekOffset);
        TextView tvSeekOffsetLabel = findViewById(R.id.tvSeekOffsetLabel);
        sbSeekOffset.setMax(1600); // -800 to +800 dp，全屏可移
        sbSeekOffset.setProgress(Math.max(0, Math.min(1600, savedSeekOffset + 800)));
        tvSeekOffsetLabel.setText(getString(R.string.settings_seek_offset, savedSeekOffset));
        sbSeekOffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvSeekOffsetLabel.setText(getString(R.string.settings_seek_offset, p - 800));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_SEEK_OFFSET_Y, sb.getProgress() - 800).apply();
            }
        });

        // ── 切换按钮大小 ──────────────────────────────────────────────────────
        {
            int savedSkipBtnSize   = prefs.getInt(PREF_SKIP_BTN_SIZE,      DEFAULT_SKIP_BTN_SIZE);
            int savedSkipBtnAlpha  = prefs.getInt(PREF_SKIP_BTN_ALPHA,     DEFAULT_SKIP_BTN_ALPHA);
            int savedSkipBtnOffset = prefs.getInt(PREF_SKIP_BTN_OFFSET_Y,  DEFAULT_SKIP_BTN_OFFSET_Y);

            SeekBar sbSkipSize = findViewById(R.id.sbSkipSize);
            TextView tvSkipSizeLabel = findViewById(R.id.tvSkipSizeLabel);
            sbSkipSize.setProgress(Math.max(0, Math.min(128, savedSkipBtnSize - 32)));
            tvSkipSizeLabel.setText(getString(R.string.settings_skip_btn_size, savedSkipBtnSize));
            sbSkipSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                    tvSkipSizeLabel.setText(getString(R.string.settings_skip_btn_size, p + 32));
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {
                    prefs.edit().putInt(PREF_SKIP_BTN_SIZE, sb.getProgress() + 32).apply();
                }
            });

            SeekBar sbSkipAlpha = findViewById(R.id.sbSkipAlpha);
            TextView tvSkipAlphaLabel = findViewById(R.id.tvSkipAlphaLabel);
            sbSkipAlpha.setProgress(Math.max(0, Math.min(90, savedSkipBtnAlpha - 10)));
            tvSkipAlphaLabel.setText(getString(R.string.settings_skip_btn_alpha, savedSkipBtnAlpha));
            sbSkipAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                    tvSkipAlphaLabel.setText(getString(R.string.settings_skip_btn_alpha, p + 10));
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {
                    prefs.edit().putInt(PREF_SKIP_BTN_ALPHA, sb.getProgress() + 10).apply();
                }
            });

            SeekBar sbSkipOffset = findViewById(R.id.sbSkipOffset);
            TextView tvSkipOffsetLabel = findViewById(R.id.tvSkipOffsetLabel);
            sbSkipOffset.setMax(1600); // -800 to +800 dp，全屏可移
            sbSkipOffset.setProgress(Math.max(0, Math.min(1600, savedSkipBtnOffset + 800)));
            tvSkipOffsetLabel.setText(getString(R.string.settings_skip_btn_offset, savedSkipBtnOffset));
            sbSkipOffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                    tvSkipOffsetLabel.setText(getString(R.string.settings_skip_btn_offset, p - 800));
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {
                    prefs.edit().putInt(PREF_SKIP_BTN_OFFSET_Y, sb.getProgress() - 800).apply();
                }
            });
        }

        // ── 暂停按钮大小/透明度/偏移 ──────────────────────────────────────
        {
            int savedPPSize   = prefs.getInt(PREF_PLAYPAUSE_BTN_SIZE,     DEFAULT_PLAYPAUSE_BTN_SIZE);
            int savedPPAlpha  = prefs.getInt(PREF_PLAYPAUSE_BTN_ALPHA,    DEFAULT_PLAYPAUSE_BTN_ALPHA);
            int savedPPOffset = prefs.getInt(PREF_PLAYPAUSE_BTN_OFFSET_Y, DEFAULT_PLAYPAUSE_BTN_OFFSET_Y);

            SeekBar sbPPSize = findViewById(R.id.sbPlayPauseSize);
            TextView tvPPSizeLabel = findViewById(R.id.tvPlayPauseSizeLabel);
            sbPPSize.setMax(128); // 32-160 dp
            sbPPSize.setProgress(Math.max(0, Math.min(128, savedPPSize - 32)));
            tvPPSizeLabel.setText(getString(R.string.settings_playpause_btn_size, savedPPSize));
            sbPPSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                    tvPPSizeLabel.setText(getString(R.string.settings_playpause_btn_size, p + 32));
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {
                    prefs.edit().putInt(PREF_PLAYPAUSE_BTN_SIZE, sb.getProgress() + 32).apply();
                }
            });

            SeekBar sbPPAlpha = findViewById(R.id.sbPlayPauseAlpha);
            TextView tvPPAlphaLabel = findViewById(R.id.tvPlayPauseAlphaLabel);
            sbPPAlpha.setMax(90); // 10-100%
            sbPPAlpha.setProgress(Math.max(0, Math.min(90, savedPPAlpha - 10)));
            tvPPAlphaLabel.setText(getString(R.string.settings_playpause_btn_alpha, savedPPAlpha));
            sbPPAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                    tvPPAlphaLabel.setText(getString(R.string.settings_playpause_btn_alpha, p + 10));
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {
                    prefs.edit().putInt(PREF_PLAYPAUSE_BTN_ALPHA, sb.getProgress() + 10).apply();
                }
            });

            SeekBar sbPPOffset = findViewById(R.id.sbPlayPauseOffset);
            TextView tvPPOffsetLabel = findViewById(R.id.tvPlayPauseOffsetLabel);
            sbPPOffset.setMax(300); // -150 to +150 dp
            sbPPOffset.setProgress(Math.max(0, Math.min(300, savedPPOffset + 150)));
            tvPPOffsetLabel.setText(getString(R.string.settings_playpause_btn_offset, savedPPOffset));
            sbPPOffset.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                    tvPPOffsetLabel.setText(getString(R.string.settings_playpause_btn_offset, p - 150));
                }
                @Override public void onStartTrackingTouch(SeekBar sb) {}
                @Override public void onStopTrackingTouch(SeekBar sb) {
                    prefs.edit().putInt(PREF_PLAYPAUSE_BTN_OFFSET_Y, sb.getProgress() - 150).apply();
                }
            });
        }

        // ── 进度条粗细 ────────────────────────────────────────────────────
        SeekBar sbSeekbarHeight = findViewById(R.id.sbSeekbarHeight);
        TextView tvSeekbarHeightLabel = findViewById(R.id.tvSeekbarHeightLabel);
        sbSeekbarHeight.setMax(16);
        sbSeekbarHeight.setProgress(Math.max(0, Math.min(16, savedSeekbarHeight - 4)));
        tvSeekbarHeightLabel.setText(getString(R.string.settings_seekbar_height, savedSeekbarHeight));
        sbSeekbarHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvSeekbarHeightLabel.setText(getString(R.string.settings_seekbar_height, p + 4));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_SEEKBAR_HEIGHT, sb.getProgress() + 4).apply();
            }
        });

        // ── 进度条已播放透明度 ─────────────────────────────────────────────
        SeekBar sbProgAlpha = findViewById(R.id.sbSeekbarProgressAlpha);
        TextView tvProgAlphaLabel = findViewById(R.id.tvSeekbarProgressAlphaLabel);
        sbProgAlpha.setMax(90);
        sbProgAlpha.setProgress(Math.max(0, Math.min(90, savedProgAlpha - 10)));
        tvProgAlphaLabel.setText(getString(R.string.settings_seekbar_progress_alpha, savedProgAlpha));
        sbProgAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvProgAlphaLabel.setText(getString(R.string.settings_seekbar_progress_alpha, p + 10));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_SEEKBAR_PROGRESS_ALPHA, sb.getProgress() + 10).apply();
            }
        });

        // ── 字幕面板/字幕默认/播放控件/设置页背景 标签切换 ───────────────────────
        {
            View sectionSubtitlePanel  = findViewById(R.id.sectionSubtitlePanel);
            View sectionSubtitleDefault = findViewById(R.id.sectionSubtitleDefault);
            View sectionPlayerControls  = findViewById(R.id.sectionPlayerControls);
            View sectionSettingsBg     = findViewById(R.id.sectionSettingsBg);

            TextView tabSubtitlePanel  = findViewById(R.id.tabSubtitlePanel);
            TextView tabSubtitleDefault = findViewById(R.id.tabSubtitleDefault);
            TextView tabPlayerControls = findViewById(R.id.tabPlayerControls);
            TextView tabSettingsBg     = findViewById(R.id.tabSettingsBg);

            View.OnClickListener miscTabToggle = v -> {
                View section;
                if (v == tabSubtitlePanel)   section = sectionSubtitlePanel;
                else if (v == tabSubtitleDefault) section = sectionSubtitleDefault;
                else if (v == tabPlayerControls)  section = sectionPlayerControls;
                else                             section = sectionSettingsBg;

                boolean wasVisible = section.getVisibility() == View.VISIBLE;
                section.setVisibility(wasVisible ? View.GONE : View.VISIBLE);

                int activeColor = getResources().getColor(R.color.colorAccent, null);
                int normalColor = 0x00000000;
                ((TextView) v).setBackgroundColor(wasVisible ? normalColor : activeColor);
            };
            tabSubtitlePanel.setOnClickListener(miscTabToggle);
            tabSubtitleDefault.setOnClickListener(miscTabToggle);
            tabPlayerControls.setOnClickListener(miscTabToggle);
            tabSettingsBg.setOnClickListener(miscTabToggle);
        }

        // ── 字幕面板背景透明度 ─────────────────────────────────────────────
        SeekBar sbPanelAlpha = findViewById(R.id.sbSubtitlePanelAlpha);
        TextView tvPanelAlphaLabel = findViewById(R.id.tvSubtitlePanelAlphaLabel);
        sbPanelAlpha.setMax(90);
        sbPanelAlpha.setProgress(Math.max(0, Math.min(90, savedPanelAlpha)));
        tvPanelAlphaLabel.setText(getString(R.string.settings_subtitle_panel_alpha, savedPanelAlpha));
        sbPanelAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvPanelAlphaLabel.setText(getString(R.string.settings_subtitle_panel_alpha, p));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_SUBTITLE_PANEL_ALPHA, sb.getProgress()).apply();
            }
        });

        // ── 字幕默认显示 ──────────────────────────────────────────────────────
        Switch swSubDefaultVisible = findViewById(R.id.swSubtitleDefaultVisible);
        if (swSubDefaultVisible != null) {
            swSubDefaultVisible.setChecked(savedSubDefaultVisible);
            swSubDefaultVisible.setOnCheckedChangeListener((b, checked) ->
                    prefs.edit().putBoolean(PREF_SUBTITLE_DEFAULT_VISIBLE, checked).apply());
        }

        // ── 播放控件 visibility + color (popup color picker) ─────────────────
        LinearLayout controlsContainer = findViewById(R.id.playerControlsContainer);
        ctrlPrefsVis   = new String[]{
                PREF_BTN_LOCK_VISIBLE, PREF_BTN_PLAYMODE_VISIBLE,
                PREF_BTN_PLAYLIST_VISIBLE, PREF_BTN_SPEED_VISIBLE,
                PREF_BTN_SUBTITLE_TOGGLE_VISIBLE, PREF_BTN_SUBTITLE_VISIBLE,
                PREF_BTN_SUBTLIST_VISIBLE, PREF_BTN_ROTATE_VISIBLE,
                PREF_BTN_SEEK_VISIBLE, PREF_BTN_PLAYPAUSE_VISIBLE,
                PREF_BTN_SKIP_VISIBLE, PREF_BTN_PLAYER_SETTINGS_VISIBLE };
        ctrlPrefsColor = new String[]{
                PREF_BTN_LOCK_COLOR, PREF_BTN_PLAYMODE_COLOR,
                PREF_BTN_PLAYLIST_COLOR, PREF_BTN_SPEED_COLOR,
                PREF_BTN_SUBTITLE_TOGGLE_COLOR, PREF_BTN_SUBTITLE_COLOR,
                PREF_BTN_SUBTLIST_COLOR, PREF_BTN_ROTATE_COLOR,
                PREF_BTN_SEEK_COLOR, PREF_BTN_PLAYPAUSE_COLOR,
                PREF_BTN_SKIP_COLOR, PREF_BTN_PLAYER_SETTINGS_COLOR };
        String[] ctrlDefColor   = {
                DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR,
                DEFAULT_PLAYLIST_BTN_COLOR, DEFAULT_BTN_COLOR,
                DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR,
                DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR,
                DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR };
        String[] ctrlLabels     = {
                getString(R.string.settings_ctrl_lock),
                getString(R.string.settings_ctrl_playmode),
                getString(R.string.settings_ctrl_playlist),
                getString(R.string.settings_ctrl_speed),
                getString(R.string.settings_ctrl_subtitle_toggle),
                getString(R.string.settings_ctrl_subtitle),
                getString(R.string.settings_ctrl_subtlist),
                getString(R.string.settings_ctrl_rotate),
                getString(R.string.settings_ctrl_seek),
                getString(R.string.settings_ctrl_playpause),
                getString(R.string.settings_ctrl_skip),
                getString(R.string.settings_ctrl_player_settings)
        };

        int numControls = ctrlLabels.length;
        selectedColors = new String[numControls];
        Switch[] ctrlSwitches = new Switch[numControls];
        View[] colorSwatches = new View[numControls];

        int dp4 = dp(4);
        int dp8 = dp(8);
        LinearLayout pairRow = null;
        for (int i = 0; i < numControls; i++) {
            final int ci = i;
            String savedColor = prefs.getString(ctrlPrefsColor[i], ctrlDefColor[i]);
            selectedColors[i] = savedColor;

            LinearLayout cell = new LinearLayout(this);
            cell.setOrientation(LinearLayout.HORIZONTAL);
            cell.setGravity(android.view.Gravity.CENTER_VERTICAL);
            cell.setPadding(0, dp4, dp4, dp4);

            TextView tvLabel = new TextView(this);
            tvLabel.setText(ctrlLabels[i]);
            tvLabel.setTextColor(getResources().getColor(R.color.textPrimary, null));
            tvLabel.setTextSize(13f);
            tvLabel.setSingleLine(true);
            tvLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvLabel.setLayoutParams(lp);
            cell.addView(tvLabel);

            View swatch = new View(this);
            int swatchSize = dp(24);
            LinearLayout.LayoutParams swatchLp = new LinearLayout.LayoutParams(swatchSize, swatchSize);
            swatchLp.setMargins(dp4, 0, dp4, 0);
            swatch.setLayoutParams(swatchLp);
            updateSwatchColor(swatch, savedColor);
            swatch.setClickable(true);
            swatch.setFocusable(true);
            colorSwatches[i] = swatch;
            swatch.setOnClickListener(v -> showColorPickerDialog(ci, colorSwatches[ci]));
            cell.addView(swatch);

            if (ctrlPrefsVis[i] != null) {
                Switch sw = new Switch(this);
                sw.setChecked(prefs.getBoolean(ctrlPrefsVis[i], true));
                final String visPref = ctrlPrefsVis[i];
                sw.setOnCheckedChangeListener((b, checked) ->
                        prefs.edit().putBoolean(visPref, checked).apply());
                ctrlSwitches[i] = sw;
                cell.addView(sw);
            }

            if (i % 2 == 0) {
                pairRow = new LinearLayout(this);
                pairRow.setOrientation(LinearLayout.HORIZONTAL);
                controlsContainer.addView(pairRow);
            }
            LinearLayout.LayoutParams cellLp =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            pairRow.addView(cell, cellLp);

            if (i % 2 == 1 || i == numControls - 1) {
                View div = new View(this);
                div.setBackgroundColor(getResources().getColor(R.color.divider, null));
                LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divLp.setMargins(0, dp4, 0, dp4);
                div.setLayoutParams(divLp);
                controlsContainer.addView(div);
            }
        }

        // ── 顶栏/中央按钮排序 标签切换 ─────────────────────────────────────────
        {
            View sectionTopBtnOrder   = findViewById(R.id.sectionTopBtnOrder);
            View sectionCenterBtnOrder = findViewById(R.id.sectionCenterBtnOrder);
            TextView tabTopBtnOrder   = findViewById(R.id.tabTopBtnOrder);
            TextView tabCenterBtnOrder = findViewById(R.id.tabCenterBtnOrder);

            View.OnClickListener btnOrderTabToggle = v -> {
                View section = (v == tabTopBtnOrder) ? sectionTopBtnOrder : sectionCenterBtnOrder;
                boolean wasVisible = section.getVisibility() == View.VISIBLE;
                section.setVisibility(wasVisible ? View.GONE : View.VISIBLE);
                int activeColor = getResources().getColor(R.color.colorAccent, null);
                int normalColor = 0x00000000;
                ((TextView) v).setBackgroundColor(wasVisible ? normalColor : activeColor);
            };
            tabTopBtnOrder.setOnClickListener(btnOrderTabToggle);
            tabCenterBtnOrder.setOnClickListener(btnOrderTabToggle);
        }

        // ── 顶栏按钮排序 ─────────────────────────────────────────────────────
        LinearLayout topBtnOrderContainer = findViewById(R.id.topBtnOrderContainer);
        refreshBtnOrderRows(topBtnOrderContainer, topBtnOrder, getTopBtnLabels());

        // ── 中央按钮排序 ──────────────────────────────────────────────────────
        LinearLayout centerBtnOrderContainer = findViewById(R.id.centerBtnOrderContainer);
        refreshBtnOrderRows(centerBtnOrderContainer, centerBtnOrder, getCenterBtnLabels());

        // ── 设置页背景透明度 ─────────────────────────────────────────────────
        int savedBgAlpha = prefs.getInt(PREF_SETTINGS_BG_ALPHA, DEFAULT_SETTINGS_BG_ALPHA);
        applySettingsBgAlpha(savedBgAlpha);

        SeekBar sbSettingsBgAlpha = findViewById(R.id.sbSettingsBgAlpha);
        TextView tvSettingsBgAlphaLabel = findViewById(R.id.tvSettingsBgAlphaLabel);
        sbSettingsBgAlpha.setProgress(savedBgAlpha);
        tvSettingsBgAlphaLabel.setText(getString(R.string.settings_bg_alpha, savedBgAlpha));
        sbSettingsBgAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvSettingsBgAlphaLabel.setText(getString(R.string.settings_bg_alpha, p));
                applySettingsBgAlpha(p);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                prefs.edit().putInt(PREF_SETTINGS_BG_ALPHA, sb.getProgress()).apply();
            }
        });

        // ── Restore button (settings auto-save; button reverts to pre-entry state) ─
        Button btnRestore = findViewById(R.id.btnSaveSettings);
        btnRestore.setOnClickListener(v -> confirmRestore());
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Restore snapshot ─────────────────────────────────────────────────────

    private void confirmRestore() {
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.settings_restore_title)
                .setMessage(R.string.settings_restore_message)
                .setPositiveButton(android.R.string.ok, (d, w) -> restoreSnapshot())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Write the entry-snapshot back to SharedPreferences and restart the Activity. */
    @SuppressWarnings("unchecked")
    private void restoreSnapshot() {
        if (originalPrefs == null) return;
        SharedPreferences.Editor editor = prefs.edit().clear();
        for (Map.Entry<String, Object> entry : originalPrefs.entrySet()) {
            Object val = entry.getValue();
            String key = entry.getKey();
            if      (val instanceof Boolean) editor.putBoolean(key, (Boolean) val);
            else if (val instanceof Integer) editor.putInt    (key, (Integer) val);
            else if (val instanceof Float)   editor.putFloat  (key, (Float)   val);
            else if (val instanceof Long)    editor.putLong   (key, (Long)    val);
            else if (val instanceof String)  editor.putString (key, (String)  val);
        }
        editor.apply();
        Toast.makeText(this, R.string.settings_restored, Toast.LENGTH_SHORT).show();
        recreate();
    }

    // ── Auto-save helpers ────────────────────────────────────────────────────

    private void saveTabOrder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tabOrder.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(tabOrder[i].trim());
        }
        prefs.edit().putString(PREF_TAB_ORDER, sb.toString()).apply();
    }

    private void saveBtnOrders() {
        StringBuilder top = new StringBuilder();
        for (int i = 0; i < topBtnOrder.length; i++) {
            if (i > 0) top.append(",");
            top.append(topBtnOrder[i].trim());
        }
        StringBuilder center = new StringBuilder();
        for (int i = 0; i < centerBtnOrder.length; i++) {
            if (i > 0) center.append(",");
            center.append(centerBtnOrder[i].trim());
        }
        prefs.edit()
                .putString(PREF_TOP_BTN_ORDER, top.toString())
                .putString(PREF_CENTER_BTN_ORDER, center.toString())
                .apply();
    }

    // ── Color picker popup ───────────────────────────────────────────────────

    private void showColorPickerDialog(int controlIndex, View swatchView) {
        // Build a grid of color circles for the dialog
        LinearLayout colorRow = new LinearLayout(this);
        colorRow.setOrientation(LinearLayout.HORIZONTAL);
        colorRow.setGravity(android.view.Gravity.CENTER);
        int dp8 = dp(8);
        colorRow.setPadding(dp8, dp8, dp8, dp8);

        int circleSize = dp(40);
        for (int c = 0; c < COLOR_VALUES.length; c++) {
            final int ci = c;
            View circle = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(circleSize, circleSize);
            lp.setMargins(dp(6), dp(6), dp(6), dp(6));
            circle.setLayoutParams(lp);
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(COLOR_INT[c]);
            if (COLOR_VALUES[c].equals(selectedColors[controlIndex])) {
                gd.setStroke(dp(3), 0xFFFFFFFF);
            }
            circle.setBackground(gd);
            circle.setClickable(true);
            circle.setFocusable(true);
            circle.setContentDescription(COLOR_LABELS[c]);

            AlertDialog[] dialogRef = new AlertDialog[1];
            circle.setOnClickListener(v -> {
                selectedColors[controlIndex] = COLOR_VALUES[ci];
                updateSwatchColor(swatchView, COLOR_VALUES[ci]);
                if (dialogRef[0] != null) dialogRef[0].dismiss();
            });
            colorRow.addView(circle);
        }

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle("选择颜色")
                .setView(colorRow)
                .setNegativeButton(R.string.cancel, null)
                .create();
        // Store reference so the lambda can dismiss it
        dialog.setOnShowListener(d -> {
            // Re-bind circle click listeners with real dialog reference
        });
        // Use a holder trick since lambdas can't capture mutable locals
        final AlertDialog[] holder = {dialog};
        for (int c = 0; c < colorRow.getChildCount(); c++) {
            final int ci = c;
            colorRow.getChildAt(c).setOnClickListener(v -> {
                selectedColors[controlIndex] = COLOR_VALUES[ci];
                updateSwatchColor(swatchView, COLOR_VALUES[ci]);
                if (ctrlPrefsColor != null && ctrlPrefsColor[controlIndex] != null) {
                    prefs.edit().putString(ctrlPrefsColor[controlIndex], COLOR_VALUES[ci]).apply();
                }
                holder[0].dismiss();
            });
        }
        dialog.show();
    }

    private void updateSwatchColor(View swatch, String colorValue) {
        int colorInt = resolveColorInt(colorValue);
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(colorInt);
        gd.setStroke(dp(1), 0x44FFFFFF);
        swatch.setBackground(gd);
    }

    private int resolveColorInt(String color) {
        for (int i = 0; i < COLOR_VALUES.length; i++) {
            if (COLOR_VALUES[i].equals(color)) return COLOR_INT[i];
        }
        return COLOR_INT[0];
    }

    // ── Tab order drag rows ──────────────────────────────────────────────────

    private void refreshTabOrderRows(LinearLayout container) {
        container.removeAllViews();
        int dp8 = dp(8);
        int dp4 = dp(4);
        for (int i = 0; i < tabOrder.length; i++) {
            final int idx = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp4, 0, dp4);

            TextView tvName = new TextView(this);
            tvName.setText((i + 1) + ". " + getTabLabel(tabOrder[i].trim()));
            tvName.setTextColor(getResources().getColor(R.color.textPrimary, null));
            tvName.setTextSize(15f);
            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            tvName.setLayoutParams(nameParams);
            row.addView(tvName);

            if (i > 0) {
                Button btnUp = new Button(this);
                btnUp.setText("↑");
                btnUp.setTextSize(16f);
                btnUp.setPadding(dp8, dp4, dp8, dp4);
                btnUp.setOnClickListener(v -> {
                    String tmp = tabOrder[idx - 1];
                    tabOrder[idx - 1] = tabOrder[idx];
                    tabOrder[idx] = tmp;
                    refreshTabOrderRows(container);
                    saveTabOrder();
                });
                row.addView(btnUp);
            }

            if (i < tabOrder.length - 1) {
                Button btnDown = new Button(this);
                btnDown.setText("↓");
                btnDown.setTextSize(16f);
                btnDown.setPadding(dp8, dp4, dp8, dp4);
                btnDown.setOnClickListener(v -> {
                    String tmp = tabOrder[idx + 1];
                    tabOrder[idx + 1] = tabOrder[idx];
                    tabOrder[idx] = tmp;
                    refreshTabOrderRows(container);
                    saveTabOrder();
                });
                row.addView(btnDown);
            }

            container.addView(row);
        }
    }

    // ── Shortcut order rows ──────────────────────────────────────────────────

    private void refreshShortcutOrderRows(LinearLayout container) {
        container.removeAllViews();
        int dp8 = dp(8);
        int dp4 = dp(4);
        for (int i = 0; i < shortcutOrder.length; i++) {
            final int idx = i;
            String id = shortcutOrder[i].trim();
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp4, 0, dp4);

            String visPref = getShortcutVisPref(id);
            if (visPref != null) {
                Switch sw = new Switch(this);
                sw.setChecked(prefs.getBoolean(visPref, true));
                sw.setOnCheckedChangeListener((btn, checked) ->
                        prefs.edit().putBoolean(visPref, checked).apply());
                sw.setPadding(0, 0, dp(6), 0);
                row.addView(sw);
            }

            TextView tvName = new TextView(this);
            tvName.setText((i + 1) + ". " + getShortcutLabel(id));
            tvName.setTextColor(getResources().getColor(R.color.textPrimary, null));
            tvName.setTextSize(15f);
            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            tvName.setLayoutParams(nameParams);
            row.addView(tvName);

            if (i > 0) {
                Button btnUp = new Button(this);
                btnUp.setText("↑");
                btnUp.setTextSize(16f);
                btnUp.setPadding(dp8, dp4, dp8, dp4);
                btnUp.setOnClickListener(v -> {
                    String tmp = shortcutOrder[idx - 1];
                    shortcutOrder[idx - 1] = shortcutOrder[idx];
                    shortcutOrder[idx] = tmp;
                    refreshShortcutOrderRows(container);
                    saveShortcutOrder();
                });
                row.addView(btnUp);
            }

            if (i < shortcutOrder.length - 1) {
                Button btnDown = new Button(this);
                btnDown.setText("↓");
                btnDown.setTextSize(16f);
                btnDown.setPadding(dp8, dp4, dp8, dp4);
                btnDown.setOnClickListener(v -> {
                    String tmp = shortcutOrder[idx + 1];
                    shortcutOrder[idx + 1] = shortcutOrder[idx];
                    shortcutOrder[idx] = tmp;
                    refreshShortcutOrderRows(container);
                    saveShortcutOrder();
                });
                row.addView(btnDown);
            }

            container.addView(row);
        }
    }

    private String[] migrateShortcutOrder(String orderStr) {
        String[] ALL_SHORTCUTS = {"library", "stats", "toggle_view", "settings"};
        java.util.List<String> current = new java.util.ArrayList<>();
        for (String s : orderStr.split(",")) {
            String t = s.trim();
            if (!t.isEmpty()) current.add(t);
        }
        boolean changed = false;
        for (String key : ALL_SHORTCUTS) {
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

    private String[] migrateTopBtnOrder(String orderStr) {
        String[] ALL = DEFAULT_TOP_BTN_ORDER.split(",");
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
            prefs.edit().putString(PREF_TOP_BTN_ORDER, sb.toString()).apply();
        }
        return current.toArray(new String[0]);
    }

    private void saveShortcutOrder() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < shortcutOrder.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(shortcutOrder[i].trim());
        }
        prefs.edit().putString(PREF_HOME_SHORTCUT_ORDER, sb.toString()).apply();
    }

    private String getShortcutLabel(String id) {
        switch (id) {
            case "stats":       return getString(R.string.tab_stats);
            case "library":     return getString(R.string.tab_library);
            case "toggle_view": return getString(R.string.shortcut_toggle_view);
            case "settings":    return getString(R.string.shortcut_settings);
            default:            return id;
        }
    }

    private String getShortcutVisPref(String id) {
        switch (id) {
            case "stats":       return PREF_HOME_BTN_STATS_VISIBLE;
            case "library":     return PREF_HOME_BTN_LIBRARY_VISIBLE;
            case "toggle_view": return PREF_HOME_BTN_TOGGLE_VIEW_VISIBLE;
            default:            return null;
        }
    }

    // ── Button order rows ────────────────────────────────────────────────────

    private void refreshBtnOrderRows(LinearLayout container, String[] order, String[] labels) {
        container.removeAllViews();
        int dp8 = dp(8);
        int dp4 = dp(4);
        for (int i = 0; i < order.length; i++) {
            final int idx = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp4, 0, dp4);

            String btnId = order[i].trim();
            String label = getBtnLabel(btnId, labels, order);

            TextView tvName = new TextView(this);
            tvName.setText((i + 1) + ". " + label);
            tvName.setTextColor(getResources().getColor(R.color.textPrimary, null));
            tvName.setTextSize(14f);
            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            tvName.setLayoutParams(nameParams);
            row.addView(tvName);

            if (i > 0) {
                Button btnUp = new Button(this);
                btnUp.setText("↑");
                btnUp.setTextSize(14f);
                btnUp.setPadding(dp8, dp(2), dp8, dp(2));
                btnUp.setOnClickListener(v -> {
                    String tmp = order[idx - 1];
                    order[idx - 1] = order[idx];
                    order[idx] = tmp;
                    refreshBtnOrderRows(container, order, labels);
                    saveBtnOrders();
                });
                row.addView(btnUp);
            }

            if (i < order.length - 1) {
                Button btnDown = new Button(this);
                btnDown.setText("↓");
                btnDown.setTextSize(14f);
                btnDown.setPadding(dp8, dp(2), dp8, dp(2));
                btnDown.setOnClickListener(v -> {
                    String tmp = order[idx + 1];
                    order[idx + 1] = order[idx];
                    order[idx] = tmp;
                    refreshBtnOrderRows(container, order, labels);
                    saveBtnOrders();
                });
                row.addView(btnDown);
            }

            container.addView(row);
        }
    }

    private String getBtnLabel(String id, String[] labels, String[] order) {
        // Match by position index in default order arrays
        String[] topDefault    = DEFAULT_TOP_BTN_ORDER.split(",");
        String[] centerDefault = DEFAULT_CENTER_BTN_ORDER.split(",");
        for (int i = 0; i < topDefault.length; i++) {
            if (topDefault[i].equals(id) && i < labels.length) return labels[i];
        }
        for (int i = 0; i < centerDefault.length; i++) {
            if (centerDefault[i].equals(id) && i < labels.length) return labels[i];
        }
        return id;
    }

    private String[] getTopBtnLabels() {
        return new String[]{
                getString(R.string.settings_ctrl_lock),
                getString(R.string.settings_ctrl_playmode),
                getString(R.string.settings_ctrl_playlist),
                getString(R.string.settings_ctrl_speed),
                getString(R.string.settings_ctrl_subtitle_toggle),
                getString(R.string.settings_ctrl_subtitle),
                getString(R.string.settings_ctrl_subtlist),
                getString(R.string.settings_ctrl_rotate),
                getString(R.string.settings_ctrl_player_settings)
        };
    }

    private String[] getCenterBtnLabels() {
        return new String[]{
                getString(R.string.btn_rewind),
                getString(R.string.settings_ctrl_playpause),
                getString(R.string.btn_forward)
        };
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private RadioButton makeRadioButton(String label) {
        RadioButton rb = new RadioButton(this);
        rb.setId(android.view.View.generateViewId());
        rb.setText(label);
        rb.setTextColor(getResources().getColor(R.color.textPrimary, null));
        rb.setPadding(dp(8), dp(8), dp(8), dp(8));
        return rb;
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

    private void applySettingsBgAlpha(int percent) {
        int alpha = (int) (percent / 100f * 255);

        int bgColor = getResources().getColor(R.color.bgDark, null);
        int bgWithAlpha = (bgColor & 0x00FFFFFF) | (alpha << 24);
        View coord = findViewById(android.R.id.content);
        if (coord != null) {
            View topView = ((android.view.ViewGroup) coord).getChildAt(0);
            if (topView != null) topView.setBackgroundColor(bgWithAlpha);
        }

        int toolbarBg = getResources().getColor(R.color.colorPrimary, null);
        int toolbarWithAlpha = (toolbarBg & 0x00FFFFFF) | (alpha << 24);
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null && toolbar.getParent() instanceof View) {
            ((View) toolbar.getParent()).setBackgroundColor(toolbarWithAlpha);
        }
    }

    private void setupGridCellSeekBar(int seekBarId, int labelId, String prefKey,
                                      int savedVal, int labelFormatResId) {
        SeekBar sb = findViewById(seekBarId);
        TextView tv = findViewById(labelId);
        sb.setMax(MAX_GRID_CELL_WIDTH_DP - MIN_GRID_CELL_WIDTH_DP);
        sb.setProgress(savedVal - MIN_GRID_CELL_WIDTH_DP);
        tv.setText(getString(labelFormatResId, savedVal));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean user) {
                int val = MIN_GRID_CELL_WIDTH_DP + p;
                tv.setText(getString(labelFormatResId, val));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {
                int val = MIN_GRID_CELL_WIDTH_DP + s.getProgress();
                prefs.edit().putInt(prefKey, val).apply();
            }
        });
    }

    private void setupPanelDirGroup(RadioButton[] buttons, String[] values,
                                    String savedValue, String prefKey) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setChecked(values[i].equals(savedValue));
        }
        for (int i = 0; i < buttons.length; i++) {
            final String val = values[i];
            buttons[i].setOnClickListener(v -> {
                for (RadioButton rb : buttons) rb.setChecked(false);
                ((RadioButton) v).setChecked(true);
                prefs.edit().putString(prefKey, val).apply();
            });
        }
    }

    private int dp(int dpVal) {
        return (int) (dpVal * getResources().getDisplayMetrics().density);
    }
}
