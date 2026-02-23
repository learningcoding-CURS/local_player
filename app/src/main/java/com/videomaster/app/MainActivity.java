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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
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

    private static final String PREFS_NAME           = SettingsActivity.PREFS_NAME;
    private static final String PREF_DEFAULT_TAB     = SettingsActivity.PREF_DEFAULT_TAB;
    private static final String PREF_TAB_ORDER       = SettingsActivity.PREF_TAB_ORDER;
    private static final String PREF_VIEW_MODE       = SettingsActivity.PREF_VIEW_MODE;
    private static final String DEFAULT_TAB_ORDER    = SettingsActivity.DEFAULT_TAB_ORDER;

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

    private final ActivityResultLauncher<Intent> settingsLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                // Reload settings after returning from SettingsActivity
                applyTabOrderAndDefault();
                applyViewMode();
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
        recyclerView.setAdapter(adapter);

        // Built-in tab
        recyclerBuiltin = findViewById(R.id.recyclerBuiltin);
        tvBuiltinEmpty  = findViewById(R.id.tvBuiltinEmpty);
        builtinAdapter  = new VideoAdapter(builtinList, item -> openPlayer(item.getUri()));
        recyclerBuiltin.setAdapter(builtinAdapter);

        applyViewMode();

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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            settingsLauncher.launch(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_toggle_view) {
            toggleViewMode();
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

    // ── View mode (grid / list) ────────────────────────────────────────────

    private void applyViewMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(PREF_VIEW_MODE, SettingsActivity.VIEW_MODE_GRID);
        boolean isGrid = SettingsActivity.VIEW_MODE_GRID.equals(mode);

        int spanCount = isGrid ? 2 : 1;
        adapter.setViewMode(isGrid
                ? VideoAdapter.ViewMode.GRID : VideoAdapter.ViewMode.LIST);
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        builtinAdapter.setViewMode(isGrid
                ? VideoAdapter.ViewMode.GRID : VideoAdapter.ViewMode.LIST);
        recyclerBuiltin.setLayoutManager(new GridLayoutManager(this, spanCount));

        invalidateOptionsMenu();
    }

    private void toggleViewMode() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String current = prefs.getString(PREF_VIEW_MODE, SettingsActivity.VIEW_MODE_GRID);
        String next = SettingsActivity.VIEW_MODE_GRID.equals(current)
                ? SettingsActivity.VIEW_MODE_LIST
                : SettingsActivity.VIEW_MODE_GRID;
        prefs.edit().putString(PREF_VIEW_MODE, next).apply();
        applyViewMode();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem toggleItem = menu.findItem(R.id.action_toggle_view);
        if (toggleItem != null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String mode = prefs.getString(PREF_VIEW_MODE, SettingsActivity.VIEW_MODE_GRID);
            boolean isGrid = SettingsActivity.VIEW_MODE_GRID.equals(mode);
            toggleItem.setIcon(isGrid ? R.drawable.ic_view_list : R.drawable.ic_view_grid);
            toggleItem.setTitle(isGrid ? R.string.settings_view_list : R.string.settings_view_grid);
        }
        return super.onPrepareOptionsMenu(menu);
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
