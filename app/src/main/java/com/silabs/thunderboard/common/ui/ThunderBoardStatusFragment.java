package com.silabs.thunderboard.common.ui;

import static android.bluetooth.BluetoothProfile.*;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardConstants;
import com.silabs.thunderboard.common.app.ThunderBoardType;
import com.silabs.thunderboard.common.injection.component.ActivityComponent;
import com.silabs.thunderboard.ble.model.ThunderBoardDevice;
import com.silabs.thunderboard.demos.ui.BaseDemoActivity;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ThunderBoardStatusFragment extends Fragment implements StatusViewListener {

    @Inject
    StatusPresenter presenter;

    @Bind(R.id.battery_indicator)
    BatteryIndicator batteryIndicator;

    @Bind(R.id.device_status)
    TextView deviceStatus;

    @Bind(R.id.device_name)
    TextView deviceName;

    @Bind(R.id.device_firmware)
    TextView deviceFirmware;

    @Bind(R.id.progress_bar)
    ProgressBar progressBar;

    private ActivityComponent component;

    private View rootView;

    private boolean isConnecting = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_device_status, container, false);
        rootView = view;
        ButterKnife.bind(this, view);

        component().inject(this);

        batteryIndicator.setVisibility(View.INVISIBLE);
        batteryIndicator.setBatteryValue(ThunderBoardConstants.POWER_SOURCE_TYPE_UNKNOWN, 0);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.setViewListener(this);
    }

    @Override
    public void onPause() {
        presenter.clearViewListener();
        super.onPause();
    }

    // StatusViewListener

    @Override
    public void onData(ThunderBoardDevice device) {
        Timber.d("name: %s, state: %d", device.getName(), device.getState());
        deviceName.setText(device.getName());
        if (device.getFirmwareVersion() == null || device.getFirmwareVersion().isEmpty()) {
            deviceFirmware.setText(getString(R.string.status_no_firmware_version));
        } else {
            deviceFirmware.setText(device.getFirmwareVersion());
        }
        int resourceId;
        switch (device.getState()) {
            case STATE_CONNECTED:
                resourceId = R.string.status_connected;
                isConnecting = false;
                batteryIndicator.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                break;
            case STATE_CONNECTING:
                resourceId = R.string.status_connecting;
                isConnecting = true;
                batteryIndicator.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            case STATE_DISCONNECTING:
                resourceId = STATE_DISCONNECTING;
                isConnecting = false;
                batteryIndicator.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                break;
            default:
                int titleId;
                int messageId;
                if (isConnecting) {
                    titleId = R.string.status_unable_to_connect;
                    messageId = R.string.status_unable_to_connect_long;
                } else {
                    titleId = R.string.status_connection_lost;
                    messageId = R.string.status_connection_lost_long;
                }
                resourceId = R.string.status_disconnected;
                isConnecting = false;
                batteryIndicator.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                animateDown();
                showNotConnectedDialog(device.getName(), titleId, messageId);

                if(getActivity() instanceof BaseDemoActivity) {
                    ((BaseDemoActivity) getActivity()).onDisconnected();
                }
                break;

        }
        deviceStatus.setText(getString(resourceId));
        int powerSource;
        if (device.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE) {
            powerSource = device.getPowerSource();
        } else {
            powerSource = ThunderBoardConstants.POWER_SOURCE_TYPE_COIN_CELL;
        }
        Timber.d("Power source: %d", powerSource);
        batteryIndicator.setBatteryValue(powerSource, device.getBatteryLavel());
    }

    protected ActivityComponent component() {
        if (component == null) {
            component = ((ThunderBoardActivity) getActivity()).component();
        }

        return component;
    }

    private void showNotConnectedDialog(String deviceName, int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setMessage(String.format(getString(messageId), deviceName))
                .setTitle(titleId)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        Intent intent = NavUtils.getParentActivityIntent(getActivity());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        NavUtils.navigateUpTo(getActivity(), intent);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void animateDown() {
        Animator animator = AnimatorInflater.loadAnimator(getActivity(), R.animator.animator_down);
        animator.setTarget(rootView);
        animator.start();
    }
}
