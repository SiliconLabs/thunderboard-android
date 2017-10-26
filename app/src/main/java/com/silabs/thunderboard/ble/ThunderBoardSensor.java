package com.silabs.thunderboard.ble;

public abstract class ThunderBoardSensor {
    public Boolean isNotificationEnabled;
    public Boolean isSensorDataChanged = false;

    public abstract ThunderboardSensorData getSensorData();
}
