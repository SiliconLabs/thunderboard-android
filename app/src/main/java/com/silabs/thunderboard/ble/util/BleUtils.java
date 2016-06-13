package com.silabs.thunderboard.ble.util;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;

import java.util.UUID;

import timber.log.Timber;

public class BleUtils {

    public static boolean readCharacteristic(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid) {
        if(gatt == null) {
            return false;
        }
        BluetoothGattService service = gatt.getService(serviceUuid);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
        if (characteristic == null) {
            return false;
        }
        return gatt.readCharacteristic(characteristic);
    }

    public static boolean readCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if(gatt == null || characteristic == null) {
            return false;
        }
        return gatt.readCharacteristic(characteristic);
    }

    public static boolean setCharacteristicNotification(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, boolean enable) {
        if(gatt == null) {
            return false;
        }
        BluetoothGattService service = gatt.getService(serviceUuid);
        if (service == null) {
            Timber.d("invalid service uuid: %", serviceUuid);
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

        if (characteristic == null) {
            Timber.d("could not get characteristic: %s for service: %s", characteristicUuid.toString(), serviceUuid.toString());
            return false;
        }

        if (!gatt.setCharacteristicNotification(characteristic, true)) {
            Timber.d("was not able to setCharacteristicNotification");
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
        if (descriptor == null) {
            Timber.d("was not able to getDescriptor");
            return false;
        }

        if (enable) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }

        return gatt.writeDescriptor(descriptor);
    }

    public static boolean writeCharacteristic(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int value, int format, int offset) {
        if(gatt == null) {
            return false;
        }
        Timber.d("prop: %02x", characteristic.getProperties());
        characteristic.setValue(value, format, offset);
        return gatt.writeCharacteristic(characteristic);
    }

    public static boolean writeCharacteristic(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, int value, int format, int offset) {
        if(gatt == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);
        boolean submitted = characteristic.setValue(value, format, offset);
        Timber.d("submitted: %s", submitted);
        return gatt.writeCharacteristic(characteristic);
    }

    public static boolean writeCharacteristic(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, byte[] value) {
        if(gatt == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = gatt.getService(serviceUuid).getCharacteristic(characteristicUuid);
        boolean submitted = characteristic.setValue(value);
        Timber.d("submitted: %s", submitted);
        return gatt.writeCharacteristic(characteristic);
    }

    public static boolean checkAllowNotifications(String deviceAddress, ThunderBoardPreferences preferences) {
        if (!preferences.beaconNotifications || preferences.beacons == null || preferences.beacons.size() == 0) {
            return false;
        }
        ThunderBoardPreferences.Beacon beacon = preferences.beacons.get(deviceAddress);
        if (beacon == null || !beacon.allowNotifications) {
            return false;
        }
        return true;
    }

    public static boolean setCharacteristicIndication(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, boolean enable) {
        if(gatt == null) {
            return false;
        }
        BluetoothGattService service = gatt.getService(serviceUuid);
        if (service == null) {
            Timber.d("invalid service uuid: %", serviceUuid);
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

        if (characteristic == null) {
            Timber.d("could not get characteristic: %s for service: %s", characteristicUuid.toString(), serviceUuid.toString());
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
        if (descriptor == null) {
            Timber.d("was not able to getDescriptor");
            return false;
        }

        if (enable) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        }

        return gatt.writeDescriptor(descriptor);
    }
}
