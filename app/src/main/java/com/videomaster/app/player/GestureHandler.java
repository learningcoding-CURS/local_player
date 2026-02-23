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
 * Portrait mode:
 *   Single tap                      → show/hide controls
 *   Double tap                      → toggle play/pause
 *   Long press (hold, no move)      → activate 2.5× speed
 *   Long press + swipe UP           → switch to PREVIOUS media
 *   Long press + swipe DOWN         → switch to NEXT media
 *   Left-side vertical scroll       → adjust brightness (up = brighter, down = dimmer)
 *
 * Landscape mode:
 *   Single tap                      → show/hide controls
 *   Double tap                      → toggle play/pause
 *   Long press (hold, no move)      → activate 2.5× speed
 *   Swipe LEFT                      → switch to NEXT media
 *   Swipe RIGHT                     → switch to PREVIOUS media
 *   Left-side vertical scroll       → adjust brightness (up = brighter, down = dimmer)
 *
 * Note: Right-side volume gesture has been removed. Use system volume buttons instead.
 *
 * Extensibility: call setLandscape(boolean) when orientation changes.
 */
public class GestureHandler implements View.OnTouchListener {

    // ── Callback interface ──────────────────────────────────────────────────

    public interface GestureListener {
        void onSingleTap();
        void onDoubleTap();
        void onLongPressStart();

        /** Fired on release when no swipe occurred during long press. */
        void onLongPressEnd();

        /**
         * Fired during left-side vertical scroll.
         * @param delta positive = brighter; negative = dimmer. Range roughly –1…+1 per event.
         */
        void onBrightnessScroll(float delta);

        /**
         * Fired when a media-switch gesture is completed.
         * @param toNext true = go to next item; false = go to previous item.
         */
        void onSwipeMedia(boolean toNext);
    }

    // ── Tuning constants ───────────────────────────────────────────────────

    private static final long  DOUBLE_TAP_TIMEOUT_MS  = 300;
    /** Minimum finger travel (dp) to register a media-switch swipe. */
    private static final float SWITCH_THRESHOLD_DP    = 72f;
    /**
     * Brightness sensitivity: fraction of [0,1] changed per pixel scrolled.
     * Higher = more responsive. Default gives full range in ~200dp.
     */
    private static final float BRIGHTNESS_SENSITIVITY = 0.005f;

    // ── State ──────────────────────────────────────────────────────────────

    private final GestureDetector detector;
    private final GestureListener listener;
    private final Handler         handler = new Handler(Looper.getMainLooper());

    private boolean isLandscape        = false;
    private float   viewWidth          = 1f;

    // Long-press state
    private boolean lpActive           = false;
    private float   lpStartX           = 0f;
    private float   lpStartY           = 0f;
    private boolean lpSwiped           = false;

    // Landscape one-shot swipe guard (reset on ACTION_DOWN)
    private boolean lsSwipeConsumed    = false;

    private final float switchThresholdPx;

    private final Runnable singleTapRunnable;

    // ── Constructor ────────────────────────────────────────────────────────

    public GestureHandler(Context context, GestureListener listener) {
        this.listener = listener;

        float density    = context.getResources().getDisplayMetrics().density;
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
                lpActive  = true;
                lpSwiped  = false;
                lpStartX  = e.getX();
                lpStartY  = e.getY();
                listener.onLongPressStart();
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
                if (lpActive) {
                    handleLongPressScroll(e2);
                } else {
                    handleNormalScroll(e1, e2, distanceX, distanceY);
                }
                return true;
            }
        });
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Update the orientation flag so gesture logic can adapt.
     * Call this from Activity.onConfigurationChanged() and during initial setup.
     */
    public void setLandscape(boolean landscape) {
        this.isLandscape = landscape;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    // ── View.OnTouchListener ───────────────────────────────────────────────

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        viewWidth = v.getWidth() > 0 ? v.getWidth() : 1f;

        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            lsSwipeConsumed = false;
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
     * Portrait: long press + vertical displacement switches media.
     * Landscape: long press only activates speed boost (no switch).
     */
    private void handleLongPressScroll(MotionEvent e2) {
        if (isLandscape) {
            // In landscape long-press only triggers speed boost; switching is done by simple swipe.
            return;
        }
        float dy = e2.getY() - lpStartY;
        if (!lpSwiped && Math.abs(dy) > switchThresholdPx) {
            lpSwiped = true;
            // Finger moves down → next; finger moves up → previous
            listener.onSwipeMedia(dy > 0);
        }
    }

    /**
     * Normal (non-long-press) scroll:
     *  – Landscape: horizontal swipe switches media (one-shot per gesture).
     *  – Both modes: left-side vertical scroll adjusts brightness.
     */
    private void handleNormalScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
        if (e1 == null) return;

        float absDX = Math.abs(distanceX);
        float absDY = Math.abs(distanceY);

        if (isLandscape && !lsSwipeConsumed) {
            float totalDx = e2.getX() - e1.getX();
            // Require clearly horizontal and above threshold
            if (absDX > absDY * 1.2f && Math.abs(totalDx) > switchThresholdPx) {
                lsSwipeConsumed = true;
                // Swipe left → next; swipe right → previous
                listener.onSwipeMedia(totalDx < 0);
                return; // don't also fire brightness
            }
        }

        // Left-side vertical scroll → brightness (both orientations).
        // distanceY > 0 when finger moves up → increase brightness.
        if (absDY > absDX) {
            boolean isLeft = e1.getX() < viewWidth / 2f;
            if (isLeft) {
                float delta = distanceY * BRIGHTNESS_SENSITIVITY;
                listener.onBrightnessScroll(delta);
            }
            // Right side intentionally unhandled — volume removed.
        }
    }
}
