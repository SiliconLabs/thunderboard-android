package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;

public class TemperatureMeter extends BaseEnvironmentMeter {

    private Context context;

    public TemperatureMeter(Context context) {
        this(context, null, 0);
    }

    public TemperatureMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TemperatureMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_temp;
    }

    /**
     * getColor
     *
     * Returns a color int value that is based on the given temperature.
     *
     * @param temperature
     * @param temperatureType Celsius (metric) or Fahrenheit (US)
     * @return color int value
     */
    protected int getColor(float temperature, int temperatureType) {
        int colorRes = R.color.sl_terbium_green;
        if (temperatureType == ThunderBoardPreferences.TEMP_FAHRENHEIT) {
            // Fahrenheit
            if (temperature > 100) {
                colorRes = R.color.sl_red;
            } else if (temperature <= 100 && temperature > 90) {
                colorRes = R.color.sl_red_orange;
            } else if (temperature <= 90 && temperature > 80) {
                colorRes = R.color.sl_pink;
            } else if (temperature <= 80 && temperature > 70) {
                colorRes = R.color.sl_yellow_orange;
            } else if (temperature <= 70 && temperature > 60) {
                colorRes = R.color.sl_yellow;
            } else if (temperature <= 60 && temperature > 50) {
                colorRes = R.color.sl_bright_green;
            } else if (temperature <= 50 && temperature > 40) {
                colorRes = R.color.sl_terbium_green;
            } else if (temperature <= 40 && temperature > 30) {
                colorRes = R.color.sl_medium_green;
            } else if (temperature <= 30 && temperature > 20) {
                colorRes = R.color.sl_light_blue;
            } else if (temperature <= 20 && temperature > 10) {
                colorRes = R.color.sl_blue;
            } else if (temperature <= 10 && temperature > 0) {
                colorRes = R.color.sl_dark_violet;
            } else if (temperature <= 0 && temperature > -10) {
                colorRes = R.color.sl_violet;
            } else if (temperature <= -10 && temperature >= -20) {
                colorRes = R.color.sl_dark_grey;
            } else if (temperature < -20) {
                colorRes = R.color.sl_grey;
            }
        } else {
            // Celsius
            if (temperature > 37.7) {
                colorRes = R.color.sl_red;
            } else if (temperature <= 37.7f && temperature > 32.2f) {
                colorRes = R.color.sl_red_orange;
            } else if (temperature <= 32.2f && temperature > 26.6f) {
                colorRes = R.color.sl_pink;
            } else if (temperature <= 26.6f && temperature > 21.1f) {
                colorRes = R.color.sl_yellow_orange;
            } else if (temperature <= 21.1f && temperature > 15.5f) {
                colorRes = R.color.sl_yellow;
            } else if (temperature <= 15.5f && temperature > 10.0f) {
                colorRes = R.color.sl_bright_green;
            } else if (temperature <= 10.0f && temperature > 4.4f) {
                colorRes = R.color.sl_terbium_green;
            } else if (temperature <= 4.4f && temperature > -1.1f) {
                colorRes = R.color.sl_medium_green;
            } else if (temperature <= -1.1f && temperature > -6.6f) {
                colorRes = R.color.sl_light_blue;
            } else if (temperature <= -6.6f && temperature > -12.2f) {
                colorRes = R.color.sl_blue;
            } else if (temperature <= -12.2f && temperature > -17.7f) {
                colorRes = R.color.sl_dark_violet;
            } else if (temperature <= -17.7f && temperature > -23.3f) {
                colorRes = R.color.sl_violet;
            } else if (temperature <= -23.3f && temperature >= -28.8f) {
                colorRes = R.color.sl_dark_grey;
            } else if (temperature < -28.8f) {
                colorRes = R.color.sl_grey;
            }
        }

        return context.getResources().getColor(colorRes);
    }
}
