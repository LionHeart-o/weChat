package com.example.wechat.Activity;


import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.R;

import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.Utils.MD5Utils;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.ContactBean;

import com.example.wechat.javaBean.ConversationBean;
import com.example.wechat.javaBean.GroupBean;
import com.example.wechat.javaBean.GroupMember;
import com.example.wechat.javaBean.LoginBean;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;


import java.io.IOException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private TextView email;
    private EditText password;
    private Button login;

    private TextView forget_password;
    private TextView jump_register;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private SQLiteHelper info_helper;//处理获取用户消息记录的数据库操作对象


    private int time = 10;

    private LoginBean loginBean=LoginBean.getInstance();


   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp=getSharedPreferences("autoLogin", Context.MODE_PRIVATE);
        editor=sp.edit();
        if(autoLogin()==false){
            setContentView(R.layout.activity_login);
            init();
        }
    }

    private boolean autoLogin(){

        String account=sp.getString("email",null);
        String password=sp.getString("password",null);
        if(account==null||password==null){
            return false;
        }else {
            requireLogin(account,password);
            return true;
        }
    }

    private void init(){
        email=(TextView) findViewById(R.id.login_email);
        password=(EditText) findViewById(R.id.login_password);

        login=(Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this,"点击登录",Toast.LENGTH_SHORT).show();
                requireLogin(email.getText().toString(), password.getText().toString());//请求登录
            }
        });

        forget_password=(TextView)findViewById(R.id.forget_password);
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, VerficationActivity.class);
                startActivity(intent);
            }
        });
        jump_register=(TextView)findViewById(R.id.jump_register);
        jump_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
    public void tipToast(String information){
        Looper.prepare();
        Toast.makeText(LoginActivity.this,information,Toast.LENGTH_SHORT).show();
        Looper.loop();
    }


    public void requireLogin(String email,String password){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"login.action?" +
                "email="+email+"&password="+ MD5Utils.stringToMD5(password)).build();//将用户的账号密码传输
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.i("nmsl",res);
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(json.getAsJsonPrimitive().getAsLong());
                    }
                });
                Gson gson = builder.setDateFormat("yyyy-MM-dd HH:mm:ss").create();

                loginBean=gson.fromJson(res,LoginBean.class);

                LoginBean.setInstance(loginBean);

                if(loginBean.getLoginState().equals("true")){
                    editor.putString("email",email);
                    editor.putString("password",password);
                    editor.commit();

                    loginBean.setLogin(true);

                    List<ContactBean> contactBeanList=loginBean.getContacts();//联系人列表
                    Map<String,Integer> contactIndex=loginBean.getContactIndex();//联系人坐标
                    List<GroupBean> groupBeanList=loginBean.getGroups();
                    Map<String,Integer> groupIndex=loginBean.getGroupIndex();//联系人坐标

                    List<ConversationBean> conversations=loginBean.getConversations();
                    if(conversations==null) conversations=new ArrayList<>();


                    //下面是从数据库里读取本地的信息，这里需要将个人和群聊信息合并加载成会话
                    info_helper = new SQLiteHelper(LoginActivity.this);
                    SQLiteDatabase database = info_helper.getReadableDatabase();
                    info_helper.onCreate(database);


                    for(int i=0;i<contactBeanList.size();i++){
                        //根据邮箱存储坐标，便于查询
                        contactIndex.put(contactBeanList.get(i).getEmail(),i);
                        Cursor cursor = info_helper.queryLastMessage(contactBeanList.get(i).getEmail());//扫描

                        ConversationBean temp=new ConversationBean();
                        temp.setAccountNumber(contactBeanList.get(i).getEmail());
                        temp.setConversation_cover(contactBeanList.get(i).getHead());
                        temp.setConversation_name(contactBeanList.get(i).getUsername());
                        temp.setConversation_type(ConversationBean.PEOPLE);

                        if(cursor.getCount()==0){
                            temp.setLast_message("");
                            temp.setLast_time("");
                        }else {
                            cursor.moveToFirst();
                            temp.setLast_time(cursor.getString(cursor.getColumnIndex("createTime")));
                            if(cursor.getInt(cursor.getColumnIndex("messageType"))==ChatBean.PIC){
                                temp.setLast_message("[图片]");
                            }else if(cursor.getInt(cursor.getColumnIndex("messageType"))==ChatBean.FILE){
                                temp.setLast_message("[文件]");
                            }else{
                                temp.setLast_message(cursor.getString(cursor.getColumnIndex("message")));
                            }
                        }
                        conversations.add(temp);
                    }


                    for(int i=0;i<groupBeanList.size();i++){
                        //根据邮箱存储坐标，便于查询
                        groupIndex.put(groupBeanList.get(i).getGroupId().toString(),i);
                        Cursor cursor = info_helper.queryLastMessage(groupBeanList.get(i).getGroupId().toString());//扫描

                        ConversationBean temp=new ConversationBean();
                        temp.setAccountNumber(groupBeanList.get(i).getGroupId().toString());
                        temp.setConversation_cover(groupBeanList.get(i).getGroupCover());
                        temp.setConversation_name(groupBeanList.get(i).getGroupName());
                        temp.setConversation_type(ConversationBean.GROUP);

                        if(cursor.getCount()==0){
                            temp.setLast_message("");
                            temp.setLast_time("");
                        }else {
                            cursor.moveToFirst();
                            temp.setLast_time(cursor.getString(cursor.getColumnIndex("createTime")));
                            if(cursor.getInt(cursor.getColumnIndex("messageType"))==ChatBean.PIC){
                                temp.setLast_message("[图片]");
                            }else if(cursor.getInt(cursor.getColumnIndex("messageType"))==ChatBean.FILE){
                                temp.setLast_message("[文件]");
                            }else{
                                temp.setLast_message(cursor.getString(cursor.getColumnIndex("message")));
                            }
                        }

                        Map<String,Integer> memberIndex=groupBeanList.get(i).getMemberIndex();
                        for(int j=0;j<groupBeanList.get(i).getGroupMembers().size();j++){
                            GroupMember groupMember=groupBeanList.get(i).getGroupMembers().get(j);
                            memberIndex.put(groupMember.getEmail(),j);
                        }

                        conversations.add(temp);
                    }

                    loginBean.setConversations(conversations);

                }else{

                    SharedPreferences.Editor editor=sp.edit();
                    //editor.clear().commit();
                }

            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                editor.clear().commit();
            }
        });
        Timer timer;
        TimerTask task;
        timer = new Timer();     //创建计时器对象
        task = new TimerTask() {
            @Override
            public void run() {
                Log.i("???",time+"");
                time=time-1;
                if(time==0){
                    tipToast("登录超时");
                    cancel();
                    finish();
                    Intent intent = new Intent(LoginActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                if(loginBean.isLogin()){
                    Intent intent = new Intent();
                    intent.setClass(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    System.gc();
                    cancel();
                    finish();
                }
            }
        };
        timer.schedule(task,1000,1000);
    }
}
