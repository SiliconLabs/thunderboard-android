package com.silabs.thunderboard.common.data.model;

import java.util.Locale;
import java.util.Map;

public class ThunderBoardPreferences {
    public static final int UNIT_METRIC = 0;
    public static final int UNIT_US = 1;

    public static final int TEMP_CELSIUS = 0;
    public static final int TEMP_FAHRENHEIT = 1;

    public static final int MODEL_TYPE_BOARD = 0;
    public static final int MODEL_TYPE_CAR = 1;

    public static final float DEFAULT_WHEEL_RADIUS = 0.01505f; //meters

    public String userName;
    public String userTitle;
    public String userEmail;
    public String userPhone;
    public boolean userCCSelf;

    public int measureUnitType;
    public int temperatureType;
    public int modelType;

    public boolean beaconNotifications;

    public float wheelRadius;

    public Map<String, Beacon> beacons;

    public ThunderBoardPreferences(Locale locale) {
        if (Locale.US.equals(locale)) {
            measureUnitType = UNIT_US;
            temperatureType = TEMP_FAHRENHEIT;
            modelType = MODEL_TYPE_BOARD;
            wheelRadius = DEFAULT_WHEEL_RADIUS;
        }
    }

    @Override
    public String toString() {
        return String.format("userName: %s, userTitle: %s, userEmail: %s, userPhone: %s, userCCSelf: %s, measureUnitType: %s, temperatureType: %s, modelType: %s, beaconNotifications: %s, beacons: %d",
                userName, userTitle, userEmail, userPhone,
                userCCSelf ? "true" : "false",
                measureUnitType == UNIT_METRIC ? "metric" : "US",
                temperatureType == TEMP_CELSIUS ? "celsius" : "fahrenheit",
                modelType == MODEL_TYPE_BOARD ? "board" : "car",
                beaconNotifications ? "true" : "false",
                beacons == null ? -1 : beacons.size());
    }


    public static final class Beacon {
        public String deviceAddress;
        public String deviceName;
        public boolean allowNotifications;

        public Beacon(String deviceAddress, String deviceName, boolean allowNotifications) {
            this.deviceAddress = deviceAddress;
            this.deviceName = deviceName;
            this.allowNotifications = allowNotifications;
        }
    }

}
