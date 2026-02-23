package com.videomaster.app.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.util.FileUtils;

import java.util.List;

/**
 * Adapter for the in-player playlist side panel.
 * Highlights the currently playing item and dispatches clicks for switching.
 *
 * Extensibility: implement {@link OnItemClickListener} to handle playback switching.
 */
public class PlayerPlaylistAdapter
        extends RecyclerView.Adapter<PlayerPlaylistAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(int index);
    }

    private final List<String>      uris;
    private       int               currentIndex;
    private final OnItemClickListener listener;

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
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (listener != null && pos != RecyclerView.NO_ID) listener.onItemClick(pos);
        });
    }

    @Override
    public int getItemCount() { return uris.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvIndex;
        final TextView tvName;

        VH(@NonNull View v) {
            super(v);
            tvIndex = v.findViewById(R.id.tvPanelItemIndex);
            tvName  = v.findViewById(R.id.tvPanelItemName);
        }
    }
}
