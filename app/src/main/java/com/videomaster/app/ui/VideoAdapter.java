package com.videomaster.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.model.VideoItem;
import com.videomaster.app.util.FileUtils;
import com.videomaster.app.util.TimeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public enum ViewMode { GRID, LIST }

    public interface OnItemClickListener {
        void onItemClick(VideoItem item);
    }

    private static final int TYPE_GRID = 0;
    private static final int TYPE_LIST = 1;

    private final List<VideoItem>     items;
    private final OnItemClickListener listener;
    private ViewMode viewMode = ViewMode.GRID;

    // Thumbnail cache (URI string → Bitmap)
    private final Map<String, Bitmap> thumbCache = new HashMap<>();
    private final ExecutorService     thumbExecutor = Executors.newFixedThreadPool(3);
    private final Handler             mainHandler   = new Handler(Looper.getMainLooper());

    public VideoAdapter(List<VideoItem> items, OnItemClickListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void setViewMode(ViewMode mode) {
        if (this.viewMode != mode) {
            this.viewMode = mode;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return viewMode == ViewMode.GRID ? TYPE_GRID : TYPE_LIST;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == TYPE_GRID)
                ? R.layout.item_video
                : R.layout.item_video_list;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvDuration.setText(TimeUtils.formatDuration(item.getDurationMs()));
        holder.tvSize.setText(FileUtils.formatSize(item.getFileSizeBytes()));
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));

        // Load thumbnail
        loadThumbnail(holder, item);
    }

    private void loadThumbnail(@NonNull VideoViewHolder holder, VideoItem item) {
        String key = item.getUri().toString();

        // Show cached thumbnail immediately if available
        Bitmap cached = thumbCache.get(key);
        if (cached != null) {
            showThumbnail(holder, cached);
            return;
        }

        // Reset to fallback while loading
        holder.imgThumbnail.setVisibility(View.GONE);
        holder.imgThumbnailFallback.setVisibility(View.VISIBLE);

        // Tag holder to avoid stale updates after recycling
        holder.itemView.setTag(key);

        // Load asynchronously
        thumbExecutor.execute(() -> {
            Bitmap bmp = fetchThumbnail(holder.itemView.getContext(), item);
            if (bmp != null) {
                thumbCache.put(key, bmp);
                final Bitmap finalBmp = bmp;
                mainHandler.post(() -> {
                    // Only apply if holder still represents this item
                    if (key.equals(holder.itemView.getTag())) {
                        showThumbnail(holder, finalBmp);
                    }
                });
            }
        });
    }

    private Bitmap fetchThumbnail(Context context, VideoItem item) {
        try {
            Uri uri = item.getUri();
            String mime = item.getMimeType();
            if (mime != null && mime.startsWith("audio/")) {
                // Audio: try to extract embedded album art
                android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
                try {
                    mmr.setDataSource(context, uri);
                    byte[] art = mmr.getEmbeddedPicture();
                    if (art != null && art.length > 0) {
                        return android.graphics.BitmapFactory.decodeByteArray(art, 0, art.length);
                    }
                } finally {
                    mmr.release();
                }
            } else {
                // Video: use ContentResolver.loadThumbnail (API 29+)
                return context.getContentResolver().loadThumbnail(uri, new Size(200, 200), null);
            }
        } catch (Exception e) {
            // Thumbnail not available — use fallback icon
        }
        return null;
    }

    private void showThumbnail(@NonNull VideoViewHolder holder, Bitmap bmp) {
        holder.imgThumbnail.setImageBitmap(bmp);
        holder.imgThumbnail.setVisibility(View.VISIBLE);
        holder.imgThumbnailFallback.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView  tvTitle, tvDuration, tvSize;
        ImageView imgThumbnail, imgThumbnailFallback;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle              = itemView.findViewById(R.id.tvTitle);
            tvDuration           = itemView.findViewById(R.id.tvDuration);
            tvSize               = itemView.findViewById(R.id.tvSize);
            imgThumbnail         = itemView.findViewById(R.id.imgThumbnail);
            imgThumbnailFallback = itemView.findViewById(R.id.imgThumbnailFallback);
        }
    }
}
