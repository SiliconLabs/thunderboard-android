package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v7.widget.CardView;
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

        inflate(context, R.layout.environmentdemo_tile, this);

        CardView cardView = findViewById(R.id.cardview_env_tile);
        cardView.setEnabled(false);

        TextView textViewDescription = findViewById(R.id.env_description);
        textViewDescription.setText(R.string.environment_hall_state);
        hallStateTextView = findViewById(R.id.env_value);

        LinearLayout layout = findViewById(R.id.env_layout_meter);

        hallStateMeter = new HallStateMeter(context);
        layout.addView(hallStateMeter);
        setEnabled(false);

        resetTamperTextView = new TextView(context);
        resetTamperTextView.setText(R.string.environment_hall_state_reset_tamper);
        ViewUtils.setTextAppearance(resetTamperTextView, R.style.EnvironmentControlLabel_HallStateTampered);
        resetTamperTextView.setVisibility(GONE);
        layout.addView(resetTamperTextView);

        setHallState(HallState.OPENED);
    }

    public void setHallState(@HallState int hallState) {
        int resetTamperVisible = GONE;
        @StringRes
        int hallStateTextResId = R.string.environment_not_initialized;
        @StyleRes
        int hallStateStyleResId = R.style.tb_robo_medium_18dp;
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
