package com.videomaster.app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.model.VideoItem;
import com.videomaster.app.util.FileUtils;
import com.videomaster.app.util.TimeUtils;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(VideoItem item);
    }

    private final List<VideoItem>     items;
    private final OnItemClickListener listener;

    public VideoAdapter(List<VideoItem> items, OnItemClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDuration.setText(TimeUtils.formatDuration(item.getDurationMs()));
        holder.tvSize.setText(FileUtils.formatSize(item.getFileSizeBytes()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDuration, tvSize;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvSize     = itemView.findViewById(R.id.tvSize);
        }
    }
}
