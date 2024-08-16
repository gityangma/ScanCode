package com.example.dxing.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.dxing.type.DecodeFormatType;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class ScanUtil {

    /**
     * 解码uri二维码图片
     *
     * @return 解码内容
     */
    public static String scanningImage(Activity mActivity, Uri uri) {
        if (uri == null) {
            return null;
        }
        Bitmap srcBitmap = getBitmapByUri(mActivity, uri);
        return scanningImageByBitmap(srcBitmap);
    }


    /**
     * 解码bitmap二维码图片
     *
     * @return 解码内容
     */
    public static String scanningImageByBitmap(Bitmap srcBitmap) {
        if (srcBitmap == null) {
            return null;
        }
        MultiFormatReader formatReader = new MultiFormatReader();
        Hashtable<DecodeHintType, Object> hints = new Hashtable<>();
        Vector<BarcodeFormat> decodeFormats = new Vector<>();
        decodeFormats.addAll(DecodeFormatType.ONE_CODE);
        decodeFormats.addAll(DecodeFormatType.TWO_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        formatReader.setHints(hints);
        Result result = null;
        int width = srcBitmap.getWidth();
        int height = srcBitmap.getHeight();
        int[] pixels = new int[width * height];
        srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            result = formatReader.decode(binaryBitmap);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        if (result != null) {
            return result.getText();
        }
        return null;
    }


    private static Bitmap getBitmapByUri(Activity mActivity, Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
