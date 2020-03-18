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
 * Displays an icon and the Hall Strength (Magnetic Field) value in a combo control.
 * <p>
 * The HallStrengthMeter and the TextView are added to the layout dynamically
 */
public class DemoEnvironmentHallStrengthControl extends LinearLayout {

    private HallStrengthMeter hallStrengthMeter;
    private TextView textView;

    public DemoEnvironmentHallStrengthControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentHallStrengthControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentHallStrengthControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_hall_strength);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        hallStrengthMeter = new HallStrengthMeter(context);
        layout.addView(hallStrengthMeter);
        setEnabled(false);

        setHallStrength(0);
    }

    public void setHallStrength(float hallStrength) {
        if (isEnabled()) {
            textView.setText(getContext().getString(R.string.environment_hall_strength_measure, hallStrength));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        hallStrengthMeter.setValue(hallStrength, isEnabled());
    }
}
