package com.silabs.thunderboard.demos.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.silabs.thunderboard.R;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DemoEnvironmentActivity extends BaseDemoActivity implements DemoEnvironmentListener {

    @Bind(R.id.temperature)
    DemoEnvironmentTemperatureControl temperatureControl;

    @Bind(R.id.humidity)
    DemoEnvironmentHumidityControl humidityControl;

    @Bind(R.id.ambient_light)
    DemoEnvironmentAmbientLightControl ambientLightControl;

    @Bind(R.id.uv_index)
    DemoEnvironmentUVControl uvIndexControl;

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
    }

    @Override
    protected BaseDemoPresenter getDemoPresenter() {
        return presenter;
    }

    @Override
    public int getToolbarColor() {
        return getResourceColor(R.color.sl_medium_green);
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

    public void setTemperatureEnabled(boolean enabled) {
        temperatureControl.setEnabled(enabled);
    }

    public void setHumidityEnabled(boolean enabled) {
        humidityControl.setEnabled(enabled);
    }

    public void setUvIndexEnabled(boolean enabled) {
        uvIndexControl.setEnabled(enabled);
    }

    public void setAmbientLightEnabled(boolean enabled) {
        ambientLightControl.setEnabled(enabled);
    }

}
