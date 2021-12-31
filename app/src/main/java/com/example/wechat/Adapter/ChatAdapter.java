package com.example.wechat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
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
import com.example.wechat.Activity.PlayMusicActivity;
import com.example.wechat.Activity.UserInfoActivity;
import com.example.wechat.R;
import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.GroupBean;
import com.example.wechat.javaBean.GroupMember;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.upload.GlideEngine;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.List;
import java.util.Locale;

import static com.example.wechat.Utils.MimeUtils.getMIMEType;

public class ChatAdapter extends BaseAdapter {
    private Context mContext;
    private List<ChatBean> chatBeanList; //聊天数据
    private List<LocalMedia> localMediaList;
    private LayoutInflater layoutInflater;

    private ContactBean contact;
    private GroupBean group;

    private LoginBean my;


    public ChatAdapter(List<ChatBean> chatBeanList,List<LocalMedia> localMediaList, Context context,ContactBean contact) {
        this.mContext=context;
        this.chatBeanList = chatBeanList;
        layoutInflater = LayoutInflater.from(context);
        this.localMediaList=localMediaList;
        this.contact=contact;
        my=LoginBean.getInstance();
    }
    public ChatAdapter(List<ChatBean> chatBeanList,List<LocalMedia> localMediaList, Context context, GroupBean group) {
        this.mContext=context;
        this.chatBeanList = chatBeanList;
        layoutInflater = LayoutInflater.from(context);
        this.localMediaList=localMediaList;
        this.group=group;
        my=LoginBean.getInstance();
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
        ChatBean chatBean=chatBeanList.get(position);
        //判断新消息是文字消息还是图片消息
        if(chatBean.getMessageType() == ChatBean.TEXT){
            //判断当前的信息是发送的信息还是接收到的信息，不同信息加载不同的view
            if (chatBean.getState() == ChatBean.RECEIVE) {
                //加载左边布局，也就是聊天对象对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_left_item,
                        null);
            } else {
                //加载右边布局，也就是用户对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_right_item,
                        null);
            }
            holder.tv_chat_content = (TextView) contentView.findViewById(R.id.tv_chat_content);
            holder.tv_chat_content.setText(chatBean.getMessage());

        }else if(chatBean.getMessageType() == ChatBean.PIC){
            //判断当前的信息是发送的信息还是接收到的信息，不同信息加载不同的view
            //Log.d("nmsl","加载图片");
            if (chatBean.getState() == ChatBean.RECEIVE) {
                //加载左边布局，也就是机器人对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_left_pic_item,
                        null);
            } else {
                //加载右边布局，也就是用户对应的布局信息
                contentView = layoutInflater.inflate(R.layout.chatting_right_pic_item,
                        null);
            }

            holder.pic=(ImageView) contentView.findViewById(R.id.tv_chat_content);
            //Log.d("nmsl",chatBean.getMessage()+"前路径");
            Glide.with(mContext).load(chatBean.getMessage()).error(R.mipmap.user).into(holder.pic);
            holder.pic.setOnClickListener((v) -> {
                //Log.d("nmsl","点击图片！");

                if (localMediaList.size() > 0) {
                    PictureSelector.create((Activity) mContext)
                            .themeStyle(R.style.picture_default_style) // xml设置主题
                            .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// 设置相册Activity方向，不设置默认使用系统
                            .isNotPreviewDownload(true)// 预览图片长按是否可以下载
                            .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                            .openExternalPreview(chatBean.getMedia_position(), localMediaList);
                }
            });


        }else if(chatBean.getMessageType() == ChatBean.FILE){
            //判断当前的信息是发送的信息还是接收到的信息，不同信息加载不同的view
            Log.d("nmsl","加载文件");
            if (chatBean.getState() == ChatBean.RECEIVE) {
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

            File file=new File(chatBean.getMessage());

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


            //给文件类型信息创建点击事件
            holder.relativeLayout.setOnClickListener(view -> {
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //Log.d("nmsl",mContext.getPackageName());
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
            });

            if(type_class.equals("audio")){//如果是音乐或者视频类型，就添加长按播放事件
                holder.relativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent intent=new Intent();
                        intent.putExtra("musicUrl",file.getPath());
                        intent.setClass(mContext, PlayMusicActivity.class);
                        mContext.startActivity(intent);
                        return false;
                    }
                });
            }

        } else{
            Log.d("nmsl","类型错了大哥");
        }


        holder.head=(ImageView) contentView.findViewById(R.id.iv_head);
        if (chatBean.getState() == ChatBean.RECEIVE) {
            Glide.with(mContext).load(chatBean.getHead()).error(R.mipmap.user).into(holder.head);
        } else {
            //加载右边布局，也就是用户对应的布局信息
            Glide.with(mContext).load(my.getHead()).error(R.mipmap.user).into(holder.head);
        }


        if(chatBean.getState() == ChatBean.RECEIVE){
            holder.head.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, UserInfoActivity.class);
                    intent.putExtra("email",chatBean.getEmail());
                    mContext.startActivity(intent);
                }
            });
        }else{
            holder.head.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext, UserInfoActivity.class);
                    intent.putExtra("email", LoginBean.getInstance().getEmail());
                    mContext.startActivity(intent);
                }
            });
        }


        return contentView;
    }
    class Holder {
        public TextView tv_chat_content; // 聊天内容
        public ImageView head,pic,file_pic;
        public TextView file_name,file_size;
        public RelativeLayout relativeLayout;

    }



}

