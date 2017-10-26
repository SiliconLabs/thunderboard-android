package com.silabs.thunderboard.ble;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.silabs.thunderboard.demos.model.HallState;

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

    public void setHallStateNotificationEnabled() {
        readStatus |= 0xC0000;
    }

    public void setTemperature(int temperature) {
        float t = (temperature / 100f);
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

    public void setSoundLevel(int soundLevel) {
        // units of 0.01dB
        float soundLevelF = (float) soundLevel / 100.0f;
        sensorData.setSound(soundLevelF);
        readStatus |= 0x0300;
        isSensorDataChanged = true;
    }

    public void setPressure(long pressure) {
        // pressure is in units of 0.1Pa, convert to millibars
        float pressuref = (float) pressure / 1000.0f;
        sensorData.setPressure(pressuref);
        readStatus |= 0x0c00;
        isSensorDataChanged = true;
    }

    public void setCO2Level(int co2Level) {
        // units of ppm
        sensorData.setCO2Level(co2Level);
        readStatus |= 0x3000;
        isSensorDataChanged = true;
    }

    public void setTVOCLevel(int tvocLevel) {
        // units of ppb
        sensorData.setTVOCLevel(tvocLevel);
        readStatus |= 0xc000;
        isSensorDataChanged = true;
    }

    public void setHallStrength(float hallStrength) {
        // units of uT (micro tesla)
        sensorData.setHallStrength(hallStrength);
        readStatus |= 0x30000;
        isSensorDataChanged = true;
    }

    public void setHallState(@HallState int hallState) {
        // unitless
        sensorData.setHallState(hallState);
        readStatus |= 0xC0000;
        isSensorDataChanged = true;
    }

    @Override
    public SensorData getSensorData() {
        return sensorData;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class SensorData implements ThunderboardSensorData {

        public float getTemperature() {
            return temperature;
        }

        public int getHumidity() {
            return humidity != null ? humidity : 0;
        }

        public int getUvIndex() {
            return uvIndex != null ? uvIndex : 0;
        }

        public long getAmbientLight() {
            return ambientLight != null ? ambientLight : 0;
        }

        public Float getSound() {
            return sound != null ? sound : 0;
        }

        public float getPressure() {
            return pressure != null ? pressure : 0;
        }

        public int getCO2Level() {
            return co2 != null ? co2 : 0;
        }

        public int getTVOCLevel() {
            return voc != null ? voc : 0;
        }

        public float getHallStrength() {
            return hallStrength != null ? hallStrength : 0;
        }

        @HallState
        public int getHallState() {
            return hallState != null ? hallState : HallState.OPENED;
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

        public void setSound(float sound) {
            this.sound = sound;
        }

        public void setPressure(float pressure) {
            this.pressure = pressure;
        }

        public void setCO2Level(int co2Level) {
            this.co2 = co2Level;
        }

        public void setTVOCLevel(int tvocLevel) {
            this.voc = tvocLevel;
        }

        public void setHallStrength(float hallStrength) {
            this.hallStrength = hallStrength;
        }

        public void setHallState(@HallState int hallState) {
            this.hallState = hallState;
        }

        @Override
        public String toString() {
            return String.format(
                    "temperature: %.2f, humidity: %d, uvIndex: %d, ambientLight: %d, hallStrength: %.1f, hallState: %d",
                    temperature,
                    humidity,
                    uvIndex,
                    ambientLight,
                    hallStrength,
                    hallState);
        }

        // Units in deg C with resolution of 0.01 deg C
        private Float temperature;
        // Unit in % with resolution of 0.01%
        private Integer humidity;
        // Unitless
        private Integer uvIndex;
        // Lux
        private Long ambientLight = Long.MIN_VALUE; // not initialized
        // Units in 0.01 dBA
        private Float sound;
        // Units in 0.1 Pa
        private Float pressure;
        // Units in ppm
        private Integer co2;
        // Units in ppb
        private Integer voc;
        // Units in uT (micro tesla)
        private Float hallStrength;
        // Unitless
        @HallState
        private Integer hallState;

        @Override
        public ThunderboardSensorData clone() {
            SensorData d = new SensorData();

            d.temperature = temperature;
            d.humidity = humidity;
            d.uvIndex = uvIndex;
            d.ambientLight = ambientLight;
            d.sound = sound;
            d.pressure = pressure;
            d.co2 = co2;
            d.voc = voc;
            d.hallStrength = hallStrength;
            d.hallState = hallState;

            return d;
        }
    }
}
