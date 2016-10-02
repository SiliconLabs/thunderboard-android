package com.silabs.thunderboard.demos.model;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

import java.util.UUID;

public class MotionEvent {
    public static final int ACTION_CALIBRATE = 1;
    public static final int ACTION_CLEAR_ORIENTATION = 2;
    public static final int ACTION_CLEAR_MEASUREMENT = 3;
    public static final int ACTION_CLEAR_ROTATION = 4;
    public static final int ACTION_CSC_CHANGED = 5;

    public final ThunderBoardDevice device;
    public final UUID characteristicUuid;
    public final Integer action;

    public MotionEvent(ThunderBoardDevice device, UUID characteristicUuid) {
        this.device = device;
        this.characteristicUuid = characteristicUuid;
        this.action = null;
    }

    public MotionEvent(ThunderBoardDevice device, UUID characteristicUuid, int action) {
        this.device = device;
        this.characteristicUuid = characteristicUuid;
        this.action = action;
    }
}
