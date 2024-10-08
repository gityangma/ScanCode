package com.example.dxing

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.ImageFormat
import android.graphics.Rect
import android.util.Size
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.dxing.model.ScanCodeModel
import com.example.dxing.result.OnScancodeListener
import com.example.dxing.type.ConfigType
import com.example.dxing.type.DecodeFormatType
import com.example.dxing.utils.AudioUtil
import com.example.dxing.utils.ImageRotateUtil
import com.example.dxing.utils.ImageUtil
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.multi.qrcode.QRCodeMultiReader
import com.example.dxing.utils.YuvToArrayUtil
import java.util.Hashtable
import java.util.Vector


class ScanCodeAnalyzer(
    mActivity: Activity,
    private val scanCodeModel: ScanCodeModel,
    private val scanRect: Rect?,
    private val onScancodeListener: OnScancodeListener
) : ImageAnalysis.Analyzer {

    private val audioUtil: AudioUtil = AudioUtil(mActivity, scanCodeModel.audioId)
    private val reader: MultiFormatReader = initReader()
    private val mMultiResultReader: QRCodeMultiReader = QRCodeMultiReader()

    private var mScanRect: Rect = Rect()

    @Volatile
    private var pauseAnalyzer = false

    private var mLastMultiReaderCodeCount: Int = 0

    private var mResolutionSize: Size? = null

    private fun initReader(): MultiFormatReader {
        val formatReader = MultiFormatReader()
        val hints = Hashtable<DecodeHintType, Any>()
        val decodeFormats = Vector<BarcodeFormat>()
        decodeFormats.addAll(DecodeFormatType.ONE_CODE)
        decodeFormats.addAll(DecodeFormatType.TWO_CODE)
        hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        hints[DecodeHintType.CHARACTER_SET] = "UTF-8"
        formatReader.setHints(hints)
        return formatReader
    }

    /**
     * 设置分辨率
     */
    fun setResolutionSize(resolutionSize: Size) {
        this.mResolutionSize = resolutionSize
    }

    /**
     * 恢复多二维码识别
     */
    fun resumeMultiReader() {
        if (!scanCodeModel.isIdentifyMultiple) {
            return
        }
        this.pauseAnalyzer = false
    }


    /**
     * 图片分析
     */
    @SuppressLint("UnsafeOptInUsageError", "UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        if (pauseAnalyzer) {
            image.close()
            return
        }
        if (ImageFormat.YUV_420_888 != image.format) {
            image.close()
            throw Throwable("${ConfigType.TAG} expect YUV_420_888, now = ${image.format}")
        }
        //将buffer数据写入数组
        val data = YuvToArrayUtil.yuvToArray(image.image!!)
        //图片宽高
        val width = image.width
        val height = image.height

        val rotateByteArray = ImageRotateUtil.instance.rotateYuvArrayImage(data, width, height, 90)

        mScanRect.set(0, 0, rotateByteArray.second, rotateByteArray.third)

        scanRect?.apply {
            //限制区域
            if (scanCodeModel.isLimitRect) {
                val copyScanRect = Rect(left, top, right, bottom)
                var scaleWidthFactor: Float
                var scaleHeightFactor: Float
                mResolutionSize?.let {
                    scaleWidthFactor = rotateByteArray.second / it.width.toFloat()
                    scaleHeightFactor = rotateByteArray.third / it.height.toFloat()
                    copyScanRect.let { rect ->
                        rect.set(
                            (rect.left * scaleWidthFactor).toInt(),
                            (rect.top * scaleHeightFactor).toInt(),
                            (rect.right * scaleWidthFactor).toInt(),
                            (rect.bottom * scaleHeightFactor).toInt()
                        )
                    }
                }
                if (copyScanRect.width() <= rotateByteArray.second && copyScanRect.height() <= rotateByteArray.third) {
                    mScanRect.set(
                        copyScanRect.left,
                        copyScanRect.top,
                        copyScanRect.right,
                        copyScanRect.bottom
                    )
                }
            }
        }

        val source = PlanarYUVLuminanceSource(
            rotateByteArray.first,
            rotateByteArray.second,
            rotateByteArray.third,
            mScanRect.left,
            mScanRect.top,
            mScanRect.width(),
            mScanRect.height(),
            false
        )

        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            if (scanCodeModel.isIdentifyMultiple) {
                val results = mMultiResultReader.decodeMultiple(bitmap)
                if (results.isEmpty()) {
                    return
                }
                if (results.size < 2) {
                    mLastMultiReaderCodeCount++
                    if (mLastMultiReaderCodeCount <= ConfigType.MULTI_READER_MIN_COUNT) {
                        return
                    }
                }
                mLastMultiReaderCodeCount = 0
                if (scanCodeModel.isPlayAudio) audioUtil.playSound()
                val snapshotBitmap = ImageUtil.nv21ToBitmap(
                    rotateByteArray.first,
                    rotateByteArray.second,
                    rotateByteArray.third
                )
                val realSize = Size(rotateByteArray.second, rotateByteArray.third)
                onScancodeListener.onBackMultiResultCode(snapshotBitmap, results, realSize)
                pauseAnalyzer = true
            } else {
                val result = reader.decode(bitmap)
                if (scanCodeModel.isPlayAudio) audioUtil.playSound()
                onScancodeListener.onBackCode(result)
            }
        } catch (e: Exception) {
            image.close()
        } finally {
            image.close()
        }
    }
}