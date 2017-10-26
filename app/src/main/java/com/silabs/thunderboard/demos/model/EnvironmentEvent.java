package com.silabs.thunderboard.demos.model;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

import java.util.UUID;

public class EnvironmentEvent {

    public final ThunderBoardDevice device;
    public final UUID characteristicUuid;

    public EnvironmentEvent(ThunderBoardDevice device, UUID characteristicUuid) {
        this.device = device;
        this.characteristicUuid = characteristicUuid;
    }
}
