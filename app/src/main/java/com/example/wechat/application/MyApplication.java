package com.example.wechat.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.example.wechat.Broadcast.HoldServiceReceiver;
import com.example.wechat.javaBean.ContactBean;

import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    //public static final String BACK_URL = "http://192.168.26.171:8848/android_back_end/";
    //public static final String BACK_URL = "http://192.168.123.22:8848/android_back_end/";
    public static final String BACK_URL = "http://159.75.27.108/android_back_end/";
    public static final String FILE_SAVE_URL = "http://159.75.27.108/websocket/upload/";
    public static final String ANNOUNCEMENT_URL = "http://159.75.27.108/announcement/";

    private final static String TAG="MyApplication";
    private static MyApplication singleton;

    private Context context;



    public static synchronized MyApplication getInstance() {
        if (singleton == null) {
            singleton = new MyApplication();
        }
        return singleton;
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG,"绑定维持信息接收服务广播");
        /*IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        HoldServiceReceiver receiver = new HoldServiceReceiver();
        registerReceiver(receiver, filter);*/
    }

    public void setContext(Context context){
        this.context=context;
    }

    public Context getContext(){
        return context;
    }
}
