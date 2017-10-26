package com.silabs.thunderboard.ble.model;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;

import com.silabs.thunderboard.ble.ThunderBoardSensorEnvironment;
import com.silabs.thunderboard.ble.ThunderBoardSensorIo;
import com.silabs.thunderboard.ble.ThunderBoardSensorMotion;
import com.silabs.thunderboard.common.app.ThunderBoardType;

import org.altbeacon.beacon.Beacon;

import timber.log.Timber;

public class ThunderBoardDevice implements Comparable<ThunderBoardDevice> {

    public static final String THUNDER_BOARD_REACT_UUID_STRING = "cef797da-2e91-4ea4-a424-f45082ac0682";
    public static final String THUNDER_BOARD_REACT_UUID_HEX = "cef797da2e914ea4a424f45082ac0682";
    public static final String THUNDER_BOARD_DEFAULT_NAME = "Thunder";
    private static final String THUNDERBOARD_REACT_MODEL_NUMBER = "RD-0057";
    private static final String THUNDERBOARD_SENSE_MODEL_NUMBER = "BRD4160A";

    private final String address;

    private String name;
    private boolean isOriginalNameNull;
    private int rssi;
    private int state = BluetoothProfile.STATE_DISCONNECTED;
    private int batteryLevel;
    private int powerSource;
    private String firmwareVersion;
    private String systemId;
    private final long timestamp = System.currentTimeMillis();

    // configuration settings
    public Boolean isBatteryConfigured;
    public Boolean isBatteryNotificationEnabled;
    public Boolean isPowerSourceConfigured;
    public Boolean isPowerSourceNotificationEnabled;
    public Boolean isServicesDiscovered;
    public Boolean isCalibrateNotificationEnabled;
    public Boolean isAccelerationNotificationEnabled;
    public Boolean isOrientationNotificationEnabled;
    public Boolean isRotationNotificationEnabled;
    public Boolean isHallStateNotificationEnabled;

    // Demo sensors
    private ThunderBoardSensorMotion sensorMotion;
    private ThunderBoardSensorEnvironment sensorEnvironment;
    private ThunderBoardSensorIo sensorIo;
    private String modelNumber;

    public ThunderBoardDevice(Beacon beacon) {
        this.address = beacon.getBluetoothAddress();
        this.name = beacon.getBluetoothName();
        Timber.d("beacon: %s name: %s, timestamp: %d", address, beacon.getBluetoothName(), timestamp);
        if (name == null) {
            this.name = String.format("%s #%08d", THUNDER_BOARD_DEFAULT_NAME, beacon.getId3().toInt());
            this.isOriginalNameNull = true;
        } else {
            this.isOriginalNameNull = false;
        }
        this.rssi = beacon.getRssi();
    }

    public ThunderBoardDevice(BluetoothDevice device, int rssi) {
        this.name = device.getName();
        this.address = device.getAddress();
        this.rssi = rssi;
        this.isOriginalNameNull = false;
    }

    public void clear() {
        state = BluetoothProfile.STATE_DISCONNECTED;
        isServicesDiscovered = false;
        sensorMotion = null;
        sensorEnvironment = null;
        sensorIo = null;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof ThunderBoardDevice)) {
            return false;
        }
        return address.equalsIgnoreCase(((ThunderBoardDevice) o).getAddress());
    }

    @Override
    public int hashCode() {
        return this.address.hashCode();
    }

    @Override
    public int compareTo(ThunderBoardDevice another) {
        return this.rssi - another.rssi;
    }

    public String getName() {
        return name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getAddress() {
        return address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPowerSource() {
        return powerSource;
    }

    public void setPowerSource(int powerSource) { this.powerSource = powerSource; }

    public int getBatteryLavel() {
        return batteryLevel;
    }

    public void setBatteryLevel(int batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public ThunderBoardSensorMotion getSensorMotion() {
        return sensorMotion;
    }

    public void setSensorMotion(ThunderBoardSensorMotion sensor) {
        sensorMotion = sensor;
        sensor.clearCharacteristicsStatus();
    }

    public ThunderBoardSensorEnvironment getSensorEnvironment() {
        return sensorEnvironment;
    }

    public void setSensorEnvironment(ThunderBoardSensorEnvironment sensor) {
        sensorEnvironment = sensor;
    }

    public ThunderBoardSensorIo getSensorIo() {
        return sensorIo;
    }

    public void setSensorIo(ThunderBoardSensorIo sensor) {
        sensorIo = sensor;
    }

    public void setName(String name) {
        this.name = name;
        this.isOriginalNameNull = false;
    }

    public boolean isOriginalNameNull() {
        return isOriginalNameNull;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getModelNumber() {
        return this.modelNumber;
    }

    public ThunderBoardType getThunderBoardType() {
        if ( THUNDERBOARD_REACT_MODEL_NUMBER.equals(this.modelNumber)) {
            return ThunderBoardType.THUNDERBOARD_REACT;
        }
        if (THUNDERBOARD_SENSE_MODEL_NUMBER.equals((this.modelNumber))) {
            return ThunderBoardType.THUNDERBOARD_SENSE;
        }
        return ThunderBoardType.UNKNOWN;
    }
}
