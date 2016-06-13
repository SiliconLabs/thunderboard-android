package com.silabs.thunderboard.common.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.silabs.thunderboard.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class BatteryIndicator extends FrameLayout {

    @Bind(R.id.battery_percent)
    TextView batteryPercent;

    @Bind(R.id.battery_meter)
    BatteryMeter batteryMeter;

    public BatteryIndicator(Context context) {
        this(context, null, 0);
    }

    public BatteryIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.battery_indicator, this, false);
        addView(view);

        ButterKnife.bind(this, view);
    }

    public void setBatteryValue(int batteryValue) {
        batteryPercent.setText(String.format("%d%%", batteryValue));
        batteryMeter.setValue(batteryValue);
    }
}
