package com.example.dxing.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({ScanMode.RESTART, ScanMode.REVERSE})
public @interface ScanMode {
    int RESTART = 1;
    int REVERSE = 2;
}
