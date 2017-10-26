package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.silabs.thunderboard.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Combo control that displays a blue or green LED in a toggle button and accompanying text
 */
public class LEDControl extends FrameLayout implements CompoundButton.OnCheckedChangeListener {

    public static final int GREEN_BACKGROUND = 0;
    public static final int BLUE_BACKGROUND = 1;

    @Bind(R.id.button)
    ToggleButton button;

    @Bind(R.id.text)
    TextView text;

    private OnCheckedChangeListener checkedChangeListener;

    public LEDControl(Context context) {
        this(context, null, 0);
    }

    public LEDControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LEDControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        int backgroundIndex = GREEN_BACKGROUND;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LEDControl);
            backgroundIndex = a.getInt(R.styleable.LEDControl_highlightColor, GREEN_BACKGROUND);
            a.recycle();
        }

        View view = LayoutInflater.from(context).inflate(R.layout.led_control, null, false);
        addView(view);
        ButterKnife.bind(this, view);

        if (backgroundIndex == GREEN_BACKGROUND) {
            button.setBackgroundResource(R.drawable.green_button_selector);
        } else if (backgroundIndex == BLUE_BACKGROUND){
            button.setBackgroundResource(R.drawable.blue_button_selector);
        }

        button.setOnCheckedChangeListener(this);
    }

    public void setBackgroundResource(@DrawableRes int resid) {
        button.setBackgroundResource(resid);
    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        text.setText(isChecked ? R.string.on : R.string.off);
        if (checkedChangeListener != null) {
            checkedChangeListener.onCheckedChanged(this, isChecked);
        }
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        checkedChangeListener = listener;
    }

    public boolean isChecked() {
        return button.isChecked();
    }

    public void setChecked(boolean checkState) {
        text.setText(checkState ? R.string.on : R.string.off);
        button.setChecked(checkState);
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(LEDControl ledControl, boolean isChecked);
    }
}
