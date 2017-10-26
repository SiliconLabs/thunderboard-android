package com.silabs.thunderboard.common.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.silabs.thunderboard.R;

import java.util.ArrayList;
import java.util.List;


public class ButtonSpinnerDrawable extends Drawable implements Animatable, Runnable {
    private static final int TRACK_COUNT = 4;
    private boolean running = false;
    private List<ButtonTrack> tracks;
    private Context context;
    private int animationsStopped;

    public ButtonSpinnerDrawable(Context context) {
        super();
        this.context = context;
        commonSetup(context);
    }

    private void commonSetup(Context context) {
        double start = 270; // Math.PI / 2.0f;
        double end = 360; // 2 * Math.PI;

        int[] trackColors = new int[]{
                R.color.sl_bright_green,
                R.color.sl_terbium_green,
                R.color.sl_medium_green,
                R.color.sl_dark_green
        };

        double[] widths = new double[]{3.0, 2.5, 2.0, 1.5};
        double[] endings = new double[]{end, end, end, end};

        double[] delayTimings = new double[]{0.0, 0.2, 0.1, 0.0};
        double[] fillTimings = new double[]{1.0, 1.2, 1.4, 1.3};
        double[] reverseTimings = new double[]{0.5, 0.6, 0.5, 0.7};
        double[] rotationTimings = new double[]{1.0, 1.0 + (1.0 / 3.0), 2.0, 4.0};

        tracks = new ArrayList<>();
        for (int i = 0; i < TRACK_COUNT; i++) {
            ButtonTrack bt = new ButtonTrack(
                    delayTimings[i] * 1000f,
                    fillTimings[i] * 1000f,
                    reverseTimings[i] * 1000f,
                    rotationTimings[i] * 1000f,
                    start,
                    endings[i],
                    dpToPx(widths[i]),
                    dpToPx(widths[i] + (10 * i)),
                    ContextCompat.getColor(context, trackColors[i])
            );
            tracks.add(bt);
        }
    }


    interface AnimationStoppedListener {
        void onAnimationStopped();
    }

    @Override
    public void draw(Canvas canvas) {
        for (ButtonTrack track : tracks) {
            track.drawTrack(canvas);
        }
    }

    @Override
    public void run() {
        invalidateSelf();
        scheduleSelf(this, AnimationUtils.currentAnimationTimeMillis() + (1000 / 60));
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void start() {
        if (!isRunning()) {
            running = true;
            for (ButtonTrack track :
                    tracks) {
                track.startAnimating();
            }
            run();
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            running = false;
            animationsStopped = 0;
            for (ButtonTrack track : tracks) {
                track.stopAnimating(new AnimationStoppedListener() {
                    @Override
                    public void onAnimationStopped() {
                        animationsStopped++;
                        if (animationsStopped == tracks.size()) {
                            unscheduleSelf(ButtonSpinnerDrawable.this);
                        }
                    }
                });
            }
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int a) {
        for (ButtonTrack track : tracks) {
            track.paint.setAlpha(a);
        }
    }

    @Override
    public void setColorFilter(ColorFilter arg0) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        for (ButtonTrack track : tracks) {
            track.setBounds(bounds);
        }
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    public int dpToPx(double dp) {
        return dpToPx((int) dp);
    }


    class ButtonTrack implements Animatable, Animator.AnimatorListener {
        final private long delayDuration;
        final private long fillDuration;
        final private long reverseDuration;
        final private long rotationDuration;
        final private double begining;
        final private double ending;
        final private double lineWidth;
        final private double inset;
        final private int trackColor;
        final private Paint paint;
        final private int stoppedColor;
        private final ValueAnimator reverseAnim;
        private final ValueAnimator startAnim;
        private final ValueAnimator stopAnim;

        @ColorInt
        private int currColor;

        private ValueAnimator rotateAnim;
        private ValueAnimator anim;

        float fillValue = 1.0f;
        private float rotation;
        private boolean animating = false;
        private boolean filling;
        private boolean starting = true;
        private boolean stopping;
        private boolean clockWise;
        private AnimationStoppedListener listener;
        private RectF rectf;


        ButtonTrack(double delayDuration, double fillDuration, double reverseDuration, double rotationDuration, double begining, double ending, double lineWidth, double inset, @ColorInt int trackColor) {
            this.delayDuration = (long) delayDuration;
            this.fillDuration = (long) fillDuration;
            this.reverseDuration = (long) reverseDuration;
            this.rotationDuration = (long) rotationDuration;
            this.begining = begining;
            this.ending = ending;
            this.lineWidth = lineWidth;
            this.inset = inset;
            this.trackColor = trackColor;
            this.stoppedColor = ContextCompat.getColor(context, R.color.sl_grey);

            this.paint = new Paint();
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setColor(stoppedColor);
            this.paint.setAntiAlias(true);
            this.paint.setStrokeCap(Paint.Cap.ROUND);
            this.paint.setStrokeWidth((float) this.lineWidth);


            this.startAnim = createStartAnimator();
            this.reverseAnim = createReverseAnimator();
            this.stopAnim = createStopAnimator();
            this.rotateAnim = createRotateAnimator();

            setupTrack();
        }

        Paint getPaint() {
            this.paint.setColor(currColor);
            return this.paint;
        }

        public void startAnimating() {
            if (animating && stopping) {
                stopping = false;
                return;
            }

            setupTrack();
            starting = true;
            stopping = false;
            animating = true;
            start();
        }

        public void stopAnimating(AnimationStoppedListener listener) {
            stopping = true;
            this.listener = listener;
        }

        @Override
        public void start() {
            setupPath(false);

            if (this.anim != null) {
                this.anim.cancel();
            }

            this.anim = startAnim.clone();
            this.anim.start();
        }

        private ValueAnimator createStartAnimator() {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f, 0.02f);
            valueAnimator.setDuration(700);
            valueAnimator.setInterpolator(new AccelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    fillValue = (float) animation.getAnimatedValue();
                }
            });
            valueAnimator.addListener(this);
            fillValue = 0.02f;
            return valueAnimator;
        }

        private void setupPath(boolean clockWise) {
            this.clockWise = clockWise;
        }

        @Override
        public void stop() {

            if (this.anim != null) {
                this.anim.cancel();
            }

            this.anim = stopAnim.clone();
            this.anim.start();
        }

        private ValueAnimator createStopAnimator() {
            final float[] from = new float[3];
            final float[] to = new float[3];
            final float[] hsv = new float[3];
            Color.colorToHSV(trackColor, from);
            Color.colorToHSV(stoppedColor, to);

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(500);
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                    hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
                    hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

                    currColor = Color.HSVToColor(hsv);
                }
            });
            valueAnimator.addListener(this);
            return valueAnimator;
        }

        @Override
        public boolean isRunning() {
            return animating;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (starting) {
                animating = true;
                starting = false;
                setupTrack();
                fill();
                rotate();
                return;
            }

            if (stopping) {
                animating = false;
                stopping = false;
                fill();
                stop();
                return;
            }

            if (!animating) {
                this.removeAllAnimations();
                return;
            }

            if (filling) {
                reverse();
            } else {
                fill(delayDuration);
            }
        }

        private void removeAllAnimations() {
            resetTrack();
            if (this.listener != null) {
                this.listener.onAnimationStopped();
            }
        }

        private void reverse() {
            filling = false;

            setupPath(false);

            if (this.anim != null) {
                this.anim.cancel();
            }
            this.anim = reverseAnim.clone();
            this.anim.start();
        }

        private ValueAnimator createReverseAnimator() {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(1.0f, 0.02f);
            valueAnimator.setDuration(reverseDuration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    fillValue = (float) animation.getAnimatedValue();
                }
            });
            valueAnimator.setInterpolator(new DecelerateInterpolator());
            valueAnimator.addListener(this);
            fillValue = 0.02f;
            return valueAnimator;
        }

        private void rotate() {
            if (!this.rotateAnim.isStarted()) {
                this.rotateAnim.start();
            }
        }

        private ValueAnimator createRotateAnimator() {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 360.0f);
            valueAnimator.setDuration(rotationDuration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ButtonTrack.this.rotation = (float) animation.getAnimatedValue();
                }
            });
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            return valueAnimator;
        }

        private void fill() {
            fill(0);
        }

        private void fill(long delayDuration) {
            filling = true;
            setupPath(true);

            if (this.anim != null) {
                this.anim.cancel();
            }
            currColor = trackColor;
            this.anim = createFillAnimator(delayDuration);
            this.anim.start();
        }

        private ValueAnimator createFillAnimator(long delayDuration) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.02f, 1.0f);
            valueAnimator.setStartDelay(delayDuration);
            valueAnimator.setDuration(fillDuration);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ButtonTrack.this.fillValue = (float) animation.getAnimatedValue();
                }
            });
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            valueAnimator.addListener(this);
            return valueAnimator;
        }

        private void setupTrack() {
            this.currColor = stoppedColor;
        }

        private void resetTrack() {
            if (this.anim != null) {
                this.anim.cancel();
            }

            if (this.rotateAnim != null) {
                this.rotateAnim.cancel();
            }

            this.clockWise = false;
            this.currColor = stoppedColor;
            this.rotation = 0;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }

        public void drawTrack(Canvas canvas) {
            if (rectf == null) return;

            canvas.save();
            canvas.rotate(rotation, rectf.centerX(), rectf.centerY());
            float till = (float) ending;
            if (animating) {
                till = (float) (ending * ((this.clockWise ? 1 : -1) * fillValue));
            }

            canvas.drawArc(rectf, (float) begining, till, false, getPaint());
            canvas.restore();
        }

        public void setBounds(Rect bounds) {
            this.rectf = new RectF(
                    (float) (bounds.left + inset),
                    (float) (bounds.top + inset),
                    (float) (bounds.right - inset),
                    (float) (bounds.bottom - inset)
            );
        }
    }

}