package com.silabs.thunderboard.demos.ui;

import static com.silabs.thunderboard.common.app.ThunderBoardConstants.POWER_SOURCE_TYPE_COIN_CELL;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardType;
import com.silabs.thunderboard.demos.model.LedRGBState;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DemoIOActivity extends BaseDemoActivity implements DemoIOViewListener, LEDControl.OnCheckedChangeListener, ColorLEDControl.ColorLEDControlListener {

    public static boolean isDemoAllowed() {
        return true;
    }

    private static final int STATE_NORMAL = 0;
    private static final int STATE_PRESSED = 1;

    @Inject
    DemoIOPresenter presenter;

    @Bind(R.id.switch0)
    SwitchControl switch0;

    @Bind(R.id.switch1)
    SwitchControl switch1;

    @Bind(R.id.led0)
    LEDControl led0;

    @Bind(R.id.led1)
    LEDControl led1;

    @Bind(R.id.color_led_control)
    ColorLEDControl colorLEDControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_demo_io, null, false);
        mainSection.addView(view);

        ButterKnife.bind(this);
        component().inject(this);

        setButton0State(STATE_NORMAL);
        setButton1State(STATE_NORMAL);

        led0.setChecked(false);
        led0.setOnCheckedChangeListener(this);

        led1.setChecked(false);
        led1.setOnCheckedChangeListener(this);

        initControls();

        presenter.setViewListener(this, deviceAddress);

        colorLEDControl.setColorLEDControlListener(this);
    }

    @Override
    protected BaseDemoPresenter getDemoPresenter() {
        return presenter;
    }

    @Override
    public int getToolbarColor() {
        return getResourceColor(R.color.sl_terbium_green);
    }

    @Override
    public String getToolbarString() {
        return getString(R.string.demo_io);
    }

    // DemoIOViewListener

    /**
     * setButton0State
     *
     * Listens for button 0 press
     *
     * @param state
     *
     */
    @Override
    public void setButton0State(int state) {
        if (state == STATE_NORMAL) {
            switch0.setChecked(false);
        } else if (state == STATE_PRESSED) {
            switch0.setChecked(true);
        }
    }

    /**
     * setButton1State
     *
     * Listens for button 1 press state
     *
     * @param state
     *
     */
    @Override
    public void setButton1State(int state) {
        if (state == STATE_NORMAL) {
            switch1.setChecked(false);
        } else if (state == STATE_PRESSED) {
            switch1.setChecked(true);
        }
    }

    // LEDControl

    /**
     * onCheckedChanged
     *
     * Turns the LED on or off while preserving the state of the other LED.
     *
     * @param ledControl
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(LEDControl ledControl, boolean isChecked) {
        Resources res = getResources();
        int action = 0;

        if (ledControl == led0) {
            if (isChecked) action = res.getInteger(R.integer.led0_on);
            if (led1.isChecked()) action |= res.getInteger(R.integer.led1_on);
        } else if (ledControl == led1) {
            if (isChecked) action = res.getInteger(R.integer.led1_on);
            if (led0.isChecked()) action |= res.getInteger(R.integer.led0_on);
        }
        presenter.ledAction(action);
    }

    @Override
    public void setLed0State(int state) {
        if (state == STATE_NORMAL) {
            led0.setChecked(false);
        } else if (state == STATE_PRESSED) {
            led0.setChecked(true);
        }
    }

    @Override
    public void setLed1State(int state) {
        if (state == STATE_NORMAL) {
            led1.setChecked(false);
        } else if (state == STATE_PRESSED) {
            led1.setChecked(true);
        }
    }

    @Override
    public void setColorLEDsValue(LedRGBState colorLEDsValue) {
        // remove and reset listener to prevent
        // repeated write commands
        colorLEDControl.setColorLEDControlListener(null);
        colorLEDControl.setColorLEDsUI(colorLEDsValue);
        colorLEDControl.setColorLEDControlListener(this);
    }

    @Override
    public void setPowerSource(int powerSource) {
        switch(powerSource) {
            case POWER_SOURCE_TYPE_COIN_CELL:
                colorLEDControl.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void initControls() {
        //colorLEDControl.setVisibility(presenter.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE
        //        ? View.VISIBLE : View.GONE);
        if (presenter.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_SENSE) {
            colorLEDControl.setVisibility(View.VISIBLE);
            colorLEDControl.setColorLEDControlListener(this);
            led0.setBackgroundResource(R.drawable.red_button_selector);
            led1.setBackgroundResource(R.drawable.green_button_selector);
        } else {
            colorLEDControl.setVisibility(View.GONE);
            led0.setBackgroundResource(R.drawable.blue_button_selector);
            led1.setBackgroundResource(R.drawable.green_button_selector);
        }
    }

    @Override
    public void updateColorLEDs(LedRGBState ledRGBState) {
        presenter.setColorLEDs(ledRGBState);
    }
}
