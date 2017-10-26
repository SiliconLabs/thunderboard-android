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

        Resources res = context.getResources();

        setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        hallStrengthMeter = new HallStrengthMeter(context);
        addView(hallStrengthMeter, layoutParams);
        setEnabled(false);

        textView = new TextView(context);
        textView.setTextColor(res.getColor(R.color.sl_silicon_grey));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.text_size_S));
        textView.setPadding(0, res.getDimensionPixelSize(R.dimen.space_S), 0, 0);

        addView(textView, layoutParams);

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
