package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.silabs.thunderboard.R;
import com.silabs.thunderboard.common.ui.ButtonSpinnerDrawable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SwitchControl extends FrameLayout {

    @BindView(R.id.switch_image)
    ImageView image;

    @BindView(R.id.switch_text)
    TextView text;

    ButtonSpinnerDrawable spinnerDrawable;

    private Context context;

    public SwitchControl(Context context) {
        this(context, null, 0);
    }

    public SwitchControl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.switch_control, null, false);
        addView(view);
        ButterKnife.bind(this, view);

        this.context = context;
    }

    public void setChecked(boolean checked) {
        if (checked) {
            image.setImageResource(R.drawable.switch_status_on);
        } else {
            image.setImageResource(R.drawable.switch_status_off);
        }

        text.setText(context.getString(checked ? R.string.on : R.string.off));
    }
}
