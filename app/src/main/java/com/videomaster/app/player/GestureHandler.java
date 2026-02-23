package com.videomaster.app.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Handles touch gestures on the player surface.
 *
 * Swipe detection works at both the view level (playerView) AND at the Activity level
 * via feedSwipeEvent(), ensuring swipes are reliably detected even when the controls
 * overlay intercepts touch events.
 *
 * Swipe direction for media switching is configurable per orientation:
 *   setPortraitSwipeDirection(SwipeDirection)   – default VERTICAL
 *   setLandscapeSwipeDirection(SwipeDirection)  – default HORIZONTAL
 *
 * Portrait VERTICAL mode:
 *   Left side vertical scroll   → adjust brightness (up = brighter)
 *   Right side DOWN swipe       → next media
 *   Right side UP   swipe       → previous media
 *
 * Portrait HORIZONTAL mode:
 *   Left side vertical scroll   → adjust brightness
 *   LEFT  swipe                 → next media
 *   RIGHT swipe                 → previous media
 *
 * Landscape HORIZONTAL mode (default):
 *   Left side vertical scroll   → adjust brightness
 *   LEFT  swipe                 → next media
 *   RIGHT swipe                 → previous media
 *
 * Landscape VERTICAL mode:
 *   Left side vertical scroll   → adjust brightness
 *   Right side DOWN swipe       → next media
 *   Right side UP   swipe       → previous media
 *
 * Long press (no movement) always activates 2.5× speed boost regardless of orientation.
 */
public class GestureHandler implements View.OnTouchListener {

    // ── Swipe direction enum ────────────────────────────────────────────────

    public enum SwipeDirection {
        /** Up/Down swipe triggers media switch (left side reserved for brightness). */
        VERTICAL,
        /** Left/Right swipe triggers media switch. */
        HORIZONTAL
    }

    // ── Callback interface ──────────────────────────────────────────────────

    public interface GestureListener {
        void onSingleTap();
        void onDoubleTap();
        void onLongPressStart();

        /** Fired on release when no swipe occurred during long press. */
        void onLongPressEnd();

        /**
         * Fired during left-side vertical scroll.
         * @param delta positive = brighter; negative = dimmer.
         */
        void onBrightnessScroll(float delta);

        /**
         * Fired when a media-switch swipe is detected.
         * @param toNext true = go to next; false = go to previous.
         */
        void onSwipeMedia(boolean toNext);
    }

    // ── Tuning constants ───────────────────────────────────────────────────

    private static final long  DOUBLE_TAP_TIMEOUT_MS  = 300;
    /** Minimum finger travel (px equivalent of dp) to register a media-switch swipe. */
    private static final float SWITCH_THRESHOLD_DP    = 50f;
    /** Brightness sensitivity: fraction of [0,1] per pixel scrolled. */
    private static final float BRIGHTNESS_SENSITIVITY = 0.005f;

    // ── State ──────────────────────────────────────────────────────────────

    private final GestureDetector detector;
    private final GestureListener listener;
    private final Handler         handler = new Handler(Looper.getMainLooper());

    private boolean isLandscape     = false;
    private float   viewWidth       = 1f;

    /** Swipe direction for media switching in portrait mode. */
    private SwipeDirection portraitSwipe  = SwipeDirection.VERTICAL;
    /** Swipe direction for media switching in landscape mode. */
    private SwipeDirection landscapeSwipe = SwipeDirection.HORIZONTAL;

    // Long-press state
    private boolean lpActive  = false;
    private boolean lpSwiped  = false;

    // Shared one-shot swipe guard — reset on each ACTION_DOWN
    // Used by BOTH the view-level and activity-level paths to prevent double-fire.
    private boolean swipeConsumed = false;

    // Stored initial touch position (avoids relying on possibly-recycled MotionEvent e1)
    private float touchStartX = 0f;
    private float touchStartY = 0f;

    // Activity-level swipe tracking (separate start position for dispatchTouchEvent path)
    private float activityStartX = 0f;
    private float activityStartY = 0f;
    private float activityWindowWidth = 1f;

    private final float switchThresholdPx;
    private final Runnable singleTapRunnable;

    // ── Constructor ────────────────────────────────────────────────────────

    public GestureHandler(Context context, GestureListener listener) {
        this.listener = listener;

        float density     = context.getResources().getDisplayMetrics().density;
        switchThresholdPx = SWITCH_THRESHOLD_DP * density;

        singleTapRunnable = listener::onSingleTap;

        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                handler.removeCallbacks(singleTapRunnable);
                handler.postDelayed(singleTapRunnable, DOUBLE_TAP_TIMEOUT_MS);
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                handler.removeCallbacks(singleTapRunnable);
                listener.onDoubleTap();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                lpActive = true;
                lpSwiped = false;
                listener.onLongPressStart();
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
                try {
                    if (lpActive) {
                        lpSwiped = true;
                    } else {
                        // Only handle brightness here; swipe handled in direct ACTION_MOVE path
                        handleBrightnessScroll(distanceX, distanceY);
                    }
                } catch (Exception ignored) {}
                return true;
            }
        });
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /** Update orientation flag. Call from onConfigurationChanged and on setup. */
    public void setLandscape(boolean landscape) {
        this.isLandscape = landscape;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    /**
     * Set the swipe direction used for media switching in portrait mode.
     * Default: VERTICAL (right-side up/down swipe).
     */
    public void setPortraitSwipeDirection(SwipeDirection dir) {
        this.portraitSwipe = dir;
    }

    /**
     * Set the swipe direction used for media switching in landscape mode.
     * Default: HORIZONTAL (left/right swipe).
     */
    public void setLandscapeSwipeDirection(SwipeDirection dir) {
        this.landscapeSwipe = dir;
    }

    /**
     * Called from PlayerActivity.dispatchTouchEvent to detect swipes at the Activity level.
     * This ensures swipes work even when the controls overlay intercepts touch events
     * before they reach playerView.
     *
     * Uses the shared {@code swipeConsumed} flag to prevent double-firing if the swipe
     * is also detected via the view-level path.
     */
    public void feedSwipeEvent(MotionEvent ev, float windowWidth) {
        if (ev == null || listener == null) return;
        try {
            activityWindowWidth = windowWidth > 0 ? windowWidth : activityWindowWidth;
            int action = ev.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN) {
                activityStartX = ev.getX();
                activityStartY = ev.getY();
                // Reset shared swipe guard on new touch (syncs with view-level path)
                swipeConsumed = false;
            } else if (action == MotionEvent.ACTION_MOVE && !swipeConsumed && !lpActive) {
                detectSwipe(ev.getX(), ev.getY(), activityStartX, activityStartY, activityWindowWidth);
            } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                // Ensure lpActive is cleared even if the view-level path missed the UP event
                if (lpActive) {
                    lpActive = false;
                    if (!lpSwiped) listener.onLongPressEnd();
                    lpSwiped = false;
                }
            }
        } catch (Exception ignored) {}
    }

    // ── View.OnTouchListener ───────────────────────────────────────────────

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event == null) return false;
        try {
            if (v != null && v.getWidth() > 0) {
                viewWidth = v.getWidth();
            }

            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN) {
                swipeConsumed = false;
                touchStartX = event.getX();
                touchStartY = event.getY();
            }

            // Feed to GestureDetector for tap/double-tap/long-press/brightness
            detector.onTouchEvent(event);

            // Direct swipe detection using stored start coords (more reliable than e1)
            if (action == MotionEvent.ACTION_MOVE && !lpActive && !swipeConsumed) {
                float width = viewWidth > 1 ? viewWidth : activityWindowWidth;
                detectSwipe(event.getX(), event.getY(), touchStartX, touchStartY, width);
            }

            if (lpActive && (action == MotionEvent.ACTION_UP
                    || action == MotionEvent.ACTION_CANCEL)) {
                lpActive = false;
                if (!lpSwiped) {
                    listener.onLongPressEnd();
                }
                lpSwiped = false;
            }
        } catch (Exception ignored) {}
        return true;
    }

    // ── Internal gesture helpers ───────────────────────────────────────────

    /**
     * Detects and fires a media-switch swipe event.
     * Uses the shared {@code swipeConsumed} flag so it fires at most once per gesture
     * regardless of which path (view-level or activity-level) calls it first.
     */
    private void detectSwipe(float currentX, float currentY,
                              float startX, float startY, float width) {
        if (listener == null || swipeConsumed) return;

        float totalDx = currentX - startX;
        float totalDy = currentY - startY;
        float absDX   = Math.abs(totalDx);
        float absDY   = Math.abs(totalDy);
        boolean isLeft = startX < width / 2f;

        // Left-side vertical movement → brightness (skip swipe check)
        if (absDY > absDX && isLeft) return;

        SwipeDirection dir = isLandscape ? landscapeSwipe : portraitSwipe;

        if (dir == SwipeDirection.HORIZONTAL) {
            if (absDX > absDY && absDX > switchThresholdPx) {
                swipeConsumed = true;
                listener.onSwipeMedia(totalDx < 0); // left = next, right = previous
            }
        } else {
            // VERTICAL — right side only (left is reserved for brightness)
            if (!isLeft && absDY > absDX && absDY > switchThresholdPx) {
                swipeConsumed = true;
                listener.onSwipeMedia(totalDy > 0); // down = next, up = previous
            }
        }
    }

    /**
     * Handles left-side vertical scroll for brightness adjustment.
     * Called from GestureDetector.onScroll where distanceX/Y are per-event deltas.
     */
    private void handleBrightnessScroll(float distanceX, float distanceY) {
        if (listener == null) return;
        float absDX  = Math.abs(distanceX);
        float absDY  = Math.abs(distanceY);
        boolean isLeft = touchStartX < viewWidth / 2f;

        if (absDY > absDX && isLeft) {
            listener.onBrightnessScroll(distanceY * BRIGHTNESS_SENSITIVITY);
        }
    }
}
