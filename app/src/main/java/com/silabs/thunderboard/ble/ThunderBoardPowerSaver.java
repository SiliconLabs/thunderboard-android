package com.silabs.thunderboard.ble;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;

import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.scanner.ui.ScannerActivity;

import org.altbeacon.beacon.BeaconManager;

import java.util.Map;

import timber.log.Timber;

@TargetApi(18)
public class ThunderBoardPowerSaver implements Application.ActivityLifecycleCallbacks {

    // the app is backgrounded (no resimed acivities)
    public static long DELAY_BETWEEN_SCANS_INACTIVE = 15000;
    // the app is foregrounded, acts as disable scan
    public static long DELAY_FOREVER = Long.MAX_VALUE;
    // do not send notifications in between this threshold
    public static final long DELAY_NOTIFICATIONS_TIME_THRESHOLD = 30000;

    private PreferenceManager preferenceManager;
    private BeaconManager beaconManager;
    private boolean isScannerActivityResumed;
    private long scannerActivityDestroyedTimestamp;
    private int activeActivityCount;

    public ThunderBoardPowerSaver(Context context, PreferenceManager preferenceManager) {

        if (android.os.Build.VERSION.SDK_INT < 18) {
            Timber.d("BackgroundPowerSaver requires API 18 or higher.");
            return;
        }

        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(this);
        } else {
            Timber.e("Context is not an application instance, so we cannot use the BackgroundPowerSaver");
        }

        this.preferenceManager = preferenceManager;
        this.beaconManager = BeaconManager.getInstanceForApplication(context);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ++this.activeActivityCount;
        if (activity instanceof ScannerActivity) {
            beaconManager.setBackgroundMode(false);
            isScannerActivityResumed = true;
            // the scanner activity runs in foreground mode, fast scanning
            Timber.d("setForegroundMode");
        } else {
            // the app is still active but scan is backgrounded indefinitely
            beaconManager.setBackgroundBetweenScanPeriod(DELAY_FOREVER);
            isScannerActivityResumed = false;
            Timber.d("setBackgroundBetweenScanPeriod to %d ", DELAY_FOREVER);
            beaconManager.setBackgroundMode(true);
            Timber.d("setBackgroundMode");
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        --this.activeActivityCount;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity instanceof ScannerActivity) {
            isScannerActivityResumed = false;
            scannerActivityDestroyedTimestamp = SystemClock.elapsedRealtime();
            long delayBetweenScans = getDelayBetweenScans();
            Timber.d("setBackgroundBetweenScanPeriod to %d ", delayBetweenScans);
            beaconManager.setBackgroundBetweenScanPeriod(delayBetweenScans);
            beaconManager.setBackgroundMode(true);
            Timber.d("setBackgroundMode");
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
    }

    public boolean isScannerActivityResumed() {
        return isScannerActivityResumed;
    }

    public long getScannerActivityDestroyedTimestamp() {
        return scannerActivityDestroyedTimestamp;
    }

    public long getDelayBetweenScans() {
        ThunderBoardPreferences prefs = preferenceManager.getPreferences();
        if (prefs.beaconNotifications && prefs.beacons != null) {
            for (Map.Entry<String, ThunderBoardPreferences.Beacon> entry : prefs.beacons.entrySet()) {
                if(entry.getValue().allowNotifications) {
                    return DELAY_BETWEEN_SCANS_INACTIVE;
                }
            }
        }

        return DELAY_FOREVER;
    }

    public boolean isApplicationBackgrounded() {
        return activeActivityCount <= 0;
    }
}
