package com.silabs.thunderboard.common.ui;

import android.os.Build;
import android.support.annotation.StyleRes;
import android.widget.TextView;

public class ViewUtils {
    @SuppressWarnings("deprecation")
    public static void setTextAppearance(TextView textView, @StyleRes int styleResId) {
        if (Build.VERSION.SDK_INT < 23) {
            textView.setTextAppearance(textView.getContext(), styleResId);
        } else {
            textView.setTextAppearance(styleResId);
        }
    }
}
