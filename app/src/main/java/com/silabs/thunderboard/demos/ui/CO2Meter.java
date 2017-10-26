package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;

public class CO2Meter extends BaseEnvironmentMeter {

    public CO2Meter(Context context) {
        this(context, null, 0);
    }

    public CO2Meter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CO2Meter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getInactiveIconResource() {
        return R.drawable.ic_carbon_dioxide_inactive;
    }

    @Override
    protected int getActiveIconResource() {
        return R.drawable.ic_carbon_dioxide;
    }

    @Override
    protected int getColorResource(float value) {
        return RangeColor.CO2.getColorRes(value);
    }
}
