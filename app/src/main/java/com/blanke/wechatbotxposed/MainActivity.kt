package com.blanke.wechatbotxposed

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.vicp.biggee.xposed.wechat.ErrorHandler

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.setDefaultUncaughtExceptionHandler(ErrorHandler)
        setContentView(R.layout.activity_main)
    }
}
