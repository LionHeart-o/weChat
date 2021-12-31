package com.example.wechat.Activity;

import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.wechat.R;
import com.example.wechat.Utils.MP3Utils;
import com.example.wechat.javaBean.MusicBean;
import com.example.wechat.service.MusicService;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayMusicActivity extends AppCompatActivity {

    private static SeekBar sb;//进度条
    private static TextView tv_progress,tv_total;//时间进度，时间总长度
    private ObjectAnimator animator;//一个动画框架
    private ImageView music_cd_cover;

    private TextView music_title;
    private TextView music_singer;
    private TextView music_album;

    private MusicService.MusicControl musicControl;;//音乐控制器，继承Binder
    private MyServiceConn conn;
    private Intent intent;

    private boolean isUnbind=false;//是否失联
    private boolean isPlaying=true;

    private String musicUrl;

    public static ImageView music_play_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Intent intent=getIntent();
        musicUrl=intent.getStringExtra("musicUrl");
        Log.d("nmsl","获取链接"+musicUrl);
        init();
    }
    private void init(){

        tv_progress=(TextView) findViewById(R.id.tv_progress);
        tv_total=(TextView) findViewById(R.id.tv_total);
        sb=(SeekBar) findViewById(R.id.sb);

        music_cd_cover=findViewById(R.id.music_cd_cover);
        music_play_btn=(ImageView) findViewById(R.id.btn_play);

        music_title=(TextView) findViewById(R.id.main_music_title);
        music_singer=(TextView) findViewById(R.id.main_music_singer);
        music_album=(TextView) findViewById(R.id.main_music_album);

        music_play_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPlaying){
                    musicControl.continuePlay();
                    animator.start();
                    Glide.with(getApplicationContext()).load(R.mipmap.play).into(music_play_btn);
                }else {
                    musicControl.pausePlay();
                    animator.pause();
                    Glide.with(getApplicationContext()).load(R.mipmap.pause).into(music_play_btn);
                }
                isPlaying=!isPlaying;
            }
        });

        intent=new Intent(this,MusicService.class);
        intent.putExtra("musicUrl",musicUrl);
        MusicBean music=new MusicBean();
        music.setMusicUrl(musicUrl);
        music= MP3Utils.getSongInfo(music);

        music_title.setText(music.getTitle());
        music_singer.setText(music.getSinger());
        music_album.setText(music.getAlbum());

        /*如果服务正在运行，直接return*/
        if (!isServiceRunning("com.example.wechat.service.MusicService.java")){
            Log.d("nmsl","服务正在运行");
            startService(intent);
        }

        conn=new MyServiceConn();
        //通过bind获取服务，第一个intent传递消息，第二个conn建立桥梁，第三个标志bind的创建方式
        this.bindService(intent,conn,BIND_AUTO_CREATE);



        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {//监听进度条变化
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress==seekBar.getMax()){//如果进度条满了就暂停动画
                    animator.pause();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress=seekBar.getProgress();
                musicControl.seekTo(progress);//拖动进度条

            }
        });

        //设置图片转动的动画效果
        animator=ObjectAnimator.ofFloat(music_cd_cover,"rotation",0f,360.0f);
        animator.setDuration(10000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(-1);

    }


    public static Handler handler=new Handler(){
        @Override
        public void handleMessage(@NotNull Message msg) {

            Bundle bundle=msg.getData();
            int duration=bundle.getInt("duration");//获取传递过来的duration值
            int currentPosition=bundle.getInt("currentPosition");//获取传递过来的当前duration值

            sb.setMax(duration);//设置最大进度条
            sb.setProgress(currentPosition);//设置当前进度条

            int minute=duration/1000/60;
            int second=duration/1000%60;
            String strMinute=null;
            String strSecond=null;

            if (minute<10){
                strMinute="0"+minute;
            }else {
                strMinute=minute+"";
            }
            if (second<10){
                strSecond="0"+second;
            }else{
                strSecond=second+"";
            }
            tv_total.setText(strMinute+":"+strSecond);
            minute = currentPosition/1000/60;
            second = currentPosition/1000%60;
            if (minute<10){
                strMinute="0"+minute;
            }else {
                strMinute=minute+"";
            }
            if (second<10){
                strSecond="0"+second;
            }else{
                strSecond=second+"";
            }
            tv_progress.setText(strMinute+":"+strSecond);
        }
    };
    /**
     onServiceConnected()
     系统调用这个来传送在service的onBind()中返回的IBinder．
     OnServiceDisconnected()
     Android系统在同service的连接意外丢失时调用这个．比如当service崩溃了或被强杀了．当客户端解除绑定时，这个方法不会被调用．
     * */
    class MyServiceConn implements ServiceConnection {//服务联系组件

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicControl=(MusicService.MusicControl) service;
            musicControl.play(musicUrl);
            animator.start();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("nmsl","建立失败");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            Log.d("nmsl","建立失效");
        }

        @Override
        public void onNullBinding(ComponentName name) {
            Log.d("nmsl","空建立");
        }


    }

    private void unbind(boolean isUnbind){
        if (!isUnbind){
            musicControl.pausePlay();
            unbindService(conn);
            stopService(intent) ;
        }
    }
    /**
     * 判断服务是否运行
     */
    private boolean isServiceRunning(final String className) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) return true;
        }
        return false;
    }




    @Override
    protected void onDestroy() {
        //unbind(isUnbind);
        Log.d("nmsl","退出");
        super.onDestroy();
    }
}
