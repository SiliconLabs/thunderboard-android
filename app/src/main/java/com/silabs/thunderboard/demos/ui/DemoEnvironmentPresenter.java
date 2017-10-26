package com.silabs.thunderboard.demos.ui;

import android.os.CountDownTimer;
import android.os.Handler;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.ble.ThunderBoardSensorEnvironment;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.ble.model.ThunderBoardUuids;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;
import com.silabs.thunderboard.demos.model.EnvironmentEvent;
import com.silabs.thunderboard.demos.model.HallState;
import com.silabs.thunderboard.web.CloudManager;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@ActivityScope
public class DemoEnvironmentPresenter extends BaseDemoPresenter {
    private static final String DEMO_ID = "environment";

    private final PreferenceManager preferenceManager;
    private Subscriber<EnvironmentEvent> environmentSubscriber;
    private int readStatus;

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
        environmentSubscriber = onEnvironment();
        bleManager.environmentDetector
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(environmentSubscriber);

        bleManager.configureEnvironment();
        boolean submitted = bleManager.readTemperature();
        ((DemoEnvironmentListener) viewListener).setTemperatureEnabled(submitted);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startEnvironmentNotifications();
                readTimer.start();
            }
        }, 500);
    }

    @Override
    protected void unsubscribe() {
        readTimer.cancel();
        clearEnvironmentNotifications();
        environmentSubscriber.unsubscribe();
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
                    ((DemoEnvironmentListener) viewListener).setTemperature(sensorData.getTemperature(),
                                                                            sensor.TEMPERATURE_TYPE);
                    ((DemoEnvironmentListener) viewListener).setHumidity(sensorData.getHumidity());
                    ((DemoEnvironmentListener) viewListener).setUvIndex(sensorData.getUvIndex());
                    ((DemoEnvironmentListener) viewListener).setAmbientLight(sensorData.getAmbientLight());
                    ((DemoEnvironmentListener) viewListener).setSoundLevel(sensorData.getSound());
                    ((DemoEnvironmentListener) viewListener).setPressure(sensorData.getPressure());
                    ((DemoEnvironmentListener) viewListener).setCO2Level(sensorData.getCO2Level());
                    ((DemoEnvironmentListener) viewListener).setTVOCLevel(sensorData.getTVOCLevel());
                    ((DemoEnvironmentListener) viewListener).setHallStrength(sensorData.getHallStrength());
                    ((DemoEnvironmentListener) viewListener).setHallState(sensorData.getHallState());
                    sensor.isSensorDataChanged = false;
                    readStatus = sensor.getReadStatus();
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
                    Timber.d("Hall state event: %s", String.valueOf(sensorData.getHallState()));
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

    private final CountDownTimer readTimer = new CountDownTimer(5000, 300) {

        @Override
        public void onTick(long millisUntilFinished) {

            // skip the first tick
            if (5000L - millisUntilFinished < 300) {
                return;
            }
            boolean submitted;
            switch (readStatus & 0x55555) { // readStatus increases 4 times with every field
                // temperature submitted already
                case 0x01:
                    submitted = bleManager.readHumidity();
                    ((DemoEnvironmentListener) viewListener).setHumidityEnabled(submitted);
                    readStatus |= 0x04;
                    Timber.d("readStatus: %02x, submitted: %s", readStatus, submitted);
                    break;
                // temperature and humidity submitted already
                case 0x05:
                    submitted = bleManager.readUvIndex();
                    ((DemoEnvironmentListener) viewListener).setUvIndexEnabled(submitted);
                    readStatus |= 0x10;
                    Timber.d("readStatus: %02x, submitted: %s", readStatus, submitted);
                    break;
                // temperature and humidity and uv index submitted already
                case 0x15:
                    submitted = bleManager.readAmbientLightReact() || bleManager.readAmbientLightSense();
                    ((DemoEnvironmentListener) viewListener).setAmbientLightEnabled(submitted);
                    readStatus |= 0x40;
                    Timber.d("readStatus: %02x, submitted: %s", readStatus, submitted);
                    break;
                // temperature, humidity, uv index, and ambient light submitted already
                case 0x55:
                    submitted = bleManager.readSoundLevel();
                    ((DemoEnvironmentListener) viewListener).setSoundLevelEnabled(submitted);
                    readStatus |= 0x100;
                    Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                    break;
                // temperature, humidity, uv index, ambient light, and sound level submitted already
                case 0x155:
                    submitted = bleManager.readPressure();
                    ((DemoEnvironmentListener) viewListener).setPressureEnabled(submitted);
                    readStatus |= 0x400;
                    Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                    break;
                case 0x555:
                    submitted = bleManager.readCO2Level();
                    ((DemoEnvironmentListener) viewListener).setCO2LevelEnabled(submitted);
                    readStatus |= 0x1000;
                    Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                    break;
                case 0x1555:
                    submitted = bleManager.readTVOCLevel();
                    ((DemoEnvironmentListener) viewListener).setTVOCLevelEnabled(submitted);
                    readStatus |= 0x4000;
                    Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                    break;
                case 0x5555:
                    submitted = bleManager.readHallStrength();
                    ((DemoEnvironmentListener) viewListener).setHallStrengthEnabled(submitted);
                    readStatus |= 0x10000;
                    Timber.d("readStatus: %04x, submitted: %s", readStatus, submitted);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onFinish() {
            readStatus = 0;
            boolean submitted = bleManager.readTemperature();
            ((DemoEnvironmentListener) viewListener).setTemperatureEnabled(submitted);
            readStatus |= 0x01;
            Timber.d("readStatus: %02x, submitted: %s", readStatus, submitted);
            start();
        }
    };

    public void startEnvironmentNotifications() {
        boolean submitted = bleManager.enableHallStateMeasurement(true);
        ((DemoEnvironmentListener) viewListener).setHallStateEnabled(submitted);
        Timber.d("start environment notifications returned %s", submitted);
    }

    public void clearEnvironmentNotifications() {
        Timber.d("stop environment notifications");
        bleManager.clearHallStateNotifications();
    }
}
