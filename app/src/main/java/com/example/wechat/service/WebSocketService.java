package com.example.wechat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.wechat.Utils.WsManager;

public class WebSocketService  extends Service {
    private WsManager wsManager=WsManager.getInstance();
    private final static String TAG="WebSocketService";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"绑定");
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"开始执行命令");
        if(wsManager.getContext()==null){
            wsManager.setContext(this);
            wsManager.connect();
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"创建websocket服务");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "服务终止");
        super.onDestroy();
    }
}
