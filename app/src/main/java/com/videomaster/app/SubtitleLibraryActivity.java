package com.videomaster.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.videomaster.app.subtitle.SubtitleFileInfo;
import com.videomaster.app.subtitle.SubtitleLibraryManager;

import java.io.File;
import java.util.List;

/**
 * Subtitle library: shows all subtitle files saved locally.
 * Tap to select (returns result to caller), long-press to rename/delete.
 * FAB adds a new subtitle file to the library.
 *
 * Result extras:
 *   EXTRA_SELECTED_PATH (String) – absolute path of selected file
 */
public class SubtitleLibraryActivity extends AppCompatActivity {

    public static final String EXTRA_SELECTED_PATH = "selected_path";

    private SubtitleLibraryManager libraryManager;
    private LibraryAdapter         adapter;
    private RecyclerView           recyclerView;
    private TextView               tvEmpty;

    private final androidx.activity.result.ActivityResultLauncher<Intent> pickerLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) addToLibrary(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subtitle_library);

        libraryManager = SubtitleLibraryManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.rvLibrary);
        tvEmpty      = findViewById(R.id.tvLibraryEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAddSubtitle);
        fab.setOnClickListener(v -> pickSubtitleFile());

        refreshList();
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    // ── List management ────────────────────────────────────────────────────

    private void refreshList() {
        List<SubtitleFileInfo> items = libraryManager.getAll();
        adapter = new LibraryAdapter(items,
                info -> {
                    Intent result = new Intent();
                    result.putExtra(EXTRA_SELECTED_PATH, info.getPath());
                    setResult(Activity.RESULT_OK, result);
                    finish();
                },
                info -> showItemOptions(info));
        recyclerView.setAdapter(adapter);
        tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }

    // ── File import ────────────────────────────────────────────────────────

    private void pickSubtitleFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/x-subrip",
                "text/vtt",
                "text/x-ssa",
                "text/plain",
                "text/markdown",
                "text/x-markdown",
                "application/octet-stream"
        });
        pickerLauncher.launch(intent);
    }

    private void addToLibrary(Uri uri) {
        String name = getFileNameFromUri(uri);
        File saved = libraryManager.importFromUri(this, uri, name);
        if (saved != null) {
            Toast.makeText(this, R.string.subtitle_library_imported, Toast.LENGTH_SHORT).show();
            refreshList();
        } else {
            Toast.makeText(this, R.string.subtitle_library_import_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String name = "subtitle.srt";
        try (android.database.Cursor cursor = getContentResolver().query(
                uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = cursor.getString(idx);
            }
        } catch (Exception e) {
            String path = uri.getPath();
            if (path != null && path.contains("/")) {
                name = path.substring(path.lastIndexOf('/') + 1);
            }
        }
        return name;
    }

    // ── Item options ───────────────────────────────────────────────────────

    private void showItemOptions(SubtitleFileInfo info) {
        String[] opts = {
                getString(R.string.subtitle_library_rename),
                getString(R.string.subtitle_library_delete)
        };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(info.getName())
                .setItems(opts, (d, which) -> {
                    if (which == 0) showRenameDialog(info);
                    else            showDeleteConfirm(info);
                })
                .show();
    }

    private void showRenameDialog(SubtitleFileInfo info) {
        EditText input = new EditText(this);
        input.setText(info.getName());
        input.setTextColor(getResources().getColor(R.color.textPrimary, null));
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad / 2, pad, pad / 2);
        input.setSelectAllOnFocus(true);

        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.subtitle_library_rename)
                .setView(input)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty() && !newName.equals(info.getName())) {
                        boolean ok = libraryManager.rename(info.getName(), newName);
                        if (ok) {
                            refreshList();
                        } else {
                            Toast.makeText(this, R.string.subtitle_library_rename_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteConfirm(SubtitleFileInfo info) {
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setMessage(getString(R.string.subtitle_library_delete_confirm, info.getName()))
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    libraryManager.delete(info.getName());
                    refreshList();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ── Inner RecyclerView Adapter ─────────────────────────────────────────

    private static class LibraryAdapter extends RecyclerView.Adapter<LibraryAdapter.VH> {

        interface OnClick     { void on(SubtitleFileInfo info); }
        interface OnLongClick { void on(SubtitleFileInfo info); }

        private final List<SubtitleFileInfo> items;
        private final OnClick     onClick;
        private final OnLongClick onLongClick;

        LibraryAdapter(List<SubtitleFileInfo> items, OnClick onClick, OnLongClick onLongClick) {
            this.items       = items;
            this.onClick     = onClick;
            this.onLongClick = onLongClick;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.widget.TextView tv = new android.widget.TextView(parent.getContext());
            tv.setTextColor(0xFFFFFFFF);
            tv.setTextSize(15f);
            int pad = (int)(16 * parent.getContext().getResources().getDisplayMetrics().density);
            tv.setPadding(pad, pad, pad, pad);
            tv.setBackgroundResource(com.videomaster.app.R.drawable.selector_panel_item);
            tv.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT));
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            SubtitleFileInfo info = items.get(position);
            holder.tv.setText(info.getName());
            holder.tv.setOnClickListener(v -> onClick.on(info));
            holder.tv.setOnLongClickListener(v -> { onLongClick.on(info); return true; });
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final android.widget.TextView tv;
            VH(android.widget.TextView tv) { super(tv); this.tv = tv; }
        }
    }
}
