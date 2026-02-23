package com.videomaster.app.interfaces;

/**
 * Player event callback interface.
 * Implement this to receive playback state changes from PlayerManager.
 */
public interface IPlayerEventListener {

    /** Called when playback starts or resumes. */
    void onPlaybackStarted();

    /** Called when playback is paused. */
    void onPlaybackPaused();

    /** Called when video reaches the end. */
    void onPlaybackCompleted();

    /**
     * Called when playback position changes.
     *
     * @param positionMs  current position in milliseconds
     * @param durationMs  total duration in milliseconds
     */
    void onPositionChanged(long positionMs, long durationMs);

    /**
     * Called when playback speed changes.
     *
     * @param speed  new playback speed (e.g. 1.0f, 1.5f)
     */
    void onSpeedChanged(float speed);

    /**
     * Called on a playback error.
     *
     * @param errorMessage human-readable error description
     */
    void onError(String errorMessage);

    /**
     * Called when buffering state changes.
     *
     * @param isBuffering  true if currently buffering
     */
    void onBufferingChanged(boolean isBuffering);
}
