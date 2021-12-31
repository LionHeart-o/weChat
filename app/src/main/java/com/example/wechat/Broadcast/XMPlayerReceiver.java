package com.example.wechat.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.example.wechat.Activity.PlayMusicActivity;
import com.example.wechat.R;
import com.example.wechat.service.MusicService;

public class XMPlayerReceiver extends BroadcastReceiver {
    public static final String PLAY_PLAY_PAUSE = "play_play_pause";
    public static final String PLAY_CLOSE = "play_close";
    private NotificationManagerCompat notificationManager = null;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(notificationManager==null) notificationManager=NotificationManagerCompat.from(context.getApplicationContext());;
        if (intent.getAction().equals(PLAY_CLOSE)){
            Log.e("XMPlayerReceiver", "通知栏点击了关闭通知栏");
            notificationManager.cancel(MusicService.notifyId);
        }

        if (intent.getAction().equals(PLAY_PLAY_PAUSE)) {
            if(MusicService.player.isPlaying()){
                MusicService.player.pause();
                Glide.with(context.getApplicationContext()).load(R.mipmap.pause).into(PlayMusicActivity.music_play_btn);
                MusicService.notification.contentView.setImageViewResource(R.id.music_playOrPause,R.mipmap.pause);
            }
            else{
                MusicService.player.start();
                Glide.with(context.getApplicationContext()).load(R.mipmap.play).into(PlayMusicActivity.music_play_btn);
                MusicService.notification.contentView.setImageViewResource(R.id.music_playOrPause,R.mipmap.play);
            }
            notificationManager.notify(MusicService.notifyId,MusicService.notification);
        }
    }
}