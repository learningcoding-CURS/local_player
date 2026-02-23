package com.videomaster.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.videomaster.app.SettingsActivity;
import com.videomaster.app.stats.PlayStats;
import com.videomaster.app.subtitle.SubtitleLibraryManager;
import com.videomaster.app.interfaces.IPlayerEventListener;
import com.videomaster.app.player.GestureHandler;
import com.videomaster.app.player.PlayMode;
import com.videomaster.app.player.PlayerManager;
import com.videomaster.app.playlist.MediaListManager;
import com.videomaster.app.subtitle.SubtitleEntry;
import com.videomaster.app.subtitle.SubtitleManager;
import com.videomaster.app.ui.PlayerPlaylistAdapter;
import com.videomaster.app.ui.SubtitleListAdapter;
import com.videomaster.app.ui.SubtitleView;
import com.videomaster.app.util.TimeUtils;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * Full-screen video/audio player.
 *
 * Gesture summary (see GestureHandler for details):
 *   Portrait  – long press + swipe UP/DOWN → previous / next media
 *   Landscape – swipe LEFT/RIGHT           → next / previous media
 *   Left side vertical scroll              → brightness (up = brighter)
 *
 * Play modes (cycle with the mode button in the top bar):
 *   SEQUENTIAL  – play in order, stop at end
 *   SHUFFLE     – randomised order, stop when all played
 *   REPEAT_ONE  – loop current item
 *   PLAY_ONCE   – stop after current item ends
 *
 * Extensibility:
 *   – Implement IPlayerEventListener to receive playback events.
 *   – Call playerManager.setPlayMode() / getPlayMode() to read/change mode.
 *   – Override onPlayModeChanged() to react to mode changes.
 */
public class PlayerActivity extends AppCompatActivity implements IPlayerEventListener {

    // ── Intent extras ──────────────────────────────────────────────────────

    public static final String EXTRA_VIDEO_URI      = "extra_video_uri";
    public static final String EXTRA_PLAYLIST_URIS  = "extra_playlist_uris";
    public static final String EXTRA_PLAYLIST_INDEX = "extra_playlist_index";
    public static final String EXTRA_PLAYLIST_ID    = "extra_playlist_id";

    // ── Timing constants ───────────────────────────────────────────────────

    private static final long CONTROLS_HIDE_DELAY  = 3000;
    private static final long INDICATOR_HIDE_DELAY = 1500;

    // ── Core objects ───────────────────────────────────────────────────────

    private PlayerManager    playerManager;
    private SubtitleManager  subtitleManager;
    private MediaListManager mediaListManager;
    private GestureHandler   gestureHandler;

    // ── Views ──────────────────────────────────────────────────────────────

    private PlayerView    playerView;
    private SubtitleView  subtitleView;
    private FrameLayout   controlsOverlay;
    private FrameLayout   lockOverlay;
    private SeekBar       seekBar;
    private TextView      tvCurrentTime;
    private TextView      tvDuration;
    private ImageButton   btnPlayPause;
    private ImageButton   btnRewind;
    private ImageButton   btnForward;
    private ImageButton   btnSkipPrev;
    private ImageButton   btnSkipNext;
    private ImageButton   btnSubtitle;
    private ImageButton   btnSubtitleToggle;
    private ImageButton   btnRotate;
    private ImageButton   btnLock;
    private ImageButton   btnUnlock;
    private ImageButton   btnPlayMode;
    private ImageButton   btnPlaylistPanel;
    private ImageButton   btnClosePanel;
    private TextView      tvSpeed;
    private TextView      tvLongPressHint;
    private View          loadingView;

    // Brightness indicator
    private View          brightnessIndicator;
    private TextView      tvBrightnessValue;

    // In-player playlist panel
    private LinearLayout  playlistPanel;
    private RecyclerView  rvPlaylistPanel;
    private PlayerPlaylistAdapter panelAdapter;

    // Subtitle list panel
    private ImageButton   btnSubtitleList;
    private LinearLayout  subtitleListPanel;
    private RecyclerView  rvSubtitleList;
    private TextView      tvSubtitleListEmpty;
    private TextView      btnToggleTimestamps;
    private SubtitleListAdapter subtitleListAdapter;
    private boolean       isSubtitlePanelVisible = false;

    // Subtitle text visibility toggle
    private boolean       isSubtitleTextVisible  = true;

    // Last subtitle entry clicked in the list panel (for toggle play/pause on re-click)
    private SubtitleEntry lastClickedSubEntry     = null;

    // Subtitle library
    private SubtitleLibraryManager subtitleLibraryManager;

    // Seek button settings (loaded from prefs)
    private int seekMs = 5000;

    // ── Handlers / runnables ───────────────────────────────────────────────

    private final Handler  uiHandler              = new Handler(Looper.getMainLooper());
    private final Runnable hideControlsRunnable    = this::hideControls;
    private final Runnable hideBrightnessRunnable  = () -> brightnessIndicator.setVisibility(View.GONE);

    // ── State ──────────────────────────────────────────────────────────────

    private boolean isDraggingSeekBar = false;
    private boolean isLocked          = false;
    private long    savedPosition     = 0;
    private boolean isPanelVisible    = false;

    // Playlist navigation context
    private ArrayList<String> playlistUris  = null;
    private int               playlistIndex = 0;
    private String            playlistId    = null;

    // Playback-time tracking (for PlayStats)
    private long playStatsStartMs    = -1L; // SystemClock.elapsedRealtime() when play resumed
    private long playStatsAccumMs    = 0L;  // total ms accumulated in this session

    // Shuffle state
    private int[] shuffleOrder      = null;
    private int   shuffleCursor     = 0;

    // ── Activity result launchers ──────────────────────────────────────────

    private final androidx.activity.result.ActivityResultLauncher<Intent> importLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) importSubtitle(uri);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<Intent> exportLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) doExportSubtitle(uri);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<Intent> subtitleLibraryLauncher =
            registerForActivityResult(
                    new androidx.activity.result.contract.ActivityResultContracts
                            .StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    String path = result.getData().getStringExtra(
                            SubtitleLibraryActivity.EXTRA_SELECTED_PATH);
                    if (path != null) importSubtitleFromFile(new java.io.File(path));
                }
            });

    private String pendingExportFormat = "SRT";

    // Double-click detection for subtitle button
    private long lastSubtitleClickMs = 0;

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mediaListManager        = MediaListManager.getInstance(this);
        subtitleLibraryManager  = SubtitleLibraryManager.getInstance(this);

        initViews();
        initPlayer();
        setupGestures();
        setupControls();

        String uriStr = getIntent().getStringExtra(EXTRA_VIDEO_URI);
        if (uriStr == null) {
            Toast.makeText(this, R.string.error_no_video, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        playlistUris  = getIntent().getStringArrayListExtra(EXTRA_PLAYLIST_URIS);
        playlistIndex = getIntent().getIntExtra(EXTRA_PLAYLIST_INDEX, 0);
        playlistId    = getIntent().getStringExtra(EXTRA_PLAYLIST_ID);

        if (savedInstanceState != null) {
            savedPosition = savedInstanceState.getLong("position", 0);
            int savedMode = savedInstanceState.getInt("play_mode", 0);
            playerManager.setPlayMode(PlayMode.values()[savedMode]);
            updatePlayModeIcon();
        }

        // Build panel adapter if we have a playlist
        setupPlaylistPanel();

        Uri videoUri = Uri.parse(uriStr);
        playerManager.play(videoUri);
        if (savedPosition > 0) playerManager.seekTo(savedPosition);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (playerManager != null) {
            outState.putLong("position", playerManager.getPosition());
            outState.putInt("play_mode", playerManager.getPlayMode().ordinal());
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean landscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (gestureHandler != null) gestureHandler.setLandscape(landscape);
    }

    /**
     * Feed ALL touch events to the gesture handler at the Activity level.
     * This guarantees swipe-to-switch-media works even when the controls overlay
     * is sitting on top of playerView and may intercept touch events first.
     *
     * Gesture feed is skipped when either overlay panel is visible so that
     * scrolling through the subtitle list or playlist panel does not accidentally
     * trigger a media switch.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isLocked && gestureHandler != null && ev != null
                && !isSubtitlePanelVisible && !isPanelVisible) {
            float windowW = getWindow().getDecorView().getWidth();
            gestureHandler.feedSwipeEvent(ev, windowW);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (isSubtitlePanelVisible) {
            closeSubtitleListPanel();
        } else if (isPanelVisible) {
            closePlaylistPanel();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playerManager != null) {
            saveCurrentProgress();
            playerManager.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enterImmersiveMode();
    }

    @Override
    protected void onDestroy() {
        // Finalize any in-progress play segment
        if (playStatsStartMs >= 0) {
            playStatsAccumMs += android.os.SystemClock.elapsedRealtime() - playStatsStartMs;
            playStatsStartMs = -1L;
        }
        // Persist to PlayStats
        if (playStatsAccumMs > 0) {
            String statsId   = playlistId != null ? playlistId : PlayStats.SINGLE_FILE_ID;
            String statsName = resolvePlayStatsName();
            PlayStats.getInstance(this).addTime(statsId, statsName, playStatsAccumMs);
        }
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
        if (playerManager != null) {
            playerManager.removeListener(this);
            playerManager.release();
        }
    }

    /** Resolves a display name for the current playback context (playlist or single file). */
    private String resolvePlayStatsName() {
        if (playlistId != null && mediaListManager != null) {
            com.videomaster.app.model.MediaList list = mediaListManager.getList(playlistId);
            if (list != null) return list.getName();
        }
        return getString(R.string.stats_single_file);
    }

    // ── Initialisation ─────────────────────────────────────────────────────

    private void initViews() {
        playerView          = findViewById(R.id.playerView);
        subtitleView        = findViewById(R.id.subtitleView);
        controlsOverlay     = findViewById(R.id.controlsOverlay);
        lockOverlay         = findViewById(R.id.lockOverlay);
        seekBar             = findViewById(R.id.seekBar);
        tvCurrentTime       = findViewById(R.id.tvCurrentTime);
        tvDuration          = findViewById(R.id.tvDuration);
        btnPlayPause        = findViewById(R.id.btnPlayPause);
        btnRewind           = findViewById(R.id.btnRewind);
        btnForward          = findViewById(R.id.btnForward);
        btnSkipPrev         = findViewById(R.id.btnSkipPrev);
        btnSkipNext         = findViewById(R.id.btnSkipNext);
        btnSubtitle         = findViewById(R.id.btnSubtitle);
        btnSubtitleToggle   = findViewById(R.id.btnSubtitleToggle);
        btnRotate           = findViewById(R.id.btnRotate);
        btnLock             = findViewById(R.id.btnLock);
        btnUnlock           = findViewById(R.id.btnUnlock);
        btnPlayMode         = findViewById(R.id.btnPlayMode);
        btnPlaylistPanel    = findViewById(R.id.btnPlaylistPanel);
        btnClosePanel       = findViewById(R.id.btnClosePanel);
        tvSpeed             = findViewById(R.id.tvSpeed);
        tvLongPressHint     = findViewById(R.id.tvLongPressHint);
        loadingView         = findViewById(R.id.loadingView);

        brightnessIndicator = findViewById(R.id.brightnessIndicator);
        tvBrightnessValue   = findViewById(R.id.tvBrightnessValue);

        playlistPanel       = findViewById(R.id.playlistPanel);
        rvPlaylistPanel     = findViewById(R.id.rvPlaylistPanel);

        btnSubtitleList     = findViewById(R.id.btnSubtitleList);
        subtitleListPanel   = findViewById(R.id.subtitleListPanel);
        rvSubtitleList      = findViewById(R.id.rvSubtitleList);
        tvSubtitleListEmpty = findViewById(R.id.tvSubtitleListEmpty);
        btnToggleTimestamps = findViewById(R.id.btnToggleTimestamps);

        applyPlayerSettings();
        enterImmersiveMode();
    }

    /** Reads all player-related settings from SharedPreferences and applies them to the UI. */
    private void applyPlayerSettings() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);

        // ── Seek buttons ──────────────────────────────────────────────────
        int seekIconSizeDp = prefs.getInt(SettingsActivity.PREF_SEEK_ICON_SIZE,
                SettingsActivity.DEFAULT_SEEK_ICON_SIZE);
        seekMs = prefs.getInt(SettingsActivity.PREF_SEEK_SECONDS,
                SettingsActivity.DEFAULT_SEEK_SECONDS) * 1000;
        int seekAlphaPct = prefs.getInt(SettingsActivity.PREF_SEEK_ALPHA,
                SettingsActivity.DEFAULT_SEEK_ALPHA);

        int sizePx   = dpToPx(seekIconSizeDp);
        int alphaVal = Math.round(seekAlphaPct * 2.55f);

        ViewGroup.LayoutParams rp = btnRewind.getLayoutParams();
        rp.width = sizePx; rp.height = sizePx;
        btnRewind.setLayoutParams(rp);

        ViewGroup.LayoutParams fp = btnForward.getLayoutParams();
        fp.width = sizePx; fp.height = sizePx;
        btnForward.setLayoutParams(fp);

        btnRewind.setImageAlpha(alphaVal);
        btnForward.setImageAlpha(alphaVal);

        // ── SeekBar appearance ────────────────────────────────────────────
        int trackHeightDp = prefs.getInt(SettingsActivity.PREF_SEEKBAR_HEIGHT,
                SettingsActivity.DEFAULT_SEEKBAR_HEIGHT);
        int progressAlpha = prefs.getInt(SettingsActivity.PREF_SEEKBAR_PROGRESS_ALPHA,
                SettingsActivity.DEFAULT_SEEKBAR_PROGRESS_ALPHA);
        applySeekbarStyle(trackHeightDp, progressAlpha);

        // ── Subtitle panel background ─────────────────────────────────────
        int panelAlpha = prefs.getInt(SettingsActivity.PREF_SUBTITLE_PANEL_ALPHA,
                SettingsActivity.DEFAULT_SUBTITLE_PANEL_ALPHA);
        applySubtitlePanelAlpha(panelAlpha);

        // ── Subtitle default visibility ───────────────────────────────────
        isSubtitleTextVisible = prefs.getBoolean(
                SettingsActivity.PREF_SUBTITLE_DEFAULT_VISIBLE, true);
        if (subtitleView != null) {
            subtitleView.setVisibility(isSubtitleTextVisible ? View.VISIBLE : View.GONE);
        }
        if (btnSubtitleToggle != null) {
            btnSubtitleToggle.setImageResource(
                    isSubtitleTextVisible ? R.drawable.ic_subtitle_toggle : R.drawable.ic_subtitle_off);
        }

        // ── Player control buttons visibility & color ─────────────────────
        applyButtonSettings(btnLock,
                SettingsActivity.PREF_BTN_LOCK_VISIBLE,
                SettingsActivity.PREF_BTN_LOCK_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonSettings(btnPlayMode,
                SettingsActivity.PREF_BTN_PLAYMODE_VISIBLE,
                SettingsActivity.PREF_BTN_PLAYMODE_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonSettings(btnPlaylistPanel,
                SettingsActivity.PREF_BTN_PLAYLIST_VISIBLE,
                SettingsActivity.PREF_BTN_PLAYLIST_COLOR,
                SettingsActivity.DEFAULT_PLAYLIST_BTN_COLOR, prefs);
        applyButtonSettings(btnSubtitle,
                SettingsActivity.PREF_BTN_SUBTITLE_VISIBLE,
                SettingsActivity.PREF_BTN_SUBTITLE_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonSettings(btnSubtitleToggle,
                SettingsActivity.PREF_BTN_SUBTITLE_TOGGLE_VISIBLE,
                SettingsActivity.PREF_BTN_SUBTITLE_TOGGLE_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonSettings(btnSubtitleList,
                SettingsActivity.PREF_BTN_SUBTLIST_VISIBLE,
                SettingsActivity.PREF_BTN_SUBTLIST_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonSettings(btnRotate,
                SettingsActivity.PREF_BTN_ROTATE_VISIBLE,
                SettingsActivity.PREF_BTN_ROTATE_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonColor(btnRewind,
                SettingsActivity.PREF_BTN_SEEK_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonColor(btnForward,
                SettingsActivity.PREF_BTN_SEEK_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonColor(btnPlayPause,
                SettingsActivity.PREF_BTN_PLAYPAUSE_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);

        // ── Skip buttons (prev/next video) ────────────────────────────────
        applyButtonSettings(btnSkipPrev,
                SettingsActivity.PREF_BTN_SKIP_VISIBLE,
                SettingsActivity.PREF_BTN_SKIP_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);
        applyButtonSettings(btnSkipNext,
                SettingsActivity.PREF_BTN_SKIP_VISIBLE,
                SettingsActivity.PREF_BTN_SKIP_COLOR,
                SettingsActivity.DEFAULT_BTN_COLOR, prefs);

        int skipBtnSizeDp = prefs.getInt(SettingsActivity.PREF_SKIP_BTN_SIZE,
                SettingsActivity.DEFAULT_SKIP_BTN_SIZE);
        int skipSizePx = dpToPx(skipBtnSizeDp);
        if (btnSkipPrev != null) {
            ViewGroup.LayoutParams sp = btnSkipPrev.getLayoutParams();
            sp.width = skipSizePx; sp.height = skipSizePx;
            btnSkipPrev.setLayoutParams(sp);
        }
        if (btnSkipNext != null) {
            ViewGroup.LayoutParams sn = btnSkipNext.getLayoutParams();
            sn.width = skipSizePx; sn.height = skipSizePx;
            btnSkipNext.setLayoutParams(sn);
        }

        // ── Button order in top/center bar ────────────────────────────────
        applyButtonOrder(prefs);
    }

    /**
     * Re-orders buttons in the top bar and center bar according to saved order preferences.
     * Preserves the flex spacer (the invisible weight view) in the middle of the top bar.
     */
    private void applyButtonOrder(SharedPreferences prefs) {
        String topOrder    = prefs.getString(SettingsActivity.PREF_TOP_BTN_ORDER,
                SettingsActivity.DEFAULT_TOP_BTN_ORDER);
        String centerOrder = prefs.getString(SettingsActivity.PREF_CENTER_BTN_ORDER,
                SettingsActivity.DEFAULT_CENTER_BTN_ORDER);

        // Top bar: find the LinearLayout with id topBar
        android.widget.LinearLayout topBar = findViewById(R.id.topBar);
        if (topBar != null) {
            // Find the flex spacer (the View with weight=1, width=0)
            View flexSpacer = null;
            for (int i = 0; i < topBar.getChildCount(); i++) {
                View v = topBar.getChildAt(i);
                if (v.getId() == View.NO_ID
                        && v.getLayoutParams() instanceof android.widget.LinearLayout.LayoutParams) {
                    android.widget.LinearLayout.LayoutParams lp =
                            (android.widget.LinearLayout.LayoutParams) v.getLayoutParams();
                    if (lp.weight > 0) { flexSpacer = v; break; }
                }
            }

            // Map button IDs to views
            java.util.LinkedHashMap<String, View> topBtnMap = new java.util.LinkedHashMap<>();
            topBtnMap.put("lock",           btnLock);
            topBtnMap.put("play-mode",      btnPlayMode);
            topBtnMap.put("playlist-panel", btnPlaylistPanel);
            topBtnMap.put("subtitle-toggle", btnSubtitleToggle);
            topBtnMap.put("subtitle",       btnSubtitle);
            topBtnMap.put("subtitle-list",  btnSubtitleList);
            topBtnMap.put("rotate",         btnRotate);

            // Remove all top-bar buttons from the layout (keep spacer and tvSpeed)
            for (View v : topBtnMap.values()) {
                if (v != null) topBar.removeView(v);
            }

            // Re-add in saved order: left-group first (before spacer), right-group after
            // We split at the spacer: buttons before it are "left", buttons after are "right"
            // For simplicity, reorder ALL buttons keeping spacer in place.
            // Strategy: insert buttons before spacer if they're in the first half, else after.
            String[] ids = topOrder.split(",");
            int half = ids.length / 2;

            // Find spacer index after removals
            int spacerIdx = flexSpacer != null ? topBar.indexOfChild(flexSpacer) : -1;

            for (int i = 0; i < ids.length; i++) {
                String id = ids[i].trim();
                View btn = topBtnMap.get(id);
                if (btn == null) continue;
                if (i < half) {
                    // Left group: insert before spacer
                    int insertAt = spacerIdx >= 0 ? spacerIdx : 0;
                    topBar.addView(btn, insertAt);
                    if (spacerIdx >= 0) spacerIdx++;
                } else {
                    // Right group: append after spacer and tvSpeed
                    topBar.addView(btn);
                }
            }
        }

        // Center bar: reorder skip-prev / rewind / play-pause / forward / skip-next
        android.widget.LinearLayout centerBar = null;
        android.widget.FrameLayout overlay = controlsOverlay;
        if (overlay != null) {
            for (int i = 0; i < overlay.getChildCount(); i++) {
                View v = overlay.getChildAt(i);
                if (v instanceof android.widget.LinearLayout) {
                    android.widget.LinearLayout ll = (android.widget.LinearLayout) v;
                    if (ll.indexOfChild(btnPlayPause) >= 0) { centerBar = ll; break; }
                }
            }
        }
        // Ensure saved order includes skip buttons (migration for existing users)
        if (!centerOrder.contains("skip-prev")) {
            centerOrder = "skip-prev," + centerOrder;
        }
        if (!centerOrder.contains("skip-next")) {
            centerOrder = centerOrder + ",skip-next";
        }
        if (centerBar != null) {
            java.util.LinkedHashMap<String, View> centerBtnMap = new java.util.LinkedHashMap<>();
            centerBtnMap.put("skip-prev",   btnSkipPrev);
            centerBtnMap.put("rewind",      btnRewind);
            centerBtnMap.put("play-pause",  btnPlayPause);
            centerBtnMap.put("forward",     btnForward);
            centerBtnMap.put("skip-next",   btnSkipNext);

            for (View v : centerBtnMap.values()) {
                if (v != null) centerBar.removeView(v);
            }

            String[] cIds = centerOrder.split(",");
            for (String cId : cIds) {
                View btn = centerBtnMap.get(cId.trim());
                if (btn != null) centerBar.addView(btn);
            }
        }
    }

    /**
     * Applies visibility and tint color to an ImageButton from SharedPreferences.
     * If the button is the playlistPanel button, visibility may be overridden later
     * by setupPlaylistPanel() based on whether a playlist is available.
     */
    private void applyButtonSettings(ImageButton btn, String visiblePrefKey,
                                     String colorPrefKey, String defaultColor,
                                     SharedPreferences prefs) {
        if (btn == null) return;
        boolean visible = prefs.getBoolean(visiblePrefKey, true);
        // For playlist panel, only hide if setting says hide AND we haven't set it based on playlist
        if (visiblePrefKey.equals(SettingsActivity.PREF_BTN_PLAYLIST_VISIBLE)) {
            if (!visible) btn.setVisibility(View.GONE);
            // If visible, setupPlaylistPanel() will control final visibility
        } else {
            btn.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        applyButtonColor(btn, colorPrefKey, defaultColor, prefs);
    }

    private void applyButtonColor(ImageButton btn, String colorPrefKey,
                                  String defaultColor, SharedPreferences prefs) {
        if (btn == null) return;
        String color = prefs.getString(colorPrefKey, defaultColor);
        btn.setImageTintList(android.content.res.ColorStateList.valueOf(resolveButtonColor(color)));
    }

    private int resolveButtonColor(String color) {
        if (color == null) return 0xB3FFFFFF;
        switch (color) {
            case "accent":  return 0xFFE94560;
            case "orange":  return 0xFFFF9800;
            case "cyan":    return 0xFF00BCD4;
            case "green":   return 0xFF4CAF50;
            case "yellow":  return 0xFFFFEB3B;
            default:        return 0xB3FFFFFF; // white 70%
        }
    }

    private void applySeekbarStyle(int trackHeightDp, int progressAlphaPct) {
        int trackPx   = dpToPx(trackHeightDp);
        int cornerPx  = trackPx / 2;
        int alphaVal  = Math.round(progressAlphaPct * 2.55f);
        int progressColor = (alphaVal << 24) | 0xE94560;

        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setShape(GradientDrawable.RECTANGLE);
        bgShape.setCornerRadius(cornerPx);
        bgShape.setColor(0x22FFFFFF);

        GradientDrawable progressShape = new GradientDrawable();
        progressShape.setShape(GradientDrawable.RECTANGLE);
        progressShape.setCornerRadius(cornerPx);
        progressShape.setColor(progressColor);

        ClipDrawable clipProgress = new ClipDrawable(progressShape, Gravity.START,
                ClipDrawable.HORIZONTAL);

        LayerDrawable layer = new LayerDrawable(new android.graphics.drawable.Drawable[]{
                bgShape, clipProgress });
        layer.setId(0, android.R.id.background);
        layer.setId(1, android.R.id.progress);
        layer.setLayerGravity(0, Gravity.CENTER_VERTICAL);
        layer.setLayerGravity(1, Gravity.CENTER_VERTICAL);
        layer.setLayerHeight(0, trackPx);
        layer.setLayerHeight(1, trackPx);

        seekBar.setProgressDrawable(layer);
        seekBar.setSplitTrack(false);
    }

    private void applySubtitlePanelAlpha(int alphaPct) {
        int a = Math.round(alphaPct * 2.55f);
        subtitleListPanel.setBackgroundColor((a << 24) | 0x0D0D0D);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void initPlayer() {
        subtitleManager = SubtitleManager.getInstance();
        playerManager   = new PlayerManager(this);
        playerManager.addListener(this);

        playerView.setPlayer(playerManager.getExoPlayer());
        playerView.setUseController(false);

        // Apply saved subtitle settings
        android.content.SharedPreferences prefs =
                getSharedPreferences("app_settings", MODE_PRIVATE);
        float subSize  = prefs.getFloat(SettingsActivity.PREF_SUBTITLE_SIZE,
                SettingsActivity.DEFAULT_SUBTITLE_SIZE);
        float lineSp   = prefs.getFloat(SettingsActivity.PREF_SUBTITLE_LINE_SP,
                SettingsActivity.DEFAULT_SUBTITLE_LINE_SP);
        subtitleView.setTextSizeSp(subSize);
        subtitleView.setLineSpacingMultiplier(lineSp);
    }

    private void setupGestures() {
        boolean landscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;

        android.content.SharedPreferences prefs =
                getSharedPreferences("app_settings", MODE_PRIVATE);
        String portraitPref  = prefs.getString("portrait_swipe",  "VERTICAL");
        String landscapePref = prefs.getString("landscape_swipe", "HORIZONTAL");

        gestureHandler = new GestureHandler(this, new GestureHandler.GestureListener() {

            @Override public void onSingleTap() {
                if (!isLocked) toggleControlsVisibility();
            }

            @Override public void onDoubleTap() {
                if (!isLocked) {
                    playerManager.togglePlayPause();
                    showControls();
                }
            }

            @Override public void onLongPressStart() {
                if (!isLocked) {
                    playerManager.activateLongPressSpeed();
                    tvLongPressHint.setVisibility(View.VISIBLE);
                    uiHandler.removeCallbacks(hideControlsRunnable);
                }
            }

            @Override public void onLongPressEnd() {
                if (!isLocked) {
                    playerManager.deactivateLongPressSpeed();
                    tvLongPressHint.setVisibility(View.GONE);
                    scheduleHideControls();
                }
            }

            @Override public void onBrightnessScroll(float delta) {
                if (!isLocked) adjustBrightness(delta);
            }

            @Override public void onSwipeMedia(boolean toNext) {
                if (!isLocked) {
                    // Cancel speed boost if it was active from a long press
                    playerManager.deactivateLongPressSpeed();
                    tvLongPressHint.setVisibility(View.GONE);
                    if (toNext) playNext();
                    else        playPrevious();
                }
            }
        });

        gestureHandler.setLandscape(landscape);
        gestureHandler.setPortraitSwipeDirection(
                "HORIZONTAL".equals(portraitPref)
                        ? GestureHandler.SwipeDirection.HORIZONTAL
                        : GestureHandler.SwipeDirection.VERTICAL);
        gestureHandler.setLandscapeSwipeDirection(
                "VERTICAL".equals(landscapePref)
                        ? GestureHandler.SwipeDirection.VERTICAL
                        : GestureHandler.SwipeDirection.HORIZONTAL);
        playerView.setOnTouchListener(gestureHandler);
    }

    private void setupControls() {
        btnPlayPause.setOnClickListener(v -> {
            playerManager.togglePlayPause();
            scheduleHideControls();
        });

        btnRewind.setOnClickListener(v -> {
            playerManager.seekRelative(-seekMs);
            scheduleHideControls();
        });

        btnForward.setOnClickListener(v -> {
            playerManager.seekRelative(seekMs);
            scheduleHideControls();
        });

        btnSkipPrev.setOnClickListener(v -> { playPrevious(); scheduleHideControls(); });
        btnSkipNext.setOnClickListener(v -> { playNext(); scheduleHideControls(); });
        btnRotate.setOnClickListener(v -> toggleOrientation());
        btnSubtitle.setOnClickListener(v -> handleSubtitleButtonClick());
        if (btnSubtitleToggle != null) {
            btnSubtitleToggle.setOnClickListener(v -> toggleSubtitleTextOnly());
        }
        tvSpeed.setOnClickListener(v -> showSpeedMenu());
        btnLock.setOnClickListener(v -> lockScreen());
        btnUnlock.setOnClickListener(v -> unlockScreen());

        btnPlayMode.setOnClickListener(v -> cyclePlayMode());
        btnPlaylistPanel.setOnClickListener(v -> togglePlaylistPanel());
        btnClosePanel.setOnClickListener(v -> closePlaylistPanel());

        btnSubtitleList.setOnClickListener(v -> toggleSubtitleListPanel());
        btnToggleTimestamps.setOnClickListener(v -> toggleSubtitleTimestamps());
        if (findViewById(R.id.btnCloseSubtitlePanel) != null) {
            findViewById(R.id.btnCloseSubtitlePanel).setOnClickListener(v -> closeSubtitleListPanel());
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) {
                    long target = (long) (playerManager.getDuration() * (progress / 1000.0));
                    tvCurrentTime.setText(TimeUtils.formatDuration(target));
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {
                isDraggingSeekBar = true;
                uiHandler.removeCallbacks(hideControlsRunnable);
            }
            @Override public void onStopTrackingTouch(SeekBar sb) {
                isDraggingSeekBar = false;
                long target = (long) (playerManager.getDuration() * (sb.getProgress() / 1000.0));
                playerManager.seekTo(target);
                subtitleManager.onSeek();
                scheduleHideControls();
            }
        });
    }

    // ── In-player playlist panel ───────────────────────────────────────────

    private void setupPlaylistPanel() {
        if (playlistUris == null || playlistUris.isEmpty()) {
            btnPlaylistPanel.setVisibility(View.GONE);
            if (btnSkipPrev != null) btnSkipPrev.setVisibility(View.GONE);
            if (btnSkipNext != null) btnSkipNext.setVisibility(View.GONE);
            return;
        }
        // Respect user visibility setting
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean showPanel = prefs.getBoolean(SettingsActivity.PREF_BTN_PLAYLIST_VISIBLE, true);
        btnPlaylistPanel.setVisibility(showPanel ? View.VISIBLE : View.GONE);
        rvPlaylistPanel.setLayoutManager(new LinearLayoutManager(this));
        panelAdapter = new PlayerPlaylistAdapter(playlistUris, playlistIndex,
                index -> {
                    if (index != playlistIndex) {
                        saveCurrentProgress();
                        playlistIndex = index;
                        switchToPlaylistItem(playlistIndex);
                    }
                    closePlaylistPanel();
                });
        rvPlaylistPanel.setAdapter(panelAdapter);
    }

    private void togglePlaylistPanel() {
        if (isPanelVisible) {
            closePlaylistPanel();
        } else {
            openPlaylistPanel();
        }
    }

    /** Read the user-configured panel slide direction for the current orientation. */
    private String getPanelDirection() {
        boolean isLandscape = getResources().getConfiguration().orientation
                == android.content.res.Configuration.ORIENTATION_LANDSCAPE;
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String key = isLandscape
                ? SettingsActivity.PREF_PANEL_DIR_LANDSCAPE
                : SettingsActivity.PREF_PANEL_DIR_PORTRAIT;
        return prefs.getString(key, SettingsActivity.PANEL_DIR_RIGHT);
    }

    /** Pixels per dp, cached lazily. */
    private float density() {
        return getResources().getDisplayMetrics().density;
    }

    private void openPlaylistPanel() {
        if (playlistUris == null || playlistUris.isEmpty()) return;

        String dir = getPanelDirection();
        int sidePx   = (int) (260 * density()); // width for LEFT/RIGHT panels
        int blockPx  = (int) (300 * density()); // height for TOP/BOTTOM panels

        // Set LayoutParams to position the panel on the correct edge
        FrameLayout.LayoutParams lp;
        switch (dir) {
            case SettingsActivity.PANEL_DIR_LEFT:
                lp = new FrameLayout.LayoutParams(sidePx, ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.START);
                break;
            case SettingsActivity.PANEL_DIR_TOP:
                lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, blockPx,
                        Gravity.TOP);
                break;
            case SettingsActivity.PANEL_DIR_BOTTOM:
                lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, blockPx,
                        Gravity.BOTTOM);
                break;
            default: // RIGHT
                lp = new FrameLayout.LayoutParams(sidePx, ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.END);
                break;
        }
        playlistPanel.setLayoutParams(lp);

        // Rotate close button arrow to point in the close direction
        switch (dir) {
            case SettingsActivity.PANEL_DIR_LEFT:   btnClosePanel.setRotation(180); break;
            case SettingsActivity.PANEL_DIR_TOP:    btnClosePanel.setRotation(270); break;
            case SettingsActivity.PANEL_DIR_BOTTOM: btnClosePanel.setRotation(90);  break;
            default:                                 btnClosePanel.setRotation(0);   break;
        }

        // Reset any leftover translation from previous open/close
        playlistPanel.setTranslationX(0);
        playlistPanel.setTranslationY(0);

        // Animate in from the correct edge using fixed pixel values (avoids getWidth()=0 when GONE)
        switch (dir) {
            case SettingsActivity.PANEL_DIR_LEFT:
                playlistPanel.setTranslationX(-sidePx);
                break;
            case SettingsActivity.PANEL_DIR_TOP:
                playlistPanel.setTranslationY(-blockPx);
                break;
            case SettingsActivity.PANEL_DIR_BOTTOM:
                playlistPanel.setTranslationY(blockPx);
                break;
            default: // RIGHT
                playlistPanel.setTranslationX(sidePx);
                break;
        }
        playlistPanel.setVisibility(View.VISIBLE);
        playlistPanel.animate().translationX(0).translationY(0).setDuration(250).start();

        isPanelVisible = true;
        uiHandler.removeCallbacks(hideControlsRunnable);
    }

    private void closePlaylistPanel() {
        if (!isPanelVisible) return;
        String dir = getPanelDirection();
        float targetX = 0, targetY = 0;
        switch (dir) {
            case SettingsActivity.PANEL_DIR_LEFT:   targetX = -playlistPanel.getWidth();  break;
            case SettingsActivity.PANEL_DIR_TOP:    targetY = -playlistPanel.getHeight(); break;
            case SettingsActivity.PANEL_DIR_BOTTOM: targetY = playlistPanel.getHeight();  break;
            default:                                 targetX = playlistPanel.getWidth();   break;
        }
        playlistPanel.animate()
                .translationX(targetX)
                .translationY(targetY)
                .setDuration(200)
                .withEndAction(() -> {
                    playlistPanel.setVisibility(View.GONE);
                    isPanelVisible = false;
                })
                .start();
    }

    // ── Subtitle list panel ────────────────────────────────────────────────

    /** Called after subtitle is imported; shows the subtitle list button if subtitles are loaded. */
    private void refreshSubtitleListButton() {
        boolean has = subtitleManager.hasSubtitles();
        btnSubtitleList.setVisibility(has ? View.VISIBLE : View.GONE);
        if (!has && isSubtitlePanelVisible) closeSubtitleListPanel();
    }

    private void toggleSubtitleListPanel() {
        if (isSubtitlePanelVisible) {
            closeSubtitleListPanel();
        } else {
            openSubtitleListPanel();
        }
    }

    private void openSubtitleListPanel() {
        java.util.List<SubtitleEntry> subs = subtitleManager.getCurrentSubtitles();
        if (subs.isEmpty()) return;

        // Build or rebuild adapter — panel stays open after click
        lastClickedSubEntry = null;
        subtitleListAdapter = new SubtitleListAdapter(subs,
                entry -> {
                    if (entry == lastClickedSubEntry) {
                        // Re-click the same subtitle → toggle play/pause
                        playerManager.togglePlayPause();
                    } else {
                        // First click or different subtitle → seek to its start time
                        lastClickedSubEntry = entry;
                        playerManager.seekTo(entry.getStartTimeMs());
                        subtitleManager.onSeek();
                    }
                    scheduleHideControls();
                });

        rvSubtitleList.setLayoutManager(new LinearLayoutManager(this));
        rvSubtitleList.setAdapter(subtitleListAdapter);

        tvSubtitleListEmpty.setVisibility(subs.isEmpty() ? View.VISIBLE : View.GONE);
        rvSubtitleList.setVisibility(subs.isEmpty() ? View.GONE : View.VISIBLE);

        // Apply panel background alpha from settings
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        int panelAlpha = prefs.getInt(SettingsActivity.PREF_SUBTITLE_PANEL_ALPHA,
                SettingsActivity.DEFAULT_SUBTITLE_PANEL_ALPHA);
        applySubtitlePanelAlpha(panelAlpha);

        subtitleListPanel.setVisibility(View.VISIBLE);
        subtitleListPanel.post(() -> {
            subtitleListPanel.setTranslationY(subtitleListPanel.getHeight());
            subtitleListPanel.animate().translationY(0).setDuration(250).start();
        });
        isSubtitlePanelVisible = true;
        uiHandler.removeCallbacks(hideControlsRunnable);

        // Pull-down gesture to close
        setupSubtitlePanelSwipeDown();

        // Scroll to active subtitle
        scrollToActiveSubtitle(playerManager.getPosition());
    }

    private float subtitlePanelDragStartY = 0f;

    private void setupSubtitlePanelSwipeDown() {
        View dragHandle = subtitleListPanel.findViewById(R.id.subtitlePanelDragHandle);
        View header = subtitleListPanel.getChildAt(1); // header LinearLayout
        View[] targets = { dragHandle, header };
        for (View target : targets) {
            if (target == null) continue;
            target.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        subtitlePanelDragStartY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dy = event.getRawY() - subtitlePanelDragStartY;
                        if (dy > dpToPx(80)) {
                            closeSubtitleListPanel();
                            return true;
                        }
                        break;
                }
                return false;
            });
        }
    }

    private void closeSubtitleListPanel() {
        if (!isSubtitlePanelVisible) return;
        lastClickedSubEntry = null;
        subtitleListPanel.animate()
                .translationY(subtitleListPanel.getHeight())
                .setDuration(200)
                .withEndAction(() -> {
                    subtitleListPanel.setVisibility(View.GONE);
                    isSubtitlePanelVisible = false;
                })
                .start();
    }

    private void toggleSubtitleTimestamps() {
        if (subtitleListAdapter == null) return;
        boolean nowShow = !subtitleListAdapter.isShowTimestamps();
        subtitleListAdapter.setShowTimestamps(nowShow);
        btnToggleTimestamps.setText(nowShow
                ? R.string.subtitle_list_hide_time
                : R.string.subtitle_list_show_time);
    }

    /** Scrolls the subtitle list panel to the subtitle active at {@code positionMs}. */
    private void scrollToActiveSubtitle(long positionMs) {
        if (subtitleListAdapter == null) return;
        java.util.List<SubtitleEntry> subs = subtitleManager.getCurrentSubtitles();
        for (int i = 0; i < subs.size(); i++) {
            SubtitleEntry e = subs.get(i);
            if (positionMs >= e.getStartTimeMs() && positionMs < e.getEndTimeMs()) {
                subtitleListAdapter.setActiveIndex(i);
                rvSubtitleList.scrollToPosition(i);
                return;
            }
        }
        subtitleListAdapter.setActiveIndex(-1);
    }

    // ── Play mode cycling ──────────────────────────────────────────────────

    private void cyclePlayMode() {
        PlayMode[] modes   = PlayMode.values();
        PlayMode  current  = playerManager.getPlayMode();
        PlayMode  next     = modes[(current.ordinal() + 1) % modes.length];
        playerManager.setPlayMode(next);
        if (next == PlayMode.SHUFFLE) buildShuffleOrder();
        updatePlayModeIcon();
        onPlayModeChanged(next);
    }

    private void updatePlayModeIcon() {
        switch (playerManager.getPlayMode()) {
            case SEQUENTIAL:
                btnPlayMode.setImageResource(R.drawable.ic_mode_sequential);
                break;
            case SHUFFLE:
                btnPlayMode.setImageResource(R.drawable.ic_mode_shuffle);
                break;
            case REPEAT_ONE:
                btnPlayMode.setImageResource(R.drawable.ic_mode_repeat_one);
                break;
            case PLAY_ONCE:
                btnPlayMode.setImageResource(R.drawable.ic_mode_play_once);
                break;
        }
    }

    /**
     * Extensibility hook: called whenever the play mode changes.
     * Override or modify to add behaviour (e.g., persist mode, notify UI).
     */
    protected void onPlayModeChanged(PlayMode newMode) {
        int resId;
        switch (newMode) {
            case SEQUENTIAL:  resId = R.string.play_mode_sequential; break;
            case SHUFFLE:     resId = R.string.play_mode_shuffle;    break;
            case REPEAT_ONE:  resId = R.string.play_mode_repeat_one; break;
            default:          resId = R.string.play_mode_once;       break;
        }
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    // ── Shuffle order management ───────────────────────────────────────────

    private void buildShuffleOrder() {
        if (playlistUris == null || playlistUris.isEmpty()) return;
        int size  = playlistUris.size();
        shuffleOrder  = new int[size];
        for (int i = 0; i < size; i++) shuffleOrder[i] = i;
        Random rnd = new Random();
        for (int i = size - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int tmp = shuffleOrder[i];
            shuffleOrder[i] = shuffleOrder[j];
            shuffleOrder[j] = tmp;
        }
        // Make sure current item is at cursor=0 so we play from it first
        for (int i = 0; i < size; i++) {
            if (shuffleOrder[i] == playlistIndex) {
                int tmp = shuffleOrder[0];
                shuffleOrder[0] = shuffleOrder[i];
                shuffleOrder[i] = tmp;
                break;
            }
        }
        shuffleCursor = 0;
    }

    private void playNextShuffle() {
        if (shuffleOrder == null) buildShuffleOrder();
        shuffleCursor++;
        if (shuffleCursor >= shuffleOrder.length) {
            Toast.makeText(this, R.string.shuffle_end, Toast.LENGTH_SHORT).show();
            buildShuffleOrder(); // rebuild for next cycle
            showControls();
            return;
        }
        saveCurrentProgress();
        playlistIndex = shuffleOrder[shuffleCursor];
        switchToPlaylistItem(playlistIndex);
    }

    // ── Playlist navigation ────────────────────────────────────────────────

    private void playNext() {
        if (playlistUris == null || playlistUris.isEmpty()) return;

        if (playerManager.getPlayMode() == PlayMode.SHUFFLE) {
            playNextShuffle();
            return;
        }

        int next = playlistIndex + 1;
        if (next >= playlistUris.size()) {
            Toast.makeText(this, R.string.playlist_end, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentProgress();
        playlistIndex = next;
        switchToPlaylistItem(playlistIndex);
    }

    private void playPrevious() {
        if (playlistUris == null || playlistUris.isEmpty()) return;
        int prev = playlistIndex - 1;
        if (prev < 0) {
            Toast.makeText(this, R.string.playlist_start, Toast.LENGTH_SHORT).show();
            return;
        }
        saveCurrentProgress();
        playlistIndex = prev;
        switchToPlaylistItem(playlistIndex);
    }

    private void switchToPlaylistItem(int index) {
        String uriStr = playlistUris.get(index);
        Uri    uri    = Uri.parse(uriStr);
        savedPosition = 0;

        long[] prog = mediaListManager.getProgress(uriStr);
        if (prog != null && prog[1] > 0) savedPosition = prog[0];

        subtitleManager.clearSubtitles();
        subtitleView.setSubtitle(null);
        playerManager.play(uri);
        if (savedPosition > 0) playerManager.seekTo(savedPosition);

        if (panelAdapter != null) panelAdapter.setCurrentIndex(index);

        Toast.makeText(this,
                getString(R.string.playlist_now_playing, index + 1, playlistUris.size()),
                Toast.LENGTH_SHORT).show();
    }

    private void saveCurrentProgress() {
        if (playlistUris != null && !playlistUris.isEmpty()) {
            String currentUri = playlistUris.get(playlistIndex);
            mediaListManager.saveProgress(currentUri,
                    playerManager.getPosition(), playerManager.getDuration());
        } else {
            String uriStr = getIntent().getStringExtra(EXTRA_VIDEO_URI);
            if (uriStr != null) {
                mediaListManager.saveProgress(uriStr,
                        playerManager.getPosition(), playerManager.getDuration());
            }
        }
    }

    private String getCurrentUri() {
        if (playlistUris != null && !playlistUris.isEmpty()) {
            return playlistUris.get(playlistIndex);
        }
        return getIntent().getStringExtra(EXTRA_VIDEO_URI);
    }

    // ── Lock screen ────────────────────────────────────────────────────────

    private void lockScreen() {
        isLocked = true;
        uiHandler.removeCallbacks(hideControlsRunnable);
        controlsOverlay.setVisibility(View.GONE);
        lockOverlay.setVisibility(View.VISIBLE);
        Toast.makeText(this, R.string.screen_locked, Toast.LENGTH_SHORT).show();
    }

    private void unlockScreen() {
        isLocked = false;
        lockOverlay.setVisibility(View.GONE);
        showControls();
        Toast.makeText(this, R.string.screen_unlocked, Toast.LENGTH_SHORT).show();
    }

    // ── Brightness ─────────────────────────────────────────────────────────

    private void adjustBrightness(float delta) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float current = lp.screenBrightness;
        if (current < 0) {
            try {
                int sys = android.provider.Settings.System.getInt(
                        getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS);
                current = sys / 255f;
            } catch (Exception e) {
                current = 0.5f;
            }
        }
        current = Math.max(0.01f, Math.min(1.0f, current + delta));
        lp.screenBrightness = current;
        getWindow().setAttributes(lp);

        int percent = (int) (current * 100);
        tvBrightnessValue.setText(percent + "%");

        brightnessIndicator.setVisibility(View.VISIBLE);
        uiHandler.removeCallbacks(hideBrightnessRunnable);
        uiHandler.postDelayed(hideBrightnessRunnable, INDICATOR_HIDE_DELAY);
    }

    // ── Controls visibility ────────────────────────────────────────────────

    private void showControls() {
        controlsOverlay.setVisibility(View.VISIBLE);
        controlsOverlay.animate().alpha(1f).setDuration(200).start();
        scheduleHideControls();
    }

    private void hideControls() {
        controlsOverlay.animate().alpha(0f).setDuration(200)
                .withEndAction(() -> controlsOverlay.setVisibility(View.GONE)).start();
    }

    private void toggleControlsVisibility() {
        if (isPanelVisible) { closePlaylistPanel(); return; }
        if (controlsOverlay.getVisibility() == View.VISIBLE) {
            uiHandler.removeCallbacks(hideControlsRunnable);
            hideControls();
        } else {
            showControls();
        }
    }

    private void scheduleHideControls() {
        uiHandler.removeCallbacks(hideControlsRunnable);
        uiHandler.postDelayed(hideControlsRunnable, CONTROLS_HIDE_DELAY);
    }

    // ── IPlayerEventListener ───────────────────────────────────────────────

    @Override
    public void onPlaybackStarted() {
        // Mark the moment playback (re)started for time tracking
        playStatsStartMs = android.os.SystemClock.elapsedRealtime();
        runOnUiThread(() -> {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            loadingView.setVisibility(View.GONE);
        });
    }

    @Override
    public void onPlaybackPaused() {
        // Accumulate time since last play start
        if (playStatsStartMs >= 0) {
            playStatsAccumMs += android.os.SystemClock.elapsedRealtime() - playStatsStartMs;
            playStatsStartMs = -1L;
        }
        runOnUiThread(() -> btnPlayPause.setImageResource(R.drawable.ic_play));
    }

    @Override
    public void onPlaybackCompleted() {
        // Accumulate any remaining time on natural completion
        if (playStatsStartMs >= 0) {
            playStatsAccumMs += android.os.SystemClock.elapsedRealtime() - playStatsStartMs;
            playStatsStartMs = -1L;
        }
        runOnUiThread(() -> {
            saveCurrentProgress();
            btnPlayPause.setImageResource(R.drawable.ic_play);
            showControls();
            handlePlayModeOnCompletion();
        });
    }

    private void handlePlayModeOnCompletion() {
        PlayMode mode = playerManager.getPlayMode();
        switch (mode) {
            case REPEAT_ONE:
                // Replay current item immediately
                uiHandler.postDelayed(() -> switchToPlaylistItem(playlistIndex), 300);
                break;

            case PLAY_ONCE:
                // Stop — do not advance
                break;

            case SHUFFLE:
                if (playlistUris != null && !playlistUris.isEmpty()) {
                    uiHandler.postDelayed(this::playNextShuffle, 800);
                }
                break;

            case SEQUENTIAL:
            default:
                if (playlistUris != null && playlistIndex + 1 < playlistUris.size()) {
                    uiHandler.postDelayed(this::playNext, 800);
                }
                break;
        }
    }

    @Override
    public void onPositionChanged(long positionMs, long durationMs) {
        runOnUiThread(() -> {
            SubtitleEntry entry = subtitleManager.getSubtitleAt(positionMs);
            subtitleView.setSubtitle(entry != null ? entry.getPlainText() : null);
            // Enforce user's subtitle visibility choice — SubtitleView.setSubtitle() may call
            // setVisibility(VISIBLE) internally; re-hide if the user has turned subtitles off.
            if (!isSubtitleTextVisible) subtitleView.setVisibility(View.GONE);

            if (!isDraggingSeekBar) {
                tvCurrentTime.setText(TimeUtils.formatDuration(positionMs));
                tvDuration.setText(TimeUtils.formatDuration(durationMs));
                if (durationMs > 0) {
                    int progress = (int) (positionMs * 1000L / durationMs);
                    seekBar.setProgress(progress);
                }
            }

            // Update subtitle list highlight when panel is visible
            if (isSubtitlePanelVisible && subtitleListAdapter != null) {
                java.util.List<SubtitleEntry> subs = subtitleManager.getCurrentSubtitles();
                int activeIdx = -1;
                for (int i = 0; i < subs.size(); i++) {
                    SubtitleEntry e = subs.get(i);
                    if (positionMs >= e.getStartTimeMs() && positionMs < e.getEndTimeMs()) {
                        activeIdx = i;
                        break;
                    }
                }
                if (activeIdx != subtitleListAdapter.getActiveIndex()) {
                    subtitleListAdapter.setActiveIndex(activeIdx);
                    if (activeIdx >= 0) rvSubtitleList.smoothScrollToPosition(activeIdx);
                }
            }
        });
    }

    @Override
    public void onSpeedChanged(float speed) {
        runOnUiThread(() -> {
            String label = speed == 1.0f ? "1×" : speed + "×";
            tvSpeed.setText(label);
        });
    }

    @Override
    public void onError(String errorMessage) {
        runOnUiThread(() -> {
            Toast.makeText(this,
                    getString(R.string.error_playback) + ": " + errorMessage,
                    Toast.LENGTH_LONG).show();
            loadingView.setVisibility(View.GONE);
        });
    }

    @Override
    public void onBufferingChanged(boolean isBuffering) {
        runOnUiThread(() ->
                loadingView.setVisibility(isBuffering ? View.VISIBLE : View.GONE));
    }

    // ── Speed menu ─────────────────────────────────────────────────────────

    private void showSpeedMenu() {
        float[] speeds  = playerManager.getSpeedProvider().getAvailableSpeeds();
        String[] labels = new String[speeds.length];
        int currentIdx  = 0;
        for (int i = 0; i < speeds.length; i++) {
            labels[i] = speeds[i] == 1.0f ? "1×" : speeds[i] + "×";
            if (Math.abs(speeds[i] - playerManager.getCurrentSpeed()) < 0.01f) currentIdx = i;
        }
        // Tap any speed option → apply immediately and dismiss (no confirm button needed)
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.speed)
                .setSingleChoiceItems(labels, currentIdx, (dialog, which) -> {
                    playerManager.setSpeed(speeds[which]);
                    scheduleHideControls();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ── Subtitle button handling (single click = pick source, double click = clear) ──

    private void handleSubtitleButtonClick() {
        long now = System.currentTimeMillis();
        if (now - lastSubtitleClickMs < 400) {
            // Double click — clear/hide subtitle
            lastSubtitleClickMs = 0;
            uiHandler.removeCallbacks(subtitleSingleClickRunnable);
            clearSubtitle();
        } else {
            lastSubtitleClickMs = now;
            uiHandler.removeCallbacks(subtitleSingleClickRunnable);
            uiHandler.postDelayed(subtitleSingleClickRunnable, 400);
        }
    }

    private final Runnable subtitleSingleClickRunnable = this::showSubtitleImportMenu;

    /**
     * Toggles subtitle text visibility only.
     * Does NOT close or affect the subtitle list panel — the list panel stays open.
     * Uses isSubtitleTextVisible state so the subtitle resumes exactly where it was.
     */
    private void toggleSubtitleTextOnly() {
        isSubtitleTextVisible = !isSubtitleTextVisible;
        subtitleView.setVisibility(isSubtitleTextVisible ? View.VISIBLE : View.GONE);
        if (btnSubtitleToggle != null) {
            btnSubtitleToggle.setImageResource(
                    isSubtitleTextVisible ? R.drawable.ic_subtitle_toggle : R.drawable.ic_subtitle_off);
        }
        Toast.makeText(this,
                isSubtitleTextVisible ? R.string.subtitle_shown : R.string.subtitle_hidden,
                Toast.LENGTH_SHORT).show();
        scheduleHideControls();
    }

    private void clearSubtitle() {
        subtitleManager.clearSubtitles();
        subtitleView.setSubtitle(null);
        refreshSubtitleListButton();
        Toast.makeText(this, R.string.subtitle_cleared, Toast.LENGTH_SHORT).show();
    }

    private void showSubtitleImportMenu() {
        String[] options = {
                getString(R.string.subtitle_from_library),
                getString(R.string.subtitle_from_file)
        };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) openSubtitleLibrary();
                    else            pickSubtitleFile();
                })
                .show();
    }

    private void openSubtitleLibrary() {
        Intent intent = new Intent(this, SubtitleLibraryActivity.class);
        subtitleLibraryLauncher.launch(intent);
    }

    private void showExportFormatDialog() {
        if (!subtitleManager.hasSubtitles()) {
            Toast.makeText(this, R.string.subtitle_none_to_export, Toast.LENGTH_SHORT).show();
            return;
        }
        String[] formats = { "SRT", "VTT" };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.subtitle_export)
                .setItems(formats, (d, which) -> startExport(formats[which]))
                .show();
    }

    private void pickSubtitleFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/x-subrip",   // .srt
                "text/vtt",               // .vtt
                "text/x-ssa",             // .ass / .ssa
                "text/plain",             // .txt
                "text/markdown",          // .md (standard MIME)
                "text/x-markdown",        // .md (alternate MIME)
                "application/octet-stream" // fallback for files without recognized MIME
        });
        importLauncher.launch(intent);
    }

    private void importSubtitle(Uri uri) {
        boolean ok = subtitleManager.loadSubtitles(this, uri, "UTF-8");
        if (ok) {
            // Also save to subtitle library for reuse
            String fileName = com.videomaster.app.util.FileUtils.getFileName(this, uri);
            subtitleLibraryManager.importFromUri(this, uri, fileName);
        }
        Toast.makeText(this,
                ok ? getString(R.string.subtitle_loaded,
                        subtitleManager.getCurrentSubtitles().size())
                   : getString(R.string.subtitle_load_failed),
                Toast.LENGTH_SHORT).show();
        refreshSubtitleListButton();
    }

    private void importSubtitleFromFile(java.io.File file) {
        boolean ok = subtitleManager.loadSubtitlesFromFile(file, "UTF-8");
        Toast.makeText(this,
                ok ? getString(R.string.subtitle_loaded,
                        subtitleManager.getCurrentSubtitles().size())
                   : getString(R.string.subtitle_load_failed),
                Toast.LENGTH_SHORT).show();
        refreshSubtitleListButton();
    }

    private void startExport(String format) {
        if (!subtitleManager.hasSubtitles()) {
            Toast.makeText(this, R.string.subtitle_none_to_export, Toast.LENGTH_SHORT).show();
            return;
        }
        pendingExportFormat = format;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String ext  = format.equalsIgnoreCase("VTT") ? ".vtt" : ".srt";
        String mime = format.equalsIgnoreCase("VTT") ? "text/vtt" : "application/x-subrip";
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_TITLE, "subtitle" + ext);
        exportLauncher.launch(intent);
    }

    private void doExportSubtitle(Uri uri) {
        try (OutputStream out = getContentResolver().openOutputStream(uri)) {
            boolean ok = subtitleManager.exportSubtitles(pendingExportFormat, out, "UTF-8");
            Toast.makeText(this,
                    ok ? R.string.subtitle_exported : R.string.subtitle_export_failed,
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.subtitle_export_failed, Toast.LENGTH_SHORT).show();
        }
    }

    // ── Orientation ────────────────────────────────────────────────────────

    private void toggleOrientation() {
        int current = getResources().getConfiguration().orientation;
        if (current == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    // ── Immersive mode ─────────────────────────────────────────────────────

    private void enterImmersiveMode() {
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }
}
