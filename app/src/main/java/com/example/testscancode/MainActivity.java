package com.example.testscancode;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

import com.example.dxing.ScanCodeActivity;
import com.example.dxing.ScanCodeConfig;
import com.example.dxing.ZBarDecoder;
import com.example.dxing.model.ScanMode;
import com.example.dxing.model.ScanStyle;
import com.tbruyelle.rxpermissions3.Permission;
import com.tbruyelle.rxpermissions3.RxPermissions;



/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private RadioGroup rgParent, rgScanType, rgCodeColor;
    private AppCompatButton btnScan, btnTwoScan, btnScanMyStyle;
    private AppCompatTextView tvCode;
    private AppCompatButton btnScanAlbum;

    private boolean isMultiple = false;

    private final ActivityResultLauncher<String> albumLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (result == null) {
                return;
            }
            //接收图片识别结果
            String code = ScanCodeConfig.scanningImage(MainActivity.this, result);
            tvCode.setText(String.format("识别结果： %s", code));
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rgParent = findViewById(R.id.rg_parent);
        rgScanType = findViewById(R.id.rg_scan_type);
        btnScan = findViewById(R.id.btn_scan);
        btnTwoScan = findViewById(R.id.btn_scantwo);
        btnScanMyStyle = findViewById(R.id.btn_scanmystyle);
        btnScanAlbum = findViewById(R.id.btn_scan_album);

        tvCode = findViewById(R.id.tv_code);
        setListener();
    }

    private void setListener() {
        //设置识别类型 单码识别 或 多码识别
        rgScanType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.rb_one_code){

                    isMultiple = false;
                }else if (checkedId == R.id.rb_multiple_code){
                    isMultiple = true;
                }
//                switch (checkedId) {
//                    case R.id.rb_one_code:
//                        isMultiple = false;
//                        break;
//                    case R.id.rb_multiple_code:
//                        isMultiple = true;
//                        break;
//                    default:
//                        break;
//                }
            }
        });
        //预定义界面
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checkedRadioButtonId = rgParent.getCheckedRadioButtonId();

                if (checkedRadioButtonId == R.id.rb_none){
                    startScan(ScanStyle.NONE, ScanCodeActivity.class);
                }else if (checkedRadioButtonId == R.id.rb_qq){
                    startScan(ScanStyle.M_QQ, ScanCodeActivity.class);
                }else if (checkedRadioButtonId == R.id.rb_wechat){
                    startScan(ScanStyle.M_WECHAT, ScanCodeActivity.class);
                }
//                switch (checkedRadioButtonId) {
//                    case R.id.rb_none:
//                        startScan(ScanStyle.NONE, ScanCodeActivity.class);
//                        break;
//                    case R.id.rb_qq:
//                        startScan(ScanStyle.QQ, ScanCodeActivity.class);
//                        break;
//                    case R.id.rb_wechat:
//                        startScan(ScanStyle.WECHAT, ScanCodeActivity.class);
//                        break;
//                    default:
//                        break;
//                }
            }
        });

        // ScanStyle.CUSTOMIZE 配置界面
        btnTwoScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toCustomization();
            }
        });

        //自定义界面
        btnScanMyStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int checkedRadioButtonId = rgParent.getCheckedRadioButtonId();

                if (checkedRadioButtonId == R.id.rb_none){
                    startScan(ScanStyle.NONE, MyScanActivity.class);
                }else if (checkedRadioButtonId == R.id.rb_qq){
                    startScan(ScanStyle.M_QQ, MyScanActivity.class);
                }else if (checkedRadioButtonId == R.id.rb_wechat){
                    startScan(ScanStyle.M_WECHAT, MyScanActivity.class);
                }
//                switch (checkedRadioButtonId) {
//                    case R.id.rb_none:
//                        startScan(ScanStyle.NONE, MyScanActivity.class);
//                        break;
//                    case R.id.rb_qq:
//                        startScan(ScanStyle.QQ, MyScanActivity.class);
//                        break;
//                    case R.id.rb_wechat:
//                        startScan(ScanStyle.WECHAT, MyScanActivity.class);
//                        break;
//                    default:
//                        break;
//                }
            }
        });

        //识别相册内二维码
        btnScanAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toAlbum();
            }
        });


    }

    private void toAlbum() {
        albumLauncher.launch("image/*");
    }


    private void toCustomization() {
        new RxPermissions(this)
                .requestEachCombined(Manifest.permission.CAMERA)
                .subscribe(new Observer<Permission>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull Permission permission) {
                        if (permission.granted) {
                            ScanCodeConfig.create(MainActivity.this)
                                    //设置扫码页样式 ScanStyle.NONE：无  ScanStyle.QQ ：仿QQ样式   ScanStyle.WECHAT ：仿微信样式  ScanStyle.CUSTOMIZE ： 自定义样式
                                    .setStyle(ScanStyle.CUSTOMIZE)
                                    //扫码成功是否播放音效  true ： 播放   false ： 不播放
                                    .setPlayAudio(true)
                                    //设置音效音频
                                    .setAudioId(com.example.dxing.R.raw.beep)
                                    ////////////////////////////////////////////
                                    //以下配置 在style为 ScanStyle.CUSTOMIZE 时生效
                                    //设置扫码框位置  left ： 边框左边位置   top ： 边框上边位置   right ： 边框右边位置   bottom ： 边框下边位置   单位/dp
//                                    .setScanRect(new ScanRect(50, 200, 300, 450), false)
                                    //是否限制识别区域为设定扫码框大小  true:限制  false：不限制   默认false：识别区域为整个屏幕
                                    .setLimitRect(true)
                                    //设置扫码框位置 scanSize : 扫码框大小   offsetX ： x轴偏移量    offsetY ：y轴偏移量   单位 /px
                                    .setScanSize(600, 0, 0)
                                    //是否显示边框上四个角标 true ： 显示  false ： 不显示
                                    .setShowFrame(true)
                                    //设置边框上四个角标颜色
                                    .setFrameColor(R.color.whilte)
                                    //设置边框上四个角标圆角  单位 /dp
                                    .setFrameRadius(2)
                                    //设置边框上四个角宽度 单位 /dp
                                    .setFrameWith(4)
                                    //设置边框上四个角长度 单位 /dp
                                    .setFrameLength(15)
                                    //设置是否显示边框外部阴影 true ： 显示  false ： 不显示
                                    .setShowShadow(true)
                                    //设置边框外部阴影颜色
                                    .setShadeColor(com.example.dxing.R.color.black_tran30)
                                    //设置扫码条运动方式   ScanMode.REVERSE : 往复运动   ScanMode.RESTART ：重复运动    默认ScanMode.RESTART
                                    .setScanMode(ScanMode.REVERSE)
                                    //设置扫码条扫一次时间  单位/ms  默认3000
                                    .setScanDuration(3000)
                                    //设置扫码条图片
                                    .setScanBitmapId(com.example.dxing.R.drawable.scan_wechatline)
                                    //////////////////////////////////////////////
                                    //////////////////////////////////////////////
                                    //以下配置在 setIdentifyMultiple 为 true 时生效
                                    //设置是否开启识别多个二维码 true：开启 false：关闭   开启后识别到多个二维码会停留在扫码页 手动选择需要解析的二维码后返回结果
                                    .setIdentifyMultiple(isMultiple)
                                    //设置 二维码提示按钮的宽度 单位：px
                                    .setQrCodeHintDrawableWidth(120)
                                    //设置 二维码提示按钮的高度 单位：px
                                    .setQrCodeHintDrawableHeight(120)
                                    //设置 二维码提示按钮的Drawable资源
//                                    .setQrCodeHintDrawableResource(R.mipmap.in)
                                    //设置 二维码提示Drawable 是否开启缩放动画效果
                                    .setStartCodeHintAnimation(true)
                                    //设置 二维码选择页 背景透明度
                                    .setQrCodeHintAlpha(0.5f)
                                    //////////////////////////////////////////////
                                    .buidler()
                                    //跳转扫码页   扫码页可自定义样式
                                    .start(MyScanActivity.class);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    //开始扫描
    private void startScan(int style, Class mClass) {
        new RxPermissions(this)
                .requestEachCombined(Manifest.permission.CAMERA)
                .subscribe(new Observer<Permission>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                    }

                    @Override
                    public void onNext(@NonNull Permission permission) {
                        if (permission.granted) {
                            ScanCodeConfig.create(MainActivity.this)
                                    //设置扫码页样式 ScanStyle.NONE：无  ScanStyle.QQ ：仿QQ样式   ScanStyle.WECHAT ：仿微信样式
                                    .setStyle(style)
                                    //扫码成功是否播放音效  true ： 播放   false ： 不播放
                                    .setPlayAudio(true)
                                    //////////////////////////////////////////////
                                    //以下配置在 setIdentifyMultiple 为 true 时生效
                                    //设置是否开启识别多个二维码 true：开启 false：关闭   开启后识别到多个二维码会停留在扫码页 手动选择需要解析的二维码后返回结果
                                    .setIdentifyMultiple(isMultiple)
                                    //设置 二维码提示按钮的宽度 单位：px
                                    .setQrCodeHintDrawableWidth(120)
                                    //设置 二维码提示按钮的高度 单位：px
                                    .setQrCodeHintDrawableHeight(120)
                                    //设置 二维码提示按钮的Drawable资源
//                                    .setQrCodeHintDrawableResource(R.mipmap.in)
                                    //设置 二维码提示Drawable 是否开启缩放动画效果
                                    .setStartCodeHintAnimation(true)
                                    //设置 二维码选择页 背景透明度
                                    .setQrCodeHintAlpha(0.5f)
                                    //////////////////////////////////////////////
                                    .buidler()
                                    //跳转扫码页   扫码页可自定义样式
                                    .start(mClass);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case ScanCodeConfig.QUESTCODE:
                    //接收扫码结果
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        int codeType = extras.getInt(ScanCodeConfig.CODE_TYPE);
                        String code = extras.getString(ScanCodeConfig.CODE_KEY);
                        tvCode.setText(String.format(
                                "扫码结果：\n" +
                                        "码类型: %s  \n" +
                                        "码值  : %s", codeType == 0 ? "一维码" : "二维码", code));
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
