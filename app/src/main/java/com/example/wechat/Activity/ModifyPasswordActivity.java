package com.example.wechat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.R;
import com.example.wechat.Utils.MD5Utils;
import com.example.wechat.application.MyApplication;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ModifyPasswordActivity extends AppCompatActivity {
    private EditText new_password;
    private EditText sure_password;

    private Button modify_password;
    private String email;

    private MyApplication application;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = (MyApplication)this.getApplication();
        setContentView(R.layout.activity_modify_password);
        Intent intent = getIntent();
        email=intent.getStringExtra("email");

        init();
    }
    private void init(){
        new_password=(EditText)findViewById(R.id.new_password);
        sure_password=(EditText)findViewById(R.id.sure_password);
        modify_password=(Button) findViewById(R.id.modify_password);

        modify_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String regexp="^(?![a-zA-z]+$)(?!\\d+$)(?![!@#$%^&*]+$)[a-zA-Z\\d!@#$%^&*]+$";
                String password=new_password.getText().toString();
                if (!password.matches(regexp)||password.length()<8){
                    Toast.makeText(ModifyPasswordActivity.this,"密码必须含有字母和数字，且长度大于等于8",Toast.LENGTH_SHORT).show();
                    //提示密码不合法，密码必须含有字母和数字，且长度大于等于8
                }else if (!password.equals(sure_password.getText().toString())){
                    Toast.makeText(ModifyPasswordActivity.this,"两次密码不一致",Toast.LENGTH_SHORT).show();
                    //提示两次密码不一致
                }else {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder().url(MyApplication.BACK_URL+"modifyPassword.action?" +
                            "email="+email+"&modifyPassword="+ MD5Utils.stringToMD5(password)).build();//在这里将用户发送的信息通过url发送给机器人
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
                                    Toast.makeText(ModifyPasswordActivity.this,"修改成功",Toast.LENGTH_SHORT).show();
                                    finish();
                                    Intent intent = new Intent();
                                    intent.setClass(ModifyPasswordActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }else {
                                    Toast.makeText(ModifyPasswordActivity.this,"修改失败",Toast.LENGTH_SHORT).show();
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

}
