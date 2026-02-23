package com.videomaster.app.stats;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.util.TimeUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

/**
 * Displays a horizontal bar chart of cumulative playback time per playlist,
 * sorted from most-played to least-played.
 */
public class StatsActivity extends AppCompatActivity {

    private RecyclerView recyclerStats;
    private TextView     tvEmpty;
    private TextView     tvSummary;

    private final ActivityResultLauncher<Intent> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) writeExportFile(uri);
                }
            });

    private final ActivityResultLauncher<Intent> importLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) readImportFile(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        Toolbar toolbar = findViewById(R.id.toolbarStats);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerStats = findViewById(R.id.recyclerStats);
        tvEmpty       = findViewById(R.id.tvStatsEmpty);
        tvSummary     = findViewById(R.id.tvStatsSummary);

        recyclerStats.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnClearStats).setOnClickListener(v -> confirmClear());
        findViewById(R.id.btnExportStats).setOnClickListener(v -> startExport());
        findViewById(R.id.btnImportStats).setOnClickListener(v -> startImport());

        loadStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Data loading ─────────────────────────────────────────────────────────

    private void loadStats() {
        PlayStats stats = PlayStats.getInstance(this);
        List<PlayStats.StatEntry> entries = stats.getStats();

        if (entries.isEmpty()) {
            recyclerStats.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
            tvSummary.setText(R.string.stats_empty);
            return;
        }

        recyclerStats.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        long totalMs = stats.getTotalMs();
        tvSummary.setText(getString(R.string.stats_summary,
                entries.size(), TimeUtils.formatDuration(totalMs)));

        long maxMs = entries.get(0).totalMs; // already sorted descending
        recyclerStats.setAdapter(new StatsAdapter(entries, maxMs));
    }

    private void confirmClear() {
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.stats_clear_title)
                .setMessage(R.string.stats_clear_message)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    PlayStats.getInstance(this).clearAll();
                    loadStats();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ── Export / Import ────────────────────────────────────────────────────────

    private void startExport() {
        PlayStats stats = PlayStats.getInstance(this);
        if (stats.getStats().isEmpty()) {
            Toast.makeText(this, R.string.stats_no_data, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "videomaster_stats.json");
        exportLauncher.launch(intent);
    }

    private void writeExportFile(Uri uri) {
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            if (out == null) throw new Exception("null stream");
            String json = PlayStats.getInstance(this).exportToJson();
            out.write(json.getBytes("UTF-8"));
            out.flush();
            Toast.makeText(this, R.string.stats_export_success, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.stats_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void startImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/json", "text/plain", "application/octet-stream"});
        importLauncher.launch(intent);
    }

    private void readImportFile(Uri uri) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getContentResolver().openInputStream(uri), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            int count = PlayStats.getInstance(this).importFromJson(sb.toString());
            if (count >= 0) {
                Toast.makeText(this, getString(R.string.stats_import_success, count),
                        Toast.LENGTH_SHORT).show();
                loadStats();
            } else {
                Toast.makeText(this, R.string.stats_import_failed, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.stats_import_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // ── Inner adapter ─────────────────────────────────────────────────────────

    private static class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.VH> {

        private final List<PlayStats.StatEntry> entries;
        private final long maxMs;

        StatsAdapter(List<PlayStats.StatEntry> entries, long maxMs) {
            this.entries = entries;
            this.maxMs   = maxMs;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = android.view.LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_stats_bar, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            PlayStats.StatEntry e = entries.get(pos);

            // Rank prefix
            h.tvName.setText(String.format(Locale.getDefault(), "%d. %s", pos + 1, e.name));
            h.tvTime.setText(TimeUtils.formatDuration(e.totalMs));

            // Bar fill: 0–1000 out of maxMs
            int progress = maxMs > 0 ? (int) (e.totalMs * 1000L / maxMs) : 0;
            h.progressBar.setProgress(progress);
        }

        @Override
        public int getItemCount() { return entries.size(); }

        static class VH extends RecyclerView.ViewHolder {
            final TextView    tvName;
            final TextView    tvTime;
            final ProgressBar progressBar;

            VH(@NonNull View v) {
                super(v);
                tvName      = v.findViewById(R.id.tvStatName);
                tvTime      = v.findViewById(R.id.tvStatTime);
                progressBar = v.findViewById(R.id.pbStatBar);
            }
        }
    }
}
