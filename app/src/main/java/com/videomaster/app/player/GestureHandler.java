package com.videomaster.app.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Handles touch gestures on the player surface:
 *  - Single tap         → show/hide controls overlay
 *  - Double tap         → toggle play/pause
 *  - Long press         → activate 2.5× speed (released on ACTION_UP without swipe)
 *  - Vertical scroll    → left half adjusts brightness; right half adjusts volume
 *  - Long press + swipe → swipe left = previous media; swipe right = next media
 */
public class GestureHandler implements View.OnTouchListener {

    public interface GestureListener {
        void onSingleTap();
        void onDoubleTap();
        void onLongPressStart();
        /** Called on release only when no horizontal swipe occurred during long press. */
        void onLongPressEnd();
        /**
         * Called during vertical scroll.
         * @param isLeftSide true = left half of screen (brightness), false = right half (volume)
         * @param delta      normalized change per event (-1.0 max decrease → +1.0 max increase)
         */
        void onVerticalScroll(boolean isLeftSide, float delta);
        /**
         * Called when a horizontal swipe is detected during an active long press.
         * @param toNext true = swipe to next item; false = swipe to previous item
         */
        void onSwipeMedia(boolean toNext);
    }

    private static final long  DOUBLE_TAP_TIMEOUT      = 300;
    private static final float SWIPE_THRESHOLD_DP      = 120f;
    private static final float SCROLL_SENSITIVITY      = 0.003f;

    private final GestureDetector detector;
    private final GestureListener listener;
    private final Handler         handler  = new Handler(Looper.getMainLooper());
    private       boolean         longPressActive = false;
    private       float           longPressStartX = 0f;
    private       boolean         swipeConsumed   = false;
    private       float           viewWidth       = 1f;
    private       float           scaledSwipeThreshold;

    private final Runnable singleTapRunnable;

    public GestureHandler(Context context, GestureListener listener) {
        this.listener = listener;

        float density = context.getResources().getDisplayMetrics().density;
        scaledSwipeThreshold = SWIPE_THRESHOLD_DP * density;

        singleTapRunnable = listener::onSingleTap;

        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                handler.removeCallbacks(singleTapRunnable);
                handler.postDelayed(singleTapRunnable, DOUBLE_TAP_TIMEOUT);
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
                longPressActive  = true;
                swipeConsumed    = false;
                longPressStartX  = e.getX();
                listener.onLongPressStart();
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
                if (longPressActive) {
                    // During long press, track horizontal displacement for media switching
                    float dx = e2.getX() - longPressStartX;
                    if (!swipeConsumed && Math.abs(dx) > scaledSwipeThreshold) {
                        swipeConsumed = true;
                        listener.onSwipeMedia(dx < 0); // swipe left → next
                    }
                } else {
                    // Normal vertical scroll → brightness / volume
                    float absDY = Math.abs(distanceY);
                    float absDX = Math.abs(distanceX);
                    // Only fire when scroll is predominantly vertical
                    if (absDY > absDX && e1 != null) {
                        boolean isLeft = e1.getX() < viewWidth / 2f;
                        float delta = -distanceY * SCROLL_SENSITIVITY; // negative because finger up = +
                        listener.onVerticalScroll(isLeft, delta);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        viewWidth = v.getWidth() > 0 ? v.getWidth() : 1f;
        detector.onTouchEvent(event);

        if (longPressActive &&
                (event.getAction() == MotionEvent.ACTION_UP ||
                 event.getAction() == MotionEvent.ACTION_CANCEL)) {
            longPressActive = false;
            if (!swipeConsumed) {
                listener.onLongPressEnd();
            }
            swipeConsumed = false;
        }
        return true;
    }
}
