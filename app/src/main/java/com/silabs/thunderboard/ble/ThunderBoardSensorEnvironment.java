package com.silabs.thunderboard.ble;

public class ThunderBoardSensorEnvironment extends ThunderBoardSensor {

    public final int TEMPERATURE_TYPE;
    public final int MAX_AMBIENT_LIGHT = 99999;

    private int readStatus;

    private SensorData sensorData = new SensorData();

    public ThunderBoardSensorEnvironment(int temperatureType) {
        this.TEMPERATURE_TYPE = temperatureType;
    }

    public int getReadStatus() {
        return readStatus;
    }

    public void setTemperature(int temperature) {
        float t = (temperature/100f);
        sensorData.setTemperature(t);
        // clear the other reads
        readStatus = 0x03;
        isSensorDataChanged = true;
    }

    public void setHumidity(int humidity) {
        sensorData.setHumidity((humidity / 100));
        readStatus |= 0x0c;
        isSensorDataChanged = true;
    }

    public void setUvIndex(int uvIndex) {
        sensorData.setUvIndex(uvIndex);
        readStatus |= 0x30;
        isSensorDataChanged = true;
    }

    public void setAmbientLight(long ambientLight) {
        ambientLight /= 100;
        sensorData.setAmbientLight(ambientLight > MAX_AMBIENT_LIGHT ? MAX_AMBIENT_LIGHT : ambientLight);
        readStatus |= 0xc0;
        isSensorDataChanged = true;
    }

    @Override
    public SensorData getSensorData() {
        return sensorData;
    }

    public static class SensorData {

        public float getTemperature() {
            return temperature;
        }

        public int getHumidity() {
            return humidity;
        }

        public int getUvIndex() {
            return uvIndex;
        }

        public long getAmbientLight() {
            return ambientLight;
        }

        private void setTemperature(float temperature) {
            this.temperature = temperature;
        }

        private void setHumidity(int humidity) {
            this.humidity = humidity;
        }

        public void setUvIndex(int uvIndex) {
            this.uvIndex = uvIndex;
        }

        public void setAmbientLight(long ambientLight) {
            this.ambientLight = ambientLight;
        }

        @Override
        public String toString() {
            return String.format("temperature: %.2f, humidity: %d, uvIndex: %d, ambientLight: %d", temperature, humidity, uvIndex, ambientLight);
        }

        // Units in deg C with resolution of 0.01 deg C
        private float temperature;
        // Unit in % with resolution of 0.01%
        private int humidity;
        // Unitless
        private int uvIndex;
        // Lux
        private long ambientLight = Long.MIN_VALUE; // not initialized
    }
}
