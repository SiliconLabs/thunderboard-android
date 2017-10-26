package com.silabs.thunderboard.scanner.ui;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

import java.util.List;

public interface ScannerViewListener {
    void onData(List<ThunderBoardDevice> devices);
    void onBluetoothDisabled();
    void onBluetoothEnabled();
}
