package com.videomaster.app.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Handles touch gestures on the player surface:
 *  - Single tap   → show/hide controls overlay
 *  - Double tap   → toggle play/pause
 *  - Long press   → activate 2.5× speed (released on ACTION_UP)
 */
public class GestureHandler implements View.OnTouchListener {

    public interface GestureListener {
        void onSingleTap();
        void onDoubleTap();
        void onLongPressStart();
        void onLongPressEnd();
    }

    private static final long DOUBLE_TAP_TIMEOUT = 300;

    private final GestureDetector detector;
    private final GestureListener listener;
    private final Handler         handler  = new Handler(Looper.getMainLooper());
    private boolean               longPressActive = false;

    private final Runnable singleTapRunnable;

    public GestureHandler(Context context, GestureListener listener) {
        this.listener = listener;

        singleTapRunnable = listener::onSingleTap;

        detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                // Delay to distinguish from double-tap
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
                longPressActive = true;
                listener.onLongPressStart();
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        detector.onTouchEvent(event);
        // Detect long-press release
        if (longPressActive &&
                (event.getAction() == MotionEvent.ACTION_UP ||
                 event.getAction() == MotionEvent.ACTION_CANCEL)) {
            longPressActive = false;
            listener.onLongPressEnd();
        }
        return true;
    }
}
