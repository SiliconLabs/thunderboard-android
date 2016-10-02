package com.silabs.thunderboard.demos.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardType;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;
import com.silabs.thunderboard.demos.model.LedRGBState;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DemoMotionActivity extends GdxDemoActivity implements DemoMotionListener {

    private static final float SCALE = 0.02f;

    @Bind(R.id.car_animation)
    FrameLayout carAnimationHolder;

    @Bind(R.id.orientation_x)
    TextView orientationX;

    @Bind(R.id.orientation_y)
    TextView orientationY;

    @Bind(R.id.orientation_z)
    TextView orientationZ;

    @Bind(R.id.acceleration_x)
    TextView accelerationX;

    @Bind(R.id.acceleration_y)
    TextView accelerationY;

    @Bind(R.id.acceleration_z)
    TextView accelerationZ;

    @Bind(R.id.speed)
    TextView speedText;

    @Bind(R.id.speed_units)
    TextView speedUnitsText;

    @Bind(R.id.rpm)
    TextView rpmText;

    @Bind(R.id.distance)
    TextView distanceText;

    @Bind(R.id.distance_units)
    TextView distanceUnitsText;

    @Bind(R.id.revolutions)
    TextView revolutionsText;

    @Bind(R.id.wheel_diameter)
    TextView wheelDiameterText;

    @Bind(R.id.wheel_container)
    LinearLayout wheelContainer;

    @Bind(R.id.rpm_container)
    LinearLayout rpmContainer;

    @Bind(R.id.linear_speed_container)
    LinearLayout linearSpeedContainer;

    @Bind(R.id.revolutions_container)
    LinearLayout revolutionsContainer;

    @Bind(R.id.linear_distance_container)
    LinearLayout linearDistanceContainer;

    @Bind(R.id.speed_distance_container)
    LinearLayout speedDistanceContainer;

    @Bind(R.id.speed_distance_scroll)
    ScrollView speedDistanceScroll;

    @Bind(R.id.speed_container)
    LinearLayout speedContainer;

    @Bind(R.id.distance_container)
    LinearLayout distanceContainer;

    private AlertDialog calibratingDialog;
    private View gdx3dView;
    private DemoMotionGdxAdapter gdxAdapter;
    private int assetType;
    private boolean sceneLoaded;

    public static boolean isDemoAllowed() {
        return true;
    }

    @Inject
    DemoMotionPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        component().inject(this);

        if (presenter.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE) {
            View view = LayoutInflater.from(this).inflate(R.layout.activity_demo_motion_sense, null, false);
            mainSection.addView(view);
        } else {
            View view = LayoutInflater.from(this).inflate(R.layout.activity_demo_motion, null, false);
            mainSection.addView(view);
        }

        ButterKnife.bind(this);


        assetType = prefsManager.getPreferences().modelType;
        if (presenter.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE || assetType == ThunderBoardPreferences.MODEL_TYPE_BOARD) {
            setupBoardView();
        }

        // temp
        setOrientation(0f, 0f, 0f);
        setAcceleration(0f, 0f, 0f);
        setSpeed(0f, 0, 0);
        setDistance(0f, 0, 0);

        gdxAdapter = new DemoMotionGdxAdapter(getContext().getResources().getColor(R.color.sl_light_grey), assetType, presenter.getThunderBoardType());
        initControls();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.clearViewListener();
    }

    private void setupBoardView() {

        int padding = getResources().getDimensionPixelSize(R.dimen.motion_speed_dist_padding);

        speedContainer.setPadding(0, 0, padding, 0);
        speedContainer.setWeightSum(1f);
        distanceContainer.setPadding(padding, 0, 0, 0);
        distanceContainer.setWeightSum(1f);

        wheelContainer.setVisibility(View.INVISIBLE);

        linearSpeedContainer.setVisibility(View.GONE);
        linearSpeedContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));

        linearDistanceContainer.setVisibility(View.GONE);
        linearDistanceContainer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0f));

        speedDistanceContainer.setOrientation(LinearLayout.HORIZONTAL);

        if (presenter.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE) {
            wheelContainer.setVisibility(View.GONE);
            speedDistanceScroll.setVisibility(View.GONE);
        } else {
            setWheelDiameterText();
        }
    }

    @Override
    protected BaseDemoPresenter getDemoPresenter() {
        return presenter;
    }

    @Override
    public int getToolbarColor() {
        return getResourceColor(R.color.sl_terbium_green);
    }

    @Override
    public String getToolbarString() {
        return getString(R.string.demo_motion);
    }

    /**
     * setAcceleration
     * <p/>
     * Listener for acceleration measurements.
     * Displays x, y, z acceleration vector magnitudes in TextViews
     * <p/>
     * Acceleration values are in multiples of g (gravitational acceleration, 9.8 m / s^2)
     *
     * @param x
     * @param y
     * @param z
     */
    @Override
    public void setAcceleration(float x, float y, float z) {
        String accelerationString = getString(R.string.motion_acceleration_g);
        accelerationX.setText(String.format(accelerationString, x));
        accelerationY.setText(String.format(accelerationString, y));
        accelerationZ.setText(String.format(accelerationString, z));
    }

    /**
     * setOrientation
     * <p/>
     * Listener for orientation measurements.
     * Displays orientation of ThunderBoard around x, y, z axes.
     * <p/>
     * Angles are measured in degrees ( -180 to 180)
     *
     * @param x
     * @param y
     * @param z
     */
    @Override
    public void setOrientation(float x, float y, float z) {
        String degreeString = getString(R.string.motion_orientation_degree);
        orientationX.setText(String.format(degreeString, x));
        orientationY.setText(String.format(degreeString, y));
        orientationZ.setText(String.format(degreeString, z));

        if (gdxAdapter != null) {
            gdxAdapter.setOrientation(x, y, z);
        }
    }

    /**
     * setWheelDiameterText
     * <p/>
     * displays the value of the wheel's diameter, which can either be
     * found in the Preferences, or hard-coded in ThunderBoardPreferences.
     */
    public void setWheelDiameterText() {
        ThunderBoardPreferences preferences = prefsManager.getPreferences();

        float wheelRadius = (preferences.wheelRadius == 0)
                ? ThunderBoardPreferences.DEFAULT_WHEEL_RADIUS : preferences.wheelRadius;

        String outString = "";
        if (preferences.measureUnitType == ThunderBoardPreferences.UNIT_METRIC) {
            outString = String.format(getString(R.string.motion_diameter_metric), wheelRadius * 200.0f);
        } else {
            outString = String.format(getString(R.string.motion_diameter_us), wheelRadius * 2.0f * 39.37f);
        }
        wheelDiameterText.setText(outString);
    }

    /**
     * setSpeed
     * <p/>
     * Listener method to display the speed of the car holding the ThunderBoard.
     * Both speed and rpm are given and the units for the speed can be either
     * metric (m / s) or US (ft / s)
     *
     * @param speed
     * @param rpm
     * @param measurementsType
     */
    @Override
    public void setSpeed(double speed, int rpm, int measurementsType) {
        speedText.setText(String.format("%.1f", speed));
        if (measurementsType == ThunderBoardPreferences.UNIT_METRIC) {
            speedUnitsText.setText(getString(R.string.motion_meters_per_second));
        } else {
            speedUnitsText.setText(getString(R.string.motion_feet_per_second));
        }
        rpmText.setText(String.valueOf(rpm));
    }

    /**
     * setDistance
     * <p/>
     * Listener method to display the distance covered and the number of wheel
     * revolutions for the model car holding the ThunderBoard. The distance can
     * be measured in either meters or feet.
     *
     * @param distance
     * @param revolutions
     * @param measurementsType
     */
    @Override
    public void setDistance(double distance, int revolutions, int measurementsType) {
        distanceText.setText(String.format("%.1f", distance));
        if (measurementsType == ThunderBoardPreferences.UNIT_METRIC) {
            distanceUnitsText.setText(getString(R.string.motion_meters));
        } else {
            distanceUnitsText.setText(getString(R.string.motion_feet));
        }
        revolutionsText.setText(String.valueOf(revolutions));
    }

    /**
     * onCalibrate
     * <p/>
     * Click listener for the Calibrate button
     */
    @OnClick(R.id.calibrate)
    public void onCalibrate() {
        popupCalibratingDialog();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                presenter.calibrate();
            }
        }, 600);
    }

    /**
     * onCalibrateComleted
     * <p/>
     * Listener method called when the calibration is completed. Closes the alert dialog.
     */
    @Override
    public void onCalibrateComleted() {
        closeCalibratingDialog();
    }

    @Override
    public void setColorLED(LedRGBState colorLED) {
        if (!sceneLoaded) return;
        if (colorLED.on) {
            gdxAdapter.setLEDColor(Color.rgb(colorLED.color.red, colorLED.color.green, colorLED.color.blue));
            gdxAdapter.turnOnLights();
        }
    }

    /**
     * popupCalibrateDialog
     * <p/>
     * Creates an non-cancellable alert dialog to display while calibrating
     */
    private void popupCalibratingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.motion_calibrating);
        builder.setCancelable(false);
        builder.setMessage(R.string.motion_calibrating_message);

        calibratingDialog = builder.create();
        calibratingDialog.show();
    }

    /**
     * closeCalibratingDialog
     * <p/>
     * Dismisses the calibrating dialog
     */
    private void closeCalibratingDialog() {
        calibratingDialog.dismiss();
    }

    @Override
    public void initControls() {
        gdxAdapter.setOnSceneLoadedListener(new DemoMotionGdxAdapter.OnSceneLoadedListener() {
            @Override
            public void onSceneLoaded() {
                sceneLoaded = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        presenter.setViewListener(DemoMotionActivity.this, deviceAddress);
                    }
                });
            }
        });

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.disableAudio = true;
        config.hideStatusBar = false;
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useImmersiveMode = false;
        config.useWakelock = false;

        gdx3dView = initializeForView(gdxAdapter, config);
        carAnimationHolder.addView(gdx3dView);
    }
}
