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

        inflate(context, R.layout.environmentdemo_tile, this);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_sound_level);
        textView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        soundLevelMeter = new SoundLevelMeter(context);
        layout.addView(soundLevelMeter);
        setEnabled(false);
        
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
