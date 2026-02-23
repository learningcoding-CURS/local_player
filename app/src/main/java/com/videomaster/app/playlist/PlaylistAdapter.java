package com.videomaster.app.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.R;
import com.videomaster.app.model.MediaList;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface OnItemListener {
        void onItemClick(MediaList list);
        void onItemLongClick(MediaList list);
    }

    private final List<MediaList> items;
    private final OnItemListener  listener;

    public PlaylistAdapter(List<MediaList> items, OnItemListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        MediaList list = items.get(position);
        h.tvName.setText(list.getName());

        String category = list.getCategory();
        if (category != null && !category.isEmpty()) {
            h.tvCategory.setVisibility(View.VISIBLE);
            h.tvCategory.setText(category);
        } else {
            h.tvCategory.setVisibility(View.GONE);
        }

        h.tvCount.setText(h.itemView.getContext()
                .getString(R.string.playlist_item_count, list.size()));

        h.itemView.setOnClickListener(v -> listener.onItemClick(list));
        h.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(list);
            return true;
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvCount;
        ViewHolder(@NonNull View v) {
            super(v);
            tvName     = v.findViewById(R.id.tvPlaylistName);
            tvCategory = v.findViewById(R.id.tvPlaylistCategory);
            tvCount    = v.findViewById(R.id.tvPlaylistCount);
        }
    }
}
