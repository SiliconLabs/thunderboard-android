package com.silabs.thunderboard.demos.ui;

import android.os.Handler;
import android.os.Message;

import com.silabs.thunderboard.ble.BleManager;
import com.silabs.thunderboard.ble.ThunderBoardSensor;
import com.silabs.thunderboard.common.app.ThunderBoardType;
import com.silabs.thunderboard.web.CloudManager;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;

import java.util.UUID;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;
import timber.log.Timber;

public abstract class BaseDemoPresenter {

    protected final BleManager bleManager;
    protected final CloudManager cloudManager;
    protected BehaviorSubject<ThunderBoardDevice> deviceMonitor;
    protected Subscriber<ThunderBoardDevice> deviceSubscriber;
    protected boolean isFirebaseInstantiated;
    protected BehaviorSubject<Boolean> wifiMonitor;

    // need it to kick the streaming
    protected String uniqueID;
    // stripped version of the device name
    protected String cloudModelName;
    // the real device name
    protected String cloudDeviceName;
    protected boolean isStreaming;
    protected ThunderBoardSensor sensor;

    protected BaseDemoViewListener viewListener;

    public BaseDemoPresenter(BleManager bleManager, CloudManager cloudManager) {
        this.bleManager = bleManager;
        this.cloudManager = cloudManager;
    }

    public void setViewListener(BaseDemoViewListener viewListener, String deviceAddress) {
        this.viewListener = viewListener;
        subscribe(deviceAddress);
    }

    public void clearViewListener() {
        unsubscribe();
        viewListener = null;
    }

    public String getSharedUrl() {
        return cloudManager.getShortUrl();
    }

    protected void subscribe(String deviceAddress) {

        Timber.d(getClass().getSimpleName());

        deviceSubscriber = onDevice();
        deviceMonitor = bleManager.selectedDeviceMonitor;
        deviceMonitor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(deviceSubscriber);

        wifiMonitor = cloudManager.wifiMonitor;
        wifiMonitor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(onWifi());
    }

    protected void unsubscribe() {

        Timber.d(getClass().getSimpleName());

        if (deviceSubscriber != null && !deviceSubscriber.isUnsubscribed()) {
            deviceSubscriber.unsubscribe();
        }
        deviceSubscriber = null;

        cloudModelName = null;
        sensor = null;

        if(isStreaming) {
            stopStreaming();
        }
    }

    protected void createCloudDeviceName(String name) {
        cloudModelName = name.replace("#", "").replaceAll(" ", "");
        cloudDeviceName = name;
    }

    public void startStreaming() {
        Timber.d(getClass().getSimpleName());
        if (sensor != null) {
            Timber.d("firabese instantieated: %s", isFirebaseInstantiated);
            if (!isFirebaseInstantiated) {
                initFirebase();
            }
            sensor.isSensorDataChanged = false;
        }

        streamSampleHandler.post(streamSampleRunnable);
        isStreaming = true;
    }

    public ThunderBoardType getThunderBoardType() {
        return bleManager.getThunderBoardType();
    }

    private void initFirebase() {
        Timber.d(getClass().getSimpleName());
        uniqueID = UUID.randomUUID().toString();
        String url = cloudManager.createFirebaseReference(cloudModelName, cloudDeviceName, uniqueID, getDemoId(), sensor);
        if (url != null) {
            isFirebaseInstantiated = true;
        }
    }

    public void stopStreaming() {
        Timber.d(getClass().getSimpleName());
        if(isStreaming) {
            isStreaming = false;
            cloudManager.clearFirebaseReference(uniqueID);
        }

        streamSampleHandler.removeCallbacks(streamSampleRunnable);
        isFirebaseInstantiated = false;
    }

    protected void pushToCloud() {
        if (sensor != null && isStreaming) {
            if (!isFirebaseInstantiated) {
                initFirebase();
            } else {
                cloudManager.push(sensor);
            }
        }
    }

    protected abstract String getDemoId();

    protected abstract Subscriber<ThunderBoardDevice> onDevice();

    private Action1<Boolean> onWifi() {
        return new Action1<Boolean>() {
            @Override
            public void call(Boolean isConnected) {
                Timber.d("wifi connected: %s", isConnected);
                if (viewListener != null) {
                    viewListener.onWifi(isConnected);
                }
            }
        };
    }

    // Streaming Sampler

    /**
     * Sample period (in milliseconds) for data to be collected for streaming session.
     * Subclasses may override this to modify how frequently samples are collected.
     *
     * @return Sample period (in milliseconds)
     */
    public int streamingSamplePeriod() {
        return 1000;
    }

    private Handler streamSampleHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            streamSampleRunnable.run();
            return true;
        }
    });

    private Runnable streamSampleRunnable = new Runnable() {
        @Override
        public void run() {
            pushToCloud();
            streamSampleHandler.postDelayed(streamSampleRunnable, streamingSamplePeriod());
        }
    };
}