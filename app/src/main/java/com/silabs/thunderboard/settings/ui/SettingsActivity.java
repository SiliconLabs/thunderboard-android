package com.silabs.thunderboard.settings.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.silabs.thunderboard.BuildConfig;
import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.data.PreferenceManager;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.common.ui.ThunderBoardActivity;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class SettingsActivity extends ThunderBoardActivity {

    @Inject
    PreferenceManager prefsManager;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.cc_switch)
    Switch ccSwitch;

    @BindView(R.id.measurement_spinner)
    Spinner measurementSpinner;

    @BindView(R.id.temperature_spinner)
    Spinner temperatureSpinner;

    @BindView(R.id.model_type_spinner)
    Spinner modelTypeSpinner;

    @BindView(R.id.beacons_status)
    TextView beaconStatus;

    @BindView(R.id.version_info)
    TextView versionInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        component().inject(this);

        setupToolbar();

        temperatureSpinner.getBackground()
                .setColorFilter(getResources().getColor(R.color.sl_silicon_grey), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> temperatureAdapter = ArrayAdapter.createFromResource(this,
                R.array.temperature_array, R.layout.simple_spinner_item);
        temperatureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        temperatureSpinner.setAdapter(temperatureAdapter);

        measurementSpinner.getBackground()
                .setColorFilter(getResources().getColor(R.color.sl_silicon_grey), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> measurementAdapter = ArrayAdapter.createFromResource(this,
                R.array.measurement_array, R.layout.simple_spinner_item);
        measurementAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measurementSpinner.setAdapter(measurementAdapter);

        modelTypeSpinner.getBackground()
                .setColorFilter(getResources().getColor(R.color.sl_silicon_grey), PorterDuff.Mode.SRC_ATOP);
        ArrayAdapter<CharSequence> modelTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.model_type_array, R.layout.simple_spinner_item);
        modelTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelTypeSpinner.setAdapter(modelTypeAdapter);

        versionInfoText.setText(String.format(getString(R.string.settings_version),
                BuildConfig.VERSION_NAME, BuildConfig.BUILD_TIME.substring(0,4)));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPersonalize();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("SettingsActivity paused");
        saveSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResourceColor(R.color.sl_terbium_green));
        toolbar.setTitle(R.string.action_settings);

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
     * Sets the widgets with data from the preferences
     */
    private void loadPersonalize() {
        ThunderBoardPreferences prefs = prefsManager.getPreferences();
        Timber.d("prefs: %s", prefs.toString());

        ccSwitch.setChecked(prefs.userCCSelf);

        if (prefs.measureUnitType == ThunderBoardPreferences.UNIT_METRIC) {
            measurementSpinner.setSelection(0);
        } else if (prefs.measureUnitType == ThunderBoardPreferences.UNIT_US){
            measurementSpinner.setSelection(1);
        }

        if (prefs.temperatureType == ThunderBoardPreferences.TEMP_CELSIUS) {
            temperatureSpinner.setSelection(0);
        } else if (prefs.temperatureType == ThunderBoardPreferences.TEMP_FAHRENHEIT) {
            temperatureSpinner.setSelection(1);
        }

        if (prefs.modelType == ThunderBoardPreferences.MODEL_TYPE_BOARD) {
            modelTypeSpinner.setSelection(0);
        } else if (prefs.modelType == ThunderBoardPreferences.MODEL_TYPE_CAR) {
            modelTypeSpinner.setSelection(1);
        }

        boolean beaconNotifications;
        if(prefs.beacons == null || prefs.beacons.size() == 0) {
            prefs.beaconNotifications = false;
            beaconNotifications = false;
        } else {
            beaconNotifications = prefs.beaconNotifications;
        }
        beaconStatus.setText(beaconNotifications ? R.string.on : R.string.off);
    }

    /**
     * saveSettings
     *
     * Save the information set by the widgets when leaving this screen.
     */
    private void saveSettings() {
        ThunderBoardPreferences prefs = prefsManager.getPreferences();

        prefs.userCCSelf = ccSwitch.isChecked();

        prefs.measureUnitType = (measurementSpinner.getSelectedItemPosition() == 0)
                ? ThunderBoardPreferences.UNIT_METRIC : ThunderBoardPreferences.UNIT_US;

        prefs.temperatureType = (temperatureSpinner.getSelectedItemPosition() == 0)
                ? ThunderBoardPreferences.TEMP_CELSIUS : ThunderBoardPreferences.TEMP_FAHRENHEIT;

        prefs.modelType = (modelTypeSpinner.getSelectedItemPosition() == 0)
                ? ThunderBoardPreferences.MODEL_TYPE_BOARD : ThunderBoardPreferences.MODEL_TYPE_CAR;

        prefsManager.setPreferences(prefs);
    }

    /**
     * clickBeaconNotifications
     *
     * Launch the Beacon Notifications screen
     */
    @OnClick(R.id.beacon_notifications)
    void clickBeaconNotifications() {

        // pass the device address if settings was invoked from a connected state
        String deviceAddress = getIntent().getStringExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS);

        Intent intent = new Intent(this, BeaconNotificationsActivity.class);
        if(deviceAddress != null) {
            intent.putExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS, deviceAddress);
        }
        startActivity(intent);
    }
}
