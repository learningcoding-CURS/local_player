package com.videomaster.app.player;

import com.videomaster.app.interfaces.ISpeedProvider;

/** Default implementation of ISpeedProvider. */
public class DefaultSpeedProvider implements ISpeedProvider {

    private static final float[] SPEEDS = {0.5f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 2.5f, 3.0f};

    @Override
    public float[] getAvailableSpeeds() { return SPEEDS; }

    @Override
    public float getLongPressSpeed() { return 2.5f; }

    @Override
    public float getDefaultSpeed() { return 1.0f; }
}
