package com.silabs.thunderboard.demos.model;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

public class StatusEvent {

    public final ThunderBoardDevice device;

    public StatusEvent(ThunderBoardDevice device) {
        this.device = device;
    }
}
