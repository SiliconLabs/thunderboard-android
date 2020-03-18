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
 * Displays an icon and the UV value in a combo control.
 *
 * The UVMeter and the TextView are added to the layout dynamically
 *
 */
public class DemoEnvironmentUVControl extends LinearLayout {

    private final String measurementString;
    private UVMeter uvMeter;
    private TextView textView;

    public DemoEnvironmentUVControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentUVControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentUVControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_uv);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        uvMeter = new UVMeter(context);
        layout.addView(uvMeter);
        setEnabled(false);

        measurementString = context.getString(R.string.environment_uv_unit);

        setUVIndex(0);
    }

    public void setUVIndex(int uvIndex) {
        if (isEnabled()) {
            textView.setText(String.format(measurementString, uvIndex));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        uvMeter.setValue(uvIndex, isEnabled());
    }
}
