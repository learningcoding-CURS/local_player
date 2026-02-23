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
 * Swipe direction for media switching is configurable per orientation:
 *   setPortraitSwipeDirection(SwipeDirection)   – default VERTICAL
 *   setLandscapeSwipeDirection(SwipeDirection)  – default HORIZONTAL
 *
 * Portrait VERTICAL mode:
 *   Left side vertical scroll   → adjust brightness (up = brighter)
 *   Right side DOWN swipe       → next media  (no long press required)
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
         * Fired when a media-switch swipe is detected (no long press required).
         * @param toNext true = go to next; false = go to previous.
         */
        void onSwipeMedia(boolean toNext);
    }

    // ── Tuning constants ───────────────────────────────────────────────────

    private static final long  DOUBLE_TAP_TIMEOUT_MS  = 300;
    /** Minimum finger travel (dp) to register a media-switch swipe. */
    private static final float SWITCH_THRESHOLD_DP    = 72f;
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

    // Long-press state (speed boost only — media switch no longer needs long press)
    private boolean lpActive  = false;
    private boolean lpSwiped  = false;

    // One-shot swipe guard — reset on each ACTION_DOWN
    private boolean swipeConsumed = false;

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
                if (lpActive) {
                    // Long press only activates speed boost; ignore scroll during long press
                    lpSwiped = true; // suppress onLongPressEnd
                } else {
                    handleNormalScroll(e1, e2, distanceX, distanceY);
                }
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

    // ── View.OnTouchListener ───────────────────────────────────────────────

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        viewWidth = v.getWidth() > 0 ? v.getWidth() : 1f;

        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            swipeConsumed = false;
        }

        detector.onTouchEvent(event);

        if (lpActive && (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL)) {
            lpActive = false;
            if (!lpSwiped) {
                listener.onLongPressEnd();
            }
            lpSwiped = false;
        }
        return true;
    }

    // ── Internal gesture helpers ───────────────────────────────────────────

    /**
     * Normal (non-long-press) scroll handler.
     *
     * Priority:
     *   1. Left-side vertical scroll → brightness (always enabled, both orientations)
     *   2. Media switch swipe according to configured direction (one-shot per gesture)
     */
    private void handleNormalScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
        if (e1 == null) return;

        float absDX    = Math.abs(distanceX);
        float absDY    = Math.abs(distanceY);
        boolean isLeft = e1.getX() < viewWidth / 2f;

        // ── Priority 1: brightness (left-side vertical, any orientation) ──
        if (absDY > absDX && isLeft) {
            // distanceY > 0 when finger moves UP → increase brightness
            listener.onBrightnessScroll(distanceY * BRIGHTNESS_SENSITIVITY);
            return;
        }

        // ── Priority 2: media switch (one-shot per gesture) ──────────────
        if (swipeConsumed) return;

        SwipeDirection dir = isLandscape ? landscapeSwipe : portraitSwipe;

        if (dir == SwipeDirection.HORIZONTAL) {
            float totalDx = e2.getX() - e1.getX();
            if (absDX > absDY * 1.2f && Math.abs(totalDx) > switchThresholdPx) {
                swipeConsumed = true;
                // swipe left → next, swipe right → previous
                listener.onSwipeMedia(totalDx < 0);
            }
        } else {
            // VERTICAL — restrict to right side to avoid brightness conflict
            if (!isLeft) {
                float totalDy = e2.getY() - e1.getY();
                if (absDY > absDX * 1.2f && Math.abs(totalDy) > switchThresholdPx) {
                    swipeConsumed = true;
                    // swipe down → next, swipe up → previous
                    listener.onSwipeMedia(totalDy > 0);
                }
            }
        }
    }
}
