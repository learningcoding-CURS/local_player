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
    public static final String PREF_PORTRAIT_SWIPE   = "portrait_swipe";
    public static final String PREF_LANDSCAPE_SWIPE  = "landscape_swipe";
    public static final String PREF_VIEW_MODE        = "view_mode";
    public static final String PREF_SUBTITLE_SIZE    = "subtitle_size";
    public static final String PREF_SUBTITLE_LINE_SP = "subtitle_line_spacing";

    // Seek button settings
    public static final String PREF_SEEK_ICON_SIZE         = "seek_icon_size";
    public static final String PREF_SEEK_SECONDS           = "seek_seconds";
    public static final String PREF_SEEK_ALPHA             = "seek_alpha";
    // Seekbar settings
    public static final String PREF_SEEKBAR_HEIGHT         = "seekbar_height";
    public static final String PREF_SEEKBAR_PROGRESS_ALPHA = "seekbar_progress_alpha";
    // Subtitle panel
    public static final String PREF_SUBTITLE_PANEL_ALPHA   = "subtitle_panel_alpha";

    // Player control button visibility (boolean)
    public static final String PREF_BTN_LOCK_VISIBLE            = "btn_lock_visible";
    public static final String PREF_BTN_PLAYMODE_VISIBLE        = "btn_playmode_visible";
    public static final String PREF_BTN_PLAYLIST_VISIBLE        = "btn_playlist_visible";
    public static final String PREF_BTN_SUBTITLE_VISIBLE        = "btn_subtitle_visible";
    public static final String PREF_BTN_SUBTITLE_TOGGLE_VISIBLE = "btn_subtitle_toggle_visible";
    public static final String PREF_BTN_SUBTLIST_VISIBLE        = "btn_subtlist_visible";
    public static final String PREF_BTN_ROTATE_VISIBLE          = "btn_rotate_visible";

    // Player control button colors (string: "white","accent","orange","cyan","green","yellow")
    public static final String PREF_BTN_LOCK_COLOR           = "btn_lock_color";
    public static final String PREF_BTN_PLAYMODE_COLOR       = "btn_playmode_color";
    public static final String PREF_BTN_PLAYLIST_COLOR       = "btn_playlist_color";
    public static final String PREF_BTN_SUBTITLE_COLOR       = "btn_subtitle_color";
    public static final String PREF_BTN_SUBTITLE_TOGGLE_COLOR= "btn_subtitle_toggle_color";
    public static final String PREF_BTN_SUBTLIST_COLOR       = "btn_subtlist_color";
    public static final String PREF_BTN_ROTATE_COLOR         = "btn_rotate_color";
    public static final String PREF_BTN_SEEK_COLOR           = "btn_seek_color";
    public static final String PREF_BTN_PLAYPAUSE_COLOR      = "btn_playpause_color";

    // Button order preferences (comma-separated IDs within each bar)
    public static final String PREF_TOP_BTN_ORDER    = "top_btn_order";
    public static final String PREF_CENTER_BTN_ORDER = "center_btn_order";

    // Default button orders
    public static final String DEFAULT_TOP_BTN_ORDER =
            "lock,play-mode,playlist-panel,subtitle-toggle,subtitle,subtitle-list,rotate";
    public static final String DEFAULT_CENTER_BTN_ORDER = "rewind,play-pause,forward";

    public static final String DEFAULT_BTN_COLOR         = "white";
    public static final String DEFAULT_PLAYLIST_BTN_COLOR = "accent";

    public static final String VIEW_MODE_GRID = "GRID";
    public static final String VIEW_MODE_LIST = "LIST";

    public static final float DEFAULT_SUBTITLE_SIZE    = 18f;
    public static final float DEFAULT_SUBTITLE_LINE_SP = 1.2f;

    public static final int DEFAULT_SEEK_ICON_SIZE         = 64;
    public static final int DEFAULT_SEEK_SECONDS           = 5;
    public static final int DEFAULT_SEEK_ALPHA             = 100;
    public static final int DEFAULT_SEEKBAR_HEIGHT         = 12;
    public static final int DEFAULT_SEEKBAR_PROGRESS_ALPHA = 80;
    public static final int DEFAULT_SUBTITLE_PANEL_ALPHA   = 60;

    static final String DEFAULT_TAB_ORDER = "builtin,library,playlist";

    // Available button color options
    private static final String[] COLOR_VALUES = {"white","accent","orange","cyan","green","yellow"};
    private static final int[]    COLOR_INT    = {0xB3FFFFFF,0xFFE94560,0xFFFF9800,0xFF00BCD4,0xFF4CAF50,0xFFFFEB3B};
    private static final String[] COLOR_LABELS = {"白","红","橙","青","绿","黄"};

    private String[] tabOrder;
    private String[] topBtnOrder;
    private String[] centerBtnOrder;

    // Tracks currently selected color per control row (index = control index)
    private String[] selectedColors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedOrderStr  = prefs.getString(PREF_TAB_ORDER, DEFAULT_TAB_ORDER);
        String savedDefault   = prefs.getString(PREF_DEFAULT_TAB, "builtin");
        String savedPortrait  = prefs.getString(PREF_PORTRAIT_SWIPE, "VERTICAL");
        String savedLandscape = prefs.getString(PREF_LANDSCAPE_SWIPE, "HORIZONTAL");
        String savedViewMode  = prefs.getString(PREF_VIEW_MODE, VIEW_MODE_GRID);
        float  savedSubSize   = prefs.getFloat(PREF_SUBTITLE_SIZE, DEFAULT_SUBTITLE_SIZE);
        float  savedLineSp    = prefs.getFloat(PREF_SUBTITLE_LINE_SP, DEFAULT_SUBTITLE_LINE_SP);
        int    savedSeekIconSize   = prefs.getInt(PREF_SEEK_ICON_SIZE,         DEFAULT_SEEK_ICON_SIZE);
        int    savedSeekSeconds    = prefs.getInt(PREF_SEEK_SECONDS,           DEFAULT_SEEK_SECONDS);
        int    savedSeekAlpha      = prefs.getInt(PREF_SEEK_ALPHA,             DEFAULT_SEEK_ALPHA);
        int    savedSeekbarHeight  = prefs.getInt(PREF_SEEKBAR_HEIGHT,         DEFAULT_SEEKBAR_HEIGHT);
        int    savedProgAlpha      = prefs.getInt(PREF_SEEKBAR_PROGRESS_ALPHA, DEFAULT_SEEKBAR_PROGRESS_ALPHA);
        int    savedPanelAlpha     = prefs.getInt(PREF_SUBTITLE_PANEL_ALPHA,   DEFAULT_SUBTITLE_PANEL_ALPHA);

        tabOrder       = savedOrderStr.split(",");
        topBtnOrder    = prefs.getString(PREF_TOP_BTN_ORDER, DEFAULT_TOP_BTN_ORDER).split(",");
        centerBtnOrder = prefs.getString(PREF_CENTER_BTN_ORDER, DEFAULT_CENTER_BTN_ORDER).split(",");

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
            rgDefault.addView(rb);
        }

        // ── Tab order ─────────────────────────────────────────────────────────
        LinearLayout tabOrderContainer = findViewById(R.id.tabOrderContainer);
        refreshTabOrderRows(tabOrderContainer);

        // ── View mode ─────────────────────────────────────────────────────────
        RadioButton rbGrid = findViewById(R.id.rbViewGrid);
        RadioButton rbList = findViewById(R.id.rbViewList);
        rbGrid.setChecked(VIEW_MODE_GRID.equals(savedViewMode));
        rbList.setChecked(VIEW_MODE_LIST.equals(savedViewMode));

        // ── Portrait swipe ────────────────────────────────────────────────────
        RadioButton rbPortraitV = findViewById(R.id.rbPortraitVertical);
        RadioButton rbPortraitH = findViewById(R.id.rbPortraitHorizontal);
        rbPortraitV.setChecked("VERTICAL".equals(savedPortrait));
        rbPortraitH.setChecked("HORIZONTAL".equals(savedPortrait));

        // ── Landscape swipe ───────────────────────────────────────────────────
        RadioButton rbLandH = findViewById(R.id.rbLandscapeHorizontal);
        RadioButton rbLandV = findViewById(R.id.rbLandscapeVertical);
        rbLandH.setChecked("HORIZONTAL".equals(savedLandscape));
        rbLandV.setChecked("VERTICAL".equals(savedLandscape));

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
            @Override public void onStopTrackingTouch(SeekBar sb) {}
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
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // ── 跳转按钮大小 ──────────────────────────────────────────────────────
        SeekBar sbSeekIconSize = findViewById(R.id.sbSeekIconSize);
        TextView tvSeekIconSizeLabel = findViewById(R.id.tvSeekIconSizeLabel);
        sbSeekIconSize.setMax(64);
        sbSeekIconSize.setProgress(Math.max(0, Math.min(64, savedSeekIconSize - 32)));
        tvSeekIconSizeLabel.setText(getString(R.string.settings_seek_icon_size, savedSeekIconSize));
        sbSeekIconSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int p, boolean user) {
                tvSeekIconSizeLabel.setText(getString(R.string.settings_seek_icon_size, p + 32));
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
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
            @Override public void onStopTrackingTouch(SeekBar sb) {}
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
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

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
            @Override public void onStopTrackingTouch(SeekBar sb) {}
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
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

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
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        // ── 播放控件 visibility + color (popup color picker) ─────────────────
        LinearLayout controlsContainer = findViewById(R.id.playerControlsContainer);
        String[] ctrlPrefsVis   = {
                PREF_BTN_LOCK_VISIBLE, PREF_BTN_PLAYMODE_VISIBLE,
                PREF_BTN_PLAYLIST_VISIBLE, PREF_BTN_SUBTITLE_TOGGLE_VISIBLE,
                PREF_BTN_SUBTITLE_VISIBLE, PREF_BTN_SUBTLIST_VISIBLE,
                PREF_BTN_ROTATE_VISIBLE, null, null };
        String[] ctrlPrefsColor = {
                PREF_BTN_LOCK_COLOR, PREF_BTN_PLAYMODE_COLOR,
                PREF_BTN_PLAYLIST_COLOR, PREF_BTN_SUBTITLE_TOGGLE_COLOR,
                PREF_BTN_SUBTITLE_COLOR, PREF_BTN_SUBTLIST_COLOR,
                PREF_BTN_ROTATE_COLOR, PREF_BTN_SEEK_COLOR, PREF_BTN_PLAYPAUSE_COLOR };
        String[] ctrlDefColor   = {
                DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR,
                DEFAULT_PLAYLIST_BTN_COLOR, DEFAULT_BTN_COLOR,
                DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR,
                DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR, DEFAULT_BTN_COLOR };
        String[] ctrlLabels     = {
                getString(R.string.settings_ctrl_lock),
                getString(R.string.settings_ctrl_playmode),
                getString(R.string.settings_ctrl_playlist),
                getString(R.string.settings_ctrl_subtitle_toggle),
                getString(R.string.settings_ctrl_subtitle),
                getString(R.string.settings_ctrl_subtlist),
                getString(R.string.settings_ctrl_rotate),
                getString(R.string.settings_ctrl_seek),
                getString(R.string.settings_ctrl_playpause)
        };

        int numControls = ctrlLabels.length;
        selectedColors = new String[numControls];
        Switch[] ctrlSwitches = new Switch[numControls];
        View[] colorSwatches = new View[numControls];

        int dp4 = dp(4);
        int dp8 = dp(8);
        for (int i = 0; i < numControls; i++) {
            final int ci = i;
            String savedColor = prefs.getString(ctrlPrefsColor[i], ctrlDefColor[i]);
            selectedColors[i] = savedColor;

            // Row container
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dp4, 0, dp4);

            // Top line: label + optional switch + color swatch button
            LinearLayout topLine = new LinearLayout(this);
            topLine.setOrientation(LinearLayout.HORIZONTAL);
            topLine.setGravity(android.view.Gravity.CENTER_VERTICAL);

            TextView tvLabel = new TextView(this);
            tvLabel.setText(ctrlLabels[i]);
            tvLabel.setTextColor(getResources().getColor(R.color.textPrimary, null));
            tvLabel.setTextSize(14f);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvLabel.setLayoutParams(lp);
            topLine.addView(tvLabel);

            // Color swatch button (circle, tappable → opens color picker dialog)
            View swatch = new View(this);
            int swatchSize = dp(28);
            LinearLayout.LayoutParams swatchLp = new LinearLayout.LayoutParams(swatchSize, swatchSize);
            swatchLp.setMargins(dp4, 0, dp8, 0);
            swatch.setLayoutParams(swatchLp);
            updateSwatchColor(swatch, savedColor);
            swatch.setClickable(true);
            swatch.setFocusable(true);
            colorSwatches[i] = swatch;
            swatch.setOnClickListener(v -> showColorPickerDialog(ci, colorSwatches[ci]));
            topLine.addView(swatch);

            if (ctrlPrefsVis[i] != null) {
                Switch sw = new Switch(this);
                sw.setChecked(prefs.getBoolean(ctrlPrefsVis[i], true));
                ctrlSwitches[i] = sw;
                topLine.addView(sw);
            }
            row.addView(topLine);
            controlsContainer.addView(row);

            // Divider between controls (not after last)
            if (i < numControls - 1) {
                View div = new View(this);
                div.setBackgroundColor(getResources().getColor(R.color.divider, null));
                LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divLp.setMargins(0, dp4, 0, dp4);
                div.setLayoutParams(divLp);
                controlsContainer.addView(div);
            }
        }

        // ── 顶栏按钮排序 ─────────────────────────────────────────────────────
        LinearLayout topBtnOrderContainer = getOrCreateBtnOrderContainer("topBtnOrderContainer");
        refreshBtnOrderRows(topBtnOrderContainer, topBtnOrder, getTopBtnLabels());

        // ── 中央按钮排序 ──────────────────────────────────────────────────────
        LinearLayout centerBtnOrderContainer = getOrCreateBtnOrderContainer("centerBtnOrderContainer");
        refreshBtnOrderRows(centerBtnOrderContainer, centerBtnOrder, getCenterBtnLabels());

        // ── Save button ───────────────────────────────────────────────────────
        Button btnSave = findViewById(R.id.btnSaveSettings);
        btnSave.setOnClickListener(v -> {
            // Default tab
            String newDefault = savedDefault;
            for (int i = 0; i < tabValues.length; i++) {
                RadioButton rb = (RadioButton) rgDefault.getChildAt(i);
                if (rb != null && rb.isChecked()) {
                    newDefault = tabValues[i];
                    break;
                }
            }

            // View mode
            String newViewMode = rbGrid.isChecked() ? VIEW_MODE_GRID : VIEW_MODE_LIST;

            // Portrait swipe
            String newPortrait = rbPortraitH.isChecked() ? "HORIZONTAL" : "VERTICAL";

            // Landscape swipe
            String newLandscape = rbLandV.isChecked() ? "VERTICAL" : "HORIZONTAL";

            // Tab order
            StringBuilder orderSb = new StringBuilder();
            for (int i = 0; i < tabOrder.length; i++) {
                if (i > 0) orderSb.append(",");
                orderSb.append(tabOrder[i].trim());
            }

            // Subtitle size
            float newSubSize = sbSubSize.getProgress() + 10f;
            float newLineSp  = 1.0f + sbLineSp.getProgress() * 0.1f;

            // Player controls
            int newSeekIconSize  = sbSeekIconSize.getProgress() + 32;
            int newSeekSeconds   = sbSeekSeconds.getProgress() + 1;
            int newSeekAlpha     = sbSeekAlpha.getProgress() + 10;
            int newSeekbarHeight = sbSeekbarHeight.getProgress() + 4;
            int newProgAlpha     = sbProgAlpha.getProgress() + 10;
            int newPanelAlpha    = sbPanelAlpha.getProgress();

            // Top / center button order
            StringBuilder topOrderSb = new StringBuilder();
            for (int i = 0; i < topBtnOrder.length; i++) {
                if (i > 0) topOrderSb.append(",");
                topOrderSb.append(topBtnOrder[i].trim());
            }
            StringBuilder centerOrderSb = new StringBuilder();
            for (int i = 0; i < centerBtnOrder.length; i++) {
                if (i > 0) centerOrderSb.append(",");
                centerOrderSb.append(centerBtnOrder[i].trim());
            }

            SharedPreferences.Editor editor = prefs.edit()
                    .putString(PREF_DEFAULT_TAB, newDefault)
                    .putString(PREF_TAB_ORDER, orderSb.toString())
                    .putString(PREF_VIEW_MODE, newViewMode)
                    .putString(PREF_PORTRAIT_SWIPE, newPortrait)
                    .putString(PREF_LANDSCAPE_SWIPE, newLandscape)
                    .putFloat(PREF_SUBTITLE_SIZE, newSubSize)
                    .putFloat(PREF_SUBTITLE_LINE_SP, newLineSp)
                    .putInt(PREF_SEEK_ICON_SIZE, newSeekIconSize)
                    .putInt(PREF_SEEK_SECONDS, newSeekSeconds)
                    .putInt(PREF_SEEK_ALPHA, newSeekAlpha)
                    .putInt(PREF_SEEKBAR_HEIGHT, newSeekbarHeight)
                    .putInt(PREF_SEEKBAR_PROGRESS_ALPHA, newProgAlpha)
                    .putInt(PREF_SUBTITLE_PANEL_ALPHA, newPanelAlpha)
                    .putString(PREF_TOP_BTN_ORDER, topOrderSb.toString())
                    .putString(PREF_CENTER_BTN_ORDER, centerOrderSb.toString());

            // Save button visibility and color settings
            for (int i = 0; i < ctrlPrefsVis.length; i++) {
                if (ctrlPrefsVis[i] != null && ctrlSwitches[i] != null) {
                    editor.putBoolean(ctrlPrefsVis[i], ctrlSwitches[i].isChecked());
                }
                if (ctrlPrefsColor[i] != null && selectedColors[i] != null) {
                    editor.putString(ctrlPrefsColor[i], selectedColors[i]);
                }
            }
            editor.apply();

            Toast.makeText(this, R.string.settings_applied, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                });
                row.addView(btnDown);
            }

            container.addView(row);
        }
    }

    // ── Button order rows ────────────────────────────────────────────────────

    private LinearLayout getOrCreateBtnOrderContainer(String tag) {
        // We embed these containers dynamically in the settings root
        LinearLayout root = findViewById(R.id.settingsRoot);
        // Find by tag if already added
        for (int i = 0; i < root.getChildCount(); i++) {
            View v = root.getChildAt(i);
            if (tag.equals(v.getTag())) return (LinearLayout) v;
        }
        // Section title
        TextView title = new TextView(this);
        title.setText("topBtnOrderContainer".equals(tag)
                ? getString(R.string.settings_btn_order_top)
                : getString(R.string.settings_btn_order_center));
        title.setTextColor(getResources().getColor(R.color.colorAccentLight, null));
        title.setTextSize(13f);
        title.setPadding(0, dp(8), 0, dp(4));
        root.addView(title);

        LinearLayout container = new LinearLayout(this);
        container.setTag(tag);
        container.setOrientation(LinearLayout.VERTICAL);
        root.addView(container);

        // Divider after
        View div = new View(this);
        div.setBackgroundColor(getResources().getColor(R.color.divider, null));
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        divLp.setMargins(0, dp(12), 0, dp(4));
        div.setLayoutParams(divLp);
        root.addView(div);

        return container;
    }

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
                getString(R.string.settings_ctrl_subtitle_toggle),
                getString(R.string.settings_ctrl_subtitle),
                getString(R.string.settings_ctrl_subtlist),
                getString(R.string.settings_ctrl_rotate)
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
            default:         return tabId;
        }
    }

    private int dp(int dpVal) {
        return (int) (dpVal * getResources().getDisplayMetrics().density);
    }
}
