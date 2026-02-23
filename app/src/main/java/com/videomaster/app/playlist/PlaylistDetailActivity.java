package com.videomaster.app.playlist;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.videomaster.app.PlayerActivity;
import com.videomaster.app.R;
import com.videomaster.app.model.MediaList;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows all media items inside a specific playlist,
 * with per-item playback progress bars and play controls.
 */
public class PlaylistDetailActivity extends AppCompatActivity {

    public static final String EXTRA_LIST_ID = "extra_list_id";

    private MediaListManager    mediaListManager;
    private MediaList           mediaList;
    private MediaItemListAdapter adapter;
    private RecyclerView        recyclerView;
    private TextView            tvEmpty;

    private final androidx.activity.result.ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        // Persist permission
                        getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        mediaListManager.addItemToList(mediaList.getId(), uri.toString());
                        refreshList();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        String listId = getIntent().getStringExtra(EXTRA_LIST_ID);
        mediaListManager = MediaListManager.getInstance(this);
        mediaList        = mediaListManager.getList(listId);

        if (mediaList == null) {
            Toast.makeText(this, R.string.error_no_video, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(mediaList.getName());
        }

        recyclerView = findViewById(R.id.recyclerItems);
        tvEmpty      = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAddMedia);
        fab.setOnClickListener(v -> pickMediaFile());

        refreshList();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    // ── List building ──────────────────────────────────────────────────────

    private void refreshList() {
        List<String> uris = mediaList.getItemUris();
        int[] progPercents = new int[uris.size()];
        for (int i = 0; i < uris.size(); i++) {
            progPercents[i] = mediaListManager.getProgressPercent(uris.get(i));
        }

        adapter = new MediaItemListAdapter(uris, progPercents,
                new MediaItemListAdapter.OnItemListener() {
                    @Override public void onItemClick(int index, String uriString) {
                        playFromIndex(index);
                    }
                    @Override public void onItemLongClick(int index, String uriString) {
                        showItemOptions(index, uriString);
                    }
                });
        recyclerView.setAdapter(adapter);
        tvEmpty.setVisibility(uris.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(uris.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ── Playback ───────────────────────────────────────────────────────────

    private void playFromIndex(int index) {
        List<String> uris = mediaList.getItemUris();
        if (uris.isEmpty()) return;

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra(PlayerActivity.EXTRA_VIDEO_URI, uris.get(index));
        intent.putStringArrayListExtra(PlayerActivity.EXTRA_PLAYLIST_URIS,
                new ArrayList<>(uris));
        intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_INDEX, index);
        intent.putExtra(PlayerActivity.EXTRA_PLAYLIST_ID, mediaList.getId());
        startActivity(intent);
    }

    // ── Media file picker ──────────────────────────────────────────────────

    private void pickMediaFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"video/*", "audio/*"});
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        filePickerLauncher.launch(intent);
    }

    // ── Item options ───────────────────────────────────────────────────────

    private void showItemOptions(int index, String uriString) {
        String[] opts = {
                getString(R.string.playlist_play_from_here),
                getString(R.string.playlist_remove_item)
        };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setItems(opts, (d, which) -> {
                    if (which == 0) {
                        playFromIndex(index);
                    } else {
                        mediaListManager.removeItemFromList(mediaList.getId(), uriString);
                        refreshList();
                    }
                })
                .show();
    }
}
