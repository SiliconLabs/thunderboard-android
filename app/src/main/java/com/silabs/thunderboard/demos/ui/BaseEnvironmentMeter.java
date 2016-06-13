package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.silabs.thunderboard.R;

public abstract class BaseEnvironmentMeter extends View {

    protected final Bitmap startBitmap;
    protected final Bitmap colorBitmap;

    private int color;

    public BaseEnvironmentMeter(Context context) {
        this(context, null, 0);
    }

    public BaseEnvironmentMeter(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseEnvironmentMeter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        color = 0xff000000;

        startBitmap = BitmapFactory.decodeResource(context.getResources(), getIconResource());
        colorBitmap = Bitmap.createBitmap(startBitmap.getWidth(), startBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightNMeasureSpec) {
        setMeasuredDimension(startBitmap.getWidth(), startBitmap.getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(colorBitmap, 0, 0, null);
    }

    /**
     * setValue
     *
     * Changes the color of the non-transparent pixels in the bitmaps
     * for the different environment quantities.
     *
     * This will only run if the color changes.
     *
     * The bitmap is converted to an integer array and those array
     * members that are not 0 and changed to the updated color integer.
     *
     * @param value
     * @param valueType
     */
    public void setValue(float value, int valueType, boolean enabled) {

        int newColor = enabled ? getColor(value, valueType) :
                getResources().getColor(R.color.primary_color);

        if (color != newColor) {
            color = newColor;

            int allpixels[] = new int[startBitmap.getWidth() * startBitmap.getHeight()];
            startBitmap.getPixels(allpixels, 0, startBitmap.getWidth(), 0, 0,
                    startBitmap.getWidth(), startBitmap.getHeight());

            for (int i = 0; i < allpixels.length; i++) {
                if (allpixels[i] != 0) {
                    allpixels[i] = color;
                }
            }
            colorBitmap.setPixels(allpixels, 0, startBitmap.getWidth(), 0, 0,
                    startBitmap.getWidth(), startBitmap.getHeight());
            invalidate();
        }
    }

    protected abstract int getIconResource();
    protected abstract int getColor(float value, int valueType);
}
