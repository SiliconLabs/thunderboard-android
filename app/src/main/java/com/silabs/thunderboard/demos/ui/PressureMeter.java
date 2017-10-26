package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;

public class PressureMeter extends BaseEnvironmentMeter {
    public PressureMeter(Context context) {
        this(context, null, 0);
    }

    public PressureMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PressureMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getInactiveIconResource() {
        return R.drawable.ic_atmospheric_pressure_inactive;
    }

    @Override
    protected int getActiveIconResource() {
        return R.drawable.ic_atmospheric_pressure;
    }

    @Override
    protected int getColorResource(float value) {
        return RangeColor.Pressure.getColorRes(value);
    }
}
