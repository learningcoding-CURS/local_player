package com.videomaster.app.stats;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.util.TimeUtils;

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
