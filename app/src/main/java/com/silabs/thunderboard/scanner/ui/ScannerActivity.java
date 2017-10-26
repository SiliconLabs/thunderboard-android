package com.silabs.thunderboard.scanner.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.ui.ThunderBoardActivity;
import com.silabs.thunderboard.demos.ui.DemosSelectionActivity;
import com.silabs.thunderboard.settings.ui.SettingsActivity;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ScannerActivity extends ThunderBoardActivity implements ScannerViewListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ACCESS_COARSE_LOCATION = 2;

    private enum BluetoothState {BLUETOOTH_DISABLED, BLUETOOTH_ENABLED, NO_DEVICE_FOUND, DEVICE_FOUND}

    private int animationDuration;
    private final LinearInterpolator linearInterpolator = new LinearInterpolator();

    @Inject
    ScannerPresenter presenter;

    @Bind(R.id.scanner_toolbar)
    Toolbar toolbar;

    @Bind(R.id.mm_logo)
    ImageView mmLogo;

    @Bind(R.id.bottom_panel)
    FrameLayout bottomPanel;

    @Bind(R.id.scanner_device_list)
    RecyclerView scannerRecyclerView;

    @Bind(R.id.bluetooth_devices_view)
    View bluetoothDevicesView;

    @Bind(R.id.bluetooth_no_devices_view)
    View bluetoothNoDevicesView;

    @Bind(R.id.scanner_status_report)
    TextView scannerStatusReport;

    @Bind(R.id.scanner_progress)
    ProgressBar scannerProgress;

    private ScannerAdapter scannerAdapter;

    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_scanner);
        ButterKnife.bind(this);
        component().inject(this);

        if (presenter == null) {
            throw new IllegalStateException("Presenter has to be injected");
        }

        animationDuration = getResources().getInteger(R.integer.scanner_animation_duration);
        initToolbar();
        initializeItems();

        if (!deviceSupportsBle) {
            setScannerVisibility(BluetoothState.BLUETOOTH_DISABLED);
        } else {
            setupScannerAdapter();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android M Permission check
                if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.app_name);
                    builder.setMessage(R.string.permission_needed_coarse_location);
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @TargetApi(23)
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                                               REQUEST_ACCESS_COARSE_LOCATION);
                        }
                    });
                    builder.show();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (deviceSupportsBle) {
            presenter.setViewListener(this);
        }
    }

    @Override
    public void onPause() {
        presenter.clearViewListener();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        } else {
            this.onBluetoothEnabled();
            presenter.onBluetoothEnabled();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.d("access coarse location: permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage(
                            "Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Timber.d("onBackPressed");
        super.onBackPressed();
        presenter.onBackPressed();
    }

    // ScannerViewListener

    /**
     * onData
     * <p>
     * Listener that gets called when there is an update to the device list.
     * When called, the scanner state gets updated and the adapter is
     * updated
     *
     * @param devices List of devices
     */
    @Override
    public void onData(List<ThunderBoardDevice> devices) {
        animateItems();
        if (devices.size() == 0) {
            setScannerVisibility(BluetoothState.NO_DEVICE_FOUND);
        } else {
            setScannerVisibility(BluetoothState.DEVICE_FOUND);
            scannerAdapter.updateDataSet(devices);
        }
    }

    /**
     * onBluetoothDisabled
     * <p>
     * Listener for when the Bluetooth gets disabled, pops up the window telling the user
     * to enable Bluetooth.
     */
    @Override
    public void onBluetoothDisabled() {
        setScannerVisibility(BluetoothState.BLUETOOTH_DISABLED);
        // If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onBluetoothEnabled() {
        setScannerVisibility(BluetoothState.BLUETOOTH_ENABLED);
    }

    /**
     * initToolbar
     * <p>
     * Sets up the toolbar, adds margin to top of toolbar; this is needed for devices running
     * Lollipop or greater. If the device is running Kitkat or below, getStatusBarHeight will
     * return 0.
     */
    private void initToolbar() {
        toolbar.setBackgroundColor(getResourceColor(R.color.transparent));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toolbar.getLayoutParams();
        params.setMargins(0, getStatusBarHeight(), 0, 0);
        toolbar.setLayoutParams(params);
    }

    /**
     * setupScannerAdapter
     * <p>
     * Sets up ScannerAdapter for the recycler view.
     */
    private void setupScannerAdapter() {
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        scannerRecyclerView.setLayoutManager(layoutManager);

        List<ThunderBoardDevice> devices = new ArrayList<>();
        scannerAdapter = new ScannerAdapter(devices);
        scannerAdapter.updateDataSet(devices);
        scannerAdapter.setListener(listener);
        scannerRecyclerView.setAdapter(scannerAdapter);
    }

    private ScannerAdapter.OnDeviceItemClickListener listener = new ScannerAdapter.OnDeviceItemClickListener() {
        @Override
        public void onDeviceItemClick(View view, ThunderBoardDevice device) {
            Intent intent = new Intent(ScannerActivity.this, DemosSelectionActivity.class);
            intent.putExtra(ThunderBoardConstants.EXTRA_DEVICE_NAME, device.getName());
            intent.putExtra(ThunderBoardConstants.EXTRA_DEVICE_ADDRESS, device.getAddress());
            startActivity(intent);
        }
    };

    /**
     * setScannerVisibility
     * <p>
     * Chooses which view to show, bluetoothDevicesView (with the RecyclerView) or
     * bluetoothNoDevicesView (with a message saying that there are no devices found
     * and an indefinite progress spinner), depending on the bluetoothState.
     *
     * @param bluetoothState
     */
    private void setScannerVisibility(BluetoothState bluetoothState) {
        switch (bluetoothState) {
            case BLUETOOTH_DISABLED:
                bluetoothDevicesView.setVisibility(View.GONE);
                bluetoothNoDevicesView.setVisibility(View.GONE);
                break;
            case BLUETOOTH_ENABLED:
                bluetoothDevicesView.setVisibility(View.GONE);
                bluetoothNoDevicesView.setVisibility(View.VISIBLE);
                scannerStatusReport.setText(R.string.scanner_looking_for_devices);
                configureProgressIndicator();
                break;
            case NO_DEVICE_FOUND:
                bluetoothDevicesView.setVisibility(View.GONE);
                bluetoothNoDevicesView.setVisibility(View.VISIBLE);
                scannerStatusReport.setText(R.string.scanner_no_devices);
                configureProgressIndicator();
                break;
            case DEVICE_FOUND:
                bluetoothDevicesView.setVisibility(View.VISIBLE);
                bluetoothNoDevicesView.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * configureProgressIndicator
     * <p>
     * Sets the color of the indeterminate progress indicator to be blue,
     * as defined in res/values/color.xml
     */
    private void configureProgressIndicator() {
        int color;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            color = getResources().getColor(R.color.sl_terbium_green);
        } else {
            color = getResources().getColor(R.color.sl_terbium_green, null);
        }
        if (scannerProgress.getIndeterminateDrawable() != null) {
            scannerProgress.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        } else {
            Timber.d("scannerProgress indeterminate drawable is null");
        }
    }

    private void initializeItems() {
        bottomPanel.setVisibility(View.INVISIBLE);
        toolbar.setVisibility(View.INVISIBLE);

        mmLogo.setVisibility(View.VISIBLE);
    }

    private void animateItems() {
        AnimatorSet animatorSet = new AnimatorSet();
        ArrayList<Animator> animatorList = new ArrayList<>();

        animatorList.add(mmLogo.getVisibility() == View.VISIBLE ? animateLogo() : null);
        animatorList.add(toolbar.getVisibility() == View.INVISIBLE ? animateToolbar() : null);
        animatorList.add(bottomPanel.getVisibility() == View.INVISIBLE ? animateBottomPanel() : null);

        animatorSet.playTogether(animatorList);
        animatorSet.start();
    }

    private Animator animateLogo() {
        ObjectAnimator mmLogoAnimator = ObjectAnimator.ofFloat(mmLogo, "alpha", 1.0f, 0.0f);
        mmLogoAnimator.setDuration(animationDuration);
        mmLogoAnimator.setInterpolator(linearInterpolator);
        mmLogoAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mmLogo.setVisibility(View.INVISIBLE);
            }
        });
        return mmLogoAnimator;
    }

    private Animator animateToolbar() {
        toolbar.setVisibility(View.VISIBLE);
        ObjectAnimator toolbarAnimator = ObjectAnimator.ofFloat(toolbar, "alpha", 0.0f, 1.0f);
        toolbarAnimator.setDuration(animationDuration);
        toolbarAnimator.setInterpolator(linearInterpolator);
        return toolbarAnimator;
    }

    private Animator animateBottomPanel() {
        bottomPanel.setVisibility(View.VISIBLE);
        ObjectAnimator bottomPanelAnimator = ObjectAnimator.ofFloat(bottomPanel,
                                                                    "translationY",
                                                                    (float) bottomPanel.getHeight(),
                                                                    0.0f);
        bottomPanelAnimator.setDuration(animationDuration);
        bottomPanelAnimator.setInterpolator(linearInterpolator);
        return bottomPanelAnimator;
    }
}
