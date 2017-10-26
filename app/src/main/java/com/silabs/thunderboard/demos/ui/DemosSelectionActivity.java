package com.silabs.thunderboard.demos.ui;

import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.silabs.thunderboard.BuildConfig;
import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardApplication;
import com.silabs.thunderboard.common.injection.component.ActivityComponent;
import com.silabs.thunderboard.common.injection.component.DaggerActivityComponent;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.ui.ThunderBoardActivity;
import com.silabs.thunderboard.demos.model.Demo;
import com.silabs.thunderboard.scanner.ui.ScannerActivity;
import com.silabs.thunderboard.settings.ui.SettingsActivity;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class DemosSelectionActivity extends ThunderBoardActivity implements DemosViewListener {

    @Inject
    DemosPresenter presenter;

    @Bind(R.id.demos_toolbar)
    Toolbar toolbar;

    @Bind(R.id.demos_list)
    RecyclerView demosRecyclerView;

    private String deviceName;
    private String deviceAddress;
    private Beacon beacon;
    private String deviceId;

    private final ArrayList<Demo> demosList = new ArrayList<>();

    // Activity

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demos);

        ButterKnife.bind(this);
        component().inject(this);

        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResourceColor(R.color.sl_terbium_green));
        toolbar.setTitle(getString(R.string.thunderboard));

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        params.height += getStatusBarHeight();
        toolbar.setLayoutParams(params);
        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setDisplayShowTitleEnabled(false);
        }

        changeStatusBarColor(getResourceColor(R.color.primary_color));

        final Intent intent = getIntent();

        beacon = intent.getParcelableExtra(ThunderBoardConstants.EXTRA_DEVICE_BEACON);
        if(beacon != null) {
            ThunderBoardDevice device = new ThunderBoardDevice(beacon);
            deviceName = device.getName();
            deviceAddress = device.getAddress();
            presenter.setNotificationDevice(device);
        } else {
            deviceName = intent.getStringExtra(ThunderBoardConstants.EXTRA_DEVICE_NAME);
            deviceAddress = intent.getStringExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS);
        }

        Timber.d("device: %s", deviceName);

        setupDemosList();

        demosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        demosRecyclerView.setAdapter(new DemosAdapter(this, demosList, deviceAddress));
        demosRecyclerView.setVisibility(View.INVISIBLE);

        presenter.setViewListener(this, deviceAddress);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // clear all notifications per iOS spec
        presenter.resetDemoConfiguration();
    }

    @Override
    public void onBackPressed() {
        Timber.d("beacon is null: %s", (beacon == null));
        if(beacon != null) {
            beacon = null;
            Intent intent = new Intent(getApplicationContext(), ScannerActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        presenter.clearViewListener();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_history).setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                intent.putExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS, deviceAddress);
                startActivity(intent);
                return true;

            case R.id.action_history:
                Uri uri = Uri.parse(String.format("%s%s/%s", BuildConfig.CLOUD_DEMO_URL, deviceId, "sessions"));
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                return true;

            case android.R.id.home:
                this.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // ThunderBoardActivity

    @Override
    public void onBluetoothDisabled() {
        finish();
    }

    @Override
    protected ActivityComponent component() {
        if (component == null) {
            component = DaggerActivityComponent.builder()
                    .thunderBoardComponent(((ThunderBoardApplication) getApplication()).component())
                    .build();
        }
        return component;
    }

    private void setupDemosList() {
        demosList.add(new Demo(getString(R.string.demo_motion),
                R.drawable.ic_motion,
                DemoMotionActivity.class,
                DemoMotionActivity.isDemoAllowed()));
        demosList.add(new Demo(getString(R.string.demo_environment),
                R.drawable.ic_environmental,
                DemoEnvironmentActivity.class,
                DemoEnvironmentActivity.isDemoAllowed()));
        demosList.add(new Demo(getString(R.string.demo_io),
                R.drawable.ic_io,
                DemoIOActivity.class,
                DemoIOActivity.isDemoAllowed()));
    }

    @Override
    public void onData(ThunderBoardDevice device) {
        if (device.getState() == BluetoothProfile.STATE_CONNECTED) {
            demosRecyclerView.setVisibility(View.VISIBLE);
        } else {
            demosRecyclerView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
