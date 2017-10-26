package com.silabs.thunderboard.ble.model;

import java.util.UUID;

import static java.util.UUID.fromString;

public class ThunderBoardUuids {

    public static final UUID

            // Service and Characteristic UUIDs

            UUID_SERVICE_GENERIC_ACCESS = fromString("00001800-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_GENERIC_ATTRIBUTE = fromString("00001801-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_DEVICE_INFORMATION = fromString("0000180a-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_BATTERY = fromString("0000180f-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_AUTOMATION_IO = fromString("00001815-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_CSC = fromString("00001816-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_ENVIRONMENT_SENSING = fromString("0000181a-0000-1000-8000-00805f9b34fb"),
            UUID_SERVICE_ACCELERATION_ORIENTATION = fromString("a4e649f4-4be5-11e5-885d-feff819cdc9f"),
            UUID_SERVICE_AMBIENT_LIGHT = fromString("d24c4f4e-17a7-4548-852c-abf51127368b"),
            UUID_SERVICE_INDOOR_AIR_QUALITY = fromString("efd658ae-c400-ef33-76e7-91b00019103b"),
            UUID_SERVICE_HALL_EFFECT = fromString("f598dbc5-2f00-4ec5-9936-b3d1aa4f957f"), // Magnetic Field Service
            UUID_SERVICE_USER_INTERFACE = fromString("fcb89c40-c600-59f3-7dc3-5ece444a401b"),
            UUID_SERVICE_POWER_MANAGEMENT = fromString("ec61a454-ed00-a5e8-b8f9-de9ec026ec51"),
            UUID_CHARACTERISTIC_DEVICE_NAME = fromString("00002a00-0000-1000-8000-00805f9b34fb"), // Generic Access Service
            UUID_CHARACTERISTIC_APPEARANCE = fromString("00002a01-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_ATTRIBUTE_CHANGED = fromString("00002a05-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_SYSTEM_ID = fromString("00002a23-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_MODEL_NUMBER = fromString("00002a24-0000-1000-8000-00805f9b34fb"),     // Device Information Service
            UUID_CHARACTERISTIC_SERIAL_NUMBER = fromString("00002a25-0000-1000-8000-00805f9b34fb"),     // Device Information Service
            UUID_CHARACTERISTIC_FIRMWARE_REVISION = fromString("00002a26-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_HARDWARE_REVISION = fromString("00002a27-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_MANUFACTURER_NAME = fromString("00002a29-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_BATTERY_LEVEL = fromString("00002a19-0000-1000-8000-00805f9b34fb"), // Battery Service
            UUID_CHARACTERISTIC_POWER_SOURCE = fromString("EC61A454-ED01-A5E8-B8F9-DE9EC026EC51"),
            UUID_CHARACTERISTIC_CSC_CONTROL_POINT = fromString("00002a55-0000-1000-8000-00805f9b34fb"), // CSC Service
            UUID_CHARACTERISTIC_CSC_MEASUREMENT = fromString("00002a5b-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_CSC_FEATURE = fromString("00002a5c-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_CSC_UNKNOWN = fromString("9f70a8fc-826c-4c6f-9c72-41b81d1c9561"),
            UUID_CHARACTERISTIC_UV_INDEX = fromString("00002a76-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_PRESSURE = fromString("00002a6d-0000-1000-8000-00805f9b34fb"), // Environment Service
            UUID_CHARACTERISTIC_TEMPERATURE = fromString("00002a6e-0000-1000-8000-00805f9b34fb"),
            UUID_CHARACTERISTIC_HUMIDITY = fromString("00002a6f-0000-1000-8000-00805f9b34fb"), // Environment Service
            UUID_CHARACTERISTIC_AMBIENT_LIGHT_REACT = fromString("c8546913-bfd9-45eb-8dde-9f8754f4a32e"), // Ambient Light Service for React board
            UUID_CHARACTERISTIC_AMBIENT_LIGHT_SENSE = fromString("c8546913-bf01-45eb-8dde-9f8754f4a32e"), // Ambient Light Service for Sense board
            UUID_CHARACTERISTIC_SOUND_LEVEL = fromString("c8546913-bf02-45eb-8dde-9f8754f4a32e"),
            UUID_CHARACTERISTIC_ENV_CONTROL_POINT = fromString("c8546913-bf03-45eb-8dde-9f8754f4a32e"),
            UUID_CHARACTERISTIC_CO2_READING = fromString("efd658ae-c401-ef33-76e7-91b00019103b"),
            UUID_CHARACTERISTIC_TVOC_READING = fromString("efd658ae-c402-ef33-76e7-91b00019103b"),
            UUID_CHARACTERISTIC_AIR_QUALITY_CONTROL_POINT = fromString("efd658ae-c403-ef33-76e7-91b00019103b"),
            UUID_CHARACTERISTIC_HALL_STATE = fromString("f598dbc5-2f01-4ec5-9936-b3d1aa4f957f"),
            UUID_CHARACTERISTIC_HALL_FIELD_STRENGTH = fromString("f598dbc5-2f02-4ec5-9936-b3d1aa4f957f"),
            UUID_CHARACTERISTIC_HALL_CONTROL_POINT = fromString("f598dbc5-2f03-4ec5-9936-b3d1aa4f957f"),
            UUID_CHARACTERISTIC_ACCELERATION = fromString("c4c1f6e2-4be5-11e5-885d-feff819cdc9f"), // Accelarion and Orientation Service
            UUID_CHARACTERISTIC_ORIENTATION = fromString("b7c4b694-bee3-45dd-ba9f-f3b5e994f49a"),
            UUID_CHARACTERISTIC_CALIBRATE = fromString("71e30b8c-4131-4703-b0a0-b0bbba75856b"),
            UUID_CHARACTERISTIC_PUSH_BUTTONS = fromString("fcb89c40-c601-59f3-7dc3-5ece444a401b"),
            UUID_CHARACTERISTIC_LEDS = fromString("fcb89c40-c602-59f3-7dc3-5ece444a401b"),
            UUID_CHARACTERISTIC_RGB_LEDS = fromString("fcb89c40-c603-59f3-7dc3-5ece444a401b"),
            UUID_CHARACTERISTIC_UI_CONTROL_POINT = fromString("fcb89c40-c604-59f3-7dc3-5ece444a401b"),
            UUID_CHARACTERISTIC_DIGITAL = fromString("00002a56-0000-1000-8000-00805f9b34fb"), // Automation IO Service
            UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION = fromString("00002902-0000-1000-8000-00805f9b34fb"), // Descriptors
            UUID_DESCRIPTOR_CHARACTERISTIC_PRESENTATION_FORMAT = fromString("00002904-0000-1000-8000-00805f9b34fb");
}
