package com.silabs.thunderboard.demos.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;

import static com.silabs.thunderboard.demos.model.HallState.CLOSED;
import static com.silabs.thunderboard.demos.model.HallState.OPENED;
import static com.silabs.thunderboard.demos.model.HallState.TAMPERED;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Retention(SOURCE)
@IntDef({ CLOSED, OPENED, TAMPERED })
public @interface HallState {
    int CLOSED = 0;
    int OPENED = 1;
    int TAMPERED = 2;
}
