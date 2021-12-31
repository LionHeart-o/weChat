package com.example.wechat.Activity.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import com.example.wechat.Activity.EditInfoActivity;
import com.example.wechat.Activity.LoginActivity;
import com.example.wechat.Activity.MainActivity;
import com.example.wechat.R;
import com.example.wechat.Utils.MD5Utils;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.customView.CircleImageViewDrawable;
import com.example.wechat.Utils.FileManager;
import com.example.wechat.Utils.WsManager;
import com.example.wechat.customView.CommonDialog;

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

import static android.content.Context.MODE_PRIVATE;
import static com.example.wechat.application.MyApplication.FILE_SAVE_URL;


public class MyInfoFragment extends Fragment {

    private final String TAG="MyInfoFragment";
    private final String LOCAL_BG_KEY = "local_bg";

    private MyInfoViewModel dashboardViewModel;
    private ImageView head;
    private ImageView my_info_bg;

    private Button loginOut;
    private TextView updateUsername;
    private RelativeLayout editInfo;
    private RelativeLayout modifyChatBg;


    private LoginBean loginBean=LoginBean.getInstance();
    private List<ContactBean> contactBeanList=LoginBean.getInstance().getContacts();
    private FileManager fileManager = FileManager.getInstance();
    private WsManager wsManager=WsManager.getInstance();
    private MyApplication application;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(MyInfoViewModel.class);
        View root = inflater.inflate(R.layout.activity_my, container, false);

        //处理头像，使其变圆
        head = root.findViewById(R.id.myHead);
        my_info_bg=root.findViewById(R.id.my_info_bg);

        modifyChatBg=root.findViewById(R.id.modify_chat_bg);

        editInfo=root.findViewById(R.id.edit_info);
        editInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity().getApplicationContext(), EditInfoActivity.class);
                startActivity(intent);
            }
        });


        my_info_bg.setAlpha((float) 0.5);
        application= (MyApplication)getActivity().getApplication();

        Glide.with(getActivity())
                .load(LoginBean.getInstance().getCover())
                .error(R.mipmap.default_bg)
                .centerCrop()
                .into(my_info_bg);


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


        head.setOnClickListener(view -> PictureSelector.create(getActivity())
            .openGallery(PictureMimeType.ofImage())
            .selectionMode(PictureConfig.SINGLE)//单选
            .isEnableCrop(true)//是否开启裁剪
            .imageEngine(GlideEngine.createGlideEngine())
            .isWeChatStyle(true)
            .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
            .scaleEnabled(true)//裁剪是否可放大缩小图片
            .isDragFrame(true)//是否可拖动裁剪框(固定)
            .rotateEnabled(false) // 裁剪是否可旋转图片
            .withAspectRatio(1,1)//裁剪比例
            .forResult(new OnResultCallbackListener<LocalMedia>() {
                @Override
                public void onResult(List<LocalMedia> result) {
                    // 结果回调,这里写上传到服务器的方法
                    //1.获取裁剪图片的路径，通过路径获取图片文件
                    File file=new File(result.get(0).getCutPath());
                    //2.将图片文件上传到服务器
                    fileManager.uploadFile(file, MD5Utils.stringToMD5(file.getName()),null,"",-1,"",-1,null);
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder().url(MyApplication.BACK_URL+"editUserInfo.action?" +
                            "email="+loginBean.getEmail()+"&password="+loginBean.getPassword()+"&head="+FILE_SAVE_URL+MD5Utils.stringToMD5(file.getName())).build();//在这里将用户发送的信息通过url发送给机器人
                    Call call = okHttpClient.newCall(request);
                    // 开启异步线程访问网络
                    call.enqueue(new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) {
                            LoginBean.getInstance().setHead("FILE_SAVE_URL"+MD5Utils.stringToMD5(file.getName()));
                        }
                        @Override
                        public void onFailure(Call call, IOException e) {

                        }
                    });

                }

                @Override
                public void onCancel() {
                    // 取消
                }
            }));



        //处理修改用户名
        updateUsername=(TextView)root.findViewById(R.id.username);
        updateUsername.setText(loginBean.getUsername());
        updateUsername.setOnClickListener(new View.OnClickListener(){
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
                        Request request = new Request.Builder().url(MyApplication.BACK_URL+"editUserInfo.action?" +
                                "email="+loginBean.getEmail()+"&username="+ dialog.getText()).build();//在这里将用户发送的信息通过url发送给机器人
                        Call call = okHttpClient.newCall(request);
                        // 开启异步线程访问网络
                        call.enqueue(new Callback() {
                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                loginBean.setUsername(dialog.getText());
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


        modifyChatBg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DisplayMetrics metric = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);

                PictureSelector.create(getActivity())
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//单选
                        .isEnableCrop(true)//是否开启裁剪
                        .imageEngine(GlideEngine.createGlideEngine())
                        .rotateEnabled(false) // 裁剪是否可旋转图片
                        .isGif(true)
                        .withAspectRatio(metric.widthPixels,metric.heightPixels)//裁剪比例
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(List<LocalMedia> result) {
                                SharedPreferences bgInfo=getActivity().getSharedPreferences(LOCAL_BG_KEY,MODE_PRIVATE);
                                SharedPreferences.Editor editor =  bgInfo.edit();//获取Editor
                                editor.putString(LOCAL_BG_KEY,result.get(0).getCutPath());
                                editor.commit();//提交修改
                            }

                            @Override
                            public void onCancel() {
                                // 取消
                            }
                        });
            }
        });


        //处理登录退出
        loginOut=(Button)root.findViewById(R.id.loginOut);
        loginOut.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                SharedPreferences sp;
                SharedPreferences.Editor editor;
                sp=getActivity().getSharedPreferences("autoLogin", Context.MODE_PRIVATE);
                editor=sp.edit();
                editor.clear().commit();

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