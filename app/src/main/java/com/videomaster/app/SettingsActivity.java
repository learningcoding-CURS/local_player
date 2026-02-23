package com.videomaster.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Full-screen Settings Activity.
 * Covers: default home tab, tab order, list display mode (grid/list),
 * portrait swipe direction, and landscape swipe direction.
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

    private String[] tabOrder;

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

        tabOrder = savedOrderStr.split(",");

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
        // SeekBar range 0-30 → actual size 10-40 sp
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
        // SeekBar range 0-20 → multiplier 1.0-3.0 (step 0.1)
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
        // range 0-64 → actual 32-96 dp
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
        // range 0-59 → actual 1-60 s
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
        // range 0-90 → actual 10-100%
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
        // range 0-16 → actual 4-20 dp
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
        // range 0-90 → actual 10-100%
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
        // range 0-90 → actual 0-90%
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

            prefs.edit()
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
                    .apply();

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
