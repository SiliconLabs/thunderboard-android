package com.silabs.thunderboard.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.ble.model.ThunderBoardUuids;
import com.silabs.thunderboard.ble.util.BleUtils;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.demos.model.EnvironmentEvent;
import com.silabs.thunderboard.demos.model.HallState;
import com.silabs.thunderboard.demos.model.LedRGB;
import com.silabs.thunderboard.demos.model.LedRGBState;
import com.silabs.thunderboard.demos.model.MotionEvent;
import com.silabs.thunderboard.demos.model.NotificationEvent;
import com.silabs.thunderboard.demos.model.StatusEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import timber.log.Timber;

/**
 * Wrapper for the {@link BluetoothGattCallback}.
 * <p>
 * Uses {@link BleManager} to submit events to the application.
 */
public class GattManager {

    private final PreferenceManager preferenceManager;
    private final BleManager bleManager;

    public GattManager(PreferenceManager prefsManager, BleManager bleManager) {
        this.preferenceManager = prefsManager;
        this.bleManager = bleManager;
    }

    /**
     * GATT client callbacks
     */
    final BluetoothGattCallback gattCallbacks = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            BluetoothDevice device = gatt.getDevice();

            Timber.d("device: %s, status: %d, newState: %d", (device != null), status, newState);

            if (BluetoothGatt.GATT_SUCCESS != status) {
                gatt.disconnect();
                return;
            }

            if (BluetoothProfile.STATE_DISCONNECTED == newState) {
                gatt.close();
            } else if (BluetoothProfile.STATE_CONNECTED == newState) {
                gatt.discoverServices();
                preferenceManager.addConnected(device.getAddress(), device.getName());
            }

            ThunderBoardDevice bgd = bleManager.getDeviceFromCache(device.getAddress());
            if (bgd != null) {
                bgd.setState(newState);
                bleManager.selectedDeviceMonitor.onNext(bgd);
                bleManager.selectedDeviceStatusMonitor.onNext(new StatusEvent(bgd));
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Timber.d("status: %d", status);

            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            ThunderBoardDevice device = bleManager.getDeviceFromCache(gatt.getDevice().getAddress());
            device.isServicesDiscovered = true;
            bleManager.readRequiredCharacteristics();
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt,
                                         final BluetoothGattCharacteristic characteristic,
                                         final int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            UUID uuid = characteristic.getUuid();
            byte[] ba = characteristic.getValue();

            if (ba == null || ba.length == 0) {
                Timber.d("characteristic: %s is not initialized", uuid.toString());
            } else {
                ThunderBoardDevice device = bleManager.getDeviceFromCache(gatt.getDevice().getAddress());
                if (ThunderBoardUuids.UUID_CHARACTERISTIC_DEVICE_NAME.equals(uuid)) {
                    String deviceName = characteristic.getStringValue(0);
                    device.setName(deviceName);
                    Timber.d("deviceName on next");
                    Timber.d("characteristic: %s %s", characteristic.getUuid().toString(), deviceName);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_MODEL_NUMBER.equals(uuid)) {
                    String modelNumber = characteristic.getStringValue(0);
                    device.setModelNumber(modelNumber);
                    Timber.d("modelNumber characteristic: %s %s", characteristic.getUuid().toString(), modelNumber);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_SYSTEM_ID.equals(uuid)) {
                    byte[] bytes = characteristic.getValue();
                    ByteBuffer bb = ByteBuffer.wrap(bytes);
                    bb.order(ByteOrder.BIG_ENDIAN);
                    long id = bb.getLong() & 0xFFFFFF;
                    String systemId = String.valueOf(id);
                    device.setSystemId(systemId);
                    Timber.d("systemId characteristic: %s %s", characteristic.getUuid().toString(),
                             systemId);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL.equals(uuid)) {
                    int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Timber.d("batteryLevel: %d", batteryLevel);
                    device.setBatteryLevel(batteryLevel);
                    device.isBatteryConfigured = true;
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_POWER_SOURCE.equals(uuid)) {
                    int powerSource = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Timber.d("powerSource: %d", powerSource);
                    device.setPowerSource(powerSource);
                    device.isPowerSourceConfigured = true;
                    bleManager.selectedDeviceStatusMonitor.onNext(new StatusEvent(device));
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_FIRMWARE_REVISION.equals(uuid)) {
                    String firmwareVerion = characteristic.getStringValue(0);
                    device.setFirmwareVersion(firmwareVerion);
                    Timber.d("firmware on next");
                    Timber.d("characteristic: %s %s", characteristic.getUuid().toString(), firmwareVerion);
                    // the last from the required read characteristics
                    bleManager.selectedDeviceStatusMonitor.onNext(new StatusEvent(device));
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_TEMPERATURE.equals(uuid)) {
                    int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                    Timber.d("temperature: %d", temperature);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setTemperature(temperature);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_HUMIDITY.equals(uuid)) {
                    int humidity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    Timber.d("humidity: %d", humidity);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setHumidity(humidity);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_UV_INDEX.equals(uuid)) {
                    int uvIndex = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Timber.d("uv index: %d", uvIndex);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setUvIndex(uvIndex);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_AMBIENT_LIGHT_REACT.equals(uuid) ||
                        ThunderBoardUuids.UUID_CHARACTERISTIC_AMBIENT_LIGHT_SENSE.equals(uuid)) {
                    int ambientLight = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    long ambientLightLong = (ambientLight < 0) ?
                            (long) Math.abs(ambientLight) + (long) Integer.MAX_VALUE : ambientLight;
                    Timber.d("ambientLight: %d", ambientLightLong);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setAmbientLight(ambientLightLong);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_SOUND_LEVEL.equals(uuid)) {
                    int soundLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                    Timber.d("sound level: %d", soundLevel);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setSoundLevel(soundLevel);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_PRESSURE.equals(uuid)) {
                    long pressure = (long) characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    Timber.d("pressure: %d", pressure);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setPressure(pressure);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CO2_READING.equals(uuid)) {
                    int co2Level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    Timber.d("C02 level: %d ppm", co2Level);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setCO2Level(co2Level);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_TVOC_READING.equals(uuid)) {
                    int tvocLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                    Timber.d("TVOC level: %d ppb", tvocLevel);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setTVOCLevel(tvocLevel);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_FIELD_STRENGTH.equals(uuid)) {
                    long hallStrength = (long) characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT32, 0);
                    Timber.d("hall strength: %d uT", hallStrength);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setHallStrength(hallStrength);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE.equals(uuid)) {
                    @HallState int hallState = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Timber.d("hall state: %d", hallState);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setHallState(hallState);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_FEATURE.equals(uuid)) {
                    byte cscFeature = ba[0];
                    Timber.d("csc feature: %02x %02x", ba[0], ba[1]);
                    ThunderBoardSensorMotion sensor = device.getSensorMotion();
                    sensor.setCscFeature(cscFeature);
                    bleManager.notificationsMonitor.onNext(new NotificationEvent(device,
                                                                                 uuid,
                                                                                 NotificationEvent.ACTION_NOTIFICATIONS_SET));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL.equals(characteristic.getUuid())) {
                    ThunderBoardSensorIo sensor = device.getSensorIo();
                    if (sensor == null) {
                        sensor = new ThunderBoardSensorIo();
                        device.setSensorIo(sensor);
                    }
                    sensor.setLed(ba[0]);
                    sensor.isSensorDataChanged = true;
                    bleManager.selectedDeviceMonitor.onNext(device);
                    return;
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_RGB_LEDS.equals(uuid)) {
                    Integer on = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Integer red = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                    Integer green = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 2);
                    Integer blue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 3);

                    LedRGBState ledState = new LedRGBState(
                            on != null && (on != 0),
                            new LedRGB(
                                    red == null ? 0 : red,
                                    green == null ? 0 : green,
                                    blue == null ? 0 : blue
                            )
                    );
//                    Timber.d(String.format("READING Color LED Value: %s", ledState));
                    ThunderBoardSensorIo sensor = device.getSensorIo();
                    if (sensor == null) {
                        sensor = new ThunderBoardSensorIo();
                        device.setSensorIo(sensor);
                    }

                    sensor.setColorLed(ledState);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else {
                    Timber.d("unknown characteristic: %s", uuid);
                }

                // another call for the rest if any
                bleManager.readRequiredCharacteristics();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Timber.d("characteristic: %s status: %d", characteristic.getUuid().toString(), status);

            byte[] ba = characteristic.getValue();
            if (ba == null || ba.length == 0) {
                Timber.d("characteristic: %s is not initialized", characteristic.getUuid().toString());
            } else {
                ThunderBoardDevice device = bleManager.getDeviceFromCache(gatt.getDevice().getAddress());
                if (ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL.equals(characteristic.getUuid())) {
                    device.getSensorIo().setLed(ba[0]);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE.equals(characteristic.getUuid())) {
                    if (ba[0] == 0x01) {
                        Timber.d("onCharacteristicWrite startCalibration value: %02x, length: %d," +
                                         " ACTION: ACTION_CALIBRATE", ba[0], ba
                                         .length);
                        bleManager.motionDetector.onNext(new MotionEvent(device, characteristic
                                .getUuid(), MotionEvent.ACTION_CALIBRATE));
                        return;
                    } else if (ba[0] == 0x02) {
                        Timber.d("onCharacteristicWrite startCalibration value: %02x, length: %d," +
                                         " ACTION: ACTION_CLEAR_ORIENTATION", ba[0], ba
                                         .length);
                        bleManager.motionDetector.onNext(new MotionEvent(device, characteristic
                                .getUuid(), MotionEvent.ACTION_CLEAR_ORIENTATION));
                        return;
                    }
                    Timber.d("onCharacteristicWrite startCalibration value: %02x, length: %d," +
                                     " ACTION: UNKOWN", ba[0], ba
                                     .length);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_CONTROL_POINT.equals(characteristic.getUuid())) {
                    boolean read = BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_HALL_EFFECT, ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE);
                    Timber.d("Characteristic read successful: %s", read);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

            UUID uuid = characteristic.getUuid();
            // leave for debugging purposes
            // Timber.d("characteristic: %s", uuid.toString());

            byte[] ba = characteristic.getValue();

            if (ba == null || ba.length == 0) {
                Timber.d("characteristic: %s is not initialized", uuid.toString());
            } else {
                ThunderBoardDevice device = bleManager.getDeviceFromCache(gatt.getDevice().getAddress());

                if (ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL.equals(uuid)) {
                    int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Timber.d("batteryLevel: %d", batteryLevel);
                    device.setBatteryLevel(batteryLevel);
                    device.isBatteryConfigured = true;
                    bleManager.selectedDeviceStatusMonitor.onNext(new StatusEvent(device));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_POWER_SOURCE.equals(uuid)) {
                    int powerSource = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Timber.d("Power source: %d", powerSource);
                    device.setPowerSource(powerSource);
                    device.isPowerSourceConfigured = true;
                    bleManager.selectedDeviceStatusMonitor.onNext(new StatusEvent(device));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL.equals(characteristic.getUuid())) {
                    Timber.d("value: %02x", ba[0]);
                    ThunderBoardSensorIo sensor = device.getSensorIo();
                    sensor.isSensorDataChanged = true;
                    sensor.setSwitch(ba[0]);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_MEASUREMENT.equals(uuid)) {
                    byte wheelRevolutionDataPresent = ba[0];
                    int cumulativeWheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32,
                                                                                1);
                    int lastWheelRevolutionTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,
                                                                             5);
                    Timber.d("csc measurement: %d", cumulativeWheelRevolutions);
                    ThunderBoardSensorMotion sensor = device.getSensorMotion();
                    sensor.setCscMesurements(wheelRevolutionDataPresent,
                                             cumulativeWheelRevolutions,
                                             lastWheelRevolutionTime);
                    bleManager.motionDetector.onNext(new MotionEvent(device, uuid,
                                                                     MotionEvent.ACTION_CSC_CHANGED));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION.equals(uuid)) {
                    int accelerationX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                    int accelerationY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2);
                    int accelerationZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
                    Timber.d("acceleration: %d %d %d", accelerationX, accelerationY, accelerationZ);
                    ThunderBoardSensorMotion sensor = device.getSensorMotion();
                    sensor.setAcceleration(accelerationX / 1000f, accelerationY / 1000f, accelerationZ / 1000f);
                    bleManager.motionDetector.onNext(new MotionEvent(device, uuid));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION.equals(uuid)) {
                    int orientationX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                    int orientationY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2);
                    int orientationZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
                    Timber.d("orientation: %d %d %d", orientationX, orientationY, orientationZ);
                    ThunderBoardSensorMotion sensor = device.getSensorMotion();
                    sensor.setOrientation(orientationX / 100f, orientationY / 100f, orientationZ / 100f);
                    bleManager.motionDetector.onNext(new MotionEvent(device, uuid));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE.equals(uuid)) {
                    Timber.d("onCharacteristicChanged startCalibration value: %02x, length: %d", ba[0], ba
                            .length);
                    if (ba[0] == 0x01) {
                        bleManager.motionDetector.onNext(new MotionEvent(device, uuid, MotionEvent
                                .ACTION_CALIBRATE));
                    } else if (ba[0] == 0x02) {
                        Timber.d("startCalibration completed with orientation reset");
                        bleManager.motionDetector.onNext(new MotionEvent(device, uuid, MotionEvent
                                .ACTION_CLEAR_ORIENTATION));
                    }
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_CONTROL_POINT.equals(uuid)) {
                    Timber.d("onCharacteristicChanged clear rotation value: %02x, length: %d",
                             ba[0], ba.length);
                    Timber.d("clearRotation completed with orientation reset");
                    bleManager.motionDetector.onNext(new MotionEvent(device, uuid, MotionEvent
                            .ACTION_CLEAR_ROTATION));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE.equals(uuid)) {
                    @HallState int hallState = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Timber.d("onCharacteristicChanged hall state: %d", hallState);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setHallState(hallState);
                    bleManager.environmentDetector.onNext(new EnvironmentEvent(device, uuid));
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

            UUID descriptorUuid = descriptor.getUuid();
            byte[] ba = descriptor.getValue();

            if (ba == null || ba.length == 0) {
                Timber.d("descriptor: %s is not initialized", descriptorUuid.toString());
                return;
            }

            UUID characteristicUuid = descriptor.getCharacteristic().getUuid();
            Timber.d("descriptor uuid: %s for characteristic: %s", descriptorUuid, characteristicUuid);

            ThunderBoardDevice device = bleManager.getDeviceFromCache(gatt.getDevice().getAddress());
            if (ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL.equals(characteristicUuid)) {
                Timber.d("battery notification enabled");
                device.isBatteryNotificationEnabled = true;
                bleManager.readRequiredCharacteristics();
                bleManager.selectedDeviceMonitor.onNext(device);
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_POWER_SOURCE.equals(characteristicUuid)) {
                Timber.d("power source enabled");
                device.isPowerSourceNotificationEnabled = true;
                bleManager.readRequiredCharacteristics();
                bleManager.selectedDeviceMonitor.onNext(device);
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL.equals(characteristicUuid)) {
                Timber.d("io notification enabled");
                ThunderBoardSensorIo sensor = device.getSensorIo();
                if (sensor == null) {
                    sensor = new ThunderBoardSensorIo();
                    device.setSensorIo(sensor);
                }
                device.getSensorIo().isNotificationEnabled = true;
                bleManager.selectedDeviceMonitor.onNext(device);
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_MEASUREMENT.equals(characteristicUuid)) {
                boolean enabled = ba[0] == 0x01;
                Timber.d("csc measurement notification enabled: %s", enabled);
                int notificationAction = enabled ? NotificationEvent.ACTION_NOTIFICATIONS_SET : NotificationEvent.ACTION_NOTIFICATIONS_CLEAR;
                device.getSensorMotion().setCscMeasurementNotificationEnabled(enabled);
                device.isRotationNotificationEnabled = enabled;
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device,
                                                                             characteristicUuid,
                                                                             notificationAction));
                if (!enabled) {
                    bleManager.motionDetector.onNext(new MotionEvent(device, characteristicUuid,
                                                                     MotionEvent.ACTION_CLEAR_MEASUREMENT));
                }
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION.equals(characteristicUuid)) {
                boolean enabled = ba[0] == 0x01;
                Timber.d("acceleration notification enabled: %s", enabled);
                device.isAccelerationNotificationEnabled = enabled;
                int notificationAction = enabled ? NotificationEvent.ACTION_NOTIFICATIONS_SET : NotificationEvent.ACTION_NOTIFICATIONS_CLEAR;
                if (device.getSensorMotion() != null) {
                    device.getSensorMotion().setAccelerationNotificationEnabled(enabled);
                }
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device,
                                                                             characteristicUuid,
                                                                             notificationAction));
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION.equals(characteristicUuid)) {
                boolean enabled = ba[0] == 0x01;
                Timber.d("orientation notification enabled: %s", enabled);
                int notificationAction = enabled ? NotificationEvent.ACTION_NOTIFICATIONS_SET : NotificationEvent.ACTION_NOTIFICATIONS_CLEAR;
                device.isOrientationNotificationEnabled = enabled;
                if (device.getSensorMotion() != null) {
                    device.getSensorMotion().setOrientationNotificationEnabled(enabled);
                }
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device,
                                                                             characteristicUuid,
                                                                             notificationAction));
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE.equals(characteristicUuid)) {
                Timber.d("onDescriptorWrite startCalibration value: %02x, length: %d", ba[0], ba
                        .length);
                boolean enabled = ba[0] == 0x01;
                int notificationAction = enabled ? NotificationEvent.ACTION_NOTIFICATIONS_SET : NotificationEvent.ACTION_NOTIFICATIONS_CLEAR;
                // this one is called once during configureMotion
                device.isCalibrateNotificationEnabled = true; // enabled;
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device,
                                                                             characteristicUuid,
                                                                             notificationAction));
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE.equals(characteristicUuid)) {
                boolean enabled = ba[0] == 0x01;
                Timber.d("hall state notification enabled: %s", enabled);
                int notificationAction = enabled ? NotificationEvent.ACTION_NOTIFICATIONS_SET : NotificationEvent.ACTION_NOTIFICATIONS_CLEAR;
                device.isHallStateNotificationEnabled = enabled;
                if (device.getSensorEnvironment() != null) {
                    device.getSensorEnvironment().setHallStateNotificationEnabled();
                }
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device,
                                                                             characteristicUuid,
                                                                             notificationAction));
            }

            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Timber.d("status: %d mtu: %d", status, mtu);
        }
    };
}
