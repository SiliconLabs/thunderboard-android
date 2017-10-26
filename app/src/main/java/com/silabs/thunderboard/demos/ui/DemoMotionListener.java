package com.silabs.thunderboard.demos.ui;

import com.silabs.thunderboard.demos.model.LedRGBState;

public interface DemoMotionListener extends BaseDemoViewListener {
    void setOrientation(float x, float y, float z);
    void setAcceleration(float x, float y, float z);
    void setSpeed(double speed, int rpm, int measurementsType);
    void setDistance(double distance, int revolutions, int measurementsType);
    void onCalibrateComleted();
    void setColorLED(LedRGBState colorLED);
}
