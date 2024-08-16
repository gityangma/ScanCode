package com.example.dxing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type.statusBars


/**
 * 封装扫码Activity基类
 * 用于自定义扫码Activity
 */

abstract class BaseScanActivity : AppCompatActivity() {

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //屏幕方向 竖向
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(getLayoutId())
        //数据
        initData()
        //状态栏
        initStateBar()
    }


    abstract fun getLayoutId(): Int

    abstract fun initData()

    private fun initStateBar(){

        val decorView = window.decorView

        val controller = ViewCompat.getWindowInsetsController(decorView)
        //隐藏状态栏
        controller?.hide(statusBars())

        //设置状态栏为透明色
        window.statusBarColor = Color.TRANSPARENT
        //设置导航栏为透明色
        window.navigationBarColor = Color.TRANSPARENT

    }
}