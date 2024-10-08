package com.example.dxing

import android.animation.AnimatorSet
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.example.dxing.BaseScanActivity
import com.example.dxing.PreviewTouchListener
import com.example.dxing.R
import com.example.dxing.model.ScanCodeModel
import com.example.dxing.model.ScanStyle
import com.example.dxing.model.ScanType
import com.example.dxing.type.DecodeFormatType
import com.example.dxing.view.BaseScanView
import com.example.dxing.view.ScanCustomizeView
import com.example.dxing.view.ScanQqView
import com.example.dxing.view.ScanWechatView
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.example.dxing.ScanCodeAnalyzer
import com.example.dxing.result.OnScancodeListener
import com.example.dxing.type.ConfigType
import com.example.dxing.view.CodeHintDefaultDrawable


import java.lang.Math.abs
import java.lang.Math.max
import java.lang.Math.min
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

open class ScanCodeActivity : BaseScanActivity(), OnScancodeListener {

    //设置所选相机
    private val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var resolutionSize: Size
    private lateinit var camera: Camera
    private lateinit var preview: Preview
    private lateinit var imageAnalyzer: ImageAnalysis
    private lateinit var cameraControl: CameraControl
    private lateinit var mCameraInfo: CameraInfo
    private lateinit var cameraExecutor: ExecutorService
    private var baseScanView: BaseScanView? = null
    private var rlParentContent: RelativeLayout? = null
    private lateinit var scModel: ScanCodeModel

    private var rlCodeHintContainer: RelativeLayout? = null
    private var mScanCodeAnalyzer: ScanCodeAnalyzer? = null

    private var mAnimeSetList: MutableList<AnimatorSet> = mutableListOf()

    companion object {
        private const val TAG_CODE_HINT = "Dxing_CodeHintContainer"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }


    override fun getLayoutId(): Int = R.layout.activity_scancode
    private lateinit var pvCamera:PreviewView
    override fun initData() {
        pvCamera = findViewById<PreviewView>(R.id.pvCamera)
        scModel = intent?.extras?.getParcelable(ScanCodeConfig.MODEL_KEY)!!
        initCodeHintContainer()
        addScanView(scModel.style)
        // Initialize our background executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        mScanCodeAnalyzer = ScanCodeAnalyzer(
            this,
            scModel,
            baseScanView?.scanRect,
            this
        )
        // surface准备监听
        pvCamera.post {
            //设置需要实现的用例（预览，拍照，图片数据解析等等）
            bindCameraUseCases()
        }
    }

    private fun initCodeHintContainer() {
        rlCodeHintContainer = RelativeLayout(this).apply {
            val lp = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            layoutParams = lp
            translationZ = 99f
            tag = TAG_CODE_HINT
        }
    }

    fun setFlashStatus(isOpenFlash: Boolean) {
        cameraControl.enableTorch(isOpenFlash)
    }

    private fun addScanView(style: Int?) {
        rlParentContent = findViewById(R.id.rlparent)
        when (style) {
            ScanStyle.M_QQ -> {
                baseScanView = ScanQqView(this)
            }

            ScanStyle.M_WECHAT -> {
                baseScanView = ScanWechatView(this)
            }

            ScanStyle.CUSTOMIZE -> {
                baseScanView = ScanCustomizeView(this).apply {
                    setScanCodeModel(scModel)
                }
            }
        }
        baseScanView?.let {
            val lp: RelativeLayout.LayoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )
            it.layoutParams = lp
            rlParentContent?.addView(it, 1)
        }
    }

    private fun bindCameraUseCases() {
        val width = pvCamera.measuredWidth
        val height = pvCamera.measuredHeight

        resolutionSize = Size(width, height)
        mScanCodeAnalyzer?.setResolutionSize(resolutionSize)

        //获取旋转角度
        val rotation = pvCamera.display.rotation

        //生命周期绑定
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            // 预览用例
            preview = getCameraPreView(rotation)

            // 图像分析用例
            imageAnalyzer = getCameraAnalyzer(rotation)

            // 必须在重新绑定用例之前取消之前绑定
            cameraProvider.unbindAll()

            try {
                //获取相机实例
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                //设置预览的view
                preview.setSurfaceProvider(pvCamera.surfaceProvider)
                cameraControl = camera.cameraControl
                mCameraInfo = camera.cameraInfo

                bindTouchListener()
            } catch (exc: Exception) {
                Log.e("", "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * 获取相机预览用例
     */
    private fun getCameraPreView(rotation: Int): Preview {
        return Preview.Builder()
            .setTargetResolution(resolutionSize)
            .setTargetRotation(rotation)
            .build()
    }

    /**
     * 获取相机分析用例
     */
    private fun getCameraAnalyzer(rotation: Int): ImageAnalysis {
        val mImageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(resolutionSize)
            .setTargetRotation(rotation)
            .build()
        mScanCodeAnalyzer?.apply {
            mImageAnalysis.setAnalyzer(
                cameraExecutor,
                this
            )
        }
        return mImageAnalysis
    }

    /**
     * 触摸事件监听（放大、缩小、对焦）
     */
    private fun bindTouchListener() {
        val zoomState = mCameraInfo.zoomState
        val cameraXPreviewViewTouchListener =
            PreviewTouchListener(this)
        cameraXPreviewViewTouchListener.setCustomTouchListener(object :
            PreviewTouchListener.CustomTouchListener {
            override fun zoom(delta: Float) {
                zoomState.value?.let {
                    val currentZoomRatio = it.zoomRatio
                    cameraControl.setZoomRatio(currentZoomRatio * delta)
                }
            }

            override fun focus(pointX: Float, pointY: Float) {
                cameraFocus(pointX, pointY)
            }
        })
        pvCamera.setOnTouchListener(cameraXPreviewViewTouchListener)
    }

    /**
     * 对焦
     */
    private fun cameraFocus(pointX: Float, pointY: Float) {
        val factory = SurfaceOrientedMeteringPointFactory(
            resolutionSize.width.toFloat(),
            resolutionSize.height.toFloat()
        )
        val point = factory.createPoint(pointX, pointY)
        val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF)
            // auto calling cancelFocusAndMetering in 4 seconds
            .setAutoCancelDuration(4, TimeUnit.SECONDS)
            .build()

        cameraControl.startFocusAndMetering(action)
    }

    /**
     * 根据传入的值获取相机应该设置的分辨率比例
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    /**
     * 获取码类型
     */
    private fun getCodeType(barcodeFormat: BarcodeFormat): Int {
        if (DecodeFormatType.TWO_CODE.contains(barcodeFormat)) {
            return ScanType.CODE_TWO
        }
        if (DecodeFormatType.ONE_CODE.contains(barcodeFormat)) {
            return ScanType.CODE_ONE
        }
        return ScanType.UN_KNOW
    }

    private fun createCodeHintView(result: Result, realSize: Size): View {
        // 位置信息根据码的方向返回 不是根据屏幕方向 永远按码的正方向返回信息
        val resultPointOne = result.resultPoints[0]
        val resultPointTwo = result.resultPoints[1]
        val resultPointThree = result.resultPoints[2]

        var initOffsetX = 0
        var initOffsetY = 0
        if (scModel.isLimitRect && baseScanView?.scanRect != null) {
            initOffsetX = baseScanView?.scanRect?.left ?: 0
            initOffsetY = baseScanView?.scanRect?.top ?: 0
        }
        val hintDrawable = CodeHintDefaultDrawable(this)
        val width =
            if (scModel.qrCodeHintDrawableWidth > 0) scModel.qrCodeHintDrawableWidth else ConfigType.DEFAULT_CODE_HINT_SIZE.width
        val height =
            if (scModel.qrCodeHintDrawableHeight > 0) scModel.qrCodeHintDrawableHeight else ConfigType.DEFAULT_CODE_HINT_SIZE.height
        hintDrawable.setBounds(0, 0, width, height)
        val drawable = if (scModel.qrCodeHintDrawableResource > 0) ContextCompat.getDrawable(
            this,
            scModel.qrCodeHintDrawableResource
        ) else hintDrawable

        val scaleWidthFactor = resolutionSize.width / realSize.width.toFloat()
        val scaleHeightFactor = resolutionSize.height / realSize.height.toFloat()

        val maxX = maxOf(resultPointOne.x, resultPointTwo.x, resultPointThree.x)
        val maxY = maxOf(resultPointOne.y, resultPointTwo.y, resultPointThree.y)
        val minX = minOf(resultPointOne.x, resultPointTwo.x, resultPointThree.x)
        val minY = minOf(resultPointOne.y, resultPointTwo.y, resultPointThree.y)
        val centX = minX + ((maxX - minX) / 2)
        val centY = minY + ((maxY - minY) / 2)
        val startX = centX - ((width shr 1) * (1 / scaleWidthFactor))
        val startY = centY - ((height shr 1) * (1 / scaleHeightFactor))

        val offsetX = initOffsetX + (startX * scaleWidthFactor)
        val offsetY = initOffsetY + (startY * scaleHeightFactor)

        val ivCodeHint = AppCompatImageView(this)
        val lp = RelativeLayout.LayoutParams(width, height)
        lp.marginStart = offsetX.toInt()
        lp.topMargin = offsetY.toInt()
        ivCodeHint.layoutParams = lp
        ivCodeHint.setImageDrawable(drawable)
        ivCodeHint.setOnClickListener {
            callBackResult(result)
        }
        return ivCodeHint
    }

    private fun createCodeSnapshotView(bitmap: Bitmap): View {
        val ivCodeHint = AppCompatImageView(this)
        val lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        ivCodeHint.layoutParams = lp
        ivCodeHint.scaleType = ImageView.ScaleType.FIT_XY
        ivCodeHint.setImageBitmap(bitmap)
        return ivCodeHint
    }

    private fun createSmegmaView(): View {
        val vSmegma = View(this)
        val lp = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.MATCH_PARENT
        )
        vSmegma.layoutParams = lp
        vSmegma.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
        vSmegma.alpha = scModel.qrCodeHintAlpha
        return vSmegma
    }

    private fun callBackResult(result: Result) {
        val intent = Intent()
        intent.putExtra(
            ScanCodeConfig.CODE_TYPE,
            getCodeType(result.barcodeFormat)
        )
        intent.putExtra(ScanCodeConfig.CODE_KEY, result.text)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    /**
     * 清除二维码提示
     */
    private fun clearCodeHintContainer(): Boolean {
        if (rlParentContent == null) {
            return false
        }
        clearAnimation()
        for (i in 0 until rlParentContent!!.childCount) {
            val view = rlParentContent!!.getChildAt(i)
            if (TAG_CODE_HINT == view.tag) {
                (view as ViewGroup).removeAllViews()
                rlParentContent!!.removeView(view)
                return true
            }
        }
        return false
    }

    private fun createSmegma(resultBitmap: Bitmap, results: Array<Result>, realSize: Size) {
        rlCodeHintContainer?.addView(createCodeSnapshotView(resultBitmap))
        rlCodeHintContainer?.addView(createSmegmaView())
        results.forEach {
            val hintView = createCodeHintView(it, realSize)
            rlCodeHintContainer?.addView(hintView)
            if (scModel.isStartCodeHintAnimation) {
                createCodeAnimeSet(hintView)
            }
        }
        rlParentContent?.addView(rlCodeHintContainer)
    }

    private fun createCodeAnimeSet(view: View) {
        val keyframe1 = Keyframe.ofFloat(0f, 1f)
        val keyframe2 = Keyframe.ofFloat(0.7f, 1f)
        val keyframe3 = Keyframe.ofFloat(0.85f, 0.7f)
        val keyframe4 = Keyframe.ofFloat(1f, 1f)
        val frameHolderX =
            PropertyValuesHolder.ofKeyframe("scaleX", keyframe1, keyframe2, keyframe3, keyframe4)

        val animatorX = ObjectAnimator.ofPropertyValuesHolder(view, frameHolderX)
        animatorX.duration = 2000
        animatorX.repeatCount = ValueAnimator.INFINITE
        animatorX.repeatMode = ValueAnimator.REVERSE

        val frameHolderY =
            PropertyValuesHolder.ofKeyframe("scaleY", keyframe1, keyframe2, keyframe3, keyframe4)

        val animatorY = ObjectAnimator.ofPropertyValuesHolder(view, frameHolderY)
        animatorY.duration = 2000
        animatorY.repeatCount = ValueAnimator.INFINITE
        animatorY.repeatMode = ValueAnimator.REVERSE

        val codeHintAnimeSet = AnimatorSet()
        codeHintAnimeSet.playTogether(animatorX, animatorY)
        // 设置插值器，使动画速度变化更加平滑
        codeHintAnimeSet.interpolator = DecelerateInterpolator()
        codeHintAnimeSet.start()

        mAnimeSetList.add(codeHintAnimeSet)
    }

    private fun clearAnimation() {
        if (mAnimeSetList.isEmpty()) {
            return
        }
        mAnimeSetList.forEach {
            it.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdownNow()
        baseScanView?.cancelAnim()
        clearAnimation()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (clearCodeHintContainer()) {
            mScanCodeAnalyzer?.resumeMultiReader()
            return
        }
        super.onBackPressed()
    }

    override fun onBackCode(result: Result) {
        callBackResult(result)
    }

    override fun onBackMultiResultCode(
        resultBitmap: Bitmap,
        results: Array<Result>,
        realSize: Size
    ) {
        runOnUiThread {
            kotlin.Result.runCatching {
                if (results.size == 1) {
                    val result = results[0]
                    callBackResult(result)
                    return@runCatching
                }
                createSmegma(resultBitmap, results, realSize)
            }.onFailure {
                mScanCodeAnalyzer?.resumeMultiReader()
            }
        }
    }
}