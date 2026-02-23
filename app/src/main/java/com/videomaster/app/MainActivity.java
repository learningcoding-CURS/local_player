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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.videomaster.app.ui.VideoAdapter;
import com.videomaster.app.model.VideoItem;
import com.videomaster.app.util.FileUtils;
import com.videomaster.app.util.PermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView      recyclerView;
    private VideoAdapter      adapter;
    private TextView          emptyView;
    private View              loadingView;

    private final List<VideoItem>        videoList = new ArrayList<>();
    private final ExecutorService        executor  = Executors.newSingleThreadExecutor();
    private final Handler                mainHandler = new Handler(Looper.getMainLooper());

    // Launcher for file picker (open video from intent)
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

        recyclerView = findViewById(R.id.recyclerView);
        emptyView    = findViewById(R.id.tvEmpty);
        loadingView  = findViewById(R.id.loadingView);

        FloatingActionButton fabOpen = findViewById(R.id.fabOpen);
        fabOpen.setOnClickListener(v -> pickVideoFile());

        adapter = new VideoAdapter(videoList, item -> openPlayer(item.getUri()));
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);

        // Handle VIEW intent from other apps
        if (Intent.ACTION_VIEW.equals(getIntent().getAction()) && getIntent().getData() != null) {
            openPlayer(getIntent().getData());
            return;
        }

        checkPermissionsAndLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVideos();
    }

    // ──────────────────────── Permission handling ────────────────────────

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

    // ──────────────────────── Video loading ────────────────────────

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
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DATE_MODIFIED
        };
        String sortOrder = MediaStore.Video.Media.DATE_MODIFIED + " DESC";
        try (Cursor cursor = getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder)) {
            if (cursor == null) return items;
            int idIdx    = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameIdx  = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
            int durIdx   = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int sizeIdx  = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
            int mimeIdx  = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
            int dateIdx  = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED);
            while (cursor.moveToNext()) {
                long id  = cursor.getLong(idIdx);
                Uri uri  = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        String.valueOf(id));
                items.add(new VideoItem(
                        cursor.getString(nameIdx),
                        uri,
                        cursor.getLong(durIdx),
                        cursor.getLong(sizeIdx),
                        cursor.getString(mimeIdx),
                        cursor.getLong(dateIdx) * 1000L
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    // ──────────────────────── Navigation ────────────────────────

    private void pickVideoFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
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
