package com.silabs.thunderboard.settings.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
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

    @BindView(R.id.measurement_toggle)
    RadioGroup measurementToggle;

    @BindView(R.id.temperature_toggle)
    RadioGroup temperatureToggle;

    @BindView(R.id.model_type_toggle)
    RadioGroup modelTypeToggle;

    @BindView(R.id.beacons_status)
    TextView beaconStatus;

    private Dialog helpDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        component().inject(this);

        setupToolbar();
        initHelpDialog();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_help:
                helpDialog.show();
                return true;

            case android.R.id.home:
                this.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initHelpDialog() {
        helpDialog = new Dialog(this);
        helpDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        helpDialog.setContentView(R.layout.dialog_help_demo_item);
        ((TextView) helpDialog.findViewById(R.id.dialog_help_version_text)).setText(getString(R.string.version_text,
                BuildConfig.VERSION_NAME));
        View okButton = helpDialog.findViewById(R.id.help_ok_button);
        TextView textView = helpDialog.findViewById(R.id.help_text_playstore);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpDialog.dismiss();
            }
        });
    }

    protected void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResourceColor(R.color.tb_red));
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
            measurementToggle.check(R.id.metric);
        } else if (prefs.measureUnitType == ThunderBoardPreferences.UNIT_US){
            measurementToggle.check(R.id.us);
        }

        if (prefs.temperatureType == ThunderBoardPreferences.TEMP_CELSIUS) {
            temperatureToggle.check(R.id.celsius);
        } else if (prefs.temperatureType == ThunderBoardPreferences.TEMP_FAHRENHEIT) {
            temperatureToggle.check(R.id.fahrenheit);
        }

        if (prefs.modelType == ThunderBoardPreferences.MODEL_TYPE_BOARD) {
            modelTypeToggle.check(R.id.board);
        } else if (prefs.modelType == ThunderBoardPreferences.MODEL_TYPE_CAR) {
            modelTypeToggle.check(R.id.car);
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

        prefs.measureUnitType = (measurementToggle.getCheckedRadioButtonId() == R.id.metric)
                ? ThunderBoardPreferences.UNIT_METRIC : ThunderBoardPreferences.UNIT_US;

        prefs.temperatureType = (temperatureToggle.getCheckedRadioButtonId() == R.id.celsius)
                ? ThunderBoardPreferences.TEMP_CELSIUS : ThunderBoardPreferences.TEMP_FAHRENHEIT;

        prefs.modelType = (modelTypeToggle.getCheckedRadioButtonId() == R.id.board)
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
