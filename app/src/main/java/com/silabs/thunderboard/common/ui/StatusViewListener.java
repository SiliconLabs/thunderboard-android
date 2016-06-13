package com.silabs.thunderboard.common.ui;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

public interface StatusViewListener {
    void onData(ThunderBoardDevice device);
}
