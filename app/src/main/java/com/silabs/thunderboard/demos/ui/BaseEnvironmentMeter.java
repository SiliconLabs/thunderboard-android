package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.silabs.thunderboard.R;

public abstract class BaseEnvironmentMeter extends View {

    protected final Paint backgroundBrush;
    protected final Bitmap inactiveBitmap;
    protected Bitmap activeBitmap;

    private int color;
    private boolean enabled;

    public BaseEnvironmentMeter(Context context) {
        this(context, null, 0);
    }

    public BaseEnvironmentMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseEnvironmentMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        color = 0xff000000;
        enabled = false;
        Resources res = context.getResources();

        inactiveBitmap = BitmapFactory.decodeResource(res, getInactiveIconResource());
        activeBitmap = BitmapFactory.decodeResource(res, getActiveIconResource());

        backgroundBrush = new Paint();
        backgroundBrush.setAntiAlias(true);
        backgroundBrush.setStyle(Paint.Style.FILL_AND_STROKE);
        backgroundBrush.setColor(color);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightNMeasureSpec) {
        setMeasuredDimension(inactiveBitmap.getWidth(), inactiveBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (enabled) {
            int width = getWidth();
            int height = getHeight();

            canvas.drawCircle(width / 2, height / 2, width / 2, backgroundBrush);
            canvas.drawBitmap(activeBitmap,
                              (width - activeBitmap.getWidth()) / 2,
                              (height - activeBitmap.getHeight()) / 2,
                              null);
        } else {
            canvas.drawBitmap(inactiveBitmap, 0, 0, null);
        }
    }

    /**
     * setValue
     * <p/>
     * Changes the color of the background brush and forces a redraw
     * This will only run if the color changes, or we change the enable state.
     *
     * @param value
     * @param enabled
     */
    public void setValue(float value, boolean enabled) {
        int newColor = enabled ? ContextCompat.getColor(getContext(), getColorResource(value)) :
                ContextCompat.getColor(getContext(), R.color.primary_color);

        if (color != newColor || this.enabled != enabled) {
            this.enabled = enabled;
            color = newColor;
            backgroundBrush.setColor(color);
            invalidate();
        }
    }

    protected abstract int getColorResource(float value);

    protected abstract int getInactiveIconResource();

    protected abstract int getActiveIconResource();
}
