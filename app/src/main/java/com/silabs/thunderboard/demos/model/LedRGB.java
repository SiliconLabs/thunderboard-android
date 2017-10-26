package com.silabs.thunderboard.demos.model;

/**
 * Created by james.ayvaz on 6/28/16.
 */

public class LedRGB {
    public final int red;
    public final int green;
    public final int blue;

    public LedRGB(final int red, final int green, final int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public String toString() {
        return "LedRGB{" +
                "blue=" + blue +
                ", red=" + red +
                ", green=" + green +
                '}';
    }
}
