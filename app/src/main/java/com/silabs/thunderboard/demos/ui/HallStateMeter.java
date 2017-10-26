package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.demos.model.HallState;

public class HallStateMeter extends BaseEnvironmentMeter {

    @HallState
    private int hallState;

    public HallStateMeter(Context context) {
        this(context, null, 0);
    }

    public HallStateMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HallStateMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setValue(float value, boolean enabled) {
        hallState = (int) value;
        activeBitmap = BitmapFactory.decodeResource(getResources(), getActiveIconResource());
        super.setValue(value, enabled);
    }

    @Override
    protected int getColorResource(float value) {
        switch (hallState) {
            case HallState.CLOSED:
                return R.color.teal_2;
            case HallState.TAMPERED:
                return R.color.sl_red;
            case HallState.OPENED:
            default:
                return R.color.sl_silicon_grey;
        }
    }

    @Override
    protected int getInactiveIconResource() {
        return R.drawable.icn_demo_hall_effect_opened;
    }

    @Override
    protected int getActiveIconResource() {
        switch (hallState) {
            case HallState.CLOSED:
                return R.drawable.icn_demo_hall_effect_closed;
            case HallState.TAMPERED:
                return R.drawable.icn_demo_hall_effect_tampered;
            case HallState.OPENED:
            default:
                return R.drawable.icn_demo_hall_effect_opened;
        }
    }
}
