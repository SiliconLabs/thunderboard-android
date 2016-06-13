package com.silabs.thunderboard.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;

import com.silabs.thunderboard.ble.model.ThunderBoardUuids;
import com.silabs.thunderboard.demos.model.MotionEvent;
import com.silabs.thunderboard.demos.model.NotificationEvent;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.demos.model.StatusEvent;

import java.util.List;
import java.util.UUID;

import timber.log.Timber;

/**
 * Wrapper for the {@link BluetoothGattCallback}.
 * <p>
 *     Uses {@link BleManager} to submit events to the application.
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

            displayGattServices(gatt.getServices());
            ThunderBoardDevice device = bleManager.getDeviceFromCache(gatt.getDevice().getAddress());
            device.isServicesDiscovered = true;
            bleManager.readRequiredCharacteristics();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
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
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL.equals(uuid)) {
                    int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                    Timber.d("batteryLevel: %d", batteryLevel);
                    device.setBatteryLevel(batteryLevel);
                    device.isBatteryConfigured = true;
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
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_AMBIENT_LIGHT.equals(uuid)) {
                    int ambientLight = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                    long ambientLightLong = (ambientLight < 0) ? (long)Math.abs(ambientLight) + (long)Integer.MAX_VALUE : ambientLight;
                    Timber.d("ambientLight: %d", ambientLightLong);
                    ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                    sensor.setAmbientLight(ambientLightLong);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_FEATURE.equals(uuid)) {
                    byte cscFeature = ba[0];
                    Timber.d("csc feature: %02x %02x", ba[0], ba[1]);
                    ThunderBoardSensorMotion sensor = device.getSensorMotion();
                    sensor.setCscFeature(cscFeature);
                    bleManager.notificationsMonitor.onNext(new NotificationEvent(device, NotificationEvent.ACTION_NOTIFICATIONS_SET));
                } else if (ThunderBoardUuids.CHARACTERISTIC_DIGITAL_OUTPUT.equals(characteristic)) {
                    ThunderBoardSensorIo sensor = device.getSensorIo();
                    sensor.setLed(ba[0]);
                    sensor.isSensorDataChanged = true;
                    bleManager.selectedDeviceMonitor.onNext(device);
                    return;
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
                if (ThunderBoardUuids.CHARACTERISTIC_DIGITAL_OUTPUT.equals(characteristic)) {
                    device.getSensorIo().setLed(ba[0]);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE.equals(characteristic.getUuid())) {
                    Timber.d("calibrate value: %02x, length: %d", ba[0], ba.length);
                    if (ba[0] == 0x01) {
                        bleManager.motionDetector.onNext(new MotionEvent(device, MotionEvent.ACTION_CALIBRATE));
                    } else if (ba[0] == 0x02) {
                        Timber.d("calibrate completed with orientation reset");
                        bleManager.motionDetector.onNext(new MotionEvent(device, MotionEvent.ACTION_CLEAR_ORIENTATION));
                    }
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
                } else if (ThunderBoardUuids.CHARACTERISTIC_DIGITAL_INPUT.equals(characteristic)) {
                    Timber.d("value: %02x", ba[0]);
                    ThunderBoardSensorIo sensor = device.getSensorIo();
                    sensor.setSwitch(ba[0]);
                    bleManager.selectedDeviceMonitor.onNext(device);
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_MEASUREMENT.equals(uuid)) {
                    byte wheelRevolutionDataPresent = ba[0];
                    int cumulativeWheelRevolutions = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 1);
                    int lastWheelRevolutionTime = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 5);
                    Timber.d("csc measurement: %d", cumulativeWheelRevolutions);
                    ThunderBoardSensorMotion sensor = device.getSensorMotion();
                    sensor.setCscMesurements(wheelRevolutionDataPresent, cumulativeWheelRevolutions, lastWheelRevolutionTime);
                    bleManager.motionDetector.onNext(new MotionEvent(device));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION.equals(uuid)) {
                    int accelerationX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                    int accelerationY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2);
                    int accelerationZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
                    Timber.d("acceleration: %d %d %d", accelerationX, accelerationY, accelerationZ);
                    ThunderBoardSensorMotion sensor = device.getSensorMotion();
                    sensor.setAcceleration(accelerationX / 1000f, accelerationY / 1000f, accelerationZ / 1000f);
                    bleManager.motionDetector.onNext(new MotionEvent(device));
                } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION.equals(uuid)) {
                    int orientationX = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                    int orientationY = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2);
                    int orientationZ = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4);
                    Timber.d("orientation: %d %d %d", orientationX, orientationY, orientationZ);
                    ThunderBoardSensorMotion sensor = device.getSensorMotion();
                    sensor.setOrientation(orientationX / 100f, orientationY / 100f, orientationZ / 100f);
                    bleManager.motionDetector.onNext(new MotionEvent(device));
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
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL.equals(characteristicUuid)) {
                Timber.d("io notification enabled");
                device.getSensorIo().isNotificationEnabled = true;
                bleManager.selectedDeviceMonitor.onNext(device);
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_MEASUREMENT.equals(characteristicUuid)) {
                boolean enabled = ba[0] == 0x01;
                Timber.d("csc measurement notification enabled: %s", enabled);
                int notificationAction = enabled ? NotificationEvent.ACTION_NOTIFICATIONS_SET : NotificationEvent.ACTION_NOTIFICATIONS_CLEAR;
                device.getSensorMotion().setCscMeasurementNotificationEnabled(enabled);
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device, notificationAction));
                if (!enabled) {
                    bleManager.motionDetector.onNext(new MotionEvent(device, MotionEvent.ACTION_CLEAR_MEASUREMENT));
                }
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION.equals(characteristicUuid)) {
                boolean enabled = ba[0] == 0x01;
                Timber.d("acceleration notification enabled: %s", enabled);
                int notificationAction = enabled ? NotificationEvent.ACTION_NOTIFICATIONS_SET : NotificationEvent.ACTION_NOTIFICATIONS_CLEAR;
                device.getSensorMotion().setAccelerationNotificationEnabled(enabled);
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device, notificationAction));
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION.equals(characteristicUuid)) {
                boolean enabled = ba[0] == 0x01;
                Timber.d("orientation notification enabled: %s", enabled);
                int notificationAction = enabled ? NotificationEvent.ACTION_NOTIFICATIONS_SET : NotificationEvent.ACTION_NOTIFICATIONS_CLEAR;
                device.getSensorMotion().setOrientationNotificationEnabled(enabled);
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device, notificationAction));
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE.equals(characteristicUuid)) {
                // this one is called once during configureMotion
                bleManager.notificationsMonitor.onNext(new NotificationEvent(device, NotificationEvent.ACTION_NOTIFICATIONS_SET));
            }

            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Timber.d("status: %d mtu: %d", status, mtu);
        }

        private void displayGattServices(List<BluetoothGattService> gattServices) {

            if (gattServices == null) return;

            UUID serviceUuid;

            // Loops through available GATT Services.
            for (BluetoothGattService gattService : gattServices) {

                serviceUuid = gattService.getUuid();
                Timber.d("service: %s", serviceUuid.toString());

                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();

                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    int prop = gattCharacteristic.getProperties();
                    if (ThunderBoardUuids.UUID_SERVICE_AUTOMATION_IO.equals(serviceUuid)) {
                        if (BluetoothGattCharacteristic.PROPERTY_WRITE == (BluetoothGattCharacteristic.PROPERTY_WRITE & prop)) {
                            ThunderBoardUuids.CHARACTERISTIC_DIGITAL_OUTPUT = gattCharacteristic;
                        } else {
                            ThunderBoardUuids.CHARACTERISTIC_DIGITAL_INPUT = gattCharacteristic;
                        }
                    }
                }
            }
        }
    };
}
