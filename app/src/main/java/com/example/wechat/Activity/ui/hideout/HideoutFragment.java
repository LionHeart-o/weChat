package com.example.wechat.Activity.ui.hideout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wechat.Activity.DynamicsActivity;
import com.example.wechat.Utils.MD5Utils;
import com.example.wechat.Utils.FileManager;
import com.example.wechat.customView.ContactListView;
import com.example.wechat.Adapter.HideoutAdapter;
import com.example.wechat.R;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.CommentBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.javaBean.ThoughtBean;
import com.example.wechat.customView.RefreshableView;
import com.example.wechat.upload.GlideEngine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.wechat.application.MyApplication.FILE_SAVE_URL;


public class HideoutFragment extends Fragment {

    public static final int UPDATE=1;
    public static final int CHANGE_HEIGHT=2;
    public static final int UPDATE_COVER=3;

    private final String TAG="HideoutFragment";

    private HideoutViewModel hideoutViewModel;

    private ImageView send_dynamic_button;
    private HideoutAdapter adapter;
    private ContactListView thought_list;
    private ImageView cover;
    private RelativeLayout relativeLayout;


    private List<ThoughtBean> thoughts=ThoughtBean.getInstance();

    private LoginBean loginBean=LoginBean.getInstance();
    private List<ContactBean> contactBeanList=loginBean.getContacts();
    private Map<String,Integer> contactIndex;
    private FileManager fileManager=FileManager.getInstance();

    private MyApplication application;

    private RefreshableView refreshableView;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideoutViewModel =
                new ViewModelProvider(this).get(HideoutViewModel.class);
        View root = inflater.inflate(R.layout.activity_hideout, container, false);
        contactIndex=loginBean.getContactIndex();
        relativeLayout=(RelativeLayout)root.findViewById(R.id.title_bar);

        cover=(ImageView) root.findViewById(R.id.bg);
        Glide.with(getContext())
                .load(LoginBean.getInstance().getCover())
                .centerCrop()
                .placeholder(R.color.app_color_f6)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.mipmap.default_bg)
                .into(cover);

        cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PictureSelector.create(getActivity())
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//单选
                        .isEnableCrop(true)//是否开启裁剪
                        .imageEngine(GlideEngine.createGlideEngine())
                        .isWeChatStyle(true)
                        .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        .scaleEnabled(true)//裁剪是否可放大缩小图片
                        .isDragFrame(true)//是否可拖动裁剪框(固定)
                        .withAspectRatio(relativeLayout.getWidth(),relativeLayout.getHeight())//裁剪比例
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(List<LocalMedia> result) {
                                // 结果回调,这里写上传到服务器的方法
                                //1.获取裁剪图片的路径，通过路径获取图片文件
                                File file=new File(result.get(0).getCutPath());
                                Date date=new Date();
                                SimpleDateFormat sdk=new SimpleDateFormat("yyyy-MM-dd");
                                String time=sdk.format(date);
                                //2.将图片文件上传到服务器
                                String fileName=file.getName();
                                int lastIndexOf = fileName.lastIndexOf(".");
                                fileManager.uploadFile(file,time+ MD5Utils.stringToMD5(file.getName())+fileName.substring(lastIndexOf),handler,"",-1,"",-1,null);
                                modifyCover(FILE_SAVE_URL+time+ MD5Utils.stringToMD5(file.getName())+fileName.substring(lastIndexOf));
                            }

                            @Override
                            public void onCancel() {
                                // 取消
                            }
                        });
                return false;
            }
        });


        send_dynamic_button=(ImageView)root.findViewById(R.id.send_dynamics);
        send_dynamic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(root.getContext(), DynamicsActivity.class));
            }
        });
        application = (MyApplication)getActivity().getApplication();
        thought_list=(ContactListView) root.findViewById(R.id.thought_list);
        adapter=new HideoutAdapter(getActivity(),application,getActivity(),thought_list);
        thought_list.setAdapter(adapter);
        adapter.setData(thoughts);

        refreshableView = (RefreshableView) root.findViewById(R.id.refreshable_view);
        refreshableView.setOnRefreshListener(new RefreshableView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    getThoughts();
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                refreshableView.finishRefreshing();
            }
        }, 0);
        if(thoughts.size()==0) getThoughts();


        return root;
    }

    private void getThoughts(){
        thoughts.clear();

        StringBuffer contactEmails=new StringBuffer();
        for(int i=0;i<contactBeanList.size();i++){
            contactEmails.append("&contactEmail="+contactBeanList.get(i).getEmail());
        }
        //Log.i(TAG,"获取说说的联系人邮箱："+contactEmails);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"getThoughts.action?" +
                "email="+loginBean.getEmail()+"&contactEmail="+loginBean.getEmail()+contactEmails.toString()).build();//将用户的账号密码传输
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.i(TAG,res);
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                thoughts=gson.fromJson(res,new TypeToken<List<ThoughtBean>>(){}.getType());

                for (int i=0;i<thoughts.size();i++){
                    int position=0;

                    if(contactIndex.containsKey(thoughts.get(i).getSend_email())){
                        position=contactIndex.get(thoughts.get(i).getSend_email());
                        thoughts.get(i).setName(contactBeanList.get(position).getUsername());
                        thoughts.get(i).setHead(contactBeanList.get(position).getHead());
                    }else{
                        thoughts.get(i).setName(loginBean.getUsername());
                        thoughts.get(i).setHead(loginBean.getHead());
                    }
                    List<CommentBean> commentBeans=thoughts.get(i).getComments();
                    for(int j=0;j<commentBeans.size();j++){
                        CommentBean temp=commentBeans.get(j);
                        int index;
                        if(contactIndex.containsKey(temp.getReply_email())){
                            index=contactIndex.get(thoughts.get(i).getSend_email());
                            temp.setReply_name(contactBeanList.get(index).getUsername());
                        }else if(temp.getReply_email().equals(loginBean.getEmail())){
                            temp.setReply_name(loginBean.getUsername());
                        }
                    }

                }

                adapter.setData(thoughts);
                ThoughtBean.setInstance(thoughts);

                Message msg = new Message();
                msg.what = UPDATE;
                handler.sendMessage(msg);

            }
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }

    private void modifyCover(String coverUrl){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"modifyCover.action?" +
                "email="+LoginBean.getInstance().getEmail()+"&coverUrl="+ coverUrl).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG,"封面修改成功:"+response.body());
                LoginBean.getInstance().setCover(coverUrl);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"封面修改失败:"+e.getMessage());
            }
        });

    }


    private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE:
                    adapter.notifyDataSetChanged();
                    break;
                /*case CHANGE_HEIGHT:
                    ViewGroup.LayoutParams params = thought_list.getLayoutParams();
                    params.height=initHeight;
                    thought_list.setLayoutParams(params);
                    break;*/
                case UPDATE_COVER:
                    Glide.with(getContext())
                            .load(LoginBean.getInstance().getCover())
                            .centerCrop()
                            .placeholder(R.color.app_color_f6)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.mipmap.default_bg)
                            .into(cover);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}