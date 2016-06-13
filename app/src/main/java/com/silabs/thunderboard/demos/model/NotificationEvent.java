package com.silabs.thunderboard.demos.model;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

public class NotificationEvent {

    public final static int ACTION_NOTIFICATIONS_CLEAR = 1;
    public final static int ACTION_NOTIFICATIONS_SET = 2;

    public final ThunderBoardDevice device;
    public final int action;

    public NotificationEvent(ThunderBoardDevice device, int action) {
        this.device = device;
        this.action = action;
    }
}
