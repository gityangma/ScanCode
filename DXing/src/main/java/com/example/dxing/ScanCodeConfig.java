package com.example.dxing;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.fragment.app.Fragment;

import com.example.dxing.model.ScanCodeModel;
import com.example.dxing.utils.ScanUtil;


/**
 * @author am
 */
public class ScanCodeConfig {
    public static final int QUESTCODE = 0x010;
    public static final String CODE_KEY = "code";
    public static final String CODE_TYPE = "code_type";
    protected static final String MODEL_KEY = "model";

    private final Activity mActivity;
    private final Fragment mFragment;
    private final ScanCodeModel mModel;

    public ScanCodeConfig(ScanCodeModel mModel) {
        this.mActivity = mModel.mActivity;
        this.mFragment = mModel.mFragment;
        this.mModel = mModel;
    }

    public static ScanCodeModel create(Activity mActivity) {
        return new ScanCodeModel(mActivity);
    }

    public static ScanCodeModel create(Activity mActivity, Fragment mFragment) {
        return new ScanCodeModel(mActivity, mFragment);
    }

    public void start(Class<?> mClass) {
        if (mFragment != null) {
            Intent intent = new Intent(mActivity, mClass);
            intent.putExtra(MODEL_KEY, mModel);
            mFragment.startActivityForResult(intent, QUESTCODE);
        } else {
            Intent intent = new Intent(mActivity, mClass);
            intent.putExtra(MODEL_KEY, mModel);
            mActivity.startActivityForResult(intent, QUESTCODE);
        }
    }

    public static String scanningImage(Activity mActivity, Uri uri) {
        return ScanUtil.scanningImage(mActivity, uri);
    }

    public static String scanningImageByBitmap(Bitmap bitmap) {
        return ScanUtil.scanningImageByBitmap(bitmap);
    }
}
