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

import com.videomaster.app.interfaces.IPlayerEventListener;
import com.videomaster.app.player.GestureHandler;
import com.videomaster.app.player.PlayerManager;
import com.videomaster.app.subtitle.SubtitleEntry;
import com.videomaster.app.subtitle.SubtitleManager;
import com.videomaster.app.ui.SubtitleView;
import com.videomaster.app.util.FileUtils;
import com.videomaster.app.util.TimeUtils;

import java.io.OutputStream;

public class PlayerActivity extends AppCompatActivity implements IPlayerEventListener {

    public static final String EXTRA_VIDEO_URI = "extra_video_uri";

    // Controls auto-hide delay in ms
    private static final long CONTROLS_HIDE_DELAY = 3000;

    private PlayerManager   playerManager;
    private SubtitleManager subtitleManager;

    // Views
    private PlayerView    playerView;
    private SubtitleView  subtitleView;
    private FrameLayout   controlsOverlay;
    private SeekBar       seekBar;
    private TextView      tvCurrentTime;
    private TextView      tvDuration;
    private ImageButton   btnPlayPause;
    private ImageButton   btnRewind;
    private ImageButton   btnForward;
    private ImageButton   btnSubtitle;
    private ImageButton   btnRotate;
    private ImageButton   btnSpeed;
    private TextView      tvSpeed;
    private TextView      tvLongPressHint;
    private View          loadingView;

    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideControlsRunnable = this::hideControls;

    private boolean isDraggingSeekBar = false;
    private long    savedPosition     = 0;

    // Subtitle import/export
    private static final int REQUEST_IMPORT_SUBTITLE = 200;
    private static final int REQUEST_EXPORT_SUBTITLE = 201;

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

        if (savedInstanceState != null) {
            savedPosition = savedInstanceState.getLong("position", 0);
        }

        Uri videoUri = Uri.parse(uriStr);
        playerManager.play(videoUri);
        if (savedPosition > 0) playerManager.seekTo(savedPosition);
    }

    private void initViews() {
        playerView      = findViewById(R.id.playerView);
        subtitleView    = findViewById(R.id.subtitleView);
        controlsOverlay = findViewById(R.id.controlsOverlay);
        seekBar         = findViewById(R.id.seekBar);
        tvCurrentTime   = findViewById(R.id.tvCurrentTime);
        tvDuration      = findViewById(R.id.tvDuration);
        btnPlayPause    = findViewById(R.id.btnPlayPause);
        btnRewind       = findViewById(R.id.btnRewind);
        btnForward      = findViewById(R.id.btnForward);
        btnSubtitle     = findViewById(R.id.btnSubtitle);
        btnRotate       = findViewById(R.id.btnRotate);
        btnSpeed        = findViewById(R.id.btnSpeed);
        tvSpeed         = findViewById(R.id.tvSpeed);
        tvLongPressHint = findViewById(R.id.tvLongPressHint);
        loadingView     = findViewById(R.id.loadingView);

        enterImmersiveMode();
    }

    private void initPlayer() {
        subtitleManager = SubtitleManager.getInstance();
        playerManager   = new PlayerManager(this);
        playerManager.addListener(this);

        // Attach ExoPlayer to PlayerView (surface only — we handle controls ourselves)
        playerView.setPlayer(playerManager.getExoPlayer());
        playerView.setUseController(false);
    }

    private void setupGestures() {
        GestureHandler gestureHandler = new GestureHandler(this,
                new GestureHandler.GestureListener() {
                    @Override public void onSingleTap() { toggleControlsVisibility(); }

                    @Override public void onDoubleTap() {
                        playerManager.togglePlayPause();
                        showControls();
                    }

                    @Override public void onLongPressStart() {
                        playerManager.activateLongPressSpeed();
                        tvLongPressHint.setVisibility(View.VISIBLE);
                        uiHandler.removeCallbacks(hideControlsRunnable);
                    }

                    @Override public void onLongPressEnd() {
                        playerManager.deactivateLongPressSpeed();
                        tvLongPressHint.setVisibility(View.GONE);
                        scheduleHideControls();
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
            btnPlayPause.setImageResource(R.drawable.ic_play);
            showControls();
        });
    }

    @Override
    public void onPositionChanged(long positionMs, long durationMs) {
        runOnUiThread(() -> {
            // Update subtitle
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

    // ──────────────────────── Subtitle import ────────────────────────

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

    // ──────────────────────── Subtitle export ────────────────────────

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
        if (playerManager != null) playerManager.pause();
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
