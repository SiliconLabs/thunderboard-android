package com.silabs.thunderboard.demos.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.silabs.thunderboard.R;

public class HueBackgroundView extends View {

    private final Paint brush;
    private final float[] hsvColor;
    private final int[] spectrumColors;

    private final float lineHeight;

    public HueBackgroundView(Context context) {
        this(context, null, 0);
    }

    public HueBackgroundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HueBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        lineHeight = context.getResources().getDimension(R.dimen.color_hue_selection_line_height);

        hsvColor = new float[3];
        spectrumColors = new int[360];
        initSpectrumColors();

        brush = new Paint();
        brush.setAntiAlias(true);
        brush.setStyle(Paint.Style.FILL_AND_STROKE);
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        if (isEnabled()) {
            LinearGradient grad = new LinearGradient(height / 2, 0.0f,
                    width - height / 2, 0.0f,
                    spectrumColors, null, Shader.TileMode.CLAMP);
            brush.setShader(grad);
            canvas.drawRect(height / 2, (height - lineHeight) / 2,
                    width - height / 2, (height + lineHeight) / 2,
                    brush);
        }

    }

    private void initSpectrumColors() {
        for (int i = 0; i < 360; i++) {
            hsvColor[0] = (float) i;
            hsvColor[1] = 1.0f;
            hsvColor[2] = 1.0f;
            spectrumColors[i] = Color.HSVToColor(hsvColor);
        }
    }
}
