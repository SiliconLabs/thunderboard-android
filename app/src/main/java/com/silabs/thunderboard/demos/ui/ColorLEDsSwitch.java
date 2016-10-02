package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.silabs.thunderboard.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ColorLEDsSwitch extends FrameLayout implements View.OnClickListener {

    public static final boolean ON_STATE = true;
    public static final boolean OFF_STATE = false;

    private boolean state;

    @Bind(R.id.color_switch_image)
    ImageView colorSwitchImage;

    @Bind(R.id.color_switch_text)
    TextView colorSwitchText;

    private ColorLEDsSwitchListener colorLEDsSwitchListener;

    public ColorLEDsSwitch(Context context) {
        this(context, null, 0);
    }

    public ColorLEDsSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorLEDsSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layoutView = (LinearLayout) inflater.inflate(R.layout.control_color_led_switch,
                this, false);
        addView(layoutView);

        ButterKnife.bind(this, this);

        setOnClickListener(this);
    }

    public void setColorLEDsSwitchListener(ColorLEDsSwitchListener listener) {
        this.colorLEDsSwitchListener = listener;
    }

    public void setSwitchState(boolean state) {
        if (state == ON_STATE) {
            colorSwitchImage.setImageResource(R.drawable.ic_led_lights_on);
            colorSwitchText.setText(R.string.on);
        } else {
            colorSwitchImage.setImageResource(R.drawable.ic_led_lights_off);
            colorSwitchText.setText(R.string.off);
        }
        this.state = state;
    }

    public boolean getSwitchState() {
        return state;
    }

    @Override
    public void onClick(View v) {
        state = !state;
        setSwitchState(state);
        if (colorLEDsSwitchListener != null) {
            colorLEDsSwitchListener.onClick(state);
        }
    }

    public interface ColorLEDsSwitchListener {
        void onClick(boolean state);
    }

}
