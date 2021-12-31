package com.example.wechat.Utils;

import android.util.Log;

import com.example.wechat.javaBean.MusicBean;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Frame;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.framebody.FrameBodyAPIC;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * MP3工具类
 * @author liudaxi
 *
 */
public class MP3Utils {


    /**
     * 解析MP3获取歌名，歌手，专辑
     * @param music 音乐Bean
     * @return
     */
    public static MusicBean getSongInfo(MusicBean music) {
        try {
            File file=new File(music.getMusicUrl());
            MP3File mp3File = (MP3File) AudioFileIO.read(file);
            AbstractID3v2Tag tag = mp3File.getID3v2Tag();
            String songName = "";
            String singer = "";
            String author = "";
            //MP3AudioHeader audioHeader = (MP3AudioHeader) mp3File.getAudioHeader();
            if(mp3File.getID3v2Tag() != null && mp3File.getID3v2Tag().frameMap != null){
                if(tag.frameMap.get("TIT2") != null){
                    songName = tag.frameMap.get("TIT2").toString();//歌名
                    if(!StringUtils.isNotBlank(songName)){
                        songName = "未知歌曲";
                    }
                    music.setTitle(reg(songName));
                }
                if(tag.frameMap.get("TPE1") != null){
                    singer = mp3File.getID3v2Tag().frameMap.get("TPE1").toString();//歌手
                    if(!StringUtils.isNotBlank(singer)){
                        singer = "未知歌手";
                    }
                    music.setSinger(reg(singer));
                }
                if(tag.frameMap.get("TALB") != null){
                    author = mp3File.getID3v2Tag().frameMap.get("TALB").toString();//专辑
                    music.setAlbum(reg(author));
                }
            }
            //int duration = audioHeader.getTrackLength();//时长
        } catch (Exception e) {
            Log.e("nmsl","s:读取MP3信息失败！");
            e.printStackTrace();

        }
        return music;
    }


    //去除不必要的字符串
    public static String reg(String input) {
        return input.substring(input.indexOf('"') + 1, input.lastIndexOf('"'));
    }

    /**
     * 获取MP3封面图片
     * @return
     * @throws InvalidAudioFrameException
     * @throws ReadOnlyFileException
     * @throws TagException
     * @throws IOException
     * @throws CannotReadException
     */
    public static byte[] getMP3Image(MusicBean music) {
        byte[] imageData = null;
        MP3File mp3File;
        try {
            mp3File = (MP3File) AudioFileIO.read(new File(music.getMusicUrl()));
            AbstractID3v2Tag tag = mp3File.getID3v2Tag();
            AbstractID3v2Frame frame = (AbstractID3v2Frame) tag.getFrame("APIC");
            FrameBodyAPIC body = (FrameBodyAPIC) frame.getBody();
            imageData = body.getImageData();
        } catch (Exception e) {Log.e("nmsl",":读取MP3封面失败！");
            return null;
        }

        return imageData;
    }

    /**
     * 获取mp3图片并将其保存至指定路径下
     * 如果没有读取到图片 ，则返回"/static/music/images/defulate.jpg"
     * @param music mp3文件对象
     * @param mp3ImageSavePath mp3图片保存位置（默认mp3ImageSavePath +"\" mp3File文件名 +".jpg" ）
     * @param cover 是否覆盖已有图片
     * @return 生成图片路径
     */
    public static String saveMP3Image(MusicBean music, String mp3ImageSavePath, boolean cover) {
        //生成mp3图片路径
        File file = new File(music.getMusicUrl());
        String mp3FileLabel = file.getName();
        String mp3ImageFullPath = mp3ImageSavePath + ("\\" + mp3FileLabel + ".jpg");

        //若为非覆盖模式，图片存在则直接返回（不再创建）
        if( !cover ) {
            File tempFile = new File(mp3ImageFullPath) ;
            if(tempFile.exists()) {
                return mp3ImageFullPath;
            }
        }

        //生成mp3存放目录
        File saveDirectory = new File(mp3ImageSavePath);
        saveDirectory.mkdirs();

        //获取mp3图片
        byte imageData[];
        imageData = getMP3Image(music);
        if(imageData == null){
            Log.e("nmsl","读取MP3封面失败！");
            //获取失败，返回默认图片路径
            return "";
        }

        //若图片不存在，则直接返回null
        if (null == imageData || imageData.length == 0) {
            return null;
        }
        //保存mp3图片文件
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mp3ImageFullPath);
            fos.write(imageData);
            fos.close();
        } catch(Exception e) {
            Log.e("nmsl","保存读取mp3图片文件失败！");
        }
        return "";
    }
}