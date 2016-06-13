package com.silabs.thunderboard.demos.ui;

import android.os.CountDownTimer;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.ble.ThunderBoardSensorEnvironment;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;
import com.silabs.thunderboard.web.CloudManager;

import javax.inject.Inject;

import rx.Subscriber;
import timber.log.Timber;

@ActivityScope
public class DemoEnvironmentPresenter extends BaseDemoPresenter {
    private static final String DEMO_ID = "environment";

    private final PreferenceManager preferenceManager;

    private int readStatus;

    @Inject
    public DemoEnvironmentPresenter(BleManager bleManager, CloudManager cloudManager, PreferenceManager preferenceManager) {
        super(bleManager, cloudManager);
        this.preferenceManager = preferenceManager;
    }

    @Override
    protected void subscribe(String deviceAddress) {
        super.subscribe(deviceAddress);
        bleManager.configureEnvironment();
        boolean submitted = bleManager.readTemperature();
        ((DemoEnvironmentListener) viewListener).setTemperatureEnabled(submitted);
        readTimer.start();
    }

    @Override
    protected void unsubscribe() {
        readTimer.cancel();
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
                    createCloudDeviceName(device.getName());
                }

                ThunderBoardSensorEnvironment sensor = device.getSensorEnvironment();
                DemoEnvironmentPresenter.this.sensor = sensor;

                if (sensor != null && sensor.isSensorDataChanged && viewListener != null) {
                    ThunderBoardSensorEnvironment.SensorData sensorData = sensor.getSensorData();
                    float t = sensorData.getTemperature();
                    if (sensor.TEMPERATURE_TYPE == ThunderBoardPreferences.TEMP_FAHRENHEIT) {
                        t = t * 1.8f + 32f;
                    }
                    ((DemoEnvironmentListener) viewListener).setTemperature(t, sensor.TEMPERATURE_TYPE);
                    ((DemoEnvironmentListener) viewListener).setHumidity(sensorData.getHumidity());
                    ((DemoEnvironmentListener) viewListener).setUvIndex(sensorData.getUvIndex());
                    ((DemoEnvironmentListener) viewListener).setAmbientLight(sensorData.getAmbientLight());
                    sensor.isSensorDataChanged = false;
                    readStatus = sensor.getReadStatus();

                    pushToCloud();
                }
            }
        };
    }

    private final CountDownTimer readTimer = new CountDownTimer(5000, 300) {

        @Override
        public void onTick(long millisUntilFinished) {

            // skip the first tick
            if (5000L - millisUntilFinished < 300) {
                return;
            }

            boolean submitted;
            switch (readStatus & 0x055) {
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
                    submitted = bleManager.readAmbientLight();
                    ((DemoEnvironmentListener) viewListener).setAmbientLightEnabled(submitted);
                    readStatus |= 0x40;
                    Timber.d("readStatus: %02x, submitted: %s", readStatus, submitted);
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
}
