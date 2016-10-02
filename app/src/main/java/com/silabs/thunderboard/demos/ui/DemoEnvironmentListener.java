package com.silabs.thunderboard.demos.ui;

public interface DemoEnvironmentListener extends BaseDemoViewListener {

    void setTemperature(float temperature, int temperatureType);
    void setHumidity(int humidity);
    void setUvIndex(int uvIndex);
    void setAmbientLight(long ambientLight);
    void setSoundLevel(float soundLevel);
    void setPressure(float pressure);
    void setCO2Level(int co2Level);
    void setTVOCLevel(int tvocLevel);

    void setTemperatureEnabled(boolean enabled);
    void setHumidityEnabled(boolean enabled);
    void setUvIndexEnabled(boolean enabled);
    void setAmbientLightEnabled(boolean enabled);
    void setSoundLevelEnabled(boolean enabled);
    void setPressureEnabled(boolean enabled);
    void setCO2LevelEnabled(boolean enabled);
    void setTVOCLevelEnabled(boolean enabled);
    void setPowerSource(int powerSource);
}
