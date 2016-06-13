package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
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

    private TemperatureMeter temperatureMeter;
    private TextView textView;

    private Context context;

    public DemoEnvironmentTemperatureControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentTemperatureControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentTemperatureControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;

        Resources res = context.getResources();

        setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        temperatureMeter = new TemperatureMeter(context);
        addView(temperatureMeter, layoutParams);
        setEnabled(false);

        textView = new TextView(context);
        textView.setTextColor(res.getColor(R.color.sl_light_grey));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.text_size_M));
        textView.setPadding(0, res.getDimensionPixelSize(R.dimen.space_S), 0, 0);
        addView(textView, layoutParams);

        setTemperature(0, 0);
    }

    public void setTemperature(float temperature, int temperatureType) {
        if (isEnabled()) {
            textView.setText(String.format(
                    temperatureType == ThunderBoardPreferences.TEMP_FAHRENHEIT ?
                            context.getString(R.string.environment_temp_f) :
                            context.getString(R.string.environment_temp_c),
                    temperature));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        temperatureMeter.setValue(temperature, temperatureType, isEnabled());
    }
}
