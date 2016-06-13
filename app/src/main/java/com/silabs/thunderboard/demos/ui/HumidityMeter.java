package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;

public class HumidityMeter extends BaseEnvironmentMeter {

    private Context context;

    public HumidityMeter(Context context) {
        this(context, null, 0);
    }

    public HumidityMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HumidityMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_humidity;
    }

    @Override
    protected int getColor(float humidity, int humidityType) {
        int colorRes = R.color.sl_terbium_green;

        if (humidity <= 45) {
            colorRes = R.color.sl_blue;
        } else if (humidity >= 46 && humidity <= 50) {
            colorRes = R.color.sl_terbium_green;
        } else if (humidity >= 51 && humidity <= 55) {
            colorRes = R.color.sl_yellow;
        } else if (humidity >= 56 && humidity <= 60) {
            colorRes = R.color.sl_yellow_orange;
        } else if (humidity >= 61 && humidity <= 65) {
            colorRes = R.color.sl_bromine_orange;
        } else if (humidity > 65) {
            colorRes = R.color.sl_red_orange;
        }
        return context.getResources().getColor(colorRes);
    }
}
