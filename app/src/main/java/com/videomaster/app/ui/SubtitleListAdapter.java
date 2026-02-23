package com.videomaster.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.subtitle.SubtitleEntry;
import com.videomaster.app.util.TimeUtils;

import java.util.List;

/**
 * Displays all subtitle entries in a panel.
 * Supports toggling timestamp visibility and highlighting the active entry.
 */
public class SubtitleListAdapter extends RecyclerView.Adapter<SubtitleListAdapter.SubtitleViewHolder> {

    public interface OnEntryClickListener {
        void onEntryClick(SubtitleEntry entry);
    }

    private final List<SubtitleEntry> entries;
    private boolean showTimestamps = true;
    private int     activeIndex    = -1;
    private final OnEntryClickListener clickListener;

    public SubtitleListAdapter(List<SubtitleEntry> entries, OnEntryClickListener clickListener) {
        this.entries       = entries;
        this.clickListener = clickListener;
    }

    public void setShowTimestamps(boolean show) {
        this.showTimestamps = show;
        notifyDataSetChanged();
    }

    public boolean isShowTimestamps() {
        return showTimestamps;
    }

    /**
     * Highlights the entry at {@code index} as currently active.
     * Pass -1 to clear the highlight.
     */
    public void setActiveIndex(int index) {
        int old = activeIndex;
        activeIndex = index;
        if (old >= 0) notifyItemChanged(old);
        if (index >= 0) notifyItemChanged(index);
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    @NonNull
    @Override
    public SubtitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtitle_entry, parent, false);
        return new SubtitleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtitleViewHolder holder, int position) {
        SubtitleEntry entry = entries.get(position);

        if (showTimestamps) {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvTime.setText(
                    TimeUtils.formatDuration(entry.getStartTimeMs())
                    + "  →  "
                    + TimeUtils.formatDuration(entry.getEndTimeMs()));
        } else {
            holder.tvTime.setVisibility(View.GONE);
        }

        holder.tvText.setText(entry.getPlainText());

        // Highlight active entry
        boolean isActive = (position == activeIndex);
        holder.itemView.setBackgroundColor(isActive ? 0x33E94560 : 0x00000000);

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onEntryClick(entry);
        });
    }

    @Override
    public int getItemCount() { return entries.size(); }

    static class SubtitleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvText;

        SubtitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvSubtitleTime);
            tvText = itemView.findViewById(R.id.tvSubtitleText);
        }
    }
}
