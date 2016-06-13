package com.silabs.thunderboard.ble.model;

import android.bluetooth.BluetoothProfile;

import com.silabs.thunderboard.ble.ThunderBoardSensorEnvironment;
import com.silabs.thunderboard.ble.ThunderBoardSensorMotion;
import com.silabs.thunderboard.ble.ThunderBoardSensorIo;

import org.altbeacon.beacon.Beacon;

import timber.log.Timber;

public class ThunderBoardDevice implements Comparable<ThunderBoardDevice> {

    public static final String THUNDER_BOARD_REACT_UUID_STRING = "cef797da-2e91-4ea4-a424-f45082ac0682";
    public static final String THUNDER_BOARD_REACT_UUID_HEX = "cef797da2e914ea4a424f45082ac0682";
    public static final String THUNDER_BOARD_REACT_NAME = "Thunder React";
    public static final String THUNDER_BOARD_DEFAULT_NAME = "Thunder";

    private final String address;

    private String name;
    private boolean isOriginalNameNull;
    private int rssi;
    private int state = BluetoothProfile.STATE_DISCONNECTED;
    private int batteryLevel;
    private String firmwareVersion;
    private final long timestamp = System.currentTimeMillis();

    // configuration settings
    public Boolean isBatteryConfigured;
    public Boolean isBatteryNotificationEnabled;
    public Boolean isServicesDiscovered;

    // Demo sensors
    private ThunderBoardSensorMotion sensorMotion;
    private ThunderBoardSensorEnvironment sensorEnvironment;
    private ThunderBoardSensorIo sensorIo;

    public ThunderBoardDevice(Beacon beacon) {
        this.address = beacon.getBluetoothAddress();
        this.name = beacon.getBluetoothName();
        Timber.d("beacon: %s name: %s, timestamp: %d", address, beacon.getBluetoothName(), timestamp);
        if (name == null) {
            if (THUNDER_BOARD_REACT_UUID_HEX.equals(beacon.getId1())) {
                this.name = String.format("%s #%08d", THUNDER_BOARD_REACT_NAME, beacon.getId3().toInt());
            } else {
                this.name = String.format("%s #%08d", THUNDER_BOARD_DEFAULT_NAME, beacon.getId3().toInt());
            }
            this.isOriginalNameNull = true;
        } else {
            this.isOriginalNameNull = false;
        }
        this.rssi = beacon.getRssi();
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
}
