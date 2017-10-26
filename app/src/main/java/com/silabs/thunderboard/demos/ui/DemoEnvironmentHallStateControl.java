package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.ui.ViewUtils;
import com.silabs.thunderboard.demos.model.HallState;

/**
 * Displays an icon and the Hall State value in a combo control.
 * <p>
 * The HallStateMeter and the TextView are added to the layout dynamically
 */
public class DemoEnvironmentHallStateControl extends LinearLayout {

    private HallStateMeter hallStateMeter;
    private TextView hallStateTextView;
    private TextView resetTamperTextView;

    public DemoEnvironmentHallStateControl(Context context) {
        this(context, null, 0);
    }

    public DemoEnvironmentHallStateControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DemoEnvironmentHallStateControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(VERTICAL);

        LayoutParams centerLayoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        centerLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        hallStateMeter = new HallStateMeter(context);
        addView(hallStateMeter, centerLayoutParams);
        setEnabled(false);

        LayoutParams hallStateLayoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        hallStateLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        hallStateLayoutParams.weight = 1;

        hallStateTextView = new TextView(context);
        ViewUtils.setTextAppearance(hallStateTextView, R.style.EnvironmentControlLabel);
        hallStateTextView.setGravity(Gravity.BOTTOM);
        addView(hallStateTextView, hallStateLayoutParams);

        resetTamperTextView = new TextView(context);
        resetTamperTextView.setText(R.string.environment_hall_state_reset_tamper);
        ViewUtils.setTextAppearance(resetTamperTextView, R.style.EnvironmentControlLabel_HallStateTampered);
        resetTamperTextView.setVisibility(GONE);
        addView(resetTamperTextView, centerLayoutParams);

        setHallState(HallState.OPENED);
    }

    public void setHallState(@HallState int hallState) {
        int resetTamperVisible = GONE;
        @StringRes
        int hallStateTextResId = R.string.environment_not_initialized;
        @StyleRes
        int hallStateStyleResId = R.style.EnvironmentControlLabel;
        if (isEnabled()) {
            switch (hallState) {
                case HallState.TAMPERED:
                    resetTamperVisible = VISIBLE;
                    hallStateTextResId = R.string.environment_hall_state_tampered;
                    hallStateStyleResId = R.style.EnvironmentControlLabel_HallStateTampered;
                    break;
                case HallState.CLOSED:
                    hallStateTextResId = R.string.environment_hall_state_closed;
                    break;
                case HallState.OPENED:
                default:
                    hallStateTextResId = R.string.environment_hall_state_opened;
                    break;
            }
        }
        hallStateTextView.setText(hallStateTextResId);
        ViewUtils.setTextAppearance(hallStateTextView, hallStateStyleResId);
        resetTamperTextView.setVisibility(resetTamperVisible);
        hallStateMeter.setValue(hallState, isEnabled());
    }
}
