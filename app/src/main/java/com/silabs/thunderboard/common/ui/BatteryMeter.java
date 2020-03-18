package com.silabs.thunderboard.common.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.silabs.thunderboard.R;

public class BatteryMeter extends ImageView {

    public static final int USB_POWER = -1;

    public BatteryMeter(Context context) {
        this(context, null, 0);
    }

    public BatteryMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setValue(int value) {
        int imageResource = R.drawable.icon_battery_0;
        if (value >= 0 && value <= 9) {
            imageResource = R.drawable.icon_battery_0;
        } else if (value > 9 && value <= 25) {
            imageResource = R.drawable.icon_battery_1;
        } else if (value > 25 && value <= 50) {
            imageResource = R.drawable.icon_battery_2;
        } else if (value > 50 && value <= 75) {
            imageResource = R.drawable.icon_battery_3;
        } else if (value > 75 && value <= 100) {
            imageResource = R.drawable.icon_battery_4;
        }

        setImageResource(imageResource);
    }
}
