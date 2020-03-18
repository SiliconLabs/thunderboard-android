package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;

public class HallStrengthMeter extends BaseEnvironmentMeter {

    public HallStrengthMeter(Context context) {
        this(context, null, 0);
    }

    public HallStrengthMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HallStrengthMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getColorResource(float value) {
        return R.color.sl_violet;
    }

    @Override
    protected int getInactiveIconResource() {
        return R.drawable.icon_magneticfield;
    }

    @Override
    protected int getActiveIconResource() {
        return R.drawable.icon_magneticfield;
    }
}
