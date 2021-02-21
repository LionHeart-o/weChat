package com.example.wechat.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;
import java.util.TimerTask;

public class TimeService extends Service {
    int time = 60;
    private Timer timer;
    private TimerTask task;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();     //创建计时器对象
        task = new TimerTask() {
            @Override
            public void run() {
                time = time - 1;
                Intent intent = new Intent();
                intent.setAction("getCaptcha");
                intent.putExtra("time",time);
                sendBroadcast(intent);
                if(time <=0){
                    stopSelf();
                }
            }
        };
        timer.schedule(task,0,1000);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        task = null;
        timer = null;
    }
}
