package com.videomaster.app.playlist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.videomaster.app.R;
import com.videomaster.app.model.MediaList;

import java.util.List;

/**
 * Screen that lists all user-created playlists.
 * Supports creating, renaming, and deleting playlists.
 */
public class PlaylistActivity extends AppCompatActivity {

    private MediaListManager mediaListManager;
    private PlaylistAdapter  adapter;
    private List<MediaList>  lists;
    private RecyclerView     recyclerView;
    private TextView         tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mediaListManager = MediaListManager.getInstance(this);
        lists            = mediaListManager.getLists();

        recyclerView = findViewById(R.id.recyclerPlaylists);
        tvEmpty      = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PlaylistAdapter(lists, new PlaylistAdapter.OnItemListener() {
            @Override public void onItemClick(MediaList list) {
                openPlaylistDetail(list);
            }
            @Override public void onItemLongClick(MediaList list) {
                showListOptions(list);
            }
        });
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabCreatePlaylist);
        fab.setOnClickListener(v -> showCreateDialog(null));

        refreshEmptyState();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
        refreshEmptyState();
    }

    // ── Create / Edit ──────────────────────────────────────────────────────

    private void showCreateDialog(MediaList existing) {
        View dialogView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);

        EditText etName     = new EditText(this);
        EditText etCategory = new EditText(this);
        etName.setHint(R.string.playlist_name_hint);
        etCategory.setHint(R.string.playlist_category_hint);
        etName.setTextColor(getResources().getColor(R.color.textPrimary, null));
        etCategory.setTextColor(getResources().getColor(R.color.textPrimary, null));
        int pad = (int) (16 * getResources().getDisplayMetrics().density);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
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
                    adapter.notifyDataSetChanged();
                    refreshEmptyState();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showListOptions(MediaList list) {
        String[] opts = {
                getString(R.string.playlist_rename),
                getString(R.string.playlist_delete)
        };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(list.getName())
                .setItems(opts, (d, which) -> {
                    if (which == 0) {
                        showCreateDialog(list);
                    } else {
                        confirmDelete(list);
                    }
                })
                .show();
    }

    private void confirmDelete(MediaList list) {
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.playlist_delete)
                .setMessage(getString(R.string.playlist_delete_confirm, list.getName()))
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    mediaListManager.deleteList(list.getId());
                    adapter.notifyDataSetChanged();
                    refreshEmptyState();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void openPlaylistDetail(MediaList list) {
        Intent intent = new Intent(this, PlaylistDetailActivity.class);
        intent.putExtra(PlaylistDetailActivity.EXTRA_LIST_ID, list.getId());
        startActivity(intent);
    }

    private void refreshEmptyState() {
        tvEmpty.setVisibility(lists.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(lists.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
