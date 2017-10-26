package com.silabs.thunderboard.demos.ui;

import android.bluetooth.BluetoothProfile;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.ble.ThunderBoardSensorMotion;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;
import com.silabs.thunderboard.demos.model.NotificationEvent;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@ActivityScope
public class DemosPresenter {

    private final BleManager bleManager;

    private DemosViewListener viewListener;

    private BehaviorSubject<ThunderBoardDevice> deviceMonitor;
    private Subscriber<ThunderBoardDevice> deviceSubscriber;

    private BehaviorSubject<NotificationEvent> notificationsMonitor;
    private Subscriber<NotificationEvent> notificationsSubscriber;

    @Inject
    public DemosPresenter(BleManager bleManager) {
        this.bleManager = bleManager;
    }

    public void setViewListener(DemosViewListener viewListener, String deviceAddress) {
        this.viewListener = viewListener;
        subscribe(deviceAddress);
    }

    public void clearViewListener() {
        unsubscribe();
        viewListener = null;
    }

    public void setNotificationDevice(ThunderBoardDevice notificationDevice) {
        bleManager.addNotificationDevice(notificationDevice);
    }

    private void subscribe(String deviceAddress) {
        Timber.d("device: %s", deviceAddress);
        deviceSubscriber = onDevice();
        deviceMonitor = bleManager.selectedDeviceMonitor;
        deviceMonitor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(deviceSubscriber);
        notificationsSubscriber = onNotification();
        notificationsMonitor = bleManager.notificationsMonitor;
        notificationsMonitor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(notificationsSubscriber);
        bleManager.connect(deviceAddress);
    }

    private void unsubscribe() {
        Timber.d(getClass().getSimpleName());
        if(deviceSubscriber != null && !deviceSubscriber.isUnsubscribed()) {
            deviceSubscriber.unsubscribe();
        }
        deviceSubscriber = null;
        if (notificationsSubscriber != null && !notificationsSubscriber.isUnsubscribed()) {
            notificationsSubscriber.unsubscribe();
        }
        notificationsSubscriber = null;
    }

    private Subscriber<ThunderBoardDevice> onDevice() {
        return new Subscriber<ThunderBoardDevice>() {
            @Override
            public void onCompleted() {
                Timber.d("completed");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d("error: %s", e.getMessage());
            }

            @Override
            public void onNext(ThunderBoardDevice device) {
                Timber.d("device: %s, state: %d", device.getName(), device.getState());
                if(viewListener != null) {
                    if(BluetoothProfile.STATE_CONNECTED == device.getState() && Boolean.TRUE.equals(device.isServicesDiscovered)) {
                        viewListener.onData(device);
                        viewListener.setDeviceId(device.getSystemId());
                    }
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
                if (bleManager.handleClearMotionNotifications(event)) {
                    return;
                }

                if(NotificationEvent.ACTION_NOTIFICATIONS_SET != event.action) {
                    return;
                }

                ThunderBoardDevice device = event.device;

                Timber.d("notification for device: %s", device.getName());

                ThunderBoardSensorMotion sensor = event.device.getSensorMotion();

                if (sensor != null) {
                    int characteristicsStatus = sensor.getCharacteristicsStatus();
                    boolean submitted;
                    switch (characteristicsStatus & 0x155) {
                        case 0x100:
                            submitted = bleManager.enableCscMeasurement(false);
                            characteristicsStatus |= 0x04;
                            Timber.d("characteristicsStatus: %02x, submitted: %s", characteristicsStatus, submitted);
                            break;
                        case 0x104:
                            submitted = bleManager.enableAcceleration(false);
                            characteristicsStatus |= 0x10;
                            Timber.d("characteristicsStatus: %02x, submitted: %s", characteristicsStatus, submitted);
                            break;
                        case 0x114:
                            submitted = bleManager.enableOrientation(false);
                            characteristicsStatus |= 0x40;
                            Timber.d("characteristicsStatus: %02x, submitted: %s", characteristicsStatus, submitted);
                            break;
                        default:
                            break;
                    }
                }
            }
        };
    }

    public void resetDemoConfiguration() {
        bleManager.clearMotionNotifications();
    }
}
