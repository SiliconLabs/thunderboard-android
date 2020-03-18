package com.silabs.thunderboard.scanner.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.silabs.thunderboard.R;

public class SignalStrengthMeter extends ImageView {

    public SignalStrengthMeter(Context context) {
        this(context, null, 0);
    }

    public SignalStrengthMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalStrengthMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * setStrength
     *
     * Depending on the signal strength, will swap between bitmap
     * resources to display in the control.
     *
     * The dbValue ranges from -100 to 0.
     *
     * @param dbValue
     */
    public void setStrength(int dbValue) {
        int bitmapResource;

        if (dbValue >= -20) {
            bitmapResource = R.drawable.icon_bars_5;
        } else if (dbValue < -20 && dbValue >= -40) {
            bitmapResource = R.drawable.icon_bars_4;
        } else if (dbValue < -40 && dbValue >= -60) {
            bitmapResource = R.drawable.icon_bars_3;
        } else if (dbValue < -60 && dbValue >= -80) {
            bitmapResource = R.drawable.icon_bars_2;
        } else if (dbValue < -80 && dbValue >= -90) {
            bitmapResource = R.drawable.icon_bars_1;
        } else {
            bitmapResource = R.drawable.icon_bars_0;
        }
        setImageResource(bitmapResource);

    }
}
