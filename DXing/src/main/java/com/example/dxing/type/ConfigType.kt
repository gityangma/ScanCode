package com.example.dxing.type

import android.util.Size

object ConfigType {

    const val TAG = "Config"

    /**
     * 默认二维码提示大小
     */
    val DEFAULT_CODE_HINT_SIZE = Size(120, 120)

    /**
     * 多二维码识别缓冲次数
     */
    const val MULTI_READER_MIN_COUNT = 8
}