package com.silabs.thunderboard.demos.ui;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

public interface DemosViewListener {
    void onData(ThunderBoardDevice device);
}
