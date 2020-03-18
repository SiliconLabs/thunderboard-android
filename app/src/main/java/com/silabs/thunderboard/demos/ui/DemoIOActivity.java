package com.silabs.thunderboard.demos.ui;

import static com.silabs.thunderboard.common.app.ThunderBoardConstants.POWER_SOURCE_TYPE_COIN_CELL;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.app.ThunderBoardType;
import com.silabs.thunderboard.demos.model.LedRGBState;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DemoIOActivity extends BaseDemoActivity implements DemoIOViewListener, CompoundButton.OnCheckedChangeListener, ColorLEDControl.ColorLEDControlListener {

    public static boolean isDemoAllowed() {
        return true;
    }

    private static final int STATE_NORMAL = 0;
    private static final int STATE_PRESSED = 1;

    @Inject
    DemoIOPresenter presenter;

    @BindView(R.id.switch0)
    SwitchControl switch0;

    @BindView(R.id.switch1)
    SwitchControl switch1;

    @BindView(R.id.led0)
    SwitchCompat led0;

    @BindView(R.id.led1)
    SwitchCompat led1;

    @BindView(R.id.color_led_control)
    ColorLEDControl colorLEDControl;

    @BindView(R.id.lightsTitle)
    TextView lightsTitle;

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

//        disable firebase for reskinning
//        checkFirebaseConnectivity();
    }

    @Override
    protected BaseDemoPresenter getDemoPresenter() {
        return presenter;
    }

    @Override
    public int getToolbarColor() {
        return getResourceColor(R.color.tb_red);
    }

    @Override
    public String getToolbarString() {
        return getString(R.string.demo_io);
    }

    // DemoIOViewListener

    /**
     * setButton0State
     * <p>
     * Listens for button 0 press
     *
     * @param state
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
     * <p>
     * Listens for button 1 press state
     *
     * @param state
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
     * <p>
     * Turns the LED on or off while preserving the state of the other LED.
     *
     * @param ledControl
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Resources res = getResources();
        int action = 0;

        if (buttonView == led0) {
            if (isChecked) action = res.getInteger(R.integer.led0_on);
            if (led1.isChecked()) action |= res.getInteger(R.integer.led1_on);
        } else if (buttonView == led1) {
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
        switch (powerSource) {
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
        } else if (presenter.getThunderBoardType() == ThunderBoardType.THUNDERBOARD_BLUE) {
            colorLEDControl.setVisibility(View.GONE);

            lightsTitle.setText("Led");
            led1.setVisibility(View.GONE);
            switch1.setVisibility(View.GONE);
        } else {
            colorLEDControl.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateColorLEDs(LedRGBState ledRGBState) {
        presenter.setColorLEDs(ledRGBState);
    }
}
