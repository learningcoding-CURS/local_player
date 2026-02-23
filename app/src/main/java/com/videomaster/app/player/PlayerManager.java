package com.videomaster.app.player;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.PlaybackParameters;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.DefaultLoadControl;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

import com.videomaster.app.interfaces.IPlayerEventListener;
import com.videomaster.app.interfaces.ISpeedProvider;
import com.videomaster.app.subtitle.SubtitleManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps ExoPlayer and exposes a clean API with extensible listener callbacks.
 *
 * Extensibility points:
 *   – {@link #setSpeedProvider(ISpeedProvider)}  custom speed table
 *   – {@link #setPlayMode(PlayMode)}             playback order (sequential / shuffle / …)
 *   – {@link #addListener(IPlayerEventListener)} / {@link #removeListener}
 */
public class PlayerManager {

    private static final long POSITION_UPDATE_INTERVAL_MS = 150;

    // Buffer configuration for smooth playback
    private static final int MIN_BUFFER_MS                = 25_000;
    private static final int MAX_BUFFER_MS                = 90_000;
    private static final int BUFFER_FOR_PLAYBACK_MS       = 800;
    private static final int BUFFER_AFTER_REBUFFER_MS     = 1_500;

    private final ExoPlayer                  player;
    private final Handler                    handler;
    private final List<IPlayerEventListener> listeners     = new ArrayList<>();
    private       ISpeedProvider             speedProvider;
    private       float                      currentSpeed;
    private       float                      previousSpeed;
    private final SubtitleManager            subtitleManager;

    // ── Play mode ──────────────────────────────────────────────────────────

    private PlayMode playMode = PlayMode.SEQUENTIAL;

    // ── Position ticker ────────────────────────────────────────────────────

    private final Runnable positionUpdater = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                long pos      = player.getCurrentPosition();
                long duration = player.getDuration();
                notifyPositionChanged(pos, duration < 0 ? 0 : duration);
                // NOTE: do NOT call subtitleManager.onSeek() here — that resets the search
                // index on every tick (every 250ms) causing O(n) full scans unnecessarily.
                // onSeek() must only be called when the user explicitly seeks.
            }
            handler.postDelayed(this, POSITION_UPDATE_INTERVAL_MS);
        }
    };

    // ── Constructor ────────────────────────────────────────────────────────

    public PlayerManager(Context context) {
        handler         = new Handler(Looper.getMainLooper());
        speedProvider   = new DefaultSpeedProvider();
        currentSpeed    = speedProvider.getDefaultSpeed();
        subtitleManager = SubtitleManager.getInstance();

        // Configure generous buffers to reduce stutter during normal playback
        DefaultLoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        MIN_BUFFER_MS,
                        MAX_BUFFER_MS,
                        BUFFER_FOR_PLAYBACK_MS,
                        BUFFER_AFTER_REBUFFER_MS)
                .build();

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
        trackSelector.setParameters(
                trackSelector.buildUponParameters()
                        .setForceHighestSupportedBitrate(false)
                        .setExceedRendererCapabilitiesIfNecessary(true)
                        .build());

        player = new ExoPlayer.Builder(context)
                .setLoadControl(loadControl)
                .setTrackSelector(trackSelector)
                .setHandleAudioBecomingNoisy(true)
                .setSeekBackIncrementMs(5_000)
                .setSeekForwardIncrementMs(5_000)
                .build();
        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    notifyStarted();
                    handler.post(positionUpdater);
                } else {
                    notifyPaused();
                    handler.removeCallbacks(positionUpdater);
                }
            }

            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    handler.removeCallbacks(positionUpdater);
                    notifyCompleted();
                } else if (state == Player.STATE_BUFFERING) {
                    notifyBuffering(true);
                } else if (state == Player.STATE_READY) {
                    notifyBuffering(false);
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                notifyError(error.getMessage() != null ? error.getMessage() : "Unknown playback error");
            }
        });
    }

    // ── Playback control ───────────────────────────────────────────────────

    /** Loads and starts playing the given URI from position 0. */
    public void play(Uri uri) {
        player.setMediaItem(MediaItem.fromUri(uri));
        player.prepare();
        player.setPlayWhenReady(true);
        applySpeed(currentSpeed);
    }

    public void pause()  { player.pause(); }
    public void resume() { player.play(); }

    public void togglePlayPause() {
        if (player.isPlaying()) pause(); else resume();
    }

    /** Seeks to an absolute position in milliseconds. */
    public void seekTo(long positionMs) {
        player.seekTo(positionMs);
        subtitleManager.onSeek();
    }

    /** Seeks relative to the current position (offset may be negative). */
    public void seekRelative(long offsetMs) {
        long target = Math.max(0, player.getCurrentPosition() + offsetMs);
        seekTo(target);
    }

    public void release() {
        handler.removeCallbacks(positionUpdater);
        player.release();
    }

    // ── Speed control ──────────────────────────────────────────────────────

    public void setSpeed(float speed) {
        currentSpeed = speed;
        applySpeed(speed);
        notifySpeedChanged(speed);
    }

    public void activateLongPressSpeed() {
        previousSpeed = currentSpeed;
        setSpeed(speedProvider.getLongPressSpeed());
    }

    public void deactivateLongPressSpeed() {
        setSpeed(previousSpeed);
    }

    private void applySpeed(float speed) {
        player.setPlaybackParameters(new PlaybackParameters(speed));
    }

    // ── Play mode ──────────────────────────────────────────────────────────

    /**
     * Sets the playback-order mode.
     * The actual navigation logic lives in PlayerActivity; this is stored here
     * so it survives configuration changes and is accessible to listeners.
     */
    public void setPlayMode(PlayMode mode) {
        this.playMode = mode;
    }

    public PlayMode getPlayMode() {
        return playMode;
    }

    // ── Getters ────────────────────────────────────────────────────────────

    public ExoPlayer getExoPlayer()    { return player; }
    public float     getCurrentSpeed() { return currentSpeed; }
    public boolean   isPlaying()       { return player.isPlaying(); }
    public long      getDuration()     { long d = player.getDuration(); return d < 0 ? 0 : d; }
    public long      getPosition()     { return player.getCurrentPosition(); }

    // ── Listener management ────────────────────────────────────────────────

    public void addListener(IPlayerEventListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeListener(IPlayerEventListener listener) {
        listeners.remove(listener);
    }

    /** Replaces the speed provider (extensibility point). */
    public void setSpeedProvider(ISpeedProvider provider) {
        this.speedProvider = provider;
    }

    public ISpeedProvider getSpeedProvider() { return speedProvider; }

    // ── Notification helpers ───────────────────────────────────────────────

    private void notifyStarted()   { for (IPlayerEventListener l : new ArrayList<>(listeners)) l.onPlaybackStarted(); }
    private void notifyPaused()    { for (IPlayerEventListener l : new ArrayList<>(listeners)) l.onPlaybackPaused(); }
    private void notifyCompleted() { for (IPlayerEventListener l : new ArrayList<>(listeners)) l.onPlaybackCompleted(); }
    private void notifyError(String msg)     { for (IPlayerEventListener l : new ArrayList<>(listeners)) l.onError(msg); }
    private void notifyBuffering(boolean b)  { for (IPlayerEventListener l : new ArrayList<>(listeners)) l.onBufferingChanged(b); }
    private void notifySpeedChanged(float s) { for (IPlayerEventListener l : new ArrayList<>(listeners)) l.onSpeedChanged(s); }
    private void notifyPositionChanged(long pos, long dur) {
        for (IPlayerEventListener l : new ArrayList<>(listeners)) l.onPositionChanged(pos, dur);
    }
}
