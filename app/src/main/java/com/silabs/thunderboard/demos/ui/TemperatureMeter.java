package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;

public class TemperatureMeter extends BaseEnvironmentMeter {

    public TemperatureMeter(Context context) {
        this(context, null, 0);
    }

    public TemperatureMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TemperatureMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getInactiveIconResource() {
        return R.drawable.ic_temp_inactive;
    }

    @Override
    protected int getActiveIconResource() {
        return R.drawable.ic_temp;
    }

    @Override
    protected int getColorResource(float value) {
        return RangeColor.Temperature.getColorRes(value);
    }
}
