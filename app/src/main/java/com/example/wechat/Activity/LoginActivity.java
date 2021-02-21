package com.example.wechat.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.methods.Encrypt;
import com.example.wechat.service.DBThread;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends AppCompatActivity {
    private TextView email;
    private EditText password;
    private Button login;

    //private TextView forget_password;
    private TextView jump_register;

    private LoginBean loginBean=new LoginBean();

    private int time = 10;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email=(TextView) findViewById(R.id.login_email);
        password=(EditText) findViewById(R.id.login_password);

        login=(Button) findViewById(R.id.login_button);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this,"点击登录",Toast.LENGTH_SHORT).show();
                DBThread dt = new DBThread();

                dt.setEmail(email.getText().toString());
                dt.setPassword(Encrypt.encrypt(password.getText().toString(),"java"));
                dt.setContext(LoginActivity.this);
                dt.setAction("登录");
                dt.setLoginBean(loginBean);

                Thread thread = new Thread(dt);
                thread.start();

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
                            intent.setClass(LoginActivity.this, ContactsActivity.class);
                            intent.putExtra("LoginBean",loginBean);
                            startActivity(intent);
                            System.gc();
                            cancel();
                            finish();
                        }
                    }
                };
                timer.schedule(task,1000,1000);
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

}
