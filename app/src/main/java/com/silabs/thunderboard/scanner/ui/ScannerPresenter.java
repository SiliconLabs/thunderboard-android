package com.silabs.thunderboard.scanner.ui;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.common.injection.scope.ActivityScope;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

@ActivityScope
public class ScannerPresenter {

    private final long timestamp;

    private final BleManager bleManager;
    private final PreferenceManager prefsManager;

    private ScannerViewListener viewListener;

    private Subscriber<List<ThunderBoardDevice>> discover;

    @Inject
    public ScannerPresenter(BleManager bleManager, PreferenceManager prefsManager) {
        this.bleManager = bleManager;
        this.prefsManager = prefsManager;
        this.timestamp = System.currentTimeMillis();
        Timber.d("timestamp: %d", timestamp);
    }

    public void setViewListener(ScannerViewListener viewListener) {
        this.viewListener = viewListener;
        if (isBluetoothEnabled()) {
            viewListener.onBluetoothEnabled();
            onBluetoothEnabled();
        } else {
            viewListener.onBluetoothDisabled();
        }
    }

    public void clearViewListener() {
        unsubscribe();
        bleManager.backgroundScan();
        viewListener = null;
    }

    public void onBluetoothEnabled() {
        subscribe();
        bleManager.foregroundScan();
    }

    private boolean isBluetoothEnabled() {
        return bleManager.isBluetoothEnabled();
    }

    private void subscribe() {

        if(discover != null) {
            Timber.d("%d has observers %s ignore, already subscribing", timestamp, bleManager.scanner.hasObservers());
            return;
        }

        discover = onDiscover();
        bleManager.scanner
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(discover);
        Timber.d("%d has observers %s", timestamp, bleManager.scanner.hasObservers());
    }

    private void unsubscribe() {
        if (discover != null && !discover.isUnsubscribed()) {
            discover.unsubscribe();
        }
        discover = null;
        Timber.d("%d has observers %s", timestamp, bleManager.scanner.hasObservers());
    }

    public void onBackPressed() {
        bleManager.backgroundScan();
        bleManager.clearDevices();
    }

    private Subscriber<List<ThunderBoardDevice>> onDiscover() {
        return new Subscriber<List<ThunderBoardDevice>>() {
            @Override
            public void onCompleted() {
                Timber.d("completed");
                if(!isUnsubscribed()) {
                    unsubscribe();
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.d("error: %s", e.getMessage());
                if(!isUnsubscribed()) {
                    unsubscribe();
                }
            }

            @Override
            public void onNext(List<ThunderBoardDevice> devices) {
                Timber.d("scanner timestamp: %d devices: %d", timestamp, devices.size());
                if (viewListener != null) {
                    viewListener.onData(devices);
                }
            }
        };
    }
}