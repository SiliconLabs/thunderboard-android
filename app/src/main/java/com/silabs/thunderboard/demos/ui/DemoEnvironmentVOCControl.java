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
 * Displays an icon and the volatile organic compound value in a combo control.
 *
 * The VOCMeter and the TextView are added to the layout dynamically
 *
 */
public class DemoEnvironmentVOCControl extends LinearLayout {

    private VOCMeter vocMeter;
    private TextView textView;

    public DemoEnvironmentVOCControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentVOCControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentVOCControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_vocs);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        vocMeter = new VOCMeter(context);
        layout.addView(vocMeter);
        setEnabled(false);
        
        setVOC(0);
    }

    public void setVOC(int vocLevel) {
        if (isEnabled()) {
            textView.setText(String.format(getContext().getString(R.string.environment_voc_measure),
                    vocLevel));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        vocMeter.setValue((float) vocLevel, isEnabled());
    }
}
