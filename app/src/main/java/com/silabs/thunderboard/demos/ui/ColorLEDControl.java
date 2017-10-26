package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.demos.model.LedRGB;
import com.silabs.thunderboard.demos.model.LedRGBState;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ColorLEDControl extends FrameLayout {

    @Bind(R.id.iodemo_color_leds)
    ColorLEDs colorLEDs;

    @Bind(R.id.iodemo_hue_select)
    SeekBar hueSelect;

    @Bind(R.id.iodemo_brightness_select)
    SeekBar brightnessSelect;

    @Bind(R.id.iodemo_color_switch)
    ColorLEDsSwitch colorSwitch;

    @Bind(R.id.iodemo_hue_background)
    HueBackgroundView hueBackgroundView;

    private float hue; // from 0 to 360
    private float brightness; // from 0 to 1
    private ColorLEDControlListener colorLEDControlListener;


    public interface ColorChangedListener {
        void onColorChanged(int colorvalue);
    }

    public ColorLEDControl(Context context) {
        this(context, null, 0);
    }

    public ColorLEDControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorLEDControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.iodemo_color_leds, this, false);
        addView(layout);

        ButterKnife.bind(this, this);

        hue = 0;
        brightness = 1;
        int color = hsvToRGB(hue, brightness);
        colorLEDs.setColor(color);
        colorLEDs.setAlpha(0xff);

        hueSelect.setMax(359);
        hueSelect.setProgress(0);
        hueSelect.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                hue = (float) progress;

                setColorLEDs(colorSwitch.getSwitchState(), hue, brightness);

                colorLEDs.setColor(hsvToRGB(hue, 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        initHueSelectBackground();

        brightnessSelect.setMax(0xff);
        brightnessSelect.setProgress(0xff);
        brightnessSelect.setOnSeekBarChangeListener(selectBrightnessListener);


        colorSwitch.setColorLEDsSwitchListener(new ColorLEDsSwitch.ColorLEDsSwitchListener() {
            @Override
            public void onClick(boolean state) {
                enableControls(state);
                setColorLEDs(state, ColorLEDControl.this.hue, ColorLEDControl.this.brightness);
            }
        });

        // initial state is off
        colorSwitch.setSwitchState(false);
        enableControls(false);
    }

    SeekBar.OnSeekBarChangeListener selectBrightnessListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            brightness = findBrightness(progress);
            setColorLEDs(colorSwitch.getSwitchState(), hue, brightness);
            colorLEDs.setAlpha(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    public void setColorLEDControlListener(ColorLEDControlListener listener) {
        colorLEDControlListener = listener;
    }


    public void setColorLEDsUI(LedRGBState colorLEDsValue) {
        boolean isOn = colorLEDsValue.on;
        colorSwitch.setSwitchState(isOn);
        enableControls(isOn);
        float[] hsv = new float[3];
        Color.RGBToHSV(
                colorLEDsValue.color.red,
                colorLEDsValue.color.green,
                colorLEDsValue.color.blue
                , hsv);

        hue = hsv[0];
        brightness = hsv[2];
        hueSelect.setProgress((int) hue);
        brightnessSelect.setProgress(findAlpha(brightness));
        colorLEDs.setEnabled(isOn);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        enableControls(enabled);
        colorSwitch.setEnabled(enabled);
        hueBackgroundView.setEnabled(enabled);
    }

    private void enableControls(boolean enable) {
        hueSelect.setEnabled(enable);
        brightnessSelect.setEnabled(enable);
        colorLEDs.setEnabled(enable);
    }

    private void initHueSelectBackground() {
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(0x00000000); // make shape transparent
        hueSelect.setProgressDrawable(shape);
    }

    private void setColorLEDs(boolean switchState, float hue, float brightness) {
        int color = hsvToRGB(hue, brightness);
        if (this.colorLEDControlListener != null) {
            colorLEDControlListener.updateColorLEDs(
                    new LedRGBState(switchState,
                            new LedRGB(
                                    Color.red(color),
                                    Color.green(color),
                                    Color.blue(color)
                            )));
        }
    }

    private int hsvToRGB(float hue, float brightness) {
        float[] hsv = new float[3];
        hsv[0] = hue;
        hsv[1] = 1.0f;
        hsv[2] = brightness;
        return Color.HSVToColor(hsv);
    }

    private float findBrightness(int alpha) {
        return (float) alpha / 255.0f;
    }

    private int findAlpha(float brightness) {
        return (int) (brightness * 255f);
    }

    public interface ColorLEDControlListener {
        void updateColorLEDs(LedRGBState ledRGBState);
    }
}
