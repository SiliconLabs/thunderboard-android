package com.silabs.thunderboard.demos.ui;

import android.os.CountDownTimer;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.ble.ThunderBoardSensorMotion;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;
import com.silabs.thunderboard.demos.model.MotionEvent;
import com.silabs.thunderboard.demos.model.NotificationEvent;
import com.silabs.thunderboard.web.CloudManager;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

@ActivityScope
public class DemoMotionPresenter extends BaseDemoPresenter {
    private static final String DEMO_ID = "motion";

    private PublishSubject<MotionEvent> motionDetector;
    private Subscriber<MotionEvent> motionSubscriber;

    private BehaviorSubject<NotificationEvent> notificationsMonitor;
    private Subscriber<NotificationEvent> notificationsSubscriber;

    private boolean isCalibrating;

    @Inject
    public DemoMotionPresenter(BleManager bleManager, CloudManager cloudManager) {
        super(bleManager, cloudManager);
    }

    @Override
    protected void subscribe(String deviceAddress) {
        super.subscribe(deviceAddress);
        motionSubscriber = onMotion();
        motionDetector = bleManager.motionDetector;
        motionDetector
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(motionSubscriber);
        notificationsSubscriber = onNotification();
        notificationsMonitor = bleManager.notificationsMonitor;
        notificationsMonitor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(notificationsSubscriber);
        bleManager.configureMotion();
    }

    @Override
    protected void unsubscribe() {
        super.unsubscribe();
        if (motionSubscriber != null && !motionSubscriber.isUnsubscribed()) {
            motionSubscriber.unsubscribe();
        }
        motionSubscriber = null;
        if (notificationsSubscriber != null && !notificationsSubscriber.isUnsubscribed()) {
            notificationsSubscriber.unsubscribe();
        }
        notificationsSubscriber = null;
        bleManager.clearMotionNotifications();

        Timber.d("cancel calibrate");
        calibrateTimer.cancel();
        isCalibrating = false;

    }

    @Override
    protected String getDemoId() {
        return DEMO_ID;
    }

    public void calibrate() {
        if(!isCalibrating) {
            Timber.d("request");
            bleManager.calibrate(0x01);
            isCalibrating = true;
        }
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
                unsubscribe();
            }
        };
    }

    protected Subscriber<MotionEvent> onMotion() {
        return new Subscriber<MotionEvent>() {
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
            public void onNext(MotionEvent event) {

                Timber.d("motion for device: %s", event.device.getName());

                if (event.action != null) {
                    if(event.action == MotionEvent.ACTION_CALIBRATE && isCalibrating) {
                        calibrateTimer.start();
                    } else {

                        if(event.action == MotionEvent.ACTION_CLEAR_ORIENTATION) {
                            bleManager.enableCscMeasurement(false);
                        } else if (event.action == MotionEvent.ACTION_CLEAR_MEASUREMENT) {
                            bleManager.enableCscMeasurement(true);
                        }

                        if(viewListener != null) {
                            ((DemoMotionListener)viewListener).onCalibrateComleted();
                        }
                    }
                }

                if (cloudModelName == null) {
                    createCloudDeviceName(event.device.getName());
                }

                ThunderBoardSensorMotion sensor = event.device.getSensorMotion();
                DemoMotionPresenter.this.sensor = sensor;

                if (sensor != null && sensor.isSensorDataChanged && viewListener != null) {
                    ThunderBoardSensorMotion.SensorData sensorData = sensor.getSensorData();
                    ((DemoMotionListener) viewListener).setOrientation(sensorData.ox, sensorData.oy, sensorData.oz);
                    ((DemoMotionListener) viewListener).setAcceleration(sensorData.ax, sensorData.ay, sensorData.az);
                    double d = sensorData.distance;
                    double s = sensorData.speed;
                    // convert to US
                    if (sensor.MEASUREMENTS_TYPE == ThunderBoardPreferences.UNIT_US) {
                        d *= 3.28084f;
                        s *= 3.28084f;
                    }
                    ((DemoMotionListener) viewListener).setDistance(d, sensor.getCumulativeWheelRevolutions(), sensor.MEASUREMENTS_TYPE);
                    ((DemoMotionListener) viewListener).setSpeed(s, sensor.getRotationsPerMinute(), sensor.MEASUREMENTS_TYPE);
                    sensor.isSensorDataChanged = false;

                    pushToCloud();
                }
            }
        };
    }

    protected Subscriber<NotificationEvent> onNotification() {
        return new Subscriber<NotificationEvent>() {
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
            public void onNext(NotificationEvent event) {

                if(NotificationEvent.ACTION_NOTIFICATIONS_SET != event.action) {
                    return;
                }

                ThunderBoardDevice device = event.device;

                Timber.d("notification for device: %s", device.getName());

                ThunderBoardSensorMotion sensor = event.device.getSensorMotion();
                DemoMotionPresenter.this.sensor = sensor;

                if (sensor != null) {
                    int characteristicsStatus = sensor.getCharacteristicsStatus();
                    switch (characteristicsStatus & 0x055) {
                        case 0x00:
                            bleManager.readCscFeature();
                            break;
                        // CSC feature submited already
                        case 0x01:
                            bleManager.enableCscMeasurement(true);
                            break;
                        // CSC feature and CSC measurement submited already
                        case 0x05:
                            bleManager.enableAcceleration(true);
                            break;
                        // CSC feature, CSC measurement and acceleration submited already
                        case 0x15:
                            bleManager.enableOrientation(true);
                            break;
                        default:
                            break;
                    }
                }
            }
        };
    }

    private final CountDownTimer calibrateTimer = new CountDownTimer(10000, 400) {

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            Timber.d("calibrate timer finished, sending reset orientation");
            bleManager.calibrate(0x02);
            isCalibrating = false;
        }
    };
}
