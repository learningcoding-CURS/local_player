package com.videomaster.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
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
import com.videomaster.app.model.VideoItem;
import com.videomaster.app.playlist.PlaylistActivity;
import com.videomaster.app.ui.VideoAdapter;
import com.videomaster.app.util.BuiltinMediaProvider;
import com.videomaster.app.util.PermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME      = "app_settings";
    private static final String PREF_DEFAULT_TAB = "default_tab";
    private static final String PREF_TAB_ORDER   = "tab_order";
    private static final String PREF_PORTRAIT_SWIPE  = "portrait_swipe";
    private static final String PREF_LANDSCAPE_SWIPE = "landscape_swipe";

    // Default tab order: builtin first, then library, then playlist
    private static final String DEFAULT_TAB_ORDER = "builtin,library,playlist";

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

    // Tab containers
    private View          tabLibrary;
    private View          tabBuiltin;

    private BottomNavigationView bottomNav;

    private final List<VideoItem> videoList   = new ArrayList<>();
    private final List<VideoItem> builtinList = new ArrayList<>();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Tab containers
        tabLibrary  = findViewById(R.id.tabLibrary);
        tabBuiltin  = findViewById(R.id.tabBuiltin);

        // Library tab
        recyclerView = findViewById(R.id.recyclerView);
        emptyView    = findViewById(R.id.tvEmpty);
        loadingView  = findViewById(R.id.loadingView);
        fabOpen      = findViewById(R.id.fabOpen);
        fabOpen.setOnClickListener(v -> pickVideoFile());
        adapter = new VideoAdapter(videoList, item -> openPlayer(item.getUri()));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Built-in tab
        recyclerBuiltin = findViewById(R.id.recyclerBuiltin);
        tvBuiltinEmpty  = findViewById(R.id.tvBuiltinEmpty);
        builtinAdapter  = new VideoAdapter(builtinList, item -> openPlayer(item.getUri()));
        recyclerBuiltin.setLayoutManager(new LinearLayoutManager(this));
        recyclerBuiltin.setAdapter(builtinAdapter);

        // Bottom navigation
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_library) {
                showTab("library");
                checkPermissionsAndLoad();
                return true;
            } else if (id == R.id.nav_playlist) {
                startActivity(new Intent(this, PlaylistActivity.class));
                return false; // don't visually select until back
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

        // Apply saved tab order and select default tab
        applyTabOrderAndDefault();
    }

    // ── Toolbar menu ───────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Tab management ─────────────────────────────────────────────────────

    /**
     * Apply the saved tab order to BottomNavigationView, then select the default tab.
     */
    private void applyTabOrderAndDefault() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String orderStr   = prefs.getString(PREF_TAB_ORDER, DEFAULT_TAB_ORDER);
        String defaultTab = prefs.getString(PREF_DEFAULT_TAB, "builtin");

        String[] order = orderStr.split(",");
        rebuildBottomNav(order);

        // Select the default tab (fires onItemSelectedListener)
        switch (defaultTab) {
            case "library":  bottomNav.setSelectedItemId(R.id.nav_library);  break;
            case "playlist": bottomNav.setSelectedItemId(R.id.nav_builtin);  break; // fallback
            default:         bottomNav.setSelectedItemId(R.id.nav_builtin);  break;
        }
    }

    /**
     * Rebuild BottomNavigationView menu in the given tab order.
     * @param order array of tab id strings: "library", "playlist", "builtin"
     */
    private void rebuildBottomNav(String[] order) {
        Menu menu = bottomNav.getMenu();
        menu.clear();
        for (String tabId : order) {
            switch (tabId.trim()) {
                case "library":
                    menu.add(Menu.NONE, R.id.nav_library, Menu.NONE, R.string.tab_library)
                            .setIcon(R.drawable.ic_home);
                    break;
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

    private void showTab(String tabId) {
        currentTabId = tabId;
        tabLibrary.setVisibility("library".equals(tabId) ? View.VISIBLE : View.GONE);
        tabBuiltin.setVisibility("builtin".equals(tabId) ? View.VISIBLE : View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if ("library".equals(tabId)) toolbar.setTitle(R.string.library_title);
        else if ("builtin".equals(tabId)) toolbar.setTitle(R.string.tab_builtin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ("library".equals(currentTabId)) loadVideos();
    }

    // ── Settings dialog ────────────────────────────────────────────────────

    private void showSettingsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedOrderStr   = prefs.getString(PREF_TAB_ORDER, DEFAULT_TAB_ORDER);
        String savedDefault    = prefs.getString(PREF_DEFAULT_TAB, "builtin");
        String savedPortrait   = prefs.getString(PREF_PORTRAIT_SWIPE, "VERTICAL");
        String savedLandscape  = prefs.getString(PREF_LANDSCAPE_SWIPE, "HORIZONTAL");

        // Working copy of tab order (mutable)
        final String[] tabOrder = savedOrderStr.split(",");

        // ── Build dialog layout ─────────────────────────────────────────
        int dp8  = dp(8);
        int dp16 = dp(16);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp16, dp8, dp16, dp8);
        scrollView.addView(root);

        // ── Section 1: Default home tab ─────────────────────────────────
        root.addView(sectionTitle("默认主界面"));
        RadioGroup rgDefault = new RadioGroup(this);
        rgDefault.setOrientation(RadioGroup.VERTICAL);
        String[] tabLabels = { getTabLabel("builtin"), getTabLabel("library"), getTabLabel("playlist") };
        String[] tabValues = { "builtin", "library", "playlist" };
        int defaultCheckedId = View.generateViewId();
        for (int i = 0; i < tabValues.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());
            rb.setText(tabLabels[i]);
            rb.setTextColor(getResources().getColor(R.color.textPrimary, null));
            rb.setPadding(dp8, dp8, dp8, dp8);
            if (tabValues[i].equals(savedDefault)) {
                defaultCheckedId = rb.getId();
            }
            rgDefault.addView(rb);
        }
        rgDefault.check(defaultCheckedId);
        root.addView(rgDefault);

        // ── Section 2: Tab order ─────────────────────────────────────────
        root.addView(sectionTitle("标签顺序（点击 ↑↓ 调整）"));
        LinearLayout tabOrderContainer = new LinearLayout(this);
        tabOrderContainer.setOrientation(LinearLayout.VERTICAL);
        refreshTabOrderRows(tabOrderContainer, tabOrder);
        root.addView(tabOrderContainer);

        // ── Section 3: Portrait swipe direction ──────────────────────────
        root.addView(sectionTitle("竖屏切换媒体手势"));
        RadioGroup rgPortrait = new RadioGroup(this);
        rgPortrait.setOrientation(RadioGroup.VERTICAL);
        RadioButton rbPortraitV = makeRadioButton("上下滑动（右侧，不需要长按）", "VERTICAL".equals(savedPortrait));
        RadioButton rbPortraitH = makeRadioButton("左右滑动", "HORIZONTAL".equals(savedPortrait));
        rgPortrait.addView(rbPortraitV);
        rgPortrait.addView(rbPortraitH);
        if ("VERTICAL".equals(savedPortrait)) rgPortrait.check(rbPortraitV.getId());
        else                                  rgPortrait.check(rbPortraitH.getId());
        root.addView(rgPortrait);

        // ── Section 4: Landscape swipe direction ─────────────────────────
        root.addView(sectionTitle("横屏切换媒体手势"));
        RadioGroup rgLandscape = new RadioGroup(this);
        rgLandscape.setOrientation(RadioGroup.VERTICAL);
        RadioButton rbLandH = makeRadioButton("左右滑动", "HORIZONTAL".equals(savedLandscape));
        RadioButton rbLandV = makeRadioButton("上下滑动（右侧）", "VERTICAL".equals(savedLandscape));
        rgLandscape.addView(rbLandH);
        rgLandscape.addView(rbLandV);
        if ("HORIZONTAL".equals(savedLandscape)) rgLandscape.check(rbLandH.getId());
        else                                      rgLandscape.check(rbLandV.getId());
        root.addView(rgLandscape);

        // ── Show dialog ──────────────────────────────────────────────────
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.settings_title)
                .setView(scrollView)
                .setPositiveButton(R.string.settings_save, (dialog, which) -> {
                    // Collect default tab
                    int checkedDefaultId = rgDefault.getCheckedRadioButtonId();
                    String newDefault = savedDefault;
                    for (int i = 0; i < tabValues.length; i++) {
                        RadioButton rb = rgDefault.findViewById(rgDefault.getChildAt(i).getId());
                        if (rb != null && rb.getId() == checkedDefaultId) {
                            newDefault = tabValues[i];
                            break;
                        }
                    }

                    // Collect portrait swipe
                    String newPortrait = rgPortrait.getCheckedRadioButtonId() == rbPortraitH.getId()
                            ? "HORIZONTAL" : "VERTICAL";
                    // Collect landscape swipe
                    String newLandscape = rgLandscape.getCheckedRadioButtonId() == rbLandV.getId()
                            ? "VERTICAL" : "HORIZONTAL";

                    // Save
                    StringBuilder orderSb = new StringBuilder();
                    for (int i = 0; i < tabOrder.length; i++) {
                        if (i > 0) orderSb.append(",");
                        orderSb.append(tabOrder[i].trim());
                    }
                    prefs.edit()
                            .putString(PREF_DEFAULT_TAB, newDefault)
                            .putString(PREF_TAB_ORDER, orderSb.toString())
                            .putString(PREF_PORTRAIT_SWIPE, newPortrait)
                            .putString(PREF_LANDSCAPE_SWIPE, newLandscape)
                            .apply();

                    // Rebuild navigation and switch to new default
                    rebuildBottomNav(tabOrder);
                    switch (newDefault) {
                        case "library":
                            bottomNav.setSelectedItemId(R.id.nav_library);
                            break;
                        default:
                            bottomNav.setSelectedItemId(R.id.nav_builtin);
                            break;
                    }

                    Toast.makeText(this, R.string.settings_applied, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Refresh the tab-order list inside the container with current order + ↑↓ buttons. */
    private void refreshTabOrderRows(LinearLayout container, String[] order) {
        container.removeAllViews();
        int dp8  = dp(8);
        int dp4  = dp(4);
        for (int i = 0; i < order.length; i++) {
            final int idx = i;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, dp4, 0, dp4);

            // Tab name (number + label)
            TextView tvName = new TextView(this);
            tvName.setText((i + 1) + ". " + getTabLabel(order[i].trim()));
            tvName.setTextColor(getResources().getColor(R.color.textPrimary, null));
            tvName.setTextSize(15f);
            LinearLayout.LayoutParams nameParams =
                    new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            nameParams.gravity = android.view.Gravity.CENTER_VERTICAL;
            tvName.setLayoutParams(nameParams);
            row.addView(tvName);

            // ↑ button
            if (i > 0) {
                android.widget.Button btnUp = new android.widget.Button(this);
                btnUp.setText("↑");
                btnUp.setTextSize(16f);
                btnUp.setPadding(dp8, dp4, dp8, dp4);
                btnUp.setOnClickListener(v -> {
                    String tmp = order[idx - 1];
                    order[idx - 1] = order[idx];
                    order[idx] = tmp;
                    refreshTabOrderRows(container, order);
                });
                row.addView(btnUp);
            }

            // ↓ button
            if (i < order.length - 1) {
                android.widget.Button btnDown = new android.widget.Button(this);
                btnDown.setText("↓");
                btnDown.setTextSize(16f);
                btnDown.setPadding(dp8, dp4, dp8, dp4);
                btnDown.setOnClickListener(v -> {
                    String tmp = order[idx + 1];
                    order[idx + 1] = order[idx];
                    order[idx] = tmp;
                    refreshTabOrderRows(container, order);
                });
                row.addView(btnDown);
            }

            container.addView(row);
        }
    }

    // ── Helper views ───────────────────────────────────────────────────────

    private TextView sectionTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(getResources().getColor(R.color.colorAccentLight, null));
        tv.setTextSize(13f);
        int dp4  = dp(4);
        int dp16 = dp(16);
        tv.setPadding(0, dp16, 0, dp4);
        return tv;
    }

    private RadioButton makeRadioButton(String label, boolean checked) {
        RadioButton rb = new RadioButton(this);
        rb.setId(View.generateViewId());
        rb.setText(label);
        rb.setTextColor(getResources().getColor(R.color.textPrimary, null));
        rb.setPadding(dp(8), dp(6), dp(8), dp(6));
        rb.setChecked(checked);
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
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URI, uri.toString());
        startActivity(intent);
    }
}
