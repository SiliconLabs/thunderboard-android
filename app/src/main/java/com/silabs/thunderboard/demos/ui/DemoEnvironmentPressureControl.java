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
 * Displays an icon and the pressure value in a combo control.
 *
 * The PressureMeter and the TextView are added to the layout dynamically
 *
 */
public class DemoEnvironmentPressureControl extends LinearLayout {

    private PressureMeter pressureMeter;
    private TextView textView;

    public DemoEnvironmentPressureControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentPressureControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentPressureControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_pressure);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        pressureMeter = new PressureMeter(context);
        layout.addView(pressureMeter);
        setEnabled(false);
        
        setPressure(0);
    }

    public void setPressure(int pressure) {
        if (isEnabled()) {
            textView.setText(String.format(getContext().getString(R.string.environment_pressure_measure),
                    pressure));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        pressureMeter.setValue(pressure, isEnabled());
    }
}
