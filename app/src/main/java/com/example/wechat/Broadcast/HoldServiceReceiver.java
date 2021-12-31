package com.example.wechat.Broadcast;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.wechat.application.MyApplication;
import com.example.wechat.service.WebSocketService;

public class HoldServiceReceiver extends BroadcastReceiver {
    private MyApplication application=MyApplication.getInstance();
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) { //检查Service状态
            boolean isServiceRunning = false;
            ActivityManager manager = (ActivityManager) application.getContext().getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service :manager.getRunningServices(Integer.MAX_VALUE))
            {
                //Service的类名
                if("com.example.wechat.service.WebSocketService".equals(service.service.getClassName())){
                    isServiceRunning = true;
                }
            }
            if (!isServiceRunning) {
                Intent i = new Intent(context, WebSocketService.class);
                context.startService(i);
            }
        }
    }
}
