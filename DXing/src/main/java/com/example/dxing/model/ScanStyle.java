package com.example.dxing.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({ScanStyle.NONE, ScanStyle.M_QQ, ScanStyle.M_WECHAT, ScanStyle.CUSTOMIZE})
public @interface ScanStyle {
    int NONE = -1;
    int M_QQ = 1001;
    int M_WECHAT = 1002;
    int CUSTOMIZE = 1003;
}
