package com.example.wechat.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.R;
import com.example.wechat.methods.sendEmail;
import com.example.wechat.service.TimeService;

import java.util.Random;

import javax.mail.MessagingException;

public class VerficationActivity extends AppCompatActivity {
    private EditText email;
    private EditText verification;

    private Button get_verification;
    private Button modify_password;

    private int verification_code=0;

    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
        init();
    }
    private void init(){
        email=(EditText)findViewById(R.id.forget_password_email);
        verification=(EditText)findViewById(R.id.verification_code);

        get_verification=(Button) findViewById(R.id.forget_password_get_verification_code);
        modify_password=(Button) findViewById(R.id.modify_password);



        get_verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag) {
                    Random rand = new Random();
                    verification_code = rand.nextInt(999999 - 100000 + 1) + 100000;
                    if(email.getText().toString().equals("")) {
                        Toast.makeText(VerficationActivity.this,"你还没有输入邮箱",Toast.LENGTH_SHORT).show();
                    }else{
                        try {
                            sendEmail.send_QQemail("2250201905@qq.com", email.getText().toString(), verification_code+"");
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(VerficationActivity.this, TimeService.class);
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

        modify_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!verification.getText().toString().equals(verification_code+"")){
                    Toast.makeText(VerficationActivity.this,"验证码不正确",Toast.LENGTH_SHORT).show();
                    //提示验证码不正确
                }else {
                    Intent intent = new Intent(VerficationActivity.this, ModifyPasswordActivity.class);
                    intent.putExtra("email",email.getText().toString());
                    startActivity(intent);
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
