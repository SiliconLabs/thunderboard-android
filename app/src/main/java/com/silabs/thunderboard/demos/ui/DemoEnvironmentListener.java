package com.silabs.thunderboard.demos.ui;

public interface DemoEnvironmentListener extends BaseDemoViewListener {

    void setTemperature(float temperature, int temperatureType);
    void setHumidity(int humidity);
    void setUvIndex(int uvIndex);
    void setAmbientLight(long ambientLight);

    void setTemperatureEnabled(boolean enabled);
    void setHumidityEnabled(boolean enabled);
    void setUvIndexEnabled(boolean enabled);
    void setAmbientLightEnabled(boolean enabled);
}
