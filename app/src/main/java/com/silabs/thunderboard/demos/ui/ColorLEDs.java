package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.View;

import com.silabs.thunderboard.R;

public class ColorLEDs extends View {

    private final Paint brush;
    private final float ledDiameter;
    private final float ledSeparation;

    private final int greyColor;

    private int color;

    public ColorLEDs(Context context) {
        this(context, null, 0);
    }

    public ColorLEDs(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorLEDs(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Resources res = context.getResources();

        ledDiameter = res.getDimension(R.dimen.color_led_diameter);
        ledSeparation = res.getDimension(R.dimen.color_led_separation);

        greyColor = res.getColor(R.color.sl_light_grey);

        brush = new Paint();
        brush.setStyle(Paint.Style.FILL_AND_STROKE);
        brush.setAntiAlias(true);
        brush.setColor(greyColor); // default is grey
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (4 * ledDiameter + 3 * ledSeparation), (int) ledDiameter);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < 4; i++) {
            canvas.drawCircle(ledDiameter / 2 + i * (ledDiameter + ledSeparation),
                    ledDiameter / 2, ledDiameter / 2, brush);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
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
        invalidate();
    }

    public void setAlpha(int alpha) {
        color = (color & 0x00ffffff) | ((alpha & 0xff) << 24);
        brush.setColor(color);
        invalidate();
    }
}
