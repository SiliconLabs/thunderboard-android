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
 * Displays an icon and the pressure value in a combo control.
 *
 * The PressureMeter and the TextView are added to the layout dynamically
 *
 */
public class DemoEnvironmentSoundLevelControl extends LinearLayout {

    private SoundLevelMeter soundLevelMeter;
    private TextView textView;

    private Context context;

    public DemoEnvironmentSoundLevelControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentSoundLevelControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentSoundLevelControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.context = context;

        Resources res = context.getResources();

        setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

        soundLevelMeter = new SoundLevelMeter(context);
        addView(soundLevelMeter, layoutParams);
        setEnabled(false);

        textView = new TextView(context);
        textView.setTextColor(res.getColor(R.color.sl_silicon_grey));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, res.getDimensionPixelSize(R.dimen.text_size_S));
        textView.setPadding(0, res.getDimensionPixelSize(R.dimen.space_S), 0, 0);
        addView(textView, layoutParams);
        
        setSoundLevel(0);
    }

    public void setSoundLevel(int soundLevel) {
        if (isEnabled()) {
            textView.setText(String.format(context.getString(R.string.environment_sound_level_measure),
                    soundLevel));
        } else {
            textView.setText(R.string.environment_not_initialized);
        }
        soundLevelMeter.setValue(soundLevel, isEnabled());
    }
}
