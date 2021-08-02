package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.wechat.Activity.ChatActivity;
import com.example.wechat.R;
import com.example.wechat.javaBean.ChatBean;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends BaseAdapter {
    private Context mContext;
    private List<ChatBean> chatBeanList; //聊天数据
    private LayoutInflater layoutInflater;

    public ChatAdapter(List<ChatBean> chatBeanList, Context context) {
        this.mContext=context;
        this.chatBeanList = chatBeanList;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setData(List<ChatBean> sb1){
        this.chatBeanList = sb1;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return chatBeanList.size();
    }
    @Override
    public Object getItem(int position) {
        return chatBeanList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {
        Holder holder = new Holder();
        //判断新消息是文字消息还是图片消息
        if(chatBeanList.get(position).getMessageType() == ChatBean.TEXT){
            //判断当前的信息是发送的信息还是接收到的信息，不同信息加载不同的view
            if (chatBeanList.get(position).getState() == ChatBean.RECEIVE) {
                //加载左边布局，也就是聊天对象对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_left_item,
                        null);
            } else {
                //加载右边布局，也就是用户对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_right_item,
                        null);
            }
            holder.tv_chat_content = (TextView) contentView.findViewById(R.id.tv_chat_content);
            holder.tv_chat_content.setText(chatBeanList.get(position).getMessage());
        }else if(chatBeanList.get(position).getMessageType() == ChatBean.PIC){
            //判断当前的信息是发送的信息还是接收到的信息，不同信息加载不同的view
            Log.d("nmsl","加载图片");
            if (chatBeanList.get(position).getState() == ChatBean.RECEIVE) {
                //加载左边布局，也就是机器人对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_left_pic_item,
                        null);
            } else {
                //加载右边布局，也就是用户对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_right_pic_item,
                        null);
            }

            holder.pic=(ImageView) contentView.findViewById(R.id.tv_chat_content);
            Log.d("nmsl",chatBeanList.get(position).getMessage()+"前路径");
            Glide.with(mContext).load(chatBeanList.get(position).getMessage()).error(R.mipmap.user).into(holder.pic);
        }else if(chatBeanList.get(position).getMessageType() == ChatBean.FILE){
            //判断当前的信息是发送的信息还是接收到的信息，不同信息加载不同的view
            Log.d("nmsl","加载文件");
            if (chatBeanList.get(position).getState() == ChatBean.RECEIVE) {
                //加载左边布局，也就是机器人对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_left_file_item,
                        null);
            } else {
                //加载右边布局，也就是用户对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_right_file_item,
                        null);
            }
            holder.file_name=(TextView) contentView.findViewById(R.id.file_name);
            holder.file_size=(TextView) contentView.findViewById(R.id.file_size);
            holder.relativeLayout=(RelativeLayout) contentView.findViewById(R.id.file_content);
            holder.file_pic=(ImageView) contentView.findViewById(R.id.file_pic);

            File file=new File(chatBeanList.get(position).getMessage());
            holder.file_name.setText(file.getName());
            holder.file_size.setText(file.length()/1024+"KB");

            String type = getMIMEType(file);
            String type_class = "";
            int dotIndex = type.lastIndexOf("/");
            if (dotIndex > 0) {
                //获取文件的后缀名
                type_class = type.substring(0,dotIndex).toLowerCase(Locale.getDefault());
            }

            if(type_class.equals("video")){
                holder.file_pic.setImageResource(R.mipmap.mp4);
            }else if(type_class.equals("text")){
                holder.file_pic.setImageResource(R.mipmap.text);
            }else if(type_class.equals("image")){
                holder.file_pic.setImageResource(R.mipmap.image);
            }else if(type_class.equals("audio")){
                holder.file_pic.setImageResource(R.mipmap.mp3);
            }else if(type.equals("application/vnd.android.package-archive")){
                //apk安装包
                holder.file_pic.setImageResource(R.mipmap.apk);
            }else if(type.equals("application/msword")||
                    type.equals("application/rtf")||
                    type.equals("application/vnd.ms-works")
            ){
                //doc文档
                holder.file_pic.setImageResource(R.mipmap.word);
            }else if(type.equals("application/vnd.ms-powerpoint")){
                //ppt文件
                holder.file_pic.setImageResource(R.mipmap.ppt);
            }else if(type.equals("application/vnd.ms-excel")){
                //excel文件
                holder.file_pic.setImageResource(R.mipmap.excel);
            }else if(type.equals("application/pdf")){
                //excel文件
                holder.file_pic.setImageResource(R.mipmap.pdf);
            }
            else if(type.equals("application/x-compress")||
                    type.equals("application/zip")||
                    type.equals("application/x-rar-compressed")||
                    type.equals("application/x-tar")||
                    type.equals("application/x-gzip")||
                    type.equals("application/x-gtar")
            ){
                //压缩文件
                holder.file_pic.setImageResource(R.mipmap.zip);
            }else if(type_class.equals("application")){
                holder.file_pic.setImageResource(R.mipmap.application);
            }else{
                holder.file_pic.setImageResource(R.mipmap.default_file);
            }


            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Log.d("nmsl",mContext.getPackageName());
                        uri= FileProvider.getUriForFile(mContext,mContext.getPackageName()+".fileprovider",file);
                    } else {
                        uri = Uri.fromFile(file);
                    }
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    //这一行很关键，没有它其他应用就无法获取到这个文件
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    //设置intent的Action属性，加入这个后就只找得到音乐播放器和发送文件等东西了
                    intent.setAction(Intent.ACTION_VIEW);

                    //获取所有的应用
                    List<ResolveInfo> resInfoList = mContext.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                    //遍历所有应用，给应用授权
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        //这个方法的含义是，给packageName应用授予路径为uri的文件的FLAG_GRANT_READ_URI_PERMISSION权限
                        mContext.grantUriPermission(packageName, uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                    //获取文件file的MIME类型
                    //设置intent的data和Type属性。
                    intent.setDataAndType(uri, type);
                    //跳转
                    mContext.startActivity(intent);
                }
            });


        } else{
            Log.d("nmsl","类型错了大哥");
        }
        holder.head=(ImageView) contentView.findViewById(R.id.iv_head);
        Glide.with(mContext).load(chatBeanList.get(position).getHeadDetail()).error(R.mipmap.user).into(holder.head);
        return contentView;

    }
    class Holder {
        public TextView tv_chat_content; // 聊天内容
        public ImageView head,pic,file_pic;
        public TextView file_name,file_size;
        public RelativeLayout relativeLayout;


    }

    private static String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex > 0) {
            //获取文件的后缀名
            String end = fName.substring(dotIndex, fName.length()).toLowerCase(Locale.getDefault());
            //在MIME和文件类型的匹配表中找到对应的MIME类型。
            HashMap<String, String> map = getMimeMap();
            if (!TextUtils.isEmpty(end) && map.keySet().contains(end)) {
                type = map.get(end);
            }
        }
        return type;
    }
    private static HashMap<String, String> getMimeMap() {
        HashMap<String, String> mapSimple = new HashMap<>();
        if (mapSimple.size() == 0) {

            mapSimple.put(".3gp", "video/3gpp");
            mapSimple.put(".asf", "video/x-ms-asf");
            mapSimple.put(".avi", "video/x-msvideo");
            mapSimple.put(".m4u", "video/vnd.mpegurl");
            mapSimple.put(".m4v", "video/x-m4v");
            mapSimple.put(".mov", "video/quicktime");
            mapSimple.put(".mp4", "video/mp4");
            mapSimple.put(".mpe", "video/mpeg");
            mapSimple.put(".mpeg", "video/mpeg");
            mapSimple.put(".mpg", "video/mpeg");
            mapSimple.put(".mpg4", "video/mp4");

            mapSimple.put(".apk", "application/vnd.android.package-archive");
            mapSimple.put(".bin", "application/octet-stream");
            mapSimple.put(".chm", "application/x-chm");
            mapSimple.put(".class", "application/octet-stream");
            mapSimple.put(".doc", "application/msword");
            mapSimple.put(".docx", "application/msword");
            mapSimple.put(".exe", "application/octet-stream");
            mapSimple.put(".gtar", "application/x-gtar");
            mapSimple.put(".gz", "application/x-gzip");
            mapSimple.put(".jar", "application/java-archive");
            mapSimple.put(".js", "application/x-javascript");
            mapSimple.put(".mpc", "application/vnd.mpohun.certificate");
            mapSimple.put(".msg", "application/vnd.ms-outlook");
            mapSimple.put(".pps", "application/vnd.ms-powerpoint");
            mapSimple.put(".ppt", "application/vnd.ms-powerpoint");
            mapSimple.put(".pptx", "application/vnd.ms-powerpoint");
            mapSimple.put(".pdf", "application/pdf");
            mapSimple.put(".xls", "application/vnd.ms-excel");
            mapSimple.put(".xlsx", "application/vnd.ms-excel");
            mapSimple.put(".z", "application/x-compress");
            mapSimple.put(".zip", "application/zip");
            mapSimple.put(".rar", "application/x-rar-compressed");
            mapSimple.put(".tar", "application/x-tar");
            mapSimple.put(".tgz", "application/x-compressed");
            mapSimple.put(".rtf", "application/rtf");
            mapSimple.put(".wps", "application/vnd.ms-works");

            mapSimple.put(".jpeg", "image/jpeg");
            mapSimple.put(".jpg", "image/jpeg");
            mapSimple.put(".gif", "image/gif");
            mapSimple.put(".bmp", "image/bmp");
            mapSimple.put(".png", "image/png");

            mapSimple.put(".c", "text/plain");
            mapSimple.put(".conf", "text/plain");
            mapSimple.put(".cpp", "text/plain");
            mapSimple.put(".h", "text/plain");
            mapSimple.put(".htm", "text/html");
            mapSimple.put(".html", "text/html");
            mapSimple.put(".java", "text/plain");
            mapSimple.put(".log", "text/plain");
            mapSimple.put(".xml", "text/plain");
            mapSimple.put(".prop", "text/plain");
            mapSimple.put(".rc", "text/plain");
            mapSimple.put(".sh", "text/plain");
            mapSimple.put(".txt", "text/plain");

            mapSimple.put(".m3u", "audio/x-mpegurl");
            mapSimple.put(".m4a", "audio/mp4a-latm");
            mapSimple.put(".m4b", "audio/mp4a-latm");
            mapSimple.put(".m4p", "audio/mp4a-latm");
            mapSimple.put(".mp2", "audio/x-mpeg");
            mapSimple.put(".mp3", "audio/x-mpeg");
            mapSimple.put(".mpga", "audio/mpeg");
            mapSimple.put(".ogg", "audio/ogg");
            mapSimple.put(".wav", "audio/x-wav");
            mapSimple.put(".wma", "audio/x-ms-wma");
            mapSimple.put(".wmv", "audio/x-ms-wmv");
            mapSimple.put(".rmvb", "audio/x-pn-realaudio");

            mapSimple.put("", "*/*");
        }
        return mapSimple;
    }
}

