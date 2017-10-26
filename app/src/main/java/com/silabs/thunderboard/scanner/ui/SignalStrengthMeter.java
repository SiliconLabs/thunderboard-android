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
            bitmapResource = R.drawable.signal_5bar;
        } else if (dbValue < -20 && dbValue >= -40) {
            bitmapResource = R.drawable.signal_4bar;
        } else if (dbValue < -40 && dbValue >= -60) {
            bitmapResource = R.drawable.signal_3bar;
        } else if (dbValue < -60 && dbValue >= -80) {
            bitmapResource = R.drawable.signal_2bar;
        } else if (dbValue < -80 && dbValue >= -90) {
            bitmapResource = R.drawable.signal_1bar;
        } else {
            bitmapResource = R.drawable.signal_0bar;
        }
        setImageResource(bitmapResource);

    }
}
