package com.example.wechat.application;

import android.app.Application;

public class MyApplication extends Application {
    private static final String BACK_URL = "http://192.168.1.6:8848/android_back_end/";

    private String back_end_url;

    @Override
    public void onCreate()
    {
        super.onCreate();
        setBack_end_url(BACK_URL); // 初始化全局变量
    }

    public String getBack_end_url() {
        return back_end_url;
    }

    public void setBack_end_url(String back_end_url) {
        this.back_end_url = back_end_url;
    }

}
