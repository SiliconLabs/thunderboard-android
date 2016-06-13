package com.silabs.thunderboard.ble;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.ble.model.ThunderBoardUuids;
import com.silabs.thunderboard.ble.util.BleUtils;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.injection.qualifier.ForApplication;
import com.silabs.thunderboard.demos.model.MotionEvent;
import com.silabs.thunderboard.demos.model.NotificationEvent;
import com.silabs.thunderboard.demos.model.StatusEvent;
import com.silabs.thunderboard.demos.ui.DemosSelectionActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Provides methods for the application to interact with the Android BLE system.
 */
@Singleton
public class BleManager implements RangeNotifier {

    private final Context context;
    private final PreferenceManager preferenceManager;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;

    private final List<ThunderBoardDevice> devices = new ArrayList<>();

    public final PublishSubject<List<ThunderBoardDevice>> scanner = PublishSubject.create();
    public final BehaviorSubject<ThunderBoardDevice> selectedDeviceMonitor = BehaviorSubject.create();
    public final BehaviorSubject<StatusEvent> selectedDeviceStatusMonitor = BehaviorSubject.create();
    public final PublishSubject<MotionEvent> motionDetector = PublishSubject.create();
    public final BehaviorSubject<NotificationEvent> notificationsMonitor = BehaviorSubject.create();
    private final BeaconManager beaconManager;
    private final GattManager gattManager;

    // Should be cleared/null when connection sessions begins
    private BluetoothGatt gatt;

    @Inject
    public BleManager(@ForApplication Context context, PreferenceManager prefsManager) {
        this.context = context;

        // the app manifest requires support for BLE, no need to check explicitly
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        preferenceManager = prefsManager;
        gattManager = new GattManager(prefsManager, this);

        // Beaconing
        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(context);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        Timber.d("setting up background monitoring for beacons and power saving");
        Identifier id1 = Identifier.parse(ThunderBoardDevice.THUNDER_BOARD_REACT_UUID_STRING);
        Region region = new Region("backgroundRegion", id1, null, null);
        regionBootstrap = new ThunderBoardBootstrap(context, this, region);
        backgroundPowerSaver = new ThunderBoardPowerSaver(context, preferenceManager);

        beaconManager.setBackgroundBetweenScanPeriod(ThunderBoardPowerSaver.DELAY_BETWEEN_SCANS_INACTIVE);
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    public void foregroundScan() {
        clearDevices();
        devicesNotFoundMonitor.start();
        Timber.d("has observers %s", scanner.hasObservers());
    }

    public void backgroundScan() {
        Timber.d("has observers %s gatt is null: %s", scanner.hasObservers(), (gatt == null));
        devicesNotFoundMonitor.cancel();
    }

    public void clearDevices() {
        Timber.d("all");
        closeGatt();
        // this will give a chance to complete the timer w/o triggering no devices
        devices.clear();
    }

    public void connect(String deviceAddress) {

        Timber.d("%s", deviceAddress);

        if (gatt != null) {
            Timber.d("gat not null, closing and reconnecting");
            closeGatt();
            connect(deviceAddress);
            return;
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

        if (device == null) {
            throw new IllegalStateException("Connecting to a non discovered device is not supported.");
        } else {
            // This is where the connection session starts
            gatt = device.connectGatt(context, false, gattManager.gattCallbacks);
            ThunderBoardDevice tbd = getDeviceFromCache(deviceAddress);
            tbd.setState(BluetoothProfile.STATE_CONNECTING);
            selectedDeviceMonitor.onNext(tbd);
            selectedDeviceStatusMonitor.onNext(new StatusEvent(tbd));
            return;
        }
    }

    // Demo Configuration interfaces

    public void configureIo() {

        if (gatt != null) {
            ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                ThunderBoardSensorIo sensor = device.getSensorIo();
                if (sensor == null) {
                    sensor = new ThunderBoardSensorIo();
                    device.setSensorIo(sensor);
                    boolean submitted = BleUtils.setCharacteristicNotification(gatt, ThunderBoardUuids.UUID_SERVICE_AUTOMATION_IO, ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL, ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
                    Timber.d("enable io notification submitted: %s", submitted);
                    if (!submitted) {
                        sensor.isNotificationEnabled = false;
                        Timber.d("Could not configure the sensor");
                        Toast.makeText(context, R.string.iodemo_alert_configuration_failed, Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        sensor.isNotificationEnabled = true;
                        return;
                    }
                } else {
                    BleUtils.readCharacteristic(gatt, ThunderBoardUuids.CHARACTERISTIC_DIGITAL_OUTPUT);
                }
            }
        }
    }

    // Demo IO actions
    public void ledAction(int ledSent) {
        boolean submitted = BleUtils.writeCharacteristic(gatt, ThunderBoardUuids.CHARACTERISTIC_DIGITAL_OUTPUT, ledSent, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        if (!submitted) {
            Toast.makeText(context, R.string.iodemo_alert_action_failed, Toast.LENGTH_SHORT).show();
        }
        Timber.d("write led  %02x submitted: %s", ledSent, submitted);
    }

    public void configureMotion() {

        if (gatt != null) {
            ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                Timber.d("configure sensor for: %s", device.getAddress());
                float wheelRadius = (preferenceManager.getPreferences().wheelRadius == 0) ? ThunderBoardPreferences.DEFAULT_WHEEL_RADIUS : preferenceManager.getPreferences().wheelRadius;
                ThunderBoardSensorMotion sensor = new ThunderBoardSensorMotion(preferenceManager.getPreferences().measureUnitType, wheelRadius);
                sensor.isNotificationEnabled = false;
                device.setSensorMotion(sensor);
                boolean submitted = BleUtils.setCharacteristicIndication(gatt, ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION, ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE, ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
                Timber.d("enable calibrate indication submitted: %s", submitted);
            }
        }
    }

    public void clearMotionNotifications() {
        if (gatt != null) {
            ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                ThunderBoardSensorMotion sensor = device.getSensorMotion();
                Timber.d("motion sensor is null: %s", (sensor == null));
                if (sensor != null) {
                    sensor.setClearCharacteristicsStatus();
                    notificationsMonitor.onNext(new NotificationEvent(device, NotificationEvent.ACTION_NOTIFICATIONS_CLEAR));
                }
            }
        }
    }

    public boolean readCscFeature() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_CSC, ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_FEATURE);
    }

    public boolean enableOrientation(boolean enable) {
        return BleUtils.setCharacteristicNotification(gatt, ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION, ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION, ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, enable);
    }

    public boolean enableAcceleration(boolean enable) {
        return BleUtils.setCharacteristicNotification(gatt, ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION, ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION, ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, enable);
    }

    public boolean enableCscMeasurement(boolean enable) {
        return BleUtils.setCharacteristicNotification(gatt, ThunderBoardUuids.UUID_SERVICE_CSC, ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_MEASUREMENT, ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, enable);
    }

    public void calibrate(int command) {
        boolean submitted = BleUtils.writeCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION, ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE, command, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        Timber.d("submitted: %s", submitted);
    }

    public void configureEnvironment() {

        if (gatt != null) {
            ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                ThunderBoardSensorEnvironment sensor = new ThunderBoardSensorEnvironment(preferenceManager.getPreferences().temperatureType);
                sensor.isNotificationEnabled = false;
                device.setSensorEnvironment(sensor);
            }
        }
    }

    public boolean readTemperature() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING, ThunderBoardUuids.UUID_CHARACTERISTIC_TEMPERATURE);
    }

    public boolean readHumidity() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING, ThunderBoardUuids.UUID_CHARACTERISTIC_HUMIDITY);
    }

    public boolean readUvIndex() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING, ThunderBoardUuids.UUID_CHARACTERISTIC_UV_INDEX);
    }

    public boolean readAmbientLight() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_AMBIENT_LIGHT, ThunderBoardUuids.UUID_CHARACTERISTIC_AMBIENT_LIGHT);
    }

    private void closeGatt() {

        if (gatt != null) {

            Timber.d("gatt device: %s, connected devices: %d", gatt.getDevice().getAddress(), bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).size());
            ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                device.clear();
            }

            if (BluetoothGatt.STATE_DISCONNECTED == bluetoothManager.getConnectionState(gatt.getDevice(), BluetoothProfile.GATT)) {
                Timber.d("close");
                gatt.close();
            } else {
                Timber.d("disconnect");
                gatt.disconnect();
            }

            gatt = null;
        }
        for (int i = 0; i < devices.size(); i++) {
            Timber.d("device: %s", devices.get(i).getAddress());
        }
    }

    ThunderBoardDevice getDeviceFromCache(String deviceAddress) {
        for (int i = 0; i < devices.size(); i++) {
            ThunderBoardDevice device = devices.get(i);
            if (device.getAddress().equals(deviceAddress)) {
                return device;
            }
        }
        return null;
    }

    void readRequiredCharacteristics() {
        if (gatt != null) {
            ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                if (device.isOriginalNameNull()) {
                    boolean submitted = BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_GENERIC_ACCESS, ThunderBoardUuids.UUID_CHARACTERISTIC_DEVICE_NAME);
                    Timber.d("read device name submitted: %s", submitted);
                } else if (device.isBatteryConfigured == null) {
                    boolean submitted = BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_BATTERY, ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL);
                    Timber.d("read battery level submitted: %s", submitted);
                } else if (device.isBatteryNotificationEnabled == null) {
                    boolean submitted = BleUtils.setCharacteristicNotification(gatt, ThunderBoardUuids.UUID_SERVICE_BATTERY, ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL, ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
                    Timber.d("enable battery notification submitted: %s", submitted);
                    if (!submitted) {
                        device.isBatteryNotificationEnabled = false;
                        readRequiredCharacteristics();
                    }
                } else if (device.getFirmwareVersion() == null) {
                    boolean submitted = BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_DEVICE_INFORMATION, ThunderBoardUuids.UUID_CHARACTERISTIC_FIRMWARE_REVISION);
                    Timber.d("read firmware submitted: %s", submitted);
                }
            }
        }
    }

    // Beaconing

    private ThunderBoardBootstrap regionBootstrap;
    private ThunderBoardPowerSaver backgroundPowerSaver;

    // RangeNotifier interface

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        Iterator<Beacon> iterator = beacons.iterator();

        while (iterator.hasNext()) {

            //EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
            Beacon beacon = iterator.next();
            Timber.d("beacon %s  is about: %f meters away.", beacon.toString(), beacon.getDistance());

            deviceFound(beacon);
            if (backgroundPowerSaver.isScannerActivityResumed()) {
                scanner.onNext(devices);
            } else if (SystemClock.elapsedRealtime() - backgroundPowerSaver.getScannerActivityDestroyedTimestamp() >
                    ThunderBoardPowerSaver.DELAY_NOTIFICATIONS_TIME_THRESHOLD
                    && backgroundPowerSaver.isApplicationBackgrounded()) {
                Timber.d("Sending notification.");
                sendNotification(beacon);
            }
        }
    }

    private void deviceFound(Beacon beacon) {
        ThunderBoardDevice old = getDeviceFromCache(beacon.getBluetoothAddress());
        if (old != null) {
            Timber.d("rssi: %d, has observers: %s, old state: %s", beacon.getRssi(), scanner.hasObservers(), old.getState());
            old.setRssi(beacon.getRssi());
        } else {
            Timber.d("appended, rssi: %d, has observers: %s", beacon.getRssi(), scanner.hasObservers());
            ThunderBoardDevice bleDevice = new ThunderBoardDevice(beacon);
            devices.add(bleDevice);
        }
    }

    public void addNotificationDevice(ThunderBoardDevice notificationDevice) {
        ThunderBoardDevice old = getDeviceFromCache(notificationDevice.getAddress());
        if (old == null) {
            devices.add(notificationDevice);
        }
    }

    private void sendNotification(Beacon beacon) {

        // check if notifications are enabled and allowed for the beacon
        if (!BleUtils.checkAllowNotifications(beacon.getBluetoothAddress(), preferenceManager.getPreferences())) {
            Timber.d("Notifications not allowed for : %s, address: %s", beacon.getBluetoothName(), beacon.getBluetoothAddress());
            return;
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText(String.format("%s is nearby.", beacon.getBluetoothName()))
                        .setSmallIcon(R.drawable.ic_stat_sl_beacon)
                        .setAutoCancel(true);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        Intent intent = new Intent(context, DemosSelectionActivity.class);
        intent.putExtra(ThunderBoardConstants.EXTRA_DEVICE_BEACON, beacon);
        stackBuilder.addParentStack(DemosSelectionActivity.class);
        stackBuilder.addNextIntent(intent);

        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(beacon.getId3().toInt(), builder.build());
    }

    private final CountDownTimer devicesNotFoundMonitor = new CountDownTimer(10000, 2000) {

        @Override
        public void onTick(long millisUntilFinished) {
            Timber.d("devices: %d", devices.size());
            if (devices.size() > 0) {
                this.cancel();
            }
        }

        @Override
        public void onFinish() {
            if (devices.size() == 0) {
                scanner.onNext(devices);
            }
        }
    };
}