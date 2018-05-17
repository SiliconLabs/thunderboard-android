package com.silabs.thunderboard.demos.ui;

import android.os.Handler;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.ble.ThunderBoardSensor;
import com.silabs.thunderboard.ble.ThunderBoardSensorEnvironment;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.ble.model.ThunderBoardUuids;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;
import com.silabs.thunderboard.demos.model.EnvironmentEvent;
import com.silabs.thunderboard.demos.model.HallState;
import com.silabs.thunderboard.demos.model.NotificationEvent;
import com.silabs.thunderboard.web.CloudManager;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@ActivityScope
public class DemoEnvironmentPresenter extends BaseDemoPresenter {
    private static final String DEMO_ID = "environment";
    private static final int BLE_RETRY_DELAY = 100;
    private static final int BLE_PERIODIC_READ_DELAY = 100;
    private static final int NOTIFICATION_MAX_RETRIES = 3;

    private final PreferenceManager preferenceManager;
    private Subscriber<EnvironmentEvent> environmentSubscriber;
    private Subscriber<EnvironmentEvent> readSubscriber;
    private Subscriber<NotificationEvent> notificationSubscriber;
    private Handler handler;
    private int readStatus;
    private boolean notificationsHaveBeenSet;
    private int notificationRetries;

    @Inject
    public DemoEnvironmentPresenter(BleManager bleManager,
                                    CloudManager cloudManager,
                                    PreferenceManager preferenceManager) {
        super(bleManager, cloudManager);
        this.preferenceManager = preferenceManager;
    }

    @Override
    protected void subscribe(String deviceAddress) {
        super.subscribe(deviceAddress);

        notificationRetries = 0;
        notificationsHaveBeenSet = false;

        notificationSubscriber = onNotification();
        environmentSubscriber = onEnvironment();
        readSubscriber = onRead();

        bleManager.notificationsMonitor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(notificationSubscriber);
        bleManager.environmentDetector
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(environmentSubscriber);
        bleManager.environmentReadMonitor
                .delay(BLE_PERIODIC_READ_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(readSubscriber);

        bleManager.configureEnvironment();

        notificationsHaveBeenSet = false;

        handler = new Handler();
    }

    @Override
    protected void unsubscribe() {
        clearEnvironmentNotifications();
        notificationSubscriber.unsubscribe();
        environmentSubscriber.unsubscribe();
        readSubscriber.unsubscribe();
        handler.removeCallbacks(startPeriodicReads);
        super.unsubscribe();
    }

    @Override
    protected String getDemoId() {
        return DEMO_ID;
    }

    @Override
    protected Subscriber<ThunderBoardDevice> onDevice() {
        return new Subscriber<ThunderBoardDevice>() {
            @Override
            public void onCompleted() {
                Timber.d("completed");
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.d("error: %s", e.getMessage());
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
            }

            @Override
            public void onNext(ThunderBoardDevice device) {

                Timber.d("device: %s", device.getName());

                deviceAvailable = true;

                if (cloudModelName == null) {
                    createCloudDeviceName(device.getSystemId());
                }

                ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                DemoEnvironmentPresenter.this.sensor = sensor;

                if (device.isPowerSourceConfigured != null && device.isPowerSourceConfigured && viewListener != null) {
                    ((DemoEnvironmentListener) viewListener).setPowerSource(device.getPowerSource());
                }

                if (sensor != null && sensor.isSensorDataChanged && viewListener != null) {
                    ThunderBoardSensorEnvironment.SensorData sensorData = sensor.getSensorData();
                    ((DemoEnvironmentListener) viewListener).setHallState(sensorData.getHallState());
                    sensor.isSensorDataChanged = false;
                    readStatus = sensor.getReadStatus();
                    if (!isUnsubscribed()) {
                        unsubscribe();
                    }
                }
                startEnvironmentNotifications();
            }
        };
    }

    private Subscriber<NotificationEvent> onNotification() {
        return new Subscriber<NotificationEvent>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(NotificationEvent event) {
                if (ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE.equals(event.characteristicUuid)) {
                    if (NotificationEvent.ACTION_NOTIFICATIONS_SET == event.action) {
                        notificationsHaveBeenSet = true;
                    }
                    readStatus = 0;
                    bleManager.readHallState();
                }
            }
        };
    }

    private Runnable startPeriodicReads = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(startPeriodicReads);
            readStatus = 0x01;
            boolean submitted = bleManager.readTemperature();
            ((DemoEnvironmentListener) viewListener).setTemperatureEnabled(submitted);
            if (!submitted) {
                handler.postDelayed(startPeriodicReads, BLE_RETRY_DELAY);
            }
        }
    };

    private Subscriber<EnvironmentEvent> onRead() {
        return new Subscriber<EnvironmentEvent>() {
            @Override
            public void onCompleted() {
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
            }

            @Override
            public void onNext(EnvironmentEvent environmentEvent) {
                ThunderBoardSensorEnvironment sensor = environmentEvent.device.getSensorEnvironment();
                if (sensor == null) {
                    return;
                }
                ThunderBoardSensorEnvironment.SensorData sensorData = sensor.getSensorData();
                if (ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE.equals(environmentEvent.characteristicUuid)) {
                    ((DemoEnvironmentListener) viewListener).setHallState(sensorData.getHallState());
                    handler.post(startPeriodicReads);
                    return;
                }
                boolean submitted;
                switch (readStatus & 0x55555) { // readStatus increases 4 times with every field
                    // temperature submitted already
                    case 0x01:
                        ((DemoEnvironmentListener) viewListener).setTemperature(sensorData.getTemperature(),
                                                                                sensor.TEMPERATURE_TYPE);
                        submitted = bleManager.readHumidity();
                        ((DemoEnvironmentListener) viewListener).setHumidityEnabled(submitted);
                        readStatus |= 0x04;
                        Timber.d("readStatus: %02x, submitted: %s", readStatus, submitted);
                        break;
                    // temperature and humidity submitted already
                    case 0x05:
                        ((DemoEnvironmentListener) viewListener).setHumidity(sensorData.getHumidity());
                        submitted = bleManager.readUvIndex();
                        ((DemoEnvironmentListener) viewListener).setUvIndexEnabled(submitted);
                        readStatus |= 0x10;
                        Timber.d("readStatus: %02x, submitted: %s", readStatus, submitted);
                        break;
                    // temperature and humidity and uv index submitted already
                    case 0x15:
                        ((DemoEnvironmentListener) viewListener).setUvIndex(sensorData.getUvIndex());
                        submitted = bleManager.readAmbientLightReact() || bleManager.readAmbientLightSense();
                        ((DemoEnvironmentListener) viewListener).setAmbientLightEnabled(submitted);
                        readStatus |= 0x40;
                        Timber.d("readStatus: %02x, submitted: %s", readStatus, submitted);
                        break;
                    // temperature, humidity, uv index, and ambient light submitted already
                    case 0x55:
                        ((DemoEnvironmentListener) viewListener).setAmbientLight(sensorData.getAmbientLight());
                        submitted = bleManager.readSoundLevel();
                        ((DemoEnvironmentListener) viewListener).setSoundLevelEnabled(submitted);
                        readStatus |= 0x100;
                        Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                        break;
                    // temperature, humidity, uv index, ambient light, and sound level submitted already
                    case 0x155:
                        ((DemoEnvironmentListener) viewListener).setSoundLevel(sensorData.getSound());
                        submitted = bleManager.readPressure();
                        ((DemoEnvironmentListener) viewListener).setPressureEnabled(submitted);
                        readStatus |= 0x400;
                        Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                        break;
                    case 0x555:
                        ((DemoEnvironmentListener) viewListener).setPressure(sensorData.getPressure());
                        submitted = bleManager.readCO2Level();
                        ((DemoEnvironmentListener) viewListener).setCO2LevelEnabled(submitted);
                        readStatus |= 0x1000;
                        Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                        break;
                    case 0x1555:
                        ((DemoEnvironmentListener) viewListener).setCO2Level(sensorData.getCO2Level());
                        submitted = bleManager.readTVOCLevel();
                        ((DemoEnvironmentListener) viewListener).setTVOCLevelEnabled(submitted);
                        readStatus |= 0x4000;
                        Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                        break;
                    case 0x5555:
                        ((DemoEnvironmentListener) viewListener).setTVOCLevel(sensorData.getTVOCLevel());
                        submitted = bleManager.readHallStrength();
                        ((DemoEnvironmentListener) viewListener).setHallStrengthEnabled(submitted);
                        readStatus |= 0x10000;
                        Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                        break;
                    case 0x15555:
                        ((DemoEnvironmentListener) viewListener).setHallStrength(sensorData.getHallStrength());
                        submitted = bleManager.readTemperature();
                        ((DemoEnvironmentListener) viewListener).setTemperatureEnabled(submitted);
                        readStatus = 0;
                        Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                        break;
                    default:
                        submitted = false;
                        break;
                }
                if (!submitted) {
                    handler.postDelayed(startPeriodicReads, 200);
                }
            }
        };
    }

    private Subscriber<EnvironmentEvent> onEnvironment() {
        return new Subscriber<EnvironmentEvent>() {
            @Override
            public void onCompleted() {
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
            }

            @Override
            public void onError(Throwable e) {
                if (!isUnsubscribed()) {
                    unsubscribe();
                }
            }

            @Override
            public void onNext(EnvironmentEvent event) {
                ThunderBoardSensorEnvironment sensor = event.device.getSensorEnvironment();
                if (sensor == null) {
                    return;
                }
                ThunderBoardSensorEnvironment.SensorData sensorData = sensor.getSensorData();
                if (ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE.equals(event.characteristicUuid)) {
                    ((DemoEnvironmentListener) viewListener).setHallState(sensorData.getHallState());
                }
            }
        };
    }

    void onHallStateClick() {
        final ThunderBoardSensorEnvironment.SensorData sensorData = sensor != null ?
                ((ThunderBoardSensorEnvironment) sensor).getSensorData() : null;
        if (sensorData != null && sensorData.getHallState() == HallState.TAMPERED) {
            bleManager.resetHallEffectTamper();
        } else {
            Timber.d("onHallStateClick had no effect: current state is not tamper.");
        }
    }

    public void startEnvironmentNotifications() {
        if (notificationsHaveBeenSet) {
            return;
        }
        boolean submitted = bleManager.enableHallStateMeasurement(true);
        ((DemoEnvironmentListener) viewListener).setHallStateEnabled(submitted);
        Timber.d("start environment notifications returned %s", submitted);
        if (!submitted) {
            if (notificationRetries > NOTIFICATION_MAX_RETRIES) {
                Timber.e("Could not start environment notifications, retry limit reached");
                handler.post(startPeriodicReads);
                return;
            }
            notificationRetries++;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startEnvironmentNotifications();
                }
            }, BLE_RETRY_DELAY);
        }
    }

    public void clearEnvironmentNotifications() {
        Timber.d("stop environment notifications");
        bleManager.clearHallStateNotifications();
    }
}
