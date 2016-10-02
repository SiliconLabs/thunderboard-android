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

        Resources res = context.getResources();

        setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        vocMeter = new VOCMeter(context);
        addView(vocMeter, layoutParams);
        setEnabled(false);

        textView = new TextView(context);
        textView.setTextColor(res.getColor(R.color.sl_silicon_grey));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.text_size_S));
        textView.setPadding(0, res.getDimensionPixelSize(R.dimen.space_S), 0, 0);
        addView(textView, layoutParams);
        
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
