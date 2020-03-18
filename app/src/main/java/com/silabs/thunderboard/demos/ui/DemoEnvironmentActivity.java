package com.silabs.thunderboard.demos.ui;

import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.demos.model.HallState;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.silabs.thunderboard.common.app.ThunderBoardConstants.POWER_SOURCE_TYPE_COIN_CELL;
import static com.silabs.thunderboard.common.app.ThunderBoardConstants.POWER_SOURCE_TYPE_UNKNOWN;

public class DemoEnvironmentActivity extends BaseDemoActivity implements DemoEnvironmentListener {

    DemoEnvironmentTemperatureControl temperatureControl;

    DemoEnvironmentHumidityControl humidityControl;

    DemoEnvironmentAmbientLightControl ambientLightControl;

    DemoEnvironmentUVControl uvIndexControl;

    DemoEnvironmentPressureControl pressureControl;

    DemoEnvironmentSoundLevelControl soundLevelControl;

    DemoEnvironmentCO2Control co2Control;

    DemoEnvironmentVOCControl vocControl;

    DemoEnvironmentHallStrengthControl hallStrengthControl;

    DemoEnvironmentHallStateControl hallStateControl;

    @BindView(R.id.env_grid)
    android.support.v7.widget.GridLayout envGrid;

    private int powerSource;

    public static boolean isDemoAllowed() {
        return true;
    }

    @Inject
    DemoEnvironmentPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_demo_environment, null, false);
        mainSection.addView(view);

        ButterKnife.bind(this);
        component().inject(this);

        presenter.setViewListener(this, deviceAddress);

        setupEnvList();
        initControls();

//        disable firebase for reskinning
//        checkFirebaseConnectivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.checkSettings();
    }

    private void setupEnvList() {
        temperatureControl = new DemoEnvironmentTemperatureControl(this);
        humidityControl = new DemoEnvironmentHumidityControl(this);
        ambientLightControl = new DemoEnvironmentAmbientLightControl(this);
        uvIndexControl = new DemoEnvironmentUVControl(this);
        pressureControl = new DemoEnvironmentPressureControl(this);
        soundLevelControl = new DemoEnvironmentSoundLevelControl(this);
        co2Control = new DemoEnvironmentCO2Control(this);
        vocControl = new DemoEnvironmentVOCControl(this);
        hallStrengthControl = new DemoEnvironmentHallStrengthControl(this);
        hallStateControl = new DemoEnvironmentHallStateControl(this);

        temperatureControl.setLayoutParams(getLayoutParams());
        soundLevelControl.setLayoutParams(getLayoutParams());
        ambientLightControl.setLayoutParams(getLayoutParams());
        uvIndexControl.setLayoutParams(getLayoutParams());
        humidityControl.setLayoutParams(getLayoutParams());
        pressureControl.setLayoutParams(getLayoutParams());
        co2Control.setLayoutParams(getLayoutParams());
        vocControl.setLayoutParams(getLayoutParams());
        hallStrengthControl.setLayoutParams(getLayoutParams());
        hallStateControl.setLayoutParams(getLayoutParams());

        hallStateControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onHallStateClick();
            }
        });
    }

    private GridLayout.LayoutParams getLayoutParams() {
        GridLayout.LayoutParams layoutParams;
        layoutParams = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED, 1f),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
        layoutParams.width = 0;
        return layoutParams;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.clearViewListener();
    }

    @Override
    protected BaseDemoPresenter getDemoPresenter() {
        return presenter;
    }

    @Override
    public int getToolbarColor() {
        return getResourceColor(R.color.tb_red);
    }

    @Override
    public String getToolbarString() {
        return getString(R.string.demo_environment);
    }

    @Override
    public void setTemperature(float temperature, int temperatureType) {
        if (temperatureControl.isEnabled()) {
            temperatureControl.setTemperature(temperature, temperatureType);
        }
    }

    @Override
    public void setHumidity(int humidity) {
        if (humidityControl.isEnabled()) {
            humidityControl.setHumidity(humidity);
        }
    }

    @Override
    public void setUvIndex(int uvIndex) {
        if (uvIndexControl.isEnabled()) {
            uvIndexControl.setUVIndex(uvIndex);
        }
    }

    @Override
    public void setAmbientLight(long ambientLight) {
        if (ambientLightControl.isEnabled()) {
            ambientLightControl.setAmbientLight(ambientLight);
        }
    }

    @Override
    public void setSoundLevel(float soundLevel) {
        if (soundLevelControl.isEnabled()) {
            soundLevelControl.setSoundLevel((int) soundLevel);
        }
    }

    @Override
    public void setPressure(float pressure) {
        if (pressureControl.isEnabled()) {
            pressureControl.setPressure((int) pressure);
        }
    }

    @Override
    public void setCO2Level(int co2Level) {
        if (co2Control.isEnabled()) {
            co2Control.setCO2(co2Level);
        }
    }

    @Override
    public void setTVOCLevel(int vocLevel) {
        if (vocControl.isEnabled()) {
            vocControl.setVOC(vocLevel);
        }
    }

    @Override
    public void setHallStrength(float hallStrength) {
        if (hallStrengthControl.isEnabled()) {
            hallStrengthControl.setHallStrength(hallStrength);
        }
    }

    @Override
    public void setHallState(@HallState int hallState) {
        if (hallStateControl.isEnabled()) {
            hallStateControl.setHallState(hallState);
        }
    }

    @Override
    public void setTemperatureEnabled(boolean enabled) {
        temperatureControl.setEnabled(enabled);
    }

    @Override
    public void setHumidityEnabled(boolean enabled) {
        humidityControl.setEnabled(enabled);
    }

    @Override
    public void setUvIndexEnabled(boolean enabled) {
        uvIndexControl.setEnabled(enabled);
    }

    @Override
    public void setAmbientLightEnabled(boolean enabled) {
        ambientLightControl.setEnabled(enabled);
    }

    @Override
    public void setSoundLevelEnabled(boolean enabled) {
        soundLevelControl.setEnabled(enabled);
    }

    @Override
    public void setPressureEnabled(boolean enabled) {
        pressureControl.setEnabled(enabled);
    }

    @Override
    public void setCO2LevelEnabled(boolean enabled) {
        co2Control.setEnabled(enabled);
    }

    @Override
    public void setTVOCLevelEnabled(boolean enabled) {
        vocControl.setEnabled(enabled);
    }

    @Override
    public void setHallStrengthEnabled(boolean enabled) {
        hallStrengthControl.setEnabled(enabled);
    }

    @Override
    public void setHallStateEnabled(boolean enabled) {
        hallStateControl.setEnabled(enabled);
    }

    boolean sufficientPower() {
        switch (powerSource) {
            case POWER_SOURCE_TYPE_UNKNOWN:
            case POWER_SOURCE_TYPE_COIN_CELL:
                return false;
            default:
                break;
        }
        return true;
    }

    @Override
    public void setPowerSource(int powerSource) {
        this.powerSource = powerSource;
    }

    @Override
    public void intGrid() {
        envGrid.addView(temperatureControl);

        if (presenter.bleManager.characteristicHumidityAvailable) {
            envGrid.addView(humidityControl);
        }
        if (presenter.bleManager.characteristicAmbientLightReactAvailable || presenter.bleManager.characteristicAmbientLightSenseAvailable) {
            envGrid.addView(ambientLightControl);
        }
        if (presenter.bleManager.characteristicUvIndexAvailable) {
            envGrid.addView(uvIndexControl);
        }
        if (presenter.bleManager.characteristicPressureAvailable) {
            envGrid.addView(pressureControl);
        }
        if (presenter.bleManager.characteristicSoundLevelAvailable) {
            envGrid.addView(soundLevelControl);
        }
        if (presenter.bleManager.characteristicCo2ReadingAvailable && sufficientPower()) {
            envGrid.addView(co2Control);
        }
        if (presenter.bleManager.characteristicTvocReadingAvailable && sufficientPower()) {
            envGrid.addView(vocControl);
        }
        if (presenter.bleManager.characteristicHallFieldStrengthAvailable) {
            envGrid.addView(hallStrengthControl);
        }
        if (presenter.bleManager.characteristicHallStateAvailable) {
            envGrid.addView(hallStateControl);
        }
    }

    @Override
    public void initControls() {
        // disable everything at first...
        setTemperatureEnabled(false);
        setHumidityEnabled(false);
        setAmbientLightEnabled(false);
        setUvIndexEnabled(false);
        setPressureEnabled(false);
        setSoundLevelEnabled(false);
        setCO2LevelEnabled(false);
        setTVOCLevelEnabled(false);
        setHallStrengthEnabled(false);
        setHallStateEnabled(false);
    }


    private void addViewToGridIfNotAddedYet(View view) {
        if (view.getParent() == null) {
            envGrid.addView(view);
        }

    }
}
