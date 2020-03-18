package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.silabs.thunderboard.R;

public class ColorLEDs extends View {

    private final Paint brush;

    private float ledWidth;
    private float ledSeparation;
    private final float ledHeight;
    private final int greyColor;

    private int color;
    private int alpha = 0xff;

    public ColorLEDs(Context context) {
        this(context, null, 0);
    }

    public ColorLEDs(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorLEDs(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources res = context.getResources();

        ledHeight = res.getDimension(R.dimen.color_led_height);
        greyColor = res.getColor(R.color.sl_light_grey);

        brush = new Paint();
        brush.setStyle(Paint.Style.FILL_AND_STROKE);
        brush.setAntiAlias(true);
        brush.setColor(greyColor); // default is grey
    }

    @Override
    public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);

        int viewWidth = getWidth();
        ledWidth = viewWidth / 6;
        ledSeparation = (viewWidth - ledWidth * 5) / 4;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int drawColor = isEnabled() ? color : greyColor;

        for (int i = 0; i < 5; i++) {
            Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.led_oval);
            d.setColorFilter(drawColor, PorterDuff.Mode.MULTIPLY);
            d.setBounds((int) (i * (ledWidth + ledSeparation)), 0, (int) (ledWidth + i * (ledWidth + ledSeparation)), (int) ledHeight);
            d.draw(canvas);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (enabled) {
            brush.setColor(color);
        } else {
            brush.setColor(greyColor);
        }
        invalidate();
    }

    public void setColor(@ColorInt int c) {
        color = c;
        brush.setColor(color);
        applyAlpha();
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
        applyAlpha();
    }

    private void applyAlpha() {
        color = (color & 0x00ffffff) | ((alpha & 0xff) << 24);

        if (isEnabled()) {
            brush.setColor(color);
        } else {
            brush.setColor(greyColor);
        }

        invalidate();
    }
}
