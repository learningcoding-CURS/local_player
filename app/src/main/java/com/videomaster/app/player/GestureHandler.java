package com.videomaster.app.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Handles touch gestures on the player surface.
 *
 * Architecture:
 *   Swipe detection is done EXCLUSIVELY at the Activity level via feedSwipeEvent().
 *   This guarantees swipes work regardless of which child view intercepts touch
 *   events (buttons, seekbar, controls overlay, etc.).
 *
 *   The view-level onTouch() path only handles: tap, double-tap, long-press,
 *   and brightness adjustment (via GestureDetector).
 *
 * Swipe direction for media switching is configurable per orientation:
 *   setPortraitSwipeDirection(SwipeDirection)   – default VERTICAL
 *   setLandscapeSwipeDirection(SwipeDirection)  – default HORIZONTAL
 *
 * Portrait VERTICAL mode (default):
 *   Any position DOWN swipe        → next media
 *   Any position UP   swipe        → previous media
 *
 * Portrait HORIZONTAL mode:
 *   LEFT  swipe                    → next media
 *   RIGHT swipe                    → previous media
 *   Left side vertical scroll      → adjust brightness
 *
 * Landscape HORIZONTAL mode (default):
 *   LEFT  swipe                    → next media
 *   RIGHT swipe                    → previous media
 *   Left side vertical scroll      → adjust brightness
 *
 * Landscape VERTICAL mode:
 *   Any position DOWN swipe        → next media
 *   Any position UP   swipe        → previous media
 *
 * Long press (no movement) always activates 2.5× speed boost regardless of orientation.
 */
public class GestureHandler implements View.OnTouchListener {

    private static final String TAG = "GestureHandler";

    // ── Swipe direction enum ────────────────────────────────────────────────

    public enum SwipeDirection {
        VERTICAL,
        HORIZONTAL
    }

    // ── Callback interface ──────────────────────────────────────────────────

    public interface GestureListener {
        void onSingleTap();
        void onDoubleTap();
        void onLongPressStart();
        void onLongPressEnd();
        void onBrightnessScroll(float delta);
        void onSwipeMedia(boolean toNext);
    }

    // ── Tuning constants ───────────────────────────────────────────────────

    private static final long  DOUBLE_TAP_TIMEOUT_MS  = 300;
    private static final float SWITCH_THRESHOLD_DP    = 50f;
    private static final float BRIGHTNESS_SENSITIVITY = 0.005f;

    // ── State ──────────────────────────────────────────────────────────────

    private final GestureDetector detector;
    private final GestureListener listener;
    private final Handler         handler = new Handler(Looper.getMainLooper());
    private final float           switchThresholdPx;
    private final Runnable        singleTapRunnable;

    private boolean isLandscape     = false;
    private float   viewWidth       = 1f;

    private SwipeDirection portraitSwipe  = SwipeDirection.VERTICAL;
    private SwipeDirection landscapeSwipe = SwipeDirection.HORIZONTAL;

    // Long-press state
    private boolean lpActive  = false;
    private boolean lpSwiped  = false;

    // View-level touch start position (used for brightness left/right detection)
    private float touchStartX = 0f;

    // ── Activity-level swipe state (EXCLUSIVE swipe detection path) ──────
    // These fields are ONLY used by feedSwipeEvent(). They are completely
    // independent of the view-level onTouch() path.
    private float   actStartX      = 0f;
    private float   actStartY      = 0f;
    private float   actWindowWidth = 1f;
    private boolean actSwipeFired  = false;

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
                        handleBrightnessScroll(distanceX, distanceY);
                    }
                } catch (Exception e) {
                    Log.w(TAG, "onScroll error", e);
                }
                return true;
            }
        });
    }

    // ── Public API ─────────────────────────────────────────────────────────

    public void setLandscape(boolean landscape) {
        this.isLandscape = landscape;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public void setPortraitSwipeDirection(SwipeDirection dir) {
        this.portraitSwipe = dir;
    }

    public void setLandscapeSwipeDirection(SwipeDirection dir) {
        this.landscapeSwipe = dir;
    }

    /**
     * Called from PlayerActivity.dispatchTouchEvent to detect swipes at the Activity level.
     * This is the ONLY path that detects media-switch swipes.
     *
     * It runs BEFORE super.dispatchTouchEvent(), so it processes every touch event
     * regardless of which child view eventually consumes it.
     *
     * Does NOT use try-catch — exceptions propagate to the caller so errors are visible.
     */
    public void feedSwipeEvent(MotionEvent ev, float windowWidth) {
        if (ev == null || listener == null) return;

        if (windowWidth > 0) actWindowWidth = windowWidth;

        int action = ev.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            actStartX     = ev.getX();
            actStartY     = ev.getY();
            actSwipeFired = false;

        } else if (action == MotionEvent.ACTION_MOVE) {
            if (!actSwipeFired) {
                checkAndFireSwipe(ev.getX(), ev.getY());
            }

        } else if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            if (lpActive) {
                lpActive = false;
                if (!lpSwiped) listener.onLongPressEnd();
                lpSwiped = false;
            }
        }
    }

    // ── View.OnTouchListener ───────────────────────────────────────────────

    /**
     * Handles taps, double-taps, long-press, and brightness only.
     * Swipe detection is NOT done here — it is handled exclusively by feedSwipeEvent().
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event == null) return false;
        try {
            if (v != null && v.getWidth() > 0) {
                viewWidth = v.getWidth();
            }

            int action = event.getActionMasked();

            if (action == MotionEvent.ACTION_DOWN) {
                touchStartX = event.getX();
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
        } catch (Exception e) {
            Log.w(TAG, "onTouch error", e);
        }
        return true;
    }

    // ── Internal helpers ───────────────────────────────────────────────────

    /**
     * Core swipe detection logic. Called ONLY from feedSwipeEvent.
     * Uses window-relative coordinates from Activity.dispatchTouchEvent.
     */
    private void checkAndFireSwipe(float currentX, float currentY) {
        float dx    = currentX - actStartX;
        float dy    = currentY - actStartY;
        float absDX = Math.abs(dx);
        float absDY = Math.abs(dy);

        SwipeDirection dir = isLandscape ? landscapeSwipe : portraitSwipe;

        boolean triggered = false;
        boolean toNext    = false;

        if (dir == SwipeDirection.HORIZONTAL) {
            boolean isLeftSide = actStartX < actWindowWidth / 2f;
            // Left-side vertical scroll is reserved for brightness
            if (absDY > absDX && isLeftSide) return;
            if (absDX > absDY && absDX > switchThresholdPx) {
                triggered = true;
                toNext    = dx < 0;  // swipe left → next
            }
        } else {
            // VERTICAL mode: up/down swipe anywhere on screen
            if (absDY > absDX && absDY > switchThresholdPx) {
                triggered = true;
                toNext    = dy > 0;  // swipe down → next
            }
        }

        if (triggered) {
            actSwipeFired = true;
            // Swipe overrides long-press speed boost
            if (lpActive) {
                lpActive = false;
                lpSwiped = true;
            }
            Log.d(TAG, "Swipe detected: toNext=" + toNext + " dir=" + dir
                    + " dx=" + dx + " dy=" + dy);
            listener.onSwipeMedia(toNext);
        }
    }

    /**
     * Handles left-side vertical scroll for brightness adjustment.
     * Called from GestureDetector.onScroll (view-level path only).
     * Suppressed once a media-switch swipe has been committed.
     */
    private void handleBrightnessScroll(float distanceX, float distanceY) {
        if (listener == null || actSwipeFired) return;
        float absDX  = Math.abs(distanceX);
        float absDY  = Math.abs(distanceY);
        boolean isLeft = touchStartX < viewWidth / 2f;

        if (absDY > absDX && isLeft) {
            listener.onBrightnessScroll(distanceY * BRIGHTNESS_SENSITIVITY);
        }
    }
}
