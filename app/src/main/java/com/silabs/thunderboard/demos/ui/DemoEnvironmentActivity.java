package com.silabs.thunderboard.demos.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.demos.model.HallState;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.GONE;
import static com.silabs.thunderboard.common.app.ThunderBoardConstants.POWER_SOURCE_TYPE_COIN_CELL;
import static com.silabs.thunderboard.common.app.ThunderBoardConstants.POWER_SOURCE_TYPE_UNKNOWN;

public class DemoEnvironmentActivity extends BaseDemoActivity implements DemoEnvironmentListener {

    @Bind(R.id.temperature)
    DemoEnvironmentTemperatureControl temperatureControl;

    @Bind(R.id.humidity)
    DemoEnvironmentHumidityControl humidityControl;

    @Bind(R.id.ambient_light)
    DemoEnvironmentAmbientLightControl ambientLightControl;

    @Bind(R.id.uv_index)
    DemoEnvironmentUVControl uvIndexControl;

    @Bind(R.id.environmentdemo_pressure_sound_block)
    View blockPressureAndSound;

    @Bind(R.id.pressure)
    DemoEnvironmentPressureControl pressureControl;

    @Bind(R.id.sound_level)
    DemoEnvironmentSoundLevelControl soundLevelControl;

    @Bind(R.id.environmentdemo_air_quality_block)
    View blockAirQuality;

    @Bind(R.id.co2)
    DemoEnvironmentCO2Control co2Control;

    @Bind(R.id.voc)
    DemoEnvironmentVOCControl vocControl;

    @Bind(R.id.environmentdemo_magnetic_field_block)
    View blockMagneticField;

    @Bind(R.id.hall_strength)
    DemoEnvironmentHallStrengthControl hallStrengthControl;

    @Bind(R.id.hall_state)
    DemoEnvironmentHallStateControl hallStateControl;

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
        initControls();
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
        if (enabled) {
            blockPressureAndSound.setVisibility(View.VISIBLE);
        }
        soundLevelControl.setEnabled(enabled);
    }

    @Override
    public void setPressureEnabled(boolean enabled) {
        if (enabled) {
            blockPressureAndSound.setVisibility(View.VISIBLE);
        }
        pressureControl.setEnabled(enabled);
    }

    @Override
    public void setCO2LevelEnabled(boolean enabled) {
        if (enabled && sufficientPower()){
            blockAirQuality.setVisibility(View.VISIBLE);
        }
        co2Control.setEnabled(enabled);
    }

    @Override
    public void setTVOCLevelEnabled(boolean enabled) {
        if (enabled && sufficientPower()){
            blockAirQuality.setVisibility(View.VISIBLE);
        }
        vocControl.setEnabled(enabled);
    }

    @Override
    public void setHallStrengthEnabled(boolean enabled) {
        if (enabled) {
            blockMagneticField.setVisibility(View.VISIBLE);
        }
        hallStrengthControl.setEnabled(enabled);
    }

    @Override
    public void setHallStateEnabled(boolean enabled) {
        if (enabled) {
            blockMagneticField.setVisibility(View.VISIBLE);
        }
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
    public void initControls() {
        blockPressureAndSound.setVisibility(GONE);
        blockAirQuality.setVisibility(GONE);
        blockMagneticField.setVisibility(GONE);

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

    @OnClick(R.id.hall_state_container)
    public void onHallStateClick() {
        presenter.onHallStateClick();
    }
}
