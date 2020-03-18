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
 * Displays an icon and the ambient light value in a combo control.
 * <p/>
 * The AmbientLightMeter and the TextView are added to the layout dynamically
 */
public class DemoEnvironmentAmbientLightControl extends LinearLayout {

    private AmbientLightMeter ambientLightMeter;
    private TextView textView;

    public DemoEnvironmentAmbientLightControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentAmbientLightControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentAmbientLightControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_ambient);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        ambientLightMeter = new AmbientLightMeter(context);
        layout.addView(ambientLightMeter);
        setEnabled(false);

        setAmbientLight(0);
    }

    public void setAmbientLight(long ambientLight) {

        if (isEnabled()) {
            textView.setText(String.format(getContext().getString(R.string.environment_ambient_lx), ambientLight));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        ambientLightMeter.setValue(ambientLight, isEnabled());
    }
}
