package com.silabs.thunderboard.ble;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.silabs.thunderboard.demos.model.LedRGBState;

public class ThunderBoardSensorIo extends ThunderBoardSensor {

    private static final byte IO_0_ON = 0x01;
    private static final byte IO_1_ON = 0x04;

    public SensorData sensorData = new SensorData((byte)0);

    public void setSwitch(byte b) {
        sensorData.sw0 = (b & IO_0_ON) != 0 ? 1 : 0;
        sensorData.sw1 = (b & IO_1_ON) != 0 ? 1 : 0;
        isSensorDataChanged = true;
    }

    public void setLed(byte b) {
        sensorData.ledb = (b & IO_0_ON) != 0 ? 1 : 0;
        sensorData.ledg = (b & IO_1_ON) != 0 ? 1 : 0;
        isSensorDataChanged = true;
    }

    public void setColorLed(LedRGBState ledstate) {
        sensorData.colorLed = ledstate;
        isSensorDataChanged = true;
    }

    public LedRGBState getColorLed() {
        return sensorData.colorLed;
    }

    @Override
    public SensorData getSensorData() {
        return sensorData;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SensorData implements ThunderboardSensorData {
        public int ledb;
        public int ledg;
        public int sw0;
        public int sw1;
        public LedRGBState colorLed;

        SensorData() {}

        public SensorData(byte b) {
            ledb = (b & IO_0_ON) != 0 ? 1 : 0;
            ledg = (b & IO_1_ON) != 0 ? 1 : 0;
            sw0 = (b & IO_0_ON) != 0 ? 1 : 0;
            sw1 = (b & IO_1_ON) != 0 ? 1 : 0;
        }

        @Override
        public String toString() {
            return String.format("%d %d %d %d", ledb, ledg, sw0, sw1);
        }

        @Override
        public ThunderboardSensorData clone() {
            SensorData d = new SensorData();

            d.ledb = ledb;
            d.ledg = ledg;
            d.sw0 = sw0;
            d.sw1 = sw1;
            d.colorLed = colorLed;

            return d;
        }
    }
}
