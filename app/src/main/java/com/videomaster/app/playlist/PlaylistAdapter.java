package com.videomaster.app.playlist;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.model.MediaList;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Adapter for the playlist list tab.
 * Supports a square GRID mode (with custom thumbnails) and a traditional LIST mode.
 */
public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface OnItemListener {
        void onItemClick(MediaList list);
        void onItemLongClick(MediaList list);
    }

    public enum ViewMode { GRID, LIST }

    private static final String THUMB_PREFS = "playlist_thumbnails";

    private final List<MediaList> items;
    private final OnItemListener  listener;
    private ViewMode              viewMode = ViewMode.GRID;

    private final ExecutorService thumbExecutor = Executors.newFixedThreadPool(2);
    private final Handler         mainHandler   = new Handler(Looper.getMainLooper());

    public PlaylistAdapter(List<MediaList> items, OnItemListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void setViewMode(ViewMode mode) {
        if (this.viewMode != mode) {
            this.viewMode = mode;
            notifyDataSetChanged();
        }
    }

    public ViewMode getViewMode() { return viewMode; }

    @Override
    public int getItemViewType(int position) {
        return viewMode == ViewMode.GRID ? 1 : 0;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == 1) ? R.layout.item_playlist_grid : R.layout.item_playlist;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);

        // Make the thumbnail square by setting its height = card width once laid out
        if (viewType == 1) {
            v.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {
                    v.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    ImageView iv = v.findViewById(R.id.ivPlaylistThumb);
                    if (iv != null && v.getWidth() > 0 && iv.getHeight() != v.getWidth()) {
                        ViewGroup.LayoutParams lp = iv.getLayoutParams();
                        lp.height = v.getWidth();
                        iv.setLayoutParams(lp);
                    }
                }
            });
        }
        return new ViewHolder(v, viewType == 1);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MediaList list = items.get(position);
        h.tvName.setText(list.getName());

        String category = list.getCategory();
        if (h.tvCategory != null) {
            if (category != null && !category.isEmpty()) {
                h.tvCategory.setVisibility(View.VISIBLE);
                h.tvCategory.setText(category);
            } else {
                h.tvCategory.setVisibility(View.GONE);
            }
        }

        h.tvCount.setText(h.itemView.getContext()
                .getString(R.string.playlist_item_count, list.size()));

        // Load thumbnail for grid cells
        if (h.isGrid && h.ivThumb != null) {
            h.ivThumb.setImageResource(R.drawable.ic_playlist);
            loadThumbnail(h.itemView.getContext(), list.getId(), h);
        }

        h.itemView.setOnClickListener(v -> listener.onItemClick(list));
        h.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(list);
            return true;
        });
    }

    @Override public int getItemCount() { return items.size(); }

    // ── Thumbnail loading ──────────────────────────────────────────────────

    private void loadThumbnail(Context context, String listId, ViewHolder holder) {
        thumbExecutor.execute(() -> {
            Bitmap bmp = null;
            // Check for custom thumbnail
            SharedPreferences thumbPrefs =
                    context.getSharedPreferences(THUMB_PREFS, Context.MODE_PRIVATE);
            String customPath = thumbPrefs.getString(listId, null);
            if (customPath != null) {
                File f = new File(customPath);
                if (f.exists()) {
                    bmp = BitmapFactory.decodeFile(customPath);
                }
            }
            // Fall back to default playlist icon (null bmp = use default icon set in bind)
            final Bitmap finalBmp = bmp;
            mainHandler.post(() -> {
                if (holder.ivThumb == null) return;
                if (finalBmp != null) {
                    holder.ivThumb.setImageBitmap(finalBmp);
                } else {
                    holder.ivThumb.setImageResource(R.drawable.ic_playlist);
                    holder.ivThumb.setColorFilter(
                            context.getResources().getColor(R.color.colorAccent, null));
                }
            });
        });
    }

    // ── ViewHolder ─────────────────────────────────────────────────────────

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvName, tvCategory, tvCount;
        ImageView ivThumb;
        boolean   isGrid;

        ViewHolder(@NonNull View v, boolean isGrid) {
            super(v);
            this.isGrid   = isGrid;
            tvName        = v.findViewById(R.id.tvPlaylistName);
            tvCategory    = v.findViewById(R.id.tvPlaylistCategory);
            tvCount       = v.findViewById(R.id.tvPlaylistCount);
            ivThumb       = v.findViewById(R.id.ivPlaylistThumb);
        }
    }
}
