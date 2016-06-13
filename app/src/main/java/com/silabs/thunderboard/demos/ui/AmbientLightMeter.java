package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.silabs.thunderboard.R;

public class AmbientLightMeter extends BaseEnvironmentMeter {

    private Context context;

    public AmbientLightMeter(Context context) {
        this(context, null, 0);
    }

    public AmbientLightMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmbientLightMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected int getIconResource() {
        return R.drawable.ic_ambient;
    }

    /**
     * getColor
     *
     * Returns a color based on a light value.
     *
     * @param light (in lx units)
     * @param lightType not used, for compatibility
     * @return color integer value
     */
    @Override
    protected int getColor(float light, int lightType) {
        int colorRes = R.color.sl_terbium_green;

        if (light >= 0 && light <= 40) {
            colorRes = R.color.sl_dark_violet;
        } else if (light > 40 && light <= 80) {
            colorRes = R.color.sl_violet;
        } else if (light > 80 && light <= 120) {
            colorRes = R.color.sl_light_violet;
        } else if (light > 120 && light <= 160) {
            colorRes = R.color.sl_white_violet;
        } else if (light > 160 && light <= 200) {
            colorRes = R.color.sl_white;
        } else if (light > 200 && light <= 300) {
            colorRes = R.color.sl_light_peach;
        } else if (light > 300 && light <= 500) {
            colorRes = R.color.sl_peach_gold;
        } else if (light > 500 && light <= 1000) {
            colorRes = R.color.sl_pink;
        } else if (light > 1000 && light <= 10000) {
            colorRes = R.color.sl_bromine_orange;
        } else if (light > 10000) {
            colorRes = R.color.sl_yellow_orange;
        }


        return context.getResources().getColor(colorRes);
    }
}
