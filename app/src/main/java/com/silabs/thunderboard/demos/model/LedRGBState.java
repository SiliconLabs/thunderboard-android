package com.silabs.thunderboard.demos.model;

/**
 * Created by james.ayvaz on 6/28/16.
 */

public class LedRGBState {
    public final boolean on;
    public final LedRGB color;

    public LedRGBState(final boolean on, final LedRGB color) {
        this.on = on;
        this.color = color;
    }

    @Override
    public String toString() {
        return "LedRGBState{" +
                "color=" + color +
                ", on=" + on +
                '}';
    }
}
