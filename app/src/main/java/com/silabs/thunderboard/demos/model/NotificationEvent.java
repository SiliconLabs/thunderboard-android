package com.silabs.thunderboard.demos.model;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

import java.util.UUID;

public class NotificationEvent {

    public final static int ACTION_NOTIFICATIONS_CLEAR = 1;
    public final static int ACTION_NOTIFICATIONS_SET = 2;

    public final ThunderBoardDevice device;
    public final UUID characteristicUuid;
    public final int action;

    public NotificationEvent(ThunderBoardDevice device, UUID characteristicUuid, int action) {
        this.device = device;
        this.action = action;
        this.characteristicUuid = characteristicUuid;
    }
}
