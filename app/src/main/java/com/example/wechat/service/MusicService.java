package com.example.wechat.service;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.wechat.Activity.PlayMusicActivity;
import com.example.wechat.Broadcast.XMPlayerReceiver;
import com.example.wechat.R;
import com.example.wechat.Utils.MP3Utils;
import com.example.wechat.javaBean.MusicBean;

import java.util.Timer;
import java.util.TimerTask;

import static android.app.Notification.CATEGORY_MESSAGE;
import static android.app.Notification.DEFAULT_ALL;
import static android.app.Notification.FLAG_ONGOING_EVENT;
import static androidx.core.app.NotificationCompat.PRIORITY_MAX;


public class MusicService extends Service {
    public static volatile MediaPlayer player=new MediaPlayer();
    public static Notification notification;

    private volatile Timer timer;
    public final static int notifyId=6324;
    private volatile static MusicBean music;
    private NotificationManagerCompat notificationManager;


    private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
    private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";
    public MusicService(){}

    @Override
    public IBinder onBind(Intent intent) {//重写onBind方法
        return new MusicControl();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        music=new MusicBean();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return super.onStartCommand(intent, flags, startId);

    }

    public void addTimer(){
        try{
            timer=new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(player==null) return ;
                    Message msg= PlayMusicActivity.handler.obtainMessage();
                    Bundle bundle = new Bundle();
                    int duration = player.getDuration();
                    int currentPosition=player.getCurrentPosition();
                    bundle.putInt("duration",duration);
                    bundle.putInt("currentPosition",currentPosition);
                    msg.setData(bundle);
                    PlayMusicActivity.handler.sendMessage(msg);
                }
            },5,100);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public class MusicControl extends Binder{//进程通信机制
        public void play(String musicUrl){
            try {
                if(!musicUrl.equals(music.getMusicUrl())){
                    if(timer==null) timer=new Timer();
                    else {
                        timer.cancel();
                        player.stop();
                        player.release();
                        player=null;
                        player=new MediaPlayer();
                    }

                }else{
                    return;
                }
                music.setMusicUrl(musicUrl);
                Uri uri=Uri.parse(musicUrl);

                player.setDataSource(getApplicationContext(),uri);
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        generateMusicNotification(initNotifyView(R.mipmap.play));
                        addTimer();
                        player.start();
                    }
                });

                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        player.release();
                    }
                });

                player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        return false;
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        public void pausePlay(){
            player.pause();
        }
        public void continuePlay(){
            player.start();
        }
        public void seekTo(int progress){
            player.seekTo(progress);
        }
    }

    private RemoteViews initNotifyView(int iconId) {

        music=MP3Utils.getSongInfo(music);

        String packageName = getApplicationContext().getPackageName();
        RemoteViews remoteView = new RemoteViews(packageName, R.layout.player_item);
        remoteView.setTextViewText(R.id.music_title, music.getTitle());
        remoteView.setTextViewText(R.id.music_detail, music.getSinger());
        remoteView.setImageViewResource(R.id.btn_play,iconId);


        Intent startPlay = new Intent(getApplicationContext(),XMPlayerReceiver.class);//播放
        startPlay.setAction(XMPlayerReceiver.PLAY_PLAY_PAUSE);
        PendingIntent intent_play = PendingIntent.getBroadcast(getApplicationContext(), 4, startPlay,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.music_playOrPause, intent_play);


        Intent closeNotification = new Intent(getApplicationContext(),XMPlayerReceiver.class);//播放
        closeNotification.setAction(XMPlayerReceiver.PLAY_CLOSE);
        PendingIntent intent_closeNotification = PendingIntent.getBroadcast(getApplicationContext(), 4,closeNotification,
                PendingIntent.FLAG_UPDATE_CURRENT);

        remoteView.setOnClickPendingIntent(R.id.close_notification, intent_closeNotification);

        return remoteView;
    }

    private void generateMusicNotification(RemoteViews remoteViews){
        String channelId = "music_notification";
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        //点击通知栏跳转的activity
        Intent intent = new Intent(getApplicationContext(), PlayMusicActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher)//这玩意在通知栏上显示一个logo
                .setCategory(CATEGORY_MESSAGE)
                .setDefaults(DEFAULT_ALL)
                .setOngoing(true)
                .setAutoCancel(false)//点击不让消失
                .setSound(null)//关了通知默认提示音
                .setPriority(PRIORITY_MAX)//咱们通知很重要
                .setVibrate(null)//关了车震
                .setContentIntent(pendingIntent)//整个点击跳转activity安排上
                .setOnlyAlertOnce(false);

        builder.setContent(remoteViews);//把自定义view放上
        builder.setCustomBigContentView(remoteViews);//把自定义view放上


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//适配一下高版本
            NotificationChannel channel = new NotificationChannel(channelId, "listen", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        notification = builder.build();

        notification.flags |= FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;//不让手动清除 通知栏常驻
        notification.sound = null;//关了通知默认提示音
        notificationManager.notify(notifyId, notification);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(player==null) return;
        if (player.isPlaying()) player.stop();
        player.release();
        player=null;
    }

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    generateMusicNotification(initNotifyView(R.mipmap.play));
                    break;
                case 2:
                    generateMusicNotification(initNotifyView(R.mipmap.pause));
                    break;
                default:
                    break;
            }
        }
    };

    /*public class PlayerReceiver extends BroadcastReceiver {
        public static final String PLAY_PLAY_PAUSE = "play_play_pause";
        public static final String PLAY_CLOSE = "play_close";
        private NotificationManagerCompat notificationManager = null;
        private Message message=new Message();
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("PlayerReceiver", "点击");
            if(notificationManager==null) notificationManager=NotificationManagerCompat.from(context.getApplicationContext());;
            if (intent.getAction().equals(PLAY_CLOSE)){//PLAY_NEXT
                Log.e("PlayerReceiver", "通知栏点击了关闭通知栏");
                notificationManager.cancel(MusicService.notifyId);
            }

            if (intent.getAction().equals(PLAY_PLAY_PAUSE)) {
                if(MusicService.player.isPlaying()){
                    MusicService.player.pause();
                    message.what=1;
                    handler.sendMessage(message);
                    Glide.with(context.getApplicationContext()).load(R.mipmap.pause).into(PlayMusicActivity.music_play_btn);
                }
                else{
                    MusicService.player.start();
                    message.what=2;
                    handler.sendMessage(message);
                    Glide.with(context.getApplicationContext()).load(R.mipmap.play).into(PlayMusicActivity.music_play_btn);
                }
            }
        }
    }*/



}
