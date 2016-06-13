package com.silabs.thunderboard.ble.model;

import android.bluetooth.BluetoothGattCharacteristic;

import static java.util.UUID.fromString;

import java.util.UUID;

public class ThunderBoardUuids {

    public static final UUID

            // Service and Characteristic UUIDs

            UUID_SERVICE_GENERIC_ACCESS = fromString("00001800-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_DEVICE_INFORMATION = fromString("0000180a-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_BATTERY = fromString("0000180f-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_AUTOMATION_IO = fromString("00001815-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_CSC = fromString("00001816-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_ENVIRONMENT_SENSING = fromString("0000181a-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_ACCELERATION_ORIENTATION = fromString("a4e649f4-4be5-11e5-885d-feff819cdc9f"),
            UUID_SERVICE_AMBIENT_LIGHT = fromString("d24c4f4e-17a7-4548-852c-abf51127368b"),
            UUID_CHARACTERISTIC_DEVICE_NAME = fromString("00002a00-0000-1000-8000-00805f9b34fb"), // Generic Access Service
            UUID_CHARACTERISTIC_APPEARANCE = fromString("00002a01-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_MODEL_NUMBER = fromString("00002a24-0000-1000-8000-00805f9b34fb"),     // Device Information Service
            UUID_CHARACTERISTIC_FIRMWARE_REVISION = fromString("00002a26-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_HARDWARE_REVISION = fromString("00002a27-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_MANUFACTURER_NAME = fromString("00002a29-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_BATTERY_LEVEL = fromString("00002a19-0000-1000-8000-00805f9b34fb"), // Battery Service
            UUID_CHARACTERISTIC_CSC_CONTROL_POINT = fromString("00002a55-0000-1000-8000-00805f9b34fb"), // CSC Service
            UUID_CHARACTERISTIC_CSC_MEASUREMENT = fromString("00002a5b-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_CSC_FEATURE = fromString("00002a5c-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_CSC_UNKNOWN = fromString("9f70a8fc-826c-4c6f-9c72-41b81d1c9561"),
            UUID_CHARACTERISTIC_HUMIDITY = fromString("00002a6f-0000-1000-8000-00805f9b34fb"), // Environment Service
            UUID_CHARACTERISTIC_TEMPERATURE = fromString("00002a6e-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_UV_INDEX = fromString("00002a76-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_AMBIENT_LIGHT = fromString("c8546913-bfd9-45eb-8dde-9f8754f4a32e"), // Ambient Light Service
            UUID_CHARACTERISTIC_ACCELERATION = fromString("c4c1f6e2-4be5-11e5-885d-feff819cdc9f"), // Accelarion and Orientation Service
            UUID_CHARACTERISTIC_ORIENTATION = fromString("b7c4b694-bee3-45dd-ba9f-f3b5e994f49a"),
            UUID_CHARACTERISTIC_CALIBRATE = fromString("71e30b8c-4131-4703-b0a0-b0bbba75856b"),
            UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = fromString("00002902-0000-1000-8000-00805f9b34fb"), // Descriptors
            UUID_DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT = fromString("00002904-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_DIGITAL = fromString("00002a56-0000-1000-8000-00805f9b34fb"); // Automation IO Service

        // Automation IO Service
        public static BluetoothGattCharacteristic CHARACTERISTIC_DIGITAL_INPUT;
        public static BluetoothGattCharacteristic CHARACTERISTIC_DIGITAL_OUTPUT;
}
