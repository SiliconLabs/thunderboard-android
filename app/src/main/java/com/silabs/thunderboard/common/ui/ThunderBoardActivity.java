package com.silabs.thunderboard.common.ui;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.silabs.thunderboard.common.app.ThunderBoardApplication;
import com.silabs.thunderboard.common.injection.component.ActivityComponent;
import com.silabs.thunderboard.common.injection.component.DaggerActivityComponent;

import timber.log.Timber;

public class ThunderBoardActivity extends AppCompatActivity {

    protected int bluetoothState = BluetoothAdapter.STATE_OFF;
    protected ActivityComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d(getClass().getSimpleName());
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Timber.d(getClass().getSimpleName());
        super.onResume();
        registerReceiver(bluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // cancel all notifications
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    @Override
    protected void onPause() {
        Timber.d(getClass().getSimpleName());
        unregisterReceiver(bluetoothStateReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Timber.d(getClass().getSimpleName());
        super.onDestroy();
    }

    @TargetApi(21)
    protected void changeStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.getWindow().setStatusBarColor(color);
        }
    }

    protected int getResourceColor(int resourceColor) {
        if (Build.VERSION.SDK_INT < 23) {
            return getResources().getColor(resourceColor);
        } else {
            return getColor(resourceColor);
        }
    }

    protected ActivityComponent component() {
        if (component == null) {
            component = DaggerActivityComponent.builder()
                    .thunderBoardComponent(((ThunderBoardApplication) getApplication()).component())
                    .build();
        }
        return component;
    }

    protected void onBluetoothEnabled() {
        // do nothing in the base class;
    }

    protected void onBluetoothDisabled() {
        // do nothing in the base class;
    }

    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (BluetoothAdapter.STATE_OFF == bluetoothState) {
                    onBluetoothDisabled();
                } else if (BluetoothAdapter.STATE_OFF == bluetoothState) {
                    onBluetoothEnabled();
                }
                Timber.d("bluetoothState: %d", bluetoothState);
            }
        }
    };

    public int getStatusBarHeight() {
        int result = 0;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            Resources res = getResources();
            int resourceID = res.getIdentifier("status_bar_height", "dimen", "android");
            if (resourceID > 0) {
                result = res.getDimensionPixelSize(resourceID);
            }
        }
        return result;
    }
}
