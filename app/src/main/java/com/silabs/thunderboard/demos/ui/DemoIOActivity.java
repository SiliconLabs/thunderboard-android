package com.silabs.thunderboard.demos.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.silabs.thunderboard.R;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DemoIOActivity extends BaseDemoActivity implements DemoIOViewListener, LEDControl.OnCheckedChangeListener {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = LayoutInflater.from(this).inflate(R.layout.activity_demo_io, null, false);
        mainSection.addView(view);

        ButterKnife.bind(this);
        component().inject(this);

        presenter.setViewListener(this, deviceAddress);

        setButton0State(STATE_NORMAL);
        setButton1State(STATE_NORMAL);

        led0.setChecked(false);
        led0.setOnCheckedChangeListener(this);

        led1.setChecked(false);
        led1.setOnCheckedChangeListener(this);
    }

    @Override
    protected BaseDemoPresenter getDemoPresenter() {
        return presenter;
    }

    @Override
    public int getToolbarColor() {
        return getResourceColor(R.color.sl_yellow);
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
}
