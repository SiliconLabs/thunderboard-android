package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;

public class VOCMeter extends BaseEnvironmentMeter {
    public VOCMeter(Context context) {
        this(context, null, 0);
    }

    public VOCMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VOCMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getInactiveIconResource() {
        return R.drawable.ic_voc_inactive;
    }

    @Override
    protected int getActiveIconResource() {
        return R.drawable.ic_voc;
    }

    @Override
    protected int getColorResource(float value) {
        return RangeColor.VOC.getColorRes(value);
    }
}
