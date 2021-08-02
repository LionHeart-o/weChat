package com.example.wechat.Activity.ui.hideout;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
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
import com.example.wechat.Activity.ChatActivity;
import com.example.wechat.Activity.DynamicsActivity;
import com.example.wechat.Activity.LoginActivity;
import com.example.wechat.Activity.MainActivity;
import com.example.wechat.Activity.ui.dashboard.MyInfoViewModel;
import com.example.wechat.Adapter.ContactListView;
import com.example.wechat.Adapter.HideoutAdapter;
import com.example.wechat.R;
import com.example.wechat.SQLite.SQLAutoLogin;
import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.CommentBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.javaBean.NotificationBean;
import com.example.wechat.javaBean.ThoughtBean;
import com.example.wechat.javaBean.getStaticBean.getNotificationBean;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
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


public class HideoutFragment extends Fragment {

    final int UPDATE=1;
    final int CHANGE_HEIGHT=2;

    private HideoutViewModel hideoutViewModel;

    private ImageView send_dynamic_button;
    private HideoutAdapter adapter;
    private ContactListView thought_list;

    private int height=0;
    private List<ThoughtBean> thoughts=ThoughtBean.getInstance();
    private List<ContactBean> contactBeanList=ContactBean.getInstance();
    private LoginBean loginBean=LoginBean.getInstance();
    private Map<String,Integer> contactIndex=ContactBean.getIndexInstance();



    private MyApplication application;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        hideoutViewModel =
                new ViewModelProvider(this).get(HideoutViewModel.class);
        View root = inflater.inflate(R.layout.activity_hideout, container, false);

        send_dynamic_button=(ImageView)root.findViewById(R.id.send_dynamics);

        send_dynamic_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(root.getContext(), DynamicsActivity.class));
            }
        });
        application = (MyApplication)getActivity().getApplication();
        thought_list=(ContactListView) root.findViewById(R.id.thought_list);

        adapter=new HideoutAdapter(getActivity(),application);
        thought_list.setAdapter(adapter);
        adapter.setData(thoughts);
        getThoughts();

        return root;
    }

    private void getThoughts(){
        String contactEmails="";
        for(int i=0;i<contactBeanList.size();i++){
            contactEmails=contactEmails+"&contactEmail="+contactBeanList.get(i).getContact_email();
        }
        Log.i("nmsl",contactEmails);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(application.getBack_end_url()+"getThoughts.action?" +
                "email="+loginBean.getEmail()+"&contactEmail="+loginBean.getEmail()+contactEmails).build();//将用户的账号密码传输
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();

                try {
                    JSONArray jsonArray=new JSONArray(res);
                    JSONObject jsonTemp;
                    for(int i=0;i<jsonArray.length();i++){


                        jsonTemp=jsonArray.getJSONObject(i);
                        //Log.d("nmsl",jsonTemp.toString());

                        ThoughtBean thoughtBean=new ThoughtBean();
                        thoughtBean.setId(jsonTemp.getString("id"));
                        thoughtBean.setMessage(jsonTemp.getString("message"));
                        thoughtBean.setSend_email(jsonTemp.getString("send_email"));

                        int index;
                        if(contactIndex.containsKey(thoughtBean.getSend_email())) {
                            index=contactIndex.get(thoughtBean.getSend_email());
                            thoughtBean.setHead(contactBeanList.get(index).getContact_head());
                            thoughtBean.setName(contactBeanList.get(index).getContact_name());
                        }
                        else if(thoughtBean.getSend_email().equals(loginBean.getEmail())){
                            thoughtBean.setHead(loginBean.getHead());
                            thoughtBean.setName(loginBean.getMyName());
                        }


                        Date date = new Date(Long.valueOf(jsonTemp.getString("createTime")));
                        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        thoughtBean.setTimestamp(sd.format(date));

                        //计算高度
                        JSONArray arrayTemp=new JSONArray(jsonTemp.getString("thoughts_pics"));
                        List<String> urls=new ArrayList<>();
                        height=height+350+(((arrayTemp.length()-1)/3)+1)*450;
                        for(int j=0;j<arrayTemp.length();j++){
                            urls.add(arrayTemp.getJSONObject(j).getString("pic_url"));
                        }
                        thoughtBean.setPic(urls);


                        arrayTemp=new JSONArray(jsonTemp.getString("comments"));
                        List<CommentBean> commentBeans=new ArrayList<>();
                        CommentBean bean;
                        Map<String,String> name=new HashMap<>();

                        for(int j=0;j<arrayTemp.length();j++){
                            bean=new CommentBean();

                            bean.setComment_email(arrayTemp.getJSONObject(j).getString("send_email"));
                            bean.setComment_name(arrayTemp.getJSONObject(j).getString("comment_name"));
                            Log.d("nmsl", arrayTemp.getJSONObject(j).getString("send_email")+"名字："+arrayTemp.getJSONObject(j).getString("comment_name"));

                            name.put(arrayTemp.getJSONObject(j).getString("send_email"),arrayTemp.getJSONObject(j).getString("comment_name"));

                            bean.setComment_text(arrayTemp.getJSONObject(j).getString("message"));
                            bean.setReply_email(arrayTemp.getJSONObject(j).getString("reply_email"));
                            bean.setReply_name(name.get(arrayTemp.getJSONObject(j).getString("reply_email")));
                            Log.d("nmsl", arrayTemp.getJSONObject(j).getString("reply_email")+"回复人名字："+name.get(arrayTemp.getJSONObject(j).getString("reply_email")));

                            commentBeans.add(bean);
                        }



                        thoughtBean.setCommentBeanList(commentBeans);

                        thoughts.add(thoughtBean);

                    }

                    Message msg = new Message();
                    msg.what = UPDATE;
                    handler.sendMessage(msg);

                    msg = new Message();
                    msg.what = CHANGE_HEIGHT;
                    handler.sendMessage(msg);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailure(Call call, IOException e) {

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
                case 2:
                    ViewGroup.LayoutParams params = thought_list.getLayoutParams();
                    Log.d("nmsl","说说数量为："+adapter.getCount());
                    params.height = height;
                    thought_list.setLayoutParams(params);
                    break;
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        thoughts.clear();
    }
}