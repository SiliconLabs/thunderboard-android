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

        Resources res = context.getResources();

        setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        co2Meter = new CO2Meter(context);
        addView(co2Meter, layoutParams);
        setEnabled(false);

        textView = new TextView(context);
        textView.setTextColor(res.getColor(R.color.sl_silicon_grey));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.text_size_S));
        textView.setPadding(0, res.getDimensionPixelSize(R.dimen.space_S), 0, 0);

        addView(textView, layoutParams);
        
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
