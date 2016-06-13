package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;

public class UVMeter extends BaseEnvironmentMeter {

    private Context context;

    public UVMeter(Context context) {
        this(context, null, 0);
    }

    public UVMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UVMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_uv;
    }

    /**
     * getColor
     *
     * Returns a color integer value based on the uvIndex value.
     *
     * @param uvIndex
     * @param uvIndexType (not used)
     * @return color integer value
     */
    @Override
    protected int getColor(float uvIndex, int uvIndexType) {
        int colorRes = R.color.sl_terbium_green;

        if (uvIndex >= 0 && uvIndex < 3) {
            colorRes = R.color.sl_terbium_green;
        } else if (uvIndex >= 3 && uvIndex < 6) {
            colorRes = R.color.sl_yellow;
        } else if (uvIndex >= 6 && uvIndex < 8) {
            colorRes = R.color.sl_yellow_orange;
        } else if (uvIndex >= 8 && uvIndex < 11) {
            colorRes = R.color.sl_red_orange;
        } else if (uvIndex >= 11) {
            colorRes = R.color.sl_violet;
        }

        return context.getResources().getColor(colorRes);
    }
}
