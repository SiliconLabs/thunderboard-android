package com.silabs.thunderboard.demos.model;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

public class MotionEvent {
    public static final int ACTION_CALIBRATE = 1;
    public static final int ACTION_CLEAR_ORIENTATION = 2;
    public static final int ACTION_CLEAR_MEASUREMENT = 3;

    public final ThunderBoardDevice device;
    public final Integer action;

    public MotionEvent(ThunderBoardDevice device) {
        this.device = device;
        this.action = null;
    }

    public MotionEvent(ThunderBoardDevice device, int action) {
        this.device = device;
        this.action = action;
    }
}
