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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.ble.model.ThunderBoardUuids;
import com.silabs.thunderboard.ble.util.BleUtils;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.app.ThunderBoardType;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.injection.qualifier.ForApplication;
import com.silabs.thunderboard.demos.model.EnvironmentEvent;
import com.silabs.thunderboard.demos.model.HallState;
import com.silabs.thunderboard.demos.model.LedRGBState;
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Provides methods for the application to interact with the Android BLE system.
 */
@Singleton
public class BleManager implements RangeNotifier {

    public final PublishSubject<List<ThunderBoardDevice>> scanner = PublishSubject.create();
    public final BehaviorSubject<ThunderBoardDevice> selectedDeviceMonitor = BehaviorSubject.create();
    public final BehaviorSubject<StatusEvent> selectedDeviceStatusMonitor = BehaviorSubject.create();
    public final PublishSubject<MotionEvent> motionDetector = PublishSubject.create();
    public final PublishSubject<EnvironmentEvent> environmentDetector = PublishSubject.create();
    public final BehaviorSubject<NotificationEvent> notificationsMonitor = BehaviorSubject.create();

    private final Context context;
    private final PreferenceManager preferenceManager;
    private final BluetoothManager bluetoothManager;
    private final BluetoothAdapter bluetoothAdapter;

    private final List<ThunderBoardDevice> devices = new ArrayList<>();

    private final BeaconManager beaconManager;
    private final GattManager gattManager;

    // Should be cleared/null when connection sessions begins
    private BluetoothGatt gatt;
    private Subscriber<NotificationEvent> configureMotionSubscriber;
    private Subscriber<NotificationEvent> configureEnvironmentSubscriber;
    private Subscriber<ThunderBoardDevice> configureIOSubscriber;

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
            if (tbd == null) {
                tbd = new ThunderBoardDevice(device, 0);
                devices.add(tbd);
            }
            tbd.setState(BluetoothProfile.STATE_CONNECTING);
            selectedDeviceMonitor.onNext(tbd);
            selectedDeviceStatusMonitor.onNext(new StatusEvent(tbd));
            return;
        }
    }

    // Demo Configuration interfaces
    public void configureIO() {
        if (gatt != null) {
            final ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                Timber.d("configure sensor for: %s", device.getAddress());
                ThunderBoardSensorIo sensor = new ThunderBoardSensorIo();
                sensor.isNotificationEnabled = false;
                device.setSensorIo(sensor);

                unsubscribeConfigureIOSubscriber();
                this.configureIOSubscriber = enableConfigureIO();
                this.selectedDeviceMonitor
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        // wait a second before subscribing to notification
                        .delay(1000, TimeUnit.MILLISECONDS)
                        .subscribe(this.configureIOSubscriber);
                configureIOSettings();
            }
        }
    }

    private void configureIOSettings() {
        BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_AUTOMATION_IO, ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL);
    }

    private Subscriber<ThunderBoardDevice> enableConfigureIO() {
        return new Subscriber<ThunderBoardDevice>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ThunderBoardDevice device) {

                boolean submitted =
                        BleUtils.setCharacteristicNotification(gatt,
                                ThunderBoardUuids.UUID_SERVICE_AUTOMATION_IO,
                                ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL,
                                ThunderBoardUuids
                                        .UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
                                true);
                if (!submitted) {
                    ThunderBoardSensorIo sensor = device.getSensorIo();
                    if (sensor != null) {
                        sensor.isNotificationEnabled = false;
                        Toast.makeText(context, R.string.iodemo_alert_configuration_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                }

                unsubscribeConfigureIOSubscriber();
            }
        };
    }

    private void unsubscribeConfigureIOSubscriber() {
        if (configureIOSubscriber != null && !configureIOSubscriber.isUnsubscribed()) {
            configureIOSubscriber.unsubscribe();
        }
        configureIOSubscriber = null;
    }

    // Demo IO actions
    public void ledAction(int ledSent) {
        boolean submitted = BleUtils.writeCharacteristics(
                gatt,
                ThunderBoardUuids.UUID_SERVICE_AUTOMATION_IO,
                ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL,
                ledSent, BluetoothGattCharacteristic.FORMAT_UINT8,
                0
        );
        if (!submitted) {
            Timber.i(context.getString(R.string.iodemo_alert_action_failed));
        }
        Timber.d("write led  %02x submitted: %s", ledSent, submitted);
    }

    private void configureMotionCalibrate(boolean enabled) {
        boolean submitted = BleUtils.setCharacteristicIndications(
                gatt,
                ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION,
                ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE,
                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, enabled);
        Timber.d("%s acceleration indication submitted: %s", enabled, submitted);
    }

    private Subscriber<NotificationEvent> enableConfigureMotion(final ThunderBoardDevice device) {
        return new Subscriber<NotificationEvent>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(NotificationEvent notificationEvent) {
                if (device.isCalibrateNotificationEnabled == null || !device.isCalibrateNotificationEnabled) {
                    configureMotionCalibrate(true);
                    return;
                }

                if (device.isAccelerationNotificationEnabled == null || !device.isAccelerationNotificationEnabled) {
                    boolean submitted = enableAcceleration(true);
                    Timber.d("enable acceleration indication submitted: %s", submitted);
                    return;
                }

                if (device.isOrientationNotificationEnabled == null || !device.isOrientationNotificationEnabled) {
                    boolean submitted = enableOrientation(true);
                    Timber.d("enable orientation indication submitted: %s", submitted);
                    return;
                }

                if (device.isRotationNotificationEnabled == null || !device.isRotationNotificationEnabled) {
                    boolean submitted = enableCscMeasurement(true);
                    Timber.d("enable rotation notification submitted: %s", submitted);
                    return;
                }

                if (getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE) {
                    BleManager.this.readColorLEDs();
                }

                unsubscribeConfigureMotionSubscriber();
            }
        };
    }

    public void configureMotion() {
        if (gatt != null) {
            final ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                Timber.d("configure sensor for: %s", device.getAddress());
                float wheelRadius = (preferenceManager.getPreferences().wheelRadius == 0) ? ThunderBoardPreferences.DEFAULT_WHEEL_RADIUS : preferenceManager.getPreferences().wheelRadius;
                ThunderBoardSensorMotion sensor = new ThunderBoardSensorMotion(preferenceManager.getPreferences().measureUnitType, wheelRadius);
                sensor.isNotificationEnabled = false;
                device.setSensorMotion(sensor);

                unsubscribeConfigureMotionSubscriber();
                this.configureMotionSubscriber = enableConfigureMotion(device);
                this.notificationsMonitor
                        .delay(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this.configureMotionSubscriber);
                configureMotionCalibrate(true);
            }
        }
    }

    private void unsubscribeConfigureMotionSubscriber() {
        if (configureMotionSubscriber != null && !configureMotionSubscriber.isUnsubscribed()) {
            configureMotionSubscriber.unsubscribe();
        }
        configureMotionSubscriber = null;
    }

    private void clearCalibrateNotification() {
        if (gatt != null) {
            boolean submittedCalibrate = BleUtils.unsetCharacteristicNotification(
                    gatt,
                    ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION,
                    ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE,
                    ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, false);
            Timber.d("disable calibration indication submitted: %s", submittedCalibrate);
        }
    }

    private void clearAccelerationNotification() {
        if (gatt != null) {
            boolean submittedAcceleration = BleUtils.unsetCharacteristicNotification(
                    gatt,
                    ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION,
                    ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION,
                    ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, false);
            Timber.d("disable acceleration indication submitted: %s", submittedAcceleration);
        }
    }

    private void clearOrientationNotification() {
        if (gatt != null) {
            boolean submittedOrientation = BleUtils.unsetCharacteristicNotification(
                    gatt,
                    ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION,
                    ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION,
                    ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, false);
            Timber.d("disable orientation indication submitted: %s", submittedOrientation);
        }
    }

    private void clearRotationNotification() {
        enableCscMeasurement(false);
    }


    public void clearMotionNotifications() {
        handleClearMotionNotifications(null);
    }

    public void clearHallStateNotifications() {
        if (gatt != null) {
            boolean submitted = BleUtils.unsetCharacteristicNotification(
                    gatt,
                    ThunderBoardUuids.UUID_SERVICE_HALL_EFFECT,
                    ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE,
                    ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, false);
            Timber.d("disable hall state submitted: %s", submitted);
        }
    }

    /**
     *
     * @param notificationEvent
     * @return returns true if clear is in progress, false otherwise
     */
    public boolean handleClearMotionNotifications(@Nullable NotificationEvent notificationEvent) {
        if (gatt != null) {
            if (notificationEvent == null) {
                clearCalibrateNotification();
                return true;
            }

            // hacky way of chaining order of operations together :-(
            // TODO REFACTOR
            if (NotificationEvent.ACTION_NOTIFICATIONS_CLEAR == notificationEvent.action) {
                if (ThunderBoardUuids.UUID_CHARACTERISTIC_CALIBRATE.equals(
                        notificationEvent.characteristicUuid)) {
                    clearAccelerationNotification();
                    return true;
                }
                if (ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION.equals(
                        notificationEvent.characteristicUuid)) {
                    clearOrientationNotification();
                    return true;
                }

                if (ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION.equals(
                        notificationEvent.characteristicUuid)) {
                    clearRotationNotification();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean readCscFeature() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_CSC, ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_FEATURE);
    }

    public boolean enableOrientation(boolean enabled) {
        return BleUtils.setCharacteristicNotification(
                gatt,
                ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION,
                ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION,
                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, enabled);
    }

    public boolean enableAcceleration(boolean enabled) {
        return BleUtils.setCharacteristicNotification(
                gatt,
                ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION,
                ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION,
                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, enabled);
    }

    public boolean enableCscMeasurement(boolean enabled) {
        return BleUtils.setCharacteristicNotification(
                gatt,
                ThunderBoardUuids.UUID_SERVICE_CSC,
                ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_MEASUREMENT,
                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, enabled);
    }

    public  boolean enableHallStateMeasurement(boolean enabled) {
        return BleUtils.setCharacteristicNotification(
                gatt,
                ThunderBoardUuids.UUID_SERVICE_HALL_EFFECT,
                ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE,
                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, enabled);
    }

    public boolean startCalibration() {
        boolean submitted = BleUtils.writeCharacteristics(gatt, ThunderBoardUuids
                .UUID_SERVICE_ACCELERATION_ORIENTATION, ThunderBoardUuids
                .UUID_CHARACTERISTIC_CALIBRATE, 0x01, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        Timber.d("submitted: %s", submitted);
        return submitted;
    }

    public boolean resetOrientation() {
        boolean submitted = BleUtils.writeCharacteristics(gatt, ThunderBoardUuids
                .UUID_SERVICE_ACCELERATION_ORIENTATION, ThunderBoardUuids
                .UUID_CHARACTERISTIC_CALIBRATE, 0x02, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
        Timber.d("submitted: %s", submitted);
        return submitted;
    }

    public boolean resetRevolutions() {
        boolean submitted = BleUtils.writeCharacteristics(gatt, ThunderBoardUuids
                .UUID_SERVICE_ACCELERATION_ORIENTATION, ThunderBoardUuids
                .UUID_CHARACTERISTIC_CSC_CONTROL_POINT, 0x01, BluetoothGattCharacteristic
                .FORMAT_UINT8, 0);
        Timber.d("submitted: %s", submitted);
        return submitted;
    }

    public boolean resetHallEffectTamper() {
        boolean submitted = BleUtils.writeCharacteristics(gatt,
                                                          ThunderBoardUuids.UUID_SERVICE_HALL_EFFECT,
                                                          ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_CONTROL_POINT,
                                                          HallState.OPENED,
                                                          BluetoothGattCharacteristic.FORMAT_UINT16,
                                                          0);
        Timber.d("submitted: %s", submitted);
        return submitted;
    }

    public void setColorLEDs(LedRGBState ledRGBState) {
        ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
        if (device != null && device.getSensorIo() != null && device.getSensorIo().getSensorData() != null) {
            device.getSensorIo().getSensorData().colorLed = null;
        }

        byte[] bytes = new byte[4];

        bytes[0] = (byte) (ledRGBState.color.blue & 0xff);
        bytes[1] = (byte) (ledRGBState.color.green & 0xff);
        bytes[2] = (byte) (ledRGBState.color.red & 0xff);
        bytes[3] = (byte) (ledRGBState.on ? 0x0f : 0x00);

        int value = ByteBuffer.wrap(bytes).getInt();
        BleUtils.writeCharacteristics(gatt,
                ThunderBoardUuids.UUID_SERVICE_USER_INTERFACE,
                ThunderBoardUuids.UUID_CHARACTERISTIC_RGB_LEDS,
                value,
                BluetoothGattCharacteristic.FORMAT_UINT32, 0);
    }

    private Subscriber<NotificationEvent> enableConfigureEnvironment(final ThunderBoardDevice device) {
        return new Subscriber<NotificationEvent>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Timber.d(e.getMessage());
            }

            @Override
            public void onNext(NotificationEvent notificationEvent) {
                if (device.isHallStateNotificationEnabled == null || !device.isHallStateNotificationEnabled) {
                    boolean submitted = enableHallStateMeasurement(true);
                    Timber.d("enable hall state notification submitted: %s", submitted);
                    return;
                }
                unsubscribeConfigureEnvironmentSubscriber();
            }
        };
    }

    public void configureEnvironment() {
        if (gatt != null) {
            ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
            if (device != null) {
                ThunderBoardSensorEnvironment sensor = new ThunderBoardSensorEnvironment(preferenceManager.getPreferences().temperatureType);
                sensor.isNotificationEnabled = false;
                device.setSensorEnvironment(sensor);

                unsubscribeConfigureEnvironmentSubscriber();
                this.configureEnvironmentSubscriber = enableConfigureEnvironment(device);
                this.notificationsMonitor
                        .delay(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(this.configureEnvironmentSubscriber);
            }
        }
    }

    private void unsubscribeConfigureEnvironmentSubscriber() {
        if (configureEnvironmentSubscriber != null && !configureEnvironmentSubscriber.isUnsubscribed()) {
            configureEnvironmentSubscriber.unsubscribe();
        }
        configureEnvironmentSubscriber = null;
    }

    public boolean readTemperature() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING,
                ThunderBoardUuids.UUID_CHARACTERISTIC_TEMPERATURE);
    }

    public boolean readHumidity() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING,
                ThunderBoardUuids.UUID_CHARACTERISTIC_HUMIDITY);
    }

    public boolean readUvIndex() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING,
                ThunderBoardUuids.UUID_CHARACTERISTIC_UV_INDEX);
    }

    public boolean readAmbientLightReact() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_AMBIENT_LIGHT,
                ThunderBoardUuids.UUID_CHARACTERISTIC_AMBIENT_LIGHT_REACT);
    }

    public boolean readAmbientLightSense() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING,
                ThunderBoardUuids.UUID_CHARACTERISTIC_AMBIENT_LIGHT_REACT);
    }

    public boolean readSoundLevel() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING,
                ThunderBoardUuids.UUID_CHARACTERISTIC_SOUND_LEVEL);
    }

    public boolean readPressure() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING,
                ThunderBoardUuids.UUID_CHARACTERISTIC_PRESSURE);
    }

    public boolean readCO2Level() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_INDOOR_AIR_QUALITY,
                ThunderBoardUuids.UUID_CHARACTERISTIC_CO2_READING);
    }

    public boolean readTVOCLevel() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_INDOOR_AIR_QUALITY,
                ThunderBoardUuids.UUID_CHARACTERISTIC_TVOC_READING);
    }

    public boolean readColorLEDs() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_USER_INTERFACE,
                ThunderBoardUuids.UUID_CHARACTERISTIC_RGB_LEDS);
    }

    public boolean readHallStrength() {
        return BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_HALL_EFFECT,
                ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_FIELD_STRENGTH);
        }


    public ThunderBoardType getThunderBoardType() {
        if (gatt == null) {
            return ThunderBoardType.UNKNOWN;
        }
        ThunderBoardDevice device = getDeviceFromCache(gatt.getDevice().getAddress());
        return device == null ? ThunderBoardType.UNKNOWN : device.getThunderBoardType();
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
                boolean readSuccessful = false;
                if (device.isOriginalNameNull()) {
                    readSuccessful = BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_GENERIC_ACCESS, ThunderBoardUuids.UUID_CHARACTERISTIC_DEVICE_NAME);
                    Timber.d("read device name submitted: %s", readSuccessful);
                } else if (device.getModelNumber() == null) {
                    readSuccessful = BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_DEVICE_INFORMATION, ThunderBoardUuids.UUID_CHARACTERISTIC_MODEL_NUMBER);
                    Timber.d("read model number submitted: %s", readSuccessful);
                } else if (device.getSystemId() == null) {
                    readSuccessful = BleUtils.readCharacteristic(gatt, ThunderBoardUuids
                            .UUID_SERVICE_DEVICE_INFORMATION, ThunderBoardUuids.UUID_CHARACTERISTIC_SYSTEM_ID);
                    Timber.d("read system id submitted: %s", readSuccessful);
                }
                else if (device.isBatteryConfigured == null) {
                    readSuccessful = BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_BATTERY, ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL);
                    if (!readSuccessful) {
                        device.isBatteryConfigured = false;
                    }
                    Timber.d("read battery level submitted: %s", readSuccessful);
                } else if (device.isBatteryNotificationEnabled == null) {
                    readSuccessful = BleUtils.setCharacteristicNotification(gatt, ThunderBoardUuids.UUID_SERVICE_BATTERY, ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL, ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
                    Timber.d("enable battery notification submitted: %s", readSuccessful);
                    if (!readSuccessful) {
                        device.isBatteryNotificationEnabled = false;
                    }
                } else if (device.isPowerSourceConfigured == null) {
                    readSuccessful= BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_POWER_MANAGEMENT, ThunderBoardUuids.UUID_CHARACTERISTIC_POWER_SOURCE);
                    if (!readSuccessful) {
                        device.isPowerSourceConfigured = false;
                    }
                    Timber.d("read power source submitted: %s", readSuccessful);
                } else if (device.isPowerSourceNotificationEnabled == null) {
                    readSuccessful = BleUtils.setCharacteristicNotification(gatt,
                            ThunderBoardUuids.UUID_SERVICE_POWER_MANAGEMENT,
                            ThunderBoardUuids.UUID_CHARACTERISTIC_POWER_SOURCE,
                            ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
                    Timber.d("enable power source notification submitted: %s", readSuccessful);
                    if (!readSuccessful) {
                        device.isPowerSourceNotificationEnabled = false;
                    }
                } else if (device.getFirmwareVersion() == null) {
                    readSuccessful = BleUtils.readCharacteristic(gatt, ThunderBoardUuids.UUID_SERVICE_DEVICE_INFORMATION, ThunderBoardUuids.UUID_CHARACTERISTIC_FIRMWARE_REVISION);
                    Timber.d("read firmware submitted: %s", readSuccessful);
                } else {
                    // out of items to read
                    readSuccessful = true;
                }

                if (!readSuccessful) {
                    readRequiredCharacteristics();
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
