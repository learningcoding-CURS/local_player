package com.videomaster.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

    public static final String PREFS_NAME           = "app_settings";
    public static final String PREF_DEFAULT_TAB     = "default_tab";
    public static final String PREF_TAB_ORDER       = "tab_order";
    public static final String PREF_PORTRAIT_SWIPE  = "portrait_swipe";
    public static final String PREF_LANDSCAPE_SWIPE = "landscape_swipe";
    public static final String PREF_VIEW_MODE       = "view_mode";

    public static final String VIEW_MODE_GRID = "GRID";
    public static final String VIEW_MODE_LIST = "LIST";

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

            prefs.edit()
                    .putString(PREF_DEFAULT_TAB, newDefault)
                    .putString(PREF_TAB_ORDER, orderSb.toString())
                    .putString(PREF_VIEW_MODE, newViewMode)
                    .putString(PREF_PORTRAIT_SWIPE, newPortrait)
                    .putString(PREF_LANDSCAPE_SWIPE, newLandscape)
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
