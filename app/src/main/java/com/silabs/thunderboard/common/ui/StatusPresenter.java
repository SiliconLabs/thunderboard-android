package com.silabs.thunderboard.common.ui;

import android.bluetooth.BluetoothProfile;
import android.os.CountDownTimer;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.demos.model.StatusEvent;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

@ActivityScope
public class StatusPresenter {

    private final BleManager bleManager;

    private StatusViewListener viewListener;

    private BehaviorSubject<StatusEvent> statusMonitor;
    private Subscriber<StatusEvent> statusSubscriber;

    private boolean isConnectivityLost;
    private ThunderBoardDevice device;

    @Inject
    public StatusPresenter(BleManager bleManager) {
        this.bleManager = bleManager;
    }

    public void setViewListener(StatusViewListener viewListener) {
        this.viewListener = viewListener;
        subscribe();
    }

    public void clearViewListener() {
        unsubscribe();
        viewListener = null;
    }

    private void subscribe() {
        Timber.d(getClass().getSimpleName());

        isConnectivityLost = true;
        connectivityHeartbeatTimer.start();

        statusSubscriber = onStatusEvent();
        statusMonitor = bleManager.selectedDeviceStatusMonitor;
        statusMonitor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(statusSubscriber);
    }

    private void unsubscribe() {
        Timber.d(getClass().getSimpleName());
        if (statusSubscriber != null && !statusSubscriber.isUnsubscribed()) {
            statusSubscriber.unsubscribe();
        }
        statusSubscriber = null;
        device = null;
        connectivityHeartbeatTimer.cancel();
    }

    private Subscriber<StatusEvent> onStatusEvent() {
        return new Subscriber<StatusEvent>() {
            @Override
            public void onCompleted() {
                Timber.d("completed");
            }

            @Override
            public void onError(Throwable e) {
                Timber.d("error: %s", e.getMessage());
            }

            @Override
            public void onNext(StatusEvent event) {

                device = event.device;
                int deviceState = device.getState();
                Timber.d("device: %s, state: %d", device.getName(), deviceState);

                if (BluetoothProfile.STATE_DISCONNECTED == deviceState) {
                    // we are done, notify the UI
                    connectivityHeartbeatTimer.cancel();
                    if(isConnectivityLost) {
                        isConnectivityLost = false;
                        viewListener.onData(device);
                    }
                } else if (BluetoothProfile.STATE_CONNECTED == deviceState && Boolean.TRUE.equals(device.isServicesDiscovered)) {
                    // good stuff, we have a status event and the services
                    isConnectivityLost = false;
                    viewListener.onData(device);
                } else if (BluetoothProfile.STATE_CONNECTING == deviceState) {
                    viewListener.onData(device);
                }
            }
        };
    }

    /*
    The OS Bluetooth connection times out after 30 seconds. Cannot make it shorter.
    We need to timeout after ~10 seconds (which is how often battery change is triggered).
    #1: onResume, start a 10 seconds countdown.
    #2: onFinish check if the connection is present. In our case the connection state is
        determined by the state of the device, the state of discovered services and it is updated
        during onNext of the device state subscriber.
     */
    private final CountDownTimer connectivityHeartbeatTimer = new CountDownTimer(10500, 20000) {

        @Override
        public void onTick(long millisUntilFinished) {
            // n/a
        }

        @Override
        public void onFinish() {
            if (isConnectivityLost) {
                // not good, did not receive a status event for a while
                if (device != null) {
                    device.setState(BluetoothProfile.STATE_DISCONNECTED);
                    viewListener.onData(device);
                    isConnectivityLost = false;
                }
            } else {
                // set another cycle during which the below should be cleared
                isConnectivityLost = true;
                start();
            }
        }
    };
}
