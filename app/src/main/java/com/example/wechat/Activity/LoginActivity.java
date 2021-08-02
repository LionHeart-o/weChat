package com.example.wechat.Activity;


import android.content.Intent;

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
import com.example.wechat.SQLite.SQLAutoLogin;

import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.ContactBean;

import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.javaBean.NotificationBean;
import com.example.wechat.javaBean.getStaticBean.getNotificationBean;
import com.example.wechat.methods.Encrypt;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    private SQLAutoLogin helper;//处理自动登录的数据库操作对象
    private SQLiteHelper info_helper;//处理获取用户消息记录的数据库操作对象


    private int time = 10;

    private MyApplication application;
    private LoginBean loginBean=LoginBean.getInstance();
    private List<ContactBean> contactBeanList=ContactBean.getInstance();
    private Map<String,Integer> contactIndex=ContactBean.getIndexInstance();


   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (MyApplication)this.getApplication();
        if(autoLogin()==false){
            setContentView(R.layout.activity_login);
            init();
        }
    }

    private boolean autoLogin(){
        helper = new SQLAutoLogin(this);
        SQLiteDatabase database = helper.getReadableDatabase();
        helper.onCreate(database);
        Cursor cursor = helper.query();
        if(cursor.getCount()==0){
            return false;
        }else {
            cursor.moveToFirst();
            requireLogin(cursor.getString(cursor.getColumnIndex("email")),cursor.getString(cursor.getColumnIndex("password")));
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
                requireLogin(email.getText().toString(),Encrypt.encrypt(password.getText().toString(),"java"));//请求登录
                helper.insert(email.getText().toString(),Encrypt.encrypt(password.getText().toString(),"java"));
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
        Request request = new Request.Builder().url(application.getBack_end_url()+"login.action?" +
                "email="+email+"&password="+ password).build();//将用户的账号密码传输
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Log.i("nmsl",res);
                try {
                    JSONObject json=new JSONObject(res);
                    if(json.getString("loginState").equals("true")){
                        loginBean.setEmail(json.getString("email"));
                        loginBean.setPassword(json.getString("password"));
                        loginBean.setHead(json.getString("head"));
                        loginBean.setMyName(json.getString("username"));
                        loginBean.setLogin(true);
                        JSONArray jsonArray=new JSONArray(json.getString("contacts"));
                        JSONObject jsonTemp;

                        ContactBean contactBean;
                        for(int i=0;i<jsonArray.length();i++){
                            contactBean = new ContactBean();
                            jsonTemp=jsonArray.getJSONObject(i);
                            contactBean.setContact_email(jsonTemp.getString("email"));
                            contactBean.setContact_head(jsonTemp.getString("head"));
                            contactBean.setContact_name(jsonTemp.getString("username"));
                            info_helper = new SQLiteHelper(LoginActivity.this);
                            SQLiteDatabase database = helper.getReadableDatabase();
                            info_helper.onCreate(database);

                            //根据邮箱存储坐标，便于查询
                            contactIndex.put(jsonTemp.getString("email"),i);
                            Cursor cursor = info_helper.queryLastMessage(loginBean.getEmail(),jsonTemp.getString("email"));
                            if(cursor.getCount()==0){
                                contactBean.setContact_last_message("");
                            }else {
                                cursor.moveToFirst();
                                contactBean.setLast_time(cursor.getString(cursor.getColumnIndex("createTime")));
                                if(cursor.getString(cursor.getColumnIndex("type")).equals("4")){
                                    contactBean.setContact_last_message("[图片]");
                                }else if(cursor.getString(cursor.getColumnIndex("type")).equals("5")){
                                    contactBean.setContact_last_message("[文件]");
                                }else{
                                    contactBean.setContact_last_message(cursor.getString(cursor.getColumnIndex("message")));
                                }

                            }
                            contactBeanList.add(contactBean);
                        }


                        jsonArray=new JSONArray(json.getString("notifications"));
                        NotificationBean notificationBean;
                        for(int i=0;i<jsonArray.length();i++){
                            notificationBean=new NotificationBean();
                            jsonTemp=jsonArray.getJSONObject(i);
                            if(jsonTemp.getString("email").equals(loginBean.getEmail())){
                                Log.i("nmsl",jsonTemp.getString("email")+"    "+loginBean.getEmail());
                                continue;
                            }
                            notificationBean.setEmail(jsonTemp.getString("email"));
                            notificationBean.setHead(jsonTemp.getString("head"));
                            notificationBean.setName(jsonTemp.getString("username"));
                            Integer state = new Integer(jsonTemp.getString("contactState"));
                            notificationBean.setState(state);
                            getNotificationBean.pythonList.add(notificationBean);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onFailure(Call call, IOException e) {

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
                    time=10;
                    tipToast("登录超时");
                    System.gc();
                    cancel();
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
