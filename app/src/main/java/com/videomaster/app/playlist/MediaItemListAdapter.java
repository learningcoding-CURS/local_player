package com.videomaster.app.playlist;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.util.FileUtils;

import java.util.List;

/**
 * Adapter for showing media items inside a playlist, with per-item playback progress bars.
 */
public class MediaItemListAdapter extends RecyclerView.Adapter<MediaItemListAdapter.ViewHolder> {

    public interface OnItemListener {
        void onItemClick(int index, String uriString);
        void onItemLongClick(int index, String uriString);
    }

    private final List<String>     uriStrings;
    private final int[]            progressPercents;
    private final OnItemListener   listener;

    public MediaItemListAdapter(List<String> uriStrings, int[] progressPercents,
                                OnItemListener listener) {
        this.uriStrings       = uriStrings;
        this.progressPercents = progressPercents;
        this.listener         = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media_progress, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        String uriStr = uriStrings.get(position);

        // Extract a display name from the URI
        String displayName;
        try {
            Uri uri = Uri.parse(uriStr);
            String path = uri.getLastPathSegment();
            displayName = (path != null) ? path : uriStr;
        } catch (Exception e) {
            displayName = uriStr;
        }
        h.tvTitle.setText(displayName);
        h.tvIndex.setText(String.valueOf(position + 1));

        int prog = (progressPercents != null && position < progressPercents.length)
                ? progressPercents[position] : 0;
        h.progressBar.setProgress(prog);
        if (prog > 0) {
            h.tvProgress.setVisibility(View.VISIBLE);
            h.tvProgress.setText(prog + "%");
        } else {
            h.tvProgress.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != androidx.recyclerview.widget.RecyclerView.NO_ID && pos < uriStrings.size()) {
                listener.onItemClick(pos, uriStrings.get(pos));
            }
        });
        h.itemView.setOnLongClickListener(v -> {
            int pos = h.getAdapterPosition();
            if (pos != androidx.recyclerview.widget.RecyclerView.NO_ID && pos < uriStrings.size()) {
                listener.onItemLongClick(pos, uriStrings.get(pos));
            }
            return true;
        });
    }

    @Override public int getItemCount() { return uriStrings.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView    tvIndex, tvTitle, tvProgress;
        ProgressBar progressBar;

        ViewHolder(@NonNull View v) {
            super(v);
            tvIndex     = v.findViewById(R.id.tvItemIndex);
            tvTitle     = v.findViewById(R.id.tvItemTitle);
            tvProgress  = v.findViewById(R.id.tvItemProgress);
            progressBar = v.findViewById(R.id.itemProgressBar);
        }
    }
}
