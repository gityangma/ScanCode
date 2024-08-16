package com.example.dxing;

import android.graphics.Bitmap;

import com.example.dxing.utils.ScanUtil;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Collections;

public class ZBarDecoder {

    public String decodeRaw(Bitmap barcodeBmp) {
        return ScanUtil.scanningImageByBitmap(barcodeBmp);
    }

    public String decodeRaw(byte[] var1, int var2, int var3) {
        return decodeByteArray(var1,var2,var3);
    }

    public String decodeCrop(byte[] var1, int var2, int var3, int var4, int var5, int var6, int var7) {
        return decodeByteArray(var1,var2,var3);
    }

    public String decodeByteArray(byte[] byteArray,int var2, int var3) {
        int width = var2; // 图片宽度
        int height = var3; // 图片高度
        int[] pixels = new int[width * height];

        // 将byte[]转换为RGB格式的像素数组
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                int r = byteArray[index] & 0xff;
                int g = byteArray[index] & 0xff;
                int b = byteArray[index] & 0xff;
                pixels[index] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }

        // 创建RGBLuminanceSource对象
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

        // 创建BinaryBitmap对象
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        // 设置解码参数
        MultiFormatReader reader = new MultiFormatReader();
        DecodeHintType hintType = DecodeHintType.TRY_HARDER;
        reader.setHints(Collections.singletonMap(hintType, Boolean.TRUE));

        try {
            // 解码
            Result result = reader.decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
