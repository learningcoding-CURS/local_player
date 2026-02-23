package com.videomaster.app.interfaces;

import com.videomaster.app.model.VideoItem;
import java.util.List;

/**
 * Video library provider interface.
 * Implement this to supply videos from alternative sources (e.g. network, cloud).
 */
public interface IVideoLibraryProvider {

    /**
     * Returns the provider name shown in the UI.
     */
    String getProviderName();

    /**
     * Loads available videos asynchronously and delivers them via the callback.
     *
     * @param callback  invoked on completion (success or failure)
     */
    void loadVideos(VideoLibraryCallback callback);

    interface VideoLibraryCallback {
        void onSuccess(List<VideoItem> videos);
        void onError(String errorMessage);
    }
}
