package com.example.wechat.Activity.ui.dashboard;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wechat.Activity.LoginActivity;
import com.example.wechat.Activity.MainActivity;
import com.example.wechat.R;
import com.example.wechat.SQLite.SQLAutoLogin;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.methods.CircleImageViewDrawable;
import com.example.wechat.server.FileManager;
import com.example.wechat.server.WsManager;
import com.example.wechat.service.CommonDialog;

import com.example.wechat.upload.GlideEngine;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MyInfoFragment extends Fragment {

    private MyInfoViewModel dashboardViewModel;
    private ImageView head;
    private Button loginOut;
    private TextView updateUsername;
    private TextView myEmail;
    private RelativeLayout item;

    private LoginBean loginBean=LoginBean.getInstance();
    private List<ContactBean> contactBeanList=ContactBean.getInstance();
    private FileManager fileManager = FileManager.getInstance();
    private WsManager wsManager=WsManager.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(MyInfoViewModel.class);
        View root = inflater.inflate(R.layout.activity_my, container, false);

        //处理头像，使其变圆
        head = root.findViewById(R.id.myHead);


        //百度上的方法已经过时，这个是新的方法
        Glide.with(getActivity()).asBitmap().load(loginBean.getHead()).into(new CustomTarget<Bitmap>() {
             @Override
             public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                 Drawable drawable = new CircleImageViewDrawable(resource);
                 head.setImageDrawable(drawable);
             }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });


        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureSelector.create(getActivity())
                    .openGallery(PictureMimeType.ofImage())
                    .selectionMode(PictureConfig.SINGLE)//单选
                    .isEnableCrop(true)//是否开启裁剪
                    .imageEngine(GlideEngine.createGlideEngine())
                    .isWeChatStyle(true)
                    .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                    .scaleEnabled(true)//裁剪是否可放大缩小图片
                    .isDragFrame(true)//是否可拖动裁剪框(固定)
                    .withAspectRatio(1,1)//裁剪比例
                    .forResult(new OnResultCallbackListener<LocalMedia>() {
                        @Override
                        public void onResult(List<LocalMedia> result) {
                            // 结果回调,这里写上传到服务器的方法
                            //1.获取裁剪图片的路径，通过路径获取图片文件
                            File file=new File(result.get(0).getCutPath());
                            //2.将图片文件上传到服务器
                            fileManager.uploadFile(file,loginBean.getEmail()+".jpeg","","",wsManager);
                            //3.清除缓存，更新头像
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.get(getActivity()).clearDiskCache();
                                }
                            }).start();

                        }

                        @Override
                        public void onCancel() {
                            // 取消
                        }
                    });
            }
        });



        //处理修改用户名
        updateUsername=(TextView)root.findViewById(R.id.username);
        updateUsername.setText("用户名："+loginBean.getMyName());
        item=(RelativeLayout)root.findViewById(R.id.updateUsername);
        item.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                final CommonDialog dialog=new CommonDialog(getActivity());
                dialog.setTitle("更改用户名");
                dialog.setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        if (dialog.getText().equals("")){
                            dialog.dismiss();
                            return;
                        }
                        OkHttpClient okHttpClient = new OkHttpClient();
                        Request request = new Request.Builder().url("http://192.168.123.22:8848/android_back_end_war_exploded/updateUsername.action?" +
                                "email="+loginBean.getEmail()+"&username="+ dialog.getText()).build();//在这里将用户发送的信息通过url发送给机器人
                        Call call = okHttpClient.newCall(request);
                        // 开启异步线程访问网络
                        call.enqueue(new Callback() {
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                loginBean.setMyName(dialog.getText());
                            }
                            @Override
                            public void onFailure(Call call, IOException e) {

                            }
                        });
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }

                });
                dialog.show();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
        });
        //加载正确的邮箱
        myEmail=(TextView)root.findViewById(R.id.userEmail);
        myEmail.setText("邮箱："+loginBean.getEmail());

        //处理登录退出
        loginOut=(Button)root.findViewById(R.id.loginOut);
        loginOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                SQLAutoLogin helper;
                helper = new SQLAutoLogin(getActivity());
                SQLiteDatabase database = helper.getReadableDatabase();
                helper.onCreate(database);
                helper.delete();
                Intent intent = new Intent();
                intent.setClass(getActivity(), LoginActivity.class);
                startActivity(intent);

                contactBeanList.clear();
                getActivity().finish();
            }
        });
        return root;
    }

}