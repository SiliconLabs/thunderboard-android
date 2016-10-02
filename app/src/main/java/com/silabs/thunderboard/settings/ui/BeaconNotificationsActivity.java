package com.silabs.thunderboard.settings.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.ui.ThunderBoardActivity;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class BeaconNotificationsActivity extends ThunderBoardActivity {
    private final ArrayList<ThunderBoardPreferences.Beacon> allowedBeaconList = new ArrayList<>();
    private final ArrayList<ThunderBoardPreferences.Beacon> otherBeaconList = new ArrayList<>();
    @Inject
    PreferenceManager prefsManager;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.beacon_switch)
    Switch beaconSwitch;
    @Bind(R.id.demo_devices_recycler_view)
    RecyclerView demodevicesRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_notifications);
        ButterKnife.bind(this);
        component().inject(this);

        setupToolbar();


        demodevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        beaconSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                demodevicesRecyclerView.setVisibility(b ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPersonalize();
    }

    @Override
    public void onPause() {
        super.onPause();
        saveSettings();
    }

    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResourceColor(R.color.sl_terbium_green));
        toolbar.setTitle(R.string.settings_beacon_notifications);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        params.height += getStatusBarHeight();
        toolbar.setLayoutParams(params);

        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * loadPersonalize
     *
     * Loads information from Preferences and creates the adapter for the RecyclerView
     */
    private void loadPersonalize() {
        ThunderBoardPreferences prefs = prefsManager.getPreferences();

        if (prefs.beacons != null && prefs.beacons.size() > 0) {
            Timber.d("prefs.beacons size: %d", prefs.beacons.size());
            allowedBeaconList.clear();
            otherBeaconList.clear();

            for (Map.Entry<String, ThunderBoardPreferences.Beacon> entry : prefs.beacons.entrySet()) {
                ThunderBoardPreferences.Beacon b = entry.getValue();
                if (b.deviceAddress == null) {
                    prefs.beacons.remove(entry.getKey());
                } else if (b.allowNotifications) {
                    allowedBeaconList.add(b);
                } else {
                    otherBeaconList.add(b);
                }
            }
        }

        beaconSwitch.setChecked(prefs.beaconNotifications);
        demodevicesRecyclerView.setVisibility(prefs.beaconNotifications ? View.VISIBLE : View.GONE);

        BeaconNotificationsAdapter adapter = new BeaconNotificationsAdapter(this, prefsManager, allowedBeaconList, otherBeaconList);
        String deviceAddress = getIntent().getStringExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS);
        if (deviceAddress != null) {
            adapter.setConnectedDeviceAddress(deviceAddress);
        }
        demodevicesRecyclerView.setAdapter(adapter);

    }

    /**
     * saveSettings
     *
     * Saves the settings, called when leaving this activity
     *
     */
    private void saveSettings() {
        ThunderBoardPreferences prefs = prefsManager.getPreferences();
        if (beaconSwitch.isChecked()) {
            prefs.beaconNotifications = true;
        } else {
            prefs.beaconNotifications = false;
        }
        prefsManager.setPreferences(prefs);
    }
}
