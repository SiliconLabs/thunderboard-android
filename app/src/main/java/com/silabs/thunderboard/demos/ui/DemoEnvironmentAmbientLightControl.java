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

        Resources res = context.getResources();

        setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        ambientLightMeter = new AmbientLightMeter(context);
        addView(ambientLightMeter, layoutParams);
        setEnabled(false);

        textView = new TextView(context);
        textView.setTextColor(res.getColor(R.color.sl_silicon_grey));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.text_size_S));
        textView.setPadding(0, res.getDimensionPixelSize(R.dimen.space_S), 0, 0);
        addView(textView, layoutParams);

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
