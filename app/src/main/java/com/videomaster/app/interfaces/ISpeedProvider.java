package com.videomaster.app.interfaces;

/**
 * Speed provider interface.
 * Implement this to customise available playback speeds.
 */
public interface ISpeedProvider {

    /**
     * Returns the list of available playback speeds.
     * Default: {0.5f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f}
     */
    float[] getAvailableSpeeds();

    /**
     * Returns the speed used when the user long-presses the screen.
     * Default: 2.5f
     */
    float getLongPressSpeed();

    /**
     * Returns the default playback speed.
     * Default: 1.0f
     */
    float getDefaultSpeed();
}
