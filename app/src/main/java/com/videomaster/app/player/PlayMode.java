package com.videomaster.app.player;

/**
 * Defines the playback order modes for a playlist.
 *
 * Extensibility: new modes can be added here without breaking existing code.
 * PlayerActivity switches on this enum in onPlaybackCompleted().
 */
public enum PlayMode {

    /** Play items in list order; auto-advance; stop at the last item. */
    SEQUENTIAL,

    /** Play items in a randomised order; auto-advance; stop when all played. */
    SHUFFLE,

    /** Loop the current item indefinitely until the user manually switches. */
    REPEAT_ONE,

    /** Play the current item once, then stop — no auto-advance. */
    PLAY_ONCE
}
