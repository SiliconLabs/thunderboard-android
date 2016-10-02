package com.silabs.thunderboard.ble;

import android.os.SystemClock;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;

import timber.log.Timber;

public class ThunderBoardSensorMotion extends ThunderBoardSensor {

    // From settings
    public final int MEASUREMENTS_TYPE;
    public final float WHEEL_CIRCUMFERENCE; // meters

    private int characteristicsStatus;

    private SensorData sensorData = new SensorData();

    // CSC Feature
    private boolean wheelRebolutionDataSupported;
    // CSC Measurements
    private boolean wheelRevolutionDataPresent;
    private int cumulativeWheelRevolutions;
    private int lastWheelRevolutionTime; // seconds
    private int rotationsPerMinute;
    private long csrMeasurementEvent;

    public ThunderBoardSensorMotion(int measureUnitType, float wheelRadius) {
        MEASUREMENTS_TYPE = measureUnitType;
        WHEEL_CIRCUMFERENCE = 2 * wheelRadius * 3.14f; // meters
    }

    public int getCharacteristicsStatus() {
        return characteristicsStatus;
    }

    public void clearCharacteristicsStatus() {
        characteristicsStatus = 0;
    }

    public void setClearCharacteristicsStatus() {
        characteristicsStatus = 0x100;
    }

    public int getCumulativeWheelRevolutions() {
        return cumulativeWheelRevolutions;
    }

    public int getRotationsPerMinute() {
        return rotationsPerMinute;
    }

    @Override
    public SensorData getSensorData() {
        return sensorData;
    }

    public void setCscFeature(byte cscFeature) {
        this.wheelRebolutionDataSupported = (cscFeature & 0x01) == 0x01;
        characteristicsStatus |= 0x03;
        Timber.d("wheelRebolutionDataSupported: %s", wheelRebolutionDataSupported);
    }

    public void setCscMeasurementNotificationEnabled(boolean enabled) {
        characteristicsStatus |= 0x0c;
    }

    public void setAccelerationNotificationEnabled(boolean enabled) {
        characteristicsStatus |= 0x30;
    }

    public void setOrientationNotificationEnabled(boolean enabled) {
        characteristicsStatus |= 0xc0;
    }

    public void setCscMesurements(byte wheelRevolutionDataPresent, int cumulativeWheelRevolutions, int lastWheelRevolutionTime) {

        this.wheelRevolutionDataPresent = (wheelRevolutionDataPresent % 0x01) == 0x01;

        int revolutionsSinceLast = (cumulativeWheelRevolutions - this.cumulativeWheelRevolutions);
        int timeSinceLastRevolutionEvent = lastWheelRevolutionTime - this.lastWheelRevolutionTime; // 1/1024 of a second
        double distanceSinceLastRevolutionEvent = revolutionsSinceLast * WHEEL_CIRCUMFERENCE;


        // 1) Initial speed is reported as 0 until a notification with an updated wheel revolution count is received.
        // 2) Speed is reported based on the event times of the two last notifications that showed different wheel revolution counts.
        // 3) A timeout counter is needed. This is reset to 0 and started every time a notification arrives that has a new wheel revolution count.
        //    If the counter reaches T=5s the speed is reported as 0.

        if (revolutionsSinceLast > 0 && timeSinceLastRevolutionEvent > 0) {

            if (revolutionsSinceLast > 0) {

                this.csrMeasurementEvent = SystemClock.elapsedRealtime();

                sensorData.speed = distanceSinceLastRevolutionEvent / timeSinceLastRevolutionEvent;
                // convert to seconds
                sensorData.speed *= 1024;
                rotationsPerMinute = revolutionsSinceLast * 1024 * 60 / timeSinceLastRevolutionEvent;
            } else if (timeSinceLastRevolutionEvent > 5 * 1024) {
                sensorData.speed = 0;
                rotationsPerMinute = 0;
            } else {
                // do nothing for 5s.
            }
        } else if (SystemClock.elapsedRealtime() - csrMeasurementEvent < 5000) {
            // do nothing;
        } else {
            sensorData.speed = 0;
            rotationsPerMinute = 0;
        }

        this.cumulativeWheelRevolutions = cumulativeWheelRevolutions;
        this.lastWheelRevolutionTime = lastWheelRevolutionTime;
        double distance = cumulativeWheelRevolutions * WHEEL_CIRCUMFERENCE;
        sensorData.distance = distance;
        isSensorDataChanged = true;

        Timber.d("wheelRevolutionDataPresent: %s, cumulativeWheelRevolutions: %d, lastWheelRevolutionTime: %d, distance: %f, speed: %f, rpm: %d", wheelRevolutionDataPresent, cumulativeWheelRevolutions, lastWheelRevolutionTime, distance, sensorData.speed, rotationsPerMinute);
    }

    public void setOrientation(float orientationX, float orientationY, float orientationZ) {
        sensorData.ox = orientationX;
        sensorData.oy = orientationY;
        sensorData.oz = orientationZ;
        isSensorDataChanged = true;
    }

    public void setAcceleration(float accelerationX, float accelerationY, float accelerationZ) {
        sensorData.ax = accelerationX;
        sensorData.ay = accelerationY;
        sensorData.az = accelerationZ;
        isSensorDataChanged = true;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SensorData implements ThunderboardSensorData {
        @Override
        public String toString() {
            return String.format("%f %f %f %f %f %f", ax, ay, az, ox, oy, oz);
        }

        // Acceleration along X-axis in . Units in g with resolution of 0.001 g
        public float ax;
        // Acceleration along Y-axis in . Units in g with resolution of 0.001 g
        public float ay;
        // Acceleration along Z-axis in . Units in g with resolution of 0.001 g
        public float az;
        // Orientation alpha angle in deg (+180 to -180) with resolution of 0.01 deg
        public float ox;
        // Orientation beta angle in deg (+90 to -90) with resolution of 0.01 deg
        public float oy;
        // Orientation gamma angle in deg (+180 to -180) with resolution of 0.01 deg
        public float oz;

        // CSC
        public double speed;
        public double distance;

        @Override
        public ThunderboardSensorData clone() {
            SensorData d = new SensorData();

            d.ax = ax;
            d.ay = ay;
            d.az = az;
            d.ox = ox;
            d.oy = oy;
            d.oz = oz;
            d.distance = distance;
            d.speed = speed;

            return d;
        }
    }
}
