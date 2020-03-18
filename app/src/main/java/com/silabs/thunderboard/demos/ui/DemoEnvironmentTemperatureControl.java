package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.data.model.ThunderBoardPreferences;

/**
 * Displays an icon and the temperature value in a combo control.
 *
 * The TemperatureMeter and the TextView are added to the layout dynamically
 *
 */
public class DemoEnvironmentTemperatureControl extends LinearLayout {

    private BaseEnvironmentMeter temperatureMeter;
    private TextView textView;

    public DemoEnvironmentTemperatureControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentTemperatureControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentTemperatureControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_temp);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        temperatureMeter = new TemperatureMeter(context);
        layout.addView(temperatureMeter);
        setEnabled(false);

        setTemperature(0, 0);
    }

    public void setTemperature(float temperature, int temperatureType) {
        if (isEnabled()) {
            textView.setText(String.format(
                    temperatureType == ThunderBoardPreferences.TEMP_FAHRENHEIT ?
                            getContext().getString(R.string.environment_temp_f) :
                            getContext().getString(R.string.environment_temp_c),
                    temperatureType == ThunderBoardPreferences.TEMP_FAHRENHEIT ?
                            temperature * 1.8f + 32f : temperature));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        temperatureMeter.setValue(temperature, isEnabled());
    }
}
