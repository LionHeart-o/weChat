package com.example.wechat.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wechat.R;
import com.example.wechat.application.MyApplication;
import com.example.wechat.customView.CircleImageViewDrawable;
import com.example.wechat.javaBean.CommentBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.javaBean.ThoughtBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
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

public class UserInfoActivity extends AppCompatActivity {
    private TextView name;
    private TextView signature;
    private TextView detail;
    private ImageView head;
    private ImageView cover;
    private TextView action_button;


    private final ImageView[] imageViews=new ImageView[4];

    private MyApplication application;

    private ContactBean user=new ContactBean();


    private final static int ME=1;
    private final static int FRIEND=2;
    private final static int STRANGER=3;

    private int action;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        application=(MyApplication)this.getApplication();
        name=findViewById(R.id.userInfo_name);
        signature=findViewById(R.id.userInfo_signature);
        detail=findViewById(R.id.userInfo_detail);
        head=findViewById(R.id.userInfo_head);
        cover=findViewById(R.id.userInfo_cover);
        action_button=(TextView)findViewById(R.id.action_button);

        action_button.setOnClickListener(v->{
            if(action==FRIEND){
                Intent intent=new Intent(UserInfoActivity.this,ChatActivity.class);
                intent.putExtra("contact",user);
                startActivity(intent);
            }else if(action==STRANGER){
                addFriend(LoginBean.getInstance().getEmail(),user.getEmail());
            }
        });

        imageViews[0]=findViewById(R.id.userInfo_one);
        imageViews[1]=findViewById(R.id.userInfo_two);
        imageViews[2]=findViewById(R.id.userInfo_three);
        imageViews[3]=findViewById(R.id.userInfo_four);

        Intent intent=getIntent();
        user= (ContactBean) intent.getSerializableExtra("userInfo");
        if(user==null){
            user=new ContactBean();
            String email=intent.getStringExtra("email");
            if(email.equals(LoginBean.getInstance().getEmail())){
                LoginBean loginBean=LoginBean.getInstance();
                user.setCover(loginBean.getCover());
                user.setResidence(loginBean.getResidence());
                user.setHead(loginBean.getHead());
                user.setSignature(loginBean.getSignature());
                user.setSex(loginBean.getSex());
                user.setEmail(loginBean.getEmail());
                List<ThoughtBean> temp=new ArrayList<>();


                Date date=loginBean.getBirthday();
                int age= (int) ((System.currentTimeMillis()-date.getTime())/31536000000L);
                user.setContact_age(age);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String[] a =simpleDateFormat.format(date).split("-");
                user.setContact_constellation(getConstellation(Integer.parseInt(a[1]),Integer.parseInt(a[2])));
                user.setBirthdayString(a[1]+"-"+a[2]);

                for(int i=0;i<ThoughtBean.getInstance().size();i++){
                    if(ThoughtBean.getInstance().get(i).getSend_email().equals(email)){
                        temp.add(ThoughtBean.getInstance().get(i));
                    }
                }
                user.setThoughtBeanList(temp);
                Message message=new Message();
                handler.sendMessage(message);
            }
            else getUserInfo(email);
        }else{
            Message message=new Message();
            handler.sendMessage(message);
        }



    }

    private void getUserInfo(String email){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"getContactInfo.action?" +
                "email="+ email).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.d("nmsl",res);

                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.create();
                user=gson.fromJson(res,ContactBean.class);

                int age= (int) ((System.currentTimeMillis()-user.getBirthday().getTime())/31536000000L);
                user.setContact_age(age);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                String[] a =simpleDateFormat.format(user.getBirthday()).split("-");
                user.setContact_constellation(getConstellation(Integer.parseInt(a[1]),Integer.parseInt(a[2])));
                user.setBirthdayString(a[1]+"-"+a[2]);

                Message message=new Message();
                handler.sendMessage(message);




            }
            @Override
            public void onFailure(Call call, IOException e) {

            }

        });

    }

    private final Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Glide.with(getApplicationContext()).asBitmap().load(user.getHead()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Drawable drawable = new CircleImageViewDrawable(resource);
                    head.setImageDrawable(drawable);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });

            Glide.with(getApplicationContext()).load(user.getCover()).error(R.drawable.ic_launcher_foreground).into(cover);
            cover.setAlpha((float) 0.5);

            int k=0;
            for(int i=0;i<user.getThoughtBeanList().size();i++){
                if(k==4) break;
                for(int j=0;j<user.getThoughtBeanList().get(i).getPic().size();j++){
                    Glide.with(getApplicationContext()).load(user.getThoughtBeanList().get(i).getPic().get(j)).centerCrop().into(imageViews[k++]);
                    if(k==4) break;
                }
            }
            name.setText(user.getUsername());
            signature.setText(user.getSignature());


            detail.setText(user.getSex()+" "+user.getContact_age()+"岁 | "+user.getBirthdayString()+" "+user.getContact_constellation()+" | "+user.getResidence());
            String email=user.getEmail();

            if(email.equals(LoginBean.getInstance().getEmail())){
                action=ME;
                action_button.setText("这是你自己");
            }else if(LoginBean.getInstance().getContactIndex().containsKey(email)){
                action=FRIEND;
                action_button.setText("发消息");
            }else{
                action=STRANGER;
                action_button.setText("添加好友");
            }


        }
    };

    private void addFriend(String email,String contactEmail){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"action_list.action?" +
                "email="+email+"&contactEmail="+ contactEmail).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject json=new JSONObject(res);
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),json.getString("message"),Toast.LENGTH_SHORT).show();
                    Looper.loop();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public static String getConstellation(int month, int day) {
        String[] starArr = {"魔羯座","水瓶座", "双鱼座", "牡羊座",
                "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座" };
        int[] DayArr = {22, 20, 19, 21, 21, 21, 22, 23, 23, 23, 23, 22};  // 两个星座分割日
        int index = month;
        // 所查询日期在分割日之前，索引-1，否则不变
        if (day < DayArr[month - 1]) {
            index = index - 1;
        }
        // 返回索引指向的星座string
        return starArr[index];
    }
}
