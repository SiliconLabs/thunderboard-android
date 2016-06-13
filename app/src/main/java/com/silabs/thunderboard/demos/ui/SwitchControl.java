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

import butterknife.Bind;
import butterknife.ButterKnife;

public class SwitchControl extends FrameLayout {

    @Bind(R.id.switch_image)
    ImageView image;

    @Bind(R.id.switch_text)
    TextView text;

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
        //image.setImageResource(checked ? R.drawable.animated_switch_183 : R.drawable.ic_switches_off);
        if (checked) {
            image.setImageResource(R.drawable.switch_on_frames);
            AnimationDrawable switchOnAnimation = (AnimationDrawable) image.getDrawable();
            switchOnAnimation.start();
        } else {
            image.setImageResource(R.drawable.ic_switches_off);
        }

        text.setText(context.getString(checked ? R.string.on : R.string.off));
    }
}
