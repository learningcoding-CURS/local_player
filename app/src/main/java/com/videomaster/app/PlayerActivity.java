package com.videomaster.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
    private ImageButton   btnSubtitle;
    private ImageButton   btnRotate;
    private ImageButton   btnLock;
    private ImageButton   btnUnlock;
    private ImageButton   btnAddToPlaylist;
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

    private String pendingExportFormat = "SRT";

    // ── Lifecycle ──────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mediaListManager = MediaListManager.getInstance(this);

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
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
        if (playerManager != null) {
            playerManager.removeListener(this);
            playerManager.release();
        }
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
        btnSubtitle         = findViewById(R.id.btnSubtitle);
        btnRotate           = findViewById(R.id.btnRotate);
        btnLock             = findViewById(R.id.btnLock);
        btnUnlock           = findViewById(R.id.btnUnlock);
        btnAddToPlaylist    = findViewById(R.id.btnAddToPlaylist);
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

        enterImmersiveMode();
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
            playerManager.seekRelative(-5000);
            scheduleHideControls();
        });

        btnForward.setOnClickListener(v -> {
            playerManager.seekRelative(5000);
            scheduleHideControls();
        });

        btnRotate.setOnClickListener(v -> toggleOrientation());
        btnSubtitle.setOnClickListener(v -> showSubtitleMenu());
        tvSpeed.setOnClickListener(v -> showSpeedMenu());
        btnLock.setOnClickListener(v -> lockScreen());
        btnUnlock.setOnClickListener(v -> unlockScreen());
        btnAddToPlaylist.setOnClickListener(v -> showAddToPlaylistMenu());

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
            return;
        }
        btnPlaylistPanel.setVisibility(View.VISIBLE);
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

    private void openPlaylistPanel() {
        if (playlistUris == null || playlistUris.isEmpty()) return;
        playlistPanel.setVisibility(View.VISIBLE);
        playlistPanel.setTranslationX(playlistPanel.getWidth());
        playlistPanel.animate().translationX(0).setDuration(250).start();
        isPanelVisible = true;
        uiHandler.removeCallbacks(hideControlsRunnable);
    }

    private void closePlaylistPanel() {
        if (!isPanelVisible) return;
        playlistPanel.animate()
                .translationX(playlistPanel.getWidth())
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
        subtitleListAdapter = new SubtitleListAdapter(subs,
                entry -> {
                    playerManager.seekTo(entry.getStartTimeMs());
                    subtitleManager.onSeek();
                    // Do NOT close panel; just scroll to the clicked entry
                    scheduleHideControls();
                });

        rvSubtitleList.setLayoutManager(new LinearLayoutManager(this));
        rvSubtitleList.setAdapter(subtitleListAdapter);

        boolean isEmpty = subs.isEmpty();
        tvSubtitleListEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvSubtitleList.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        subtitleListPanel.setVisibility(View.VISIBLE);
        subtitleListPanel.post(() -> {
            subtitleListPanel.setTranslationY(subtitleListPanel.getHeight());
            subtitleListPanel.animate().translationY(0).setDuration(250).start();
        });
        isSubtitlePanelVisible = true;
        uiHandler.removeCallbacks(hideControlsRunnable);

        // Scroll to active subtitle
        scrollToActiveSubtitle(playerManager.getPosition());
    }

    private void closeSubtitleListPanel() {
        if (!isSubtitlePanelVisible) return;
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

    // ── Add to playlist ────────────────────────────────────────────────────

    private void showAddToPlaylistMenu() {
        java.util.List<com.videomaster.app.model.MediaList> lists = mediaListManager.getLists();
        if (lists.isEmpty()) {
            new AlertDialog.Builder(this, R.style.DarkDialog)
                    .setTitle(R.string.playlist_add_to)
                    .setMessage(R.string.playlist_none_create_first)
                    .setPositiveButton(R.string.playlist_create,
                            (d, w) -> showCreatePlaylistAndAdd())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return;
        }

        String[] names = new String[lists.size()];
        for (int i = 0; i < lists.size(); i++) names[i] = lists.get(i).getName();

        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.playlist_add_to)
                .setItems(names, (d, which) -> {
                    String uriStr = getCurrentUri();
                    if (uriStr != null) {
                        mediaListManager.addItemToList(lists.get(which).getId(), uriStr);
                        Toast.makeText(this,
                                getString(R.string.playlist_added, lists.get(which).getName()),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton(R.string.playlist_create,
                        (d, w) -> showCreatePlaylistAndAdd())
                .show();
    }

    private void showCreatePlaylistAndAdd() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(R.string.playlist_name_hint);
        input.setTextColor(getResources().getColor(R.color.textPrimary, null));
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(pad, pad / 2, pad, pad / 2);

        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.playlist_create)
                .setView(input)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        com.videomaster.app.model.MediaList list =
                                mediaListManager.createList(name, "");
                        String uriStr = getCurrentUri();
                        if (uriStr != null) {
                            mediaListManager.addItemToList(list.getId(), uriStr);
                            Toast.makeText(this,
                                    getString(R.string.playlist_added, name),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
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
        runOnUiThread(() -> {
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            loadingView.setVisibility(View.GONE);
        });
    }

    @Override
    public void onPlaybackPaused() {
        runOnUiThread(() -> btnPlayPause.setImageResource(R.drawable.ic_play));
    }

    @Override
    public void onPlaybackCompleted() {
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

    // ── Subtitle menu ──────────────────────────────────────────────────────

    private void showSubtitleMenu() {
        String[] options = {
                getString(R.string.subtitle_import),
                getString(R.string.subtitle_export),
                getString(R.string.subtitle_clear)
        };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.subtitle)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: pickSubtitleFile();       break;
                        case 1: showExportFormatDialog(); break;
                        case 2:
                            subtitleManager.clearSubtitles();
                            subtitleView.setSubtitle(null);
                            refreshSubtitleListButton();
                            Toast.makeText(this, R.string.subtitle_cleared,
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
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
        intent.putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"application/x-subrip", "text/vtt", "text/x-ssa", "text/plain"});
        importLauncher.launch(intent);
    }

    private void importSubtitle(Uri uri) {
        boolean ok = subtitleManager.loadSubtitles(this, uri, "UTF-8");
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
