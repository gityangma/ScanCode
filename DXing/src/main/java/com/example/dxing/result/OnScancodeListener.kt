package com.example.dxing.result

import android.graphics.Bitmap
import android.util.Size
import com.google.zxing.Result

/**
 * 识别成功回调
 */
interface OnScancodeListener {
    /**
     * 扫码内容回调
     */
    fun onBackCode(result: Result)

    /**
     * 多个扫码内容回调， 只限二维码
     */
    fun onBackMultiResultCode(resultBitmap: Bitmap, results: Array<Result>, realSize: Size)
}