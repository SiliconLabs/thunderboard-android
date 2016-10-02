package com.silabs.thunderboard.demos.ui;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.ble.ThunderBoardSensorIo;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.common.app.ThunderBoardType;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;
import com.silabs.thunderboard.demos.model.LedRGBState;
import com.silabs.thunderboard.web.CloudManager;

import javax.inject.Inject;

import rx.Subscriber;
import timber.log.Timber;

@ActivityScope
public class DemoIOPresenter extends BaseDemoPresenter {

    private static final String DEMO_ID = "io";

    private Integer ledSent;
    private Integer ledReceived;

    @Inject
    public DemoIOPresenter(BleManager bleManager, CloudManager cloudManager) {
        super(bleManager, cloudManager);
    }

    public void ledAction(int action) {
        Timber.d("action: %02x, streaming: %s", action, isStreaming);
        ledReceived = action;

        if (ledSent == null) {
            ledSent = ledReceived;
            bleManager.ledAction(ledSent);
        } else {
            // wait until cleared
        }
    }

    @Override
    protected void subscribe(String deviceAddress) {
        super.subscribe(deviceAddress);
        bleManager.configureIO();
    }

    @Override
    protected String getDemoId() {
        return DEMO_ID;
    }

    @Override
    public void stopStreaming() {
        super.stopStreaming();
        ledSent = null;
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
                ThunderBoardSensorIo sensor = device.getSensorIo();
                if ( sensor == null ) {
                    return;
                }

                if (cloudModelName == null) {
                    createCloudDeviceName(device.getSystemId());
                }

                DemoIOPresenter.this.sensor = sensor;
                if (device.isPowerSourceConfigured != null && device.isPowerSourceConfigured && viewListener != null) {
                    ((DemoIOViewListener) viewListener).setPowerSource(device.getPowerSource());
                }

                if (sensor.isSensorDataChanged && viewListener != null) {
                    ThunderBoardSensorIo.SensorData sensorData = sensor.getSensorData();
                    ((DemoIOViewListener) viewListener).setButton0State(sensorData.sw0);
                    ((DemoIOViewListener) viewListener).setButton1State(sensorData.sw1);
                    if (ledSent != null && !ledSent.equals(ledReceived)) {
                        Timber.d("1");
                        ledSent = ledReceived;
                        bleManager.ledAction(ledSent);
                    } else if (ledSent == null && ledReceived == null) {
                        ((DemoIOViewListener) viewListener).setLed0State(sensorData.ledb);
                        ((DemoIOViewListener) viewListener).setLed1State(sensorData.ledg);
                    } else {
                        Timber.d("3");
                        ledSent = null;
                    }
                    sensor.isSensorDataChanged = false;

                    if (bleManager.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE) {
                        if (sensorData.colorLed != null) {
                            ((DemoIOViewListener) viewListener).setColorLEDsValue(sensorData.colorLed);
                        } else {
                            bleManager.readColorLEDs();
                        }
                    }
                }

                if (bleManager.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE && sensor != null && sensor.getSensorData().colorLed == null) {
                    bleManager.readColorLEDs();
                }
            }
        };
    }

    public void setColorLEDs(LedRGBState ledRGBState) {
        bleManager.setColorLEDs(ledRGBState);
    }
}
