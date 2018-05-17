package com.silabs.thunderboard.scanner.ui;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.silabs.thunderboard.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Combination control that displays a set of signal strength bars and a TextView showing the
 * signal strength in dB.
 *
 */
public class SignalStrengthIndicator extends FrameLayout {

    @BindView(R.id.signal_strength_meter)
    SignalStrengthMeter signalStrengthMeter;

    @BindView(R.id.signal_strength_text)
    TextView signalStrengthText;

    private Resources res;

    public SignalStrengthIndicator(Context context) {
        this(context, null, 0);
    }

    public SignalStrengthIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SignalStrengthIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        ViewGroup rootView =
                (ViewGroup) LayoutInflater.from(context).inflate(R.layout.control_signal_strength, null, false);
        addView(rootView);
        ButterKnife.bind(this, rootView);

        res = context.getResources();
    }

    public void setSignalStrength(int dbValue) {
        signalStrengthMeter.setStrength(dbValue);
        String signalStrengthString =
                String.format(res.getString(R.string.scanner_signal_strength), dbValue);
        signalStrengthText.setText(signalStrengthString);
    }
}
