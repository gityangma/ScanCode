package com.example.dxing.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.dxing.R;
import com.example.dxing.utils.SizeUtil;

/**
 *
 */
public class ScanQqView extends BaseScanView {

    private int scanMarginWith;
    private int scanMarginHeight;

    private Paint paint;
    private Bitmap scanLine;
    private Rect scanRect;
    private Rect lineRect;
    /**
     * 画布截取
     */
    private Rect interceptRect;
    /**
     * 扫描线位置
     */
    private int scanLineTop;
    /**
     * 扫描框大小
     */
    private int scanWidth;
    private int bitmapHeight;

    public ScanQqView(Context context) {
        super(context);
        init();
    }

    public ScanQqView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScanQqView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public Rect getScanRect() {
        return scanRect;
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scanLine = BitmapFactory.decodeResource(getResources(),
                R.drawable.scanqq);

        bitmapHeight = scanLine.getHeight();

        interceptRect = new Rect();
        scanRect = new Rect();
        lineRect = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        scanMarginWith = getMeasuredWidth() / 10;
        scanMarginHeight = getMeasuredHeight() >> 2;
        scanWidth = getMeasuredWidth() - 2 * scanMarginWith;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        scanRect.set(scanMarginWith, scanMarginHeight, getWidth() - scanMarginWith, scanMarginHeight + scanWidth);
        drawFrameBounds(canvas, scanRect);
        lineRect.set(scanMarginWith, scanLineTop, getWidth() - scanMarginWith, scanLineTop + bitmapHeight);
        canvas.drawBitmap(scanLine, null, lineRect, paint);
        initAnim();
    }


    /**
     * 绘制取景框边框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameBounds(Canvas canvas, Rect frame) {
        paint.setColor(ContextCompat.getColor(getContext(), R.color.qqscan));
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.FILL);

        int corWidth = SizeUtil.dp2px(getContext(), 4);
        int corLength = SizeUtil.dp2px(getContext(), 15);
        int radius = SizeUtil.dp2px(getContext(), 2);

        interceptRect.set(scanRect.left - corWidth, scanRect.top - corWidth, scanRect.right + corWidth, scanRect.bottom + corWidth);

        // 左上角
        canvas.drawRoundRect(frame.left - corWidth, frame.top - corWidth, frame.left, frame.top
                + corLength, radius, radius, paint);
        canvas.drawRoundRect(frame.left - corWidth, frame.top - corWidth, frame.left
                + corLength, frame.top, radius, radius, paint);
        // 右上角
        canvas.drawRoundRect(frame.right, frame.top - corWidth, frame.right + corWidth,
                frame.top + corLength, radius, radius, paint);
        canvas.drawRoundRect(frame.right - corLength, frame.top - corWidth,
                frame.right + corWidth, frame.top, radius, radius, paint);
        // 左下角
        canvas.drawRoundRect(frame.left - corWidth, frame.bottom - corLength,
                frame.left, frame.bottom + corWidth, radius, radius, paint);
        canvas.drawRoundRect(frame.left - corWidth, frame.bottom, frame.left
                + corLength, frame.bottom + corWidth, radius, radius, paint);
        // 右下角
        canvas.drawRoundRect(frame.right, frame.bottom - corLength, frame.right
                + corWidth, frame.bottom + corWidth, radius, radius, paint);
        canvas.drawRoundRect(frame.right - corLength, frame.bottom, frame.right
                + corWidth, frame.bottom + corWidth, radius, radius, paint);

        canvas.clipRect(interceptRect);
    }

    @Override
    public void initAnim() {
        if (valueAnimator == null) {
            valueAnimator = ValueAnimator.ofInt(scanRect.top - bitmapHeight, scanRect.bottom - bitmapHeight);
            valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    scanLineTop = (int) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
            valueAnimator.start();
        }
    }

    @Override
    public void startAnim() {
        if (valueAnimator != null) {
            valueAnimator.start();
        }
    }

    @Override
    public void pauseAnim() {
        if (valueAnimator != null) {
            valueAnimator.pause();
        }
    }


    @Override
    public void cancelAnim() {
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }
}
