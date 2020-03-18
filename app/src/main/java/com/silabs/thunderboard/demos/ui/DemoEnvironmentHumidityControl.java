package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.silabs.thunderboard.R;

/**
 * Displays an icon and the humidity value in a combo control.
 *
 * The HumidityMeter and the TextView are added to the layout dynamically
 *
 */
public class DemoEnvironmentHumidityControl extends LinearLayout {

    private HumidityMeter humidityMeter;
    private TextView textView;

    public DemoEnvironmentHumidityControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentHumidityControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentHumidityControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_humidity);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        humidityMeter = new HumidityMeter(context);
        layout.addView(humidityMeter);
        setEnabled(false);

        setHumidity(0);
    }

    public void setHumidity(int humidity) {
        if (isEnabled()) {
            textView.setText(String.format(getContext().getString(R.string.environment_humidity_measure),
                    humidity));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        humidityMeter.setValue((float) humidity, isEnabled());
    }
}
