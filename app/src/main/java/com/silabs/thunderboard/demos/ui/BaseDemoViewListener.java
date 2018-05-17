package com.silabs.thunderboard.demos.ui;

public interface BaseDemoViewListener {
    void onWifi(boolean isConnected);

    void setStreamingEnabled(boolean enabled, boolean reconnecting);
}
