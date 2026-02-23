package com.videomaster.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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

    private final List<VideoItem> videoList   = new ArrayList<>();
    private final List<VideoItem> builtinList = new ArrayList<>();
    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

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
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_library) {
                showTab(0);
                return true;
            } else if (id == R.id.nav_playlist) {
                startActivity(new Intent(this, PlaylistActivity.class));
                return false; // don't visually select until back
            } else if (id == R.id.nav_builtin) {
                showTab(1);
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

        checkPermissionsAndLoad();
    }

    private void showTab(int tab) {
        tabLibrary.setVisibility(tab == 0 ? View.VISIBLE : View.GONE);
        tabBuiltin.setVisibility(tab == 1 ? View.VISIBLE : View.GONE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (tab == 0) toolbar.setTitle(R.string.library_title);
        else if (tab == 1) toolbar.setTitle(R.string.tab_builtin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tabLibrary.getVisibility() == View.VISIBLE) loadVideos();
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
        // Query video files
        items.addAll(queryMediaStoreUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_MODIFIED));
        // Query audio files
        items.addAll(queryMediaStoreUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.MIME_TYPE,
                MediaStore.Audio.Media.DATE_MODIFIED));

        // Sort by date modified descending
        items.sort((a, b) -> Long.compare(b.getLastModified(), a.getLastModified()));
        return items;
    }

    private List<VideoItem> queryMediaStoreUri(android.net.Uri contentUri,
            String colId, String colName, String colDuration,
            String colSize, String colMime, String colDate) {
        List<VideoItem> items = new ArrayList<>();
        String[] projection = {colId, colName, colDuration, colSize, colMime, colDate};
        String sortOrder = colDate + " DESC";
        try (Cursor cursor = getContentResolver().query(
                contentUri, projection, null, null, sortOrder)) {
            if (cursor == null) return items;
            int idIdx   = cursor.getColumnIndexOrThrow(colId);
            int nameIdx = cursor.getColumnIndexOrThrow(colName);
            int durIdx  = cursor.getColumnIndexOrThrow(colDuration);
            int sizeIdx = cursor.getColumnIndexOrThrow(colSize);
            int mimeIdx = cursor.getColumnIndexOrThrow(colMime);
            int dateIdx = cursor.getColumnIndexOrThrow(colDate);
            while (cursor.moveToNext()) {
                long id = cursor.getLong(idIdx);
                Uri  uri = Uri.withAppendedPath(contentUri, String.valueOf(id));
                items.add(new VideoItem(
                        cursor.getString(nameIdx),
                        uri,
                        cursor.getLong(durIdx),
                        cursor.getLong(sizeIdx),
                        cursor.getString(mimeIdx),
                        cursor.getLong(dateIdx) * 1000L));
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
