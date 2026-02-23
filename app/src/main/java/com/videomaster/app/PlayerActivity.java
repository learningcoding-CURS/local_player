package com.videomaster.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
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
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.ui.PlayerView;

import com.videomaster.app.interfaces.IPlayerEventListener;
import com.videomaster.app.player.GestureHandler;
import com.videomaster.app.player.PlayerManager;
import com.videomaster.app.playlist.MediaListManager;
import com.videomaster.app.subtitle.SubtitleEntry;
import com.videomaster.app.subtitle.SubtitleManager;
import com.videomaster.app.ui.SubtitleView;
import com.videomaster.app.util.FileUtils;
import com.videomaster.app.util.TimeUtils;

import java.io.OutputStream;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity implements IPlayerEventListener {

    public static final String EXTRA_VIDEO_URI       = "extra_video_uri";
    public static final String EXTRA_PLAYLIST_URIS   = "extra_playlist_uris";
    public static final String EXTRA_PLAYLIST_INDEX  = "extra_playlist_index";
    public static final String EXTRA_PLAYLIST_ID     = "extra_playlist_id";

    private static final long  CONTROLS_HIDE_DELAY   = 3000;
    private static final long  INDICATOR_HIDE_DELAY  = 1500;

    private PlayerManager   playerManager;
    private SubtitleManager subtitleManager;
    private MediaListManager mediaListManager;

    // Views
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
    private ImageButton   btnSpeed;
    private ImageButton   btnLock;
    private ImageButton   btnUnlock;
    private ImageButton   btnAddToPlaylist;
    private TextView      tvSpeed;
    private TextView      tvLongPressHint;
    private View          loadingView;

    // Brightness indicator
    private View          brightnessIndicator;
    private ProgressBar   brightnessBar;
    private TextView      tvBrightnessValue;

    // Volume indicator
    private View          volumeIndicator;
    private ProgressBar   volumeBar;
    private TextView      tvVolumeValue;

    private final Handler uiHandler             = new Handler(Looper.getMainLooper());
    private final Runnable hideControlsRunnable = this::hideControls;
    private final Runnable hideBrightnessRunnable = () -> brightnessIndicator.setVisibility(View.GONE);
    private final Runnable hideVolumeRunnable     = () -> volumeIndicator.setVisibility(View.GONE);

    private boolean isDraggingSeekBar = false;
    private boolean isLocked          = false;
    private long    savedPosition     = 0;

    // Playlist navigation
    private ArrayList<String> playlistUris  = null;
    private int               playlistIndex = 0;
    private String            playlistId    = null;

    // Audio manager for volume control
    private AudioManager audioManager;

    // Subtitle import/export
    private final androidx.activity.result.ActivityResultLauncher<Intent> importLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts
                    .StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) importSubtitle(uri);
                }
            });

    private final androidx.activity.result.ActivityResultLauncher<Intent> exportLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts
                    .StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) doExportSubtitle(uri);
                }
            });

    private String pendingExportFormat = "SRT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        audioManager     = (AudioManager) getSystemService(AUDIO_SERVICE);
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

        // Playlist context
        playlistUris  = getIntent().getStringArrayListExtra(EXTRA_PLAYLIST_URIS);
        playlistIndex = getIntent().getIntExtra(EXTRA_PLAYLIST_INDEX, 0);
        playlistId    = getIntent().getStringExtra(EXTRA_PLAYLIST_ID);

        if (savedInstanceState != null) {
            savedPosition = savedInstanceState.getLong("position", 0);
        }

        Uri videoUri = Uri.parse(uriStr);
        playerManager.play(videoUri);
        if (savedPosition > 0) playerManager.seekTo(savedPosition);
    }

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
        btnSpeed            = findViewById(R.id.btnSpeed);
        btnLock             = findViewById(R.id.btnLock);
        btnUnlock           = findViewById(R.id.btnUnlock);
        btnAddToPlaylist    = findViewById(R.id.btnAddToPlaylist);
        tvSpeed             = findViewById(R.id.tvSpeed);
        tvLongPressHint     = findViewById(R.id.tvLongPressHint);
        loadingView         = findViewById(R.id.loadingView);

        brightnessIndicator = findViewById(R.id.brightnessIndicator);
        brightnessBar       = findViewById(R.id.brightnessBar);
        tvBrightnessValue   = findViewById(R.id.tvBrightnessValue);

        volumeIndicator     = findViewById(R.id.volumeIndicator);
        volumeBar           = findViewById(R.id.volumeBar);
        tvVolumeValue       = findViewById(R.id.tvVolumeValue);

        enterImmersiveMode();
    }

    private void initPlayer() {
        subtitleManager = SubtitleManager.getInstance();
        playerManager   = new PlayerManager(this);
        playerManager.addListener(this);

        playerView.setPlayer(playerManager.getExoPlayer());
        playerView.setUseController(false);
    }

    private void setupGestures() {
        GestureHandler gestureHandler = new GestureHandler(this,
                new GestureHandler.GestureListener() {
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

                    @Override public void onVerticalScroll(boolean isLeftSide, float delta) {
                        if (!isLocked) {
                            if (isLeftSide) adjustBrightness(delta);
                            else            adjustVolume(delta);
                        }
                    }

                    @Override public void onSwipeMedia(boolean toNext) {
                        if (!isLocked) {
                            playerManager.deactivateLongPressSpeed();
                            tvLongPressHint.setVisibility(View.GONE);
                            if (toNext) playNext();
                            else        playPrevious();
                        }
                    }
                });
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
        btnSpeed.setOnClickListener(v -> showSpeedMenu());

        btnLock.setOnClickListener(v -> lockScreen());
        btnUnlock.setOnClickListener(v -> unlockScreen());

        btnAddToPlaylist.setOnClickListener(v -> showAddToPlaylistMenu());

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

    // ──────────────────────── Lock screen ────────────────────────

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

    // ──────────────────────── Brightness ────────────────────────

    private void adjustBrightness(float delta) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        float current = lp.screenBrightness;
        if (current < 0) {
            // Read current system brightness and normalize
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
        brightnessBar.setProgress(percent);
        tvBrightnessValue.setText(percent + "%");

        brightnessIndicator.setVisibility(View.VISIBLE);
        uiHandler.removeCallbacks(hideBrightnessRunnable);
        uiHandler.postDelayed(hideBrightnessRunnable, INDICATOR_HIDE_DELAY);
    }

    // ──────────────────────── Volume ────────────────────────

    private void adjustVolume(float delta) {
        int maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int newVol = (int) Math.max(0, Math.min(maxVol, curVol + delta * maxVol));
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0);

        int percent = (int) (newVol * 100f / maxVol);
        volumeBar.setProgress(percent);
        tvVolumeValue.setText(percent + "%");

        volumeIndicator.setVisibility(View.VISIBLE);
        uiHandler.removeCallbacks(hideVolumeRunnable);
        uiHandler.postDelayed(hideVolumeRunnable, INDICATOR_HIDE_DELAY);
    }

    // ──────────────────────── Playlist navigation ────────────────────────

    private void playNext() {
        if (playlistUris == null || playlistUris.isEmpty()) return;
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
        Uri uri = Uri.parse(uriStr);
        savedPosition = 0;

        // Restore saved progress for this item
        long[] prog = mediaListManager.getProgress(uriStr);
        if (prog != null && prog[1] > 0) savedPosition = prog[0];

        subtitleManager.clearSubtitles();
        subtitleView.setSubtitle(null);
        playerManager.play(uri);
        if (savedPosition > 0) playerManager.seekTo(savedPosition);

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

    // ──────────────────────── Add to playlist ────────────────────────

    private void showAddToPlaylistMenu() {
        java.util.List<com.videomaster.app.model.MediaList> lists = mediaListManager.getLists();
        if (lists.isEmpty()) {
            new AlertDialog.Builder(this, R.style.DarkDialog)
                    .setTitle(R.string.playlist_add_to)
                    .setMessage(R.string.playlist_none_create_first)
                    .setPositiveButton(R.string.playlist_create, (d, w) -> showCreatePlaylistAndAdd())
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
                .setNeutralButton(R.string.playlist_create, (d, w) -> showCreatePlaylistAndAdd())
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

    private String getCurrentUri() {
        if (playlistUris != null && !playlistUris.isEmpty()) {
            return playlistUris.get(playlistIndex);
        }
        return getIntent().getStringExtra(EXTRA_VIDEO_URI);
    }

    // ──────────────────────── Controls visibility ────────────────────────

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

    // ──────────────────────── IPlayerEventListener ────────────────────────

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
            // Auto-advance playlist
            if (playlistUris != null && playlistIndex + 1 < playlistUris.size()) {
                uiHandler.postDelayed(this::playNext, 1000);
            }
        });
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
            Toast.makeText(this, getString(R.string.error_playback) + ": " + errorMessage,
                    Toast.LENGTH_LONG).show();
            loadingView.setVisibility(View.GONE);
        });
    }

    @Override
    public void onBufferingChanged(boolean isBuffering) {
        runOnUiThread(() -> loadingView.setVisibility(isBuffering ? View.VISIBLE : View.GONE));
    }

    // ──────────────────────── Speed menu ────────────────────────

    private void showSpeedMenu() {
        float[] speeds = playerManager.getSpeedProvider().getAvailableSpeeds();
        String[] labels = new String[speeds.length];
        int currentIdx = 0;
        for (int i = 0; i < speeds.length; i++) {
            labels[i] = speeds[i] == 1.0f ? "1×" : speeds[i] + "×";
            if (Math.abs(speeds[i] - playerManager.getCurrentSpeed()) < 0.01f) currentIdx = i;
        }
        final int[] selected = {currentIdx};
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.speed)
                .setSingleChoiceItems(labels, currentIdx, (dialog, which) -> selected[0] = which)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    playerManager.setSpeed(speeds[selected[0]]);
                    scheduleHideControls();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ──────────────────────── Subtitle menu ────────────────────────

    private void showSubtitleMenu() {
        String[] options = {
                getString(R.string.subtitle_import),
                getString(R.string.subtitle_export_srt),
                getString(R.string.subtitle_export_vtt),
                getString(R.string.subtitle_clear)
        };
        new AlertDialog.Builder(this, R.style.DarkDialog)
                .setTitle(R.string.subtitle)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: pickSubtitleFile(); break;
                        case 1: startExport("SRT"); break;
                        case 2: startExport("VTT"); break;
                        case 3:
                            subtitleManager.clearSubtitles();
                            subtitleView.setSubtitle(null);
                            Toast.makeText(this, R.string.subtitle_cleared, Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
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
                ok ? getString(R.string.subtitle_loaded, subtitleManager.getCurrentSubtitles().size())
                   : getString(R.string.subtitle_load_failed),
                Toast.LENGTH_SHORT).show();
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

    // ──────────────────────── Orientation ────────────────────────

    private void toggleOrientation() {
        int current = getResources().getConfiguration().orientation;
        if (current == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    // ──────────────────────── Immersive mode ────────────────────────

    private void enterImmersiveMode() {
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        }
    }

    // ──────────────────────── Lifecycle ────────────────────────

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (playerManager != null) outState.putLong("position", playerManager.getPosition());
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
}
