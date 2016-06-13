package com.silabs.thunderboard.demos.ui;

public interface DemoIOViewListener extends BaseDemoViewListener {
    void setButton0State(int state);
    void setButton1State(int state);
    void setLed0State(int state);
    void setLed1State(int state);
}
