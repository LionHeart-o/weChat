package com.example.wechat.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.R;
import com.example.wechat.application.MyApplication;
import com.example.wechat.methods.Encrypt;
import com.example.wechat.methods.sendEmail;
import com.example.wechat.service.TimeService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Random;

import javax.mail.MessagingException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private EditText sure_password;
    private EditText verification;
    private EditText user_name;

    private Button get_verification;
    private Button now_register;
    private int verification_code=0;
    private Connection con =null;
    private PreparedStatement sql=null;

    private String emailStr;
    private String passwordStr;

    private boolean flag = true;

    private MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (MyApplication)this.getApplication();
        setContentView(R.layout.activity_register);

        //没有这个，发送验证码会程序崩溃，这个可以忽略掉发送验证码时线程不安全的问题。
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());

        init();
    }
    private void init(){
        email=(EditText)findViewById(R.id.email);
        password=(EditText)findViewById(R.id.register_password);
        sure_password=(EditText)findViewById(R.id.sure_register_password);
        verification=(EditText)findViewById(R.id.verification_code);
        user_name=(EditText)findViewById(R.id.user_name);

        get_verification=(Button) findViewById(R.id.get_verification_code);
        now_register=(Button) findViewById(R.id.now_register);


        get_verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    Random rand = new Random();
                    verification_code = rand.nextInt(999999 - 100000 + 1) + 100000;
                    if(email.getText().toString().equals("")) {
                        Toast.makeText(RegisterActivity.this,"你还没有输入邮箱",Toast.LENGTH_SHORT).show();
                    }else{
                        try {
                            sendEmail.send_QQemail("2250201905@qq.com", email.getText().toString(), verification_code+"");
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(RegisterActivity.this, TimeService.class);
                        startService(intent);
                        get_verification.setText("倒计时60秒");
                    }

                }
            }
        });

        MyBroadcastReceiver  receiver = new MyBroadcastReceiver(); //实例化广播接收者
        String action = "getCaptcha";
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        registerReceiver(receiver,intentFilter); //注册广播

        now_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String regexp="^(?![a-zA-z]+$)(?!\\d+$)(?![!@#$%^&*]+$)[a-zA-Z\\d!@#$%^&*]+$";
                emailStr=email.getText().toString();
                passwordStr=password.getText().toString();
                if (!passwordStr.matches(regexp)||passwordStr.length()<8){
                    Toast.makeText(RegisterActivity.this,"密码必须含有字母和数字，且长度大于等于8",Toast.LENGTH_SHORT).show();
                    //提示密码不合法，密码必须含有字母和数字，且长度大于等于8
                }else if (!password.getText().toString().equals(sure_password.getText().toString())){
                    Toast.makeText(RegisterActivity.this,"两次密码不一致",Toast.LENGTH_SHORT).show();
                    //提示两次密码不一致
                }else if(!verification.getText().toString().equals(verification_code+"")){
                    Toast.makeText(RegisterActivity.this,"验证码不正确",Toast.LENGTH_SHORT).show();
                    //提示验证码不正确
                }else if(user_name.getText().toString().equals("")){
                    Toast.makeText(RegisterActivity.this,"用户名不能为空",Toast.LENGTH_SHORT).show();
                }else{
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder().url(application.getBack_end_url()+"register.action?" +
                            "email="+emailStr+"&username="+user_name.getText().toString()+"&password="+ Encrypt.encrypt(passwordStr,"java")).build();//在这里将用户发送的信息通过url发送给机器人
                    Call call = okHttpClient.newCall(request);
                    // 开启异步线程访问网络
                    call.enqueue(new Callback() {
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String res = response.body().string();
                            try {
                                Looper.prepare();
                                JSONObject json=new JSONObject(res);
                                if(json.getString("registerState").equals("true")){
                                    Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent();
                                    intent.setClass(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }else {
                                    Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
                                }
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
            }
        });
    }
    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int time = intent.getIntExtra("time",60);
            if(time < 60 && time >0){
                flag = false;
                get_verification.setText("倒计时"+time+"秒");
            }else{
                flag = true;
                get_verification.setText("获取验证码");
            }
        }
    }
}
