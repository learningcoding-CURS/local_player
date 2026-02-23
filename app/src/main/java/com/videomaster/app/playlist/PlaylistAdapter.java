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

        // Load thumbnail for both grid and list cells
        if (h.ivThumb != null) {
            h.ivThumb.clearColorFilter();
            h.ivThumb.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
            h.ivThumb.setImageResource(R.drawable.ic_playlist);
            h.ivThumb.setColorFilter(
                    h.itemView.getContext().getResources()
                            .getColor(R.color.colorAccent, null));
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
        // Tag the view so we can detect if the ViewHolder was recycled before the load finishes
        if (holder.ivThumb != null) holder.ivThumb.setTag(R.id.ivPlaylistThumb, listId);

        thumbExecutor.execute(() -> {
            Bitmap bmp = null;
            SharedPreferences thumbPrefs =
                    context.getSharedPreferences(THUMB_PREFS, Context.MODE_PRIVATE);
            String customPath = thumbPrefs.getString(listId, null);
            if (customPath != null) {
                File f = new File(customPath);
                if (f.exists()) {
                    try {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inSampleSize = 1;
                        bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
                    } catch (Exception ignored) {}
                }
            }
            final Bitmap finalBmp = bmp;
            mainHandler.post(() -> {
                if (holder.ivThumb == null) return;
                // Only apply if the view is still bound to the same list item
                Object tag = holder.ivThumb.getTag(R.id.ivPlaylistThumb);
                if (!listId.equals(tag)) return;

                if (finalBmp != null) {
                    holder.ivThumb.clearColorFilter();
                    holder.ivThumb.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    holder.ivThumb.setImageBitmap(finalBmp);
                } else {
                    holder.ivThumb.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
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
            this.isGrid = isGrid;
            tvName      = v.findViewById(R.id.tvPlaylistName);
            tvCategory  = v.findViewById(R.id.tvPlaylistCategory);
            tvCount     = v.findViewById(R.id.tvPlaylistCount);
            ivThumb     = v.findViewById(R.id.ivPlaylistThumb);
        }
    }
}
