package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.silabs.thunderboard.R;

/**
 * Displays an icon and the CO2 value in a combo control.
 *
 * The CO2Meter and the TextView are added to the layout dynamically
 *
 */
public class DemoEnvironmentCO2Control extends LinearLayout {

    private CO2Meter co2Meter;
    private TextView textView;

    public DemoEnvironmentCO2Control(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentCO2Control(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentCO2Control(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_co2);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        co2Meter = new CO2Meter(context);
        layout.addView(co2Meter);
        setEnabled(false);
        
        setCO2(0);
    }

    public void setCO2(int co2Level) {
        if (isEnabled()) {
            textView.setText(String.format(getContext().getString(R.string.environment_co2_measure),
                    co2Level));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        co2Meter.setValue((float) co2Level, isEnabled());
    }
}
