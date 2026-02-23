package com.videomaster.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
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
import com.videomaster.app.util.FileUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Adapter for the in-player playlist side panel.
 * Highlights the currently playing item, shows an async thumbnail for each entry,
 * and dispatches clicks for switching.
 */
public class PlayerPlaylistAdapter
        extends RecyclerView.Adapter<PlayerPlaylistAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(int index);
    }

    private final List<String>        uris;
    private       int                 currentIndex;
    private final OnItemClickListener listener;

    // Thumbnail cache keyed by URI string
    private final Map<String, Bitmap> thumbCache    = new HashMap<>();
    private final ExecutorService     thumbExecutor = Executors.newFixedThreadPool(2);
    private final Handler             mainHandler   = new Handler(Looper.getMainLooper());

    public PlayerPlaylistAdapter(List<String> uris, int currentIndex,
                                 OnItemClickListener listener) {
        this.uris         = uris;
        this.currentIndex = currentIndex;
        this.listener     = listener;
    }

    /** Update the highlighted item without rebuilding the whole list. */
    public void setCurrentIndex(int index) {
        int old = currentIndex;
        currentIndex = index;
        if (old >= 0 && old < uris.size()) notifyItemChanged(old);
        if (index >= 0 && index < uris.size()) notifyItemChanged(index);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_player_playlist, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        String uriStr = uris.get(position);

        // File name
        String name;
        try {
            name = FileUtils.getFileName(holder.itemView.getContext(), Uri.parse(uriStr));
        } catch (Exception e) {
            name = Uri.parse(uriStr).getLastPathSegment();
            if (name == null) name = uriStr;
        }
        holder.tvIndex.setText(String.valueOf(position + 1));
        holder.tvName.setText(name);
        holder.itemView.setActivated(position == currentIndex);

        // Thumbnail
        loadThumbnail(holder, uriStr);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_ID) listener.onItemClick(pos);
        });
    }

    @Override
    public int getItemCount() { return uris.size(); }

    // ── Thumbnail loading ──────────────────────────────────────────────────

    private void loadThumbnail(@NonNull VH holder, String uriStr) {
        // Show cached result immediately
        Bitmap cached = thumbCache.get(uriStr);
        if (cached != null) {
            showThumb(holder, cached);
            return;
        }
        // Reset to fallback while loading
        holder.imgThumb.setVisibility(View.GONE);
        holder.imgFallback.setVisibility(View.VISIBLE);
        // Tag to prevent stale updates after recycling
        holder.itemView.setTag(uriStr);

        thumbExecutor.execute(() -> {
            Bitmap bmp = fetchThumbnail(holder.itemView.getContext(), uriStr);
            if (bmp != null) {
                thumbCache.put(uriStr, bmp);
            }
            final Bitmap finalBmp = bmp;
            mainHandler.post(() -> {
                if (uriStr.equals(holder.itemView.getTag())) {
                    showThumb(holder, finalBmp);
                }
            });
        });
    }

    private void showThumb(@NonNull VH holder, Bitmap bmp) {
        if (bmp != null) {
            holder.imgThumb.setImageBitmap(bmp);
            holder.imgThumb.setVisibility(View.VISIBLE);
            holder.imgFallback.setVisibility(View.GONE);
        } else {
            holder.imgThumb.setVisibility(View.GONE);
            holder.imgFallback.setVisibility(View.VISIBLE);
        }
    }

    /** Fetch a thumbnail for any URI (video frame or audio album art). */
    private Bitmap fetchThumbnail(Context context, String uriStr) {
        try {
            Uri uri = Uri.parse(uriStr);

            // Fast path: ContentResolver thumbnail (works for MediaStore content:// URIs)
            try {
                return context.getContentResolver()
                        .loadThumbnail(uri, new Size(120, 90), null);
            } catch (Exception ignored) {}

            // Fallback: MediaMetadataRetriever (works for SAF document URIs)
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(context, uri);

                // Try video frame first
                Bitmap frame = mmr.getFrameAtTime(
                        0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (frame != null) {
                    // Scale down to avoid excessive memory usage
                    int w = frame.getWidth(), h = frame.getHeight();
                    if (w > 0 && h > 0) {
                        int tw = 120, th = (int) (120f * h / w);
                        return Bitmap.createScaledBitmap(frame, tw, Math.max(1, th), true);
                    }
                    return frame;
                }

                // Try embedded audio album art
                byte[] art = mmr.getEmbeddedPicture();
                if (art != null && art.length > 0) {
                    Bitmap raw = BitmapFactory.decodeByteArray(art, 0, art.length);
                    if (raw != null) {
                        return Bitmap.createScaledBitmap(raw, 90, 90, true);
                    }
                }
            } finally {
                mmr.release();
            }
        } catch (Exception ignored) {}
        return null;
    }

    // ── ViewHolder ─────────────────────────────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {
        final TextView  tvIndex;
        final TextView  tvName;
        final ImageView imgThumb;
        final ImageView imgFallback;

        VH(@NonNull View v) {
            super(v);
            tvIndex    = v.findViewById(R.id.tvPanelItemIndex);
            tvName     = v.findViewById(R.id.tvPanelItemName);
            imgThumb   = v.findViewById(R.id.imgPanelThumb);
            imgFallback = v.findViewById(R.id.imgPanelThumbFallback);
        }
    }
}
