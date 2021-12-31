package com.example.wechat.Activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.wechat.R;
import com.example.wechat.Utils.LocationUtils;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.LoginBean;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EditInfoActivity extends AppCompatActivity {

    private EditText name;
    private EditText signature;
    private RelativeLayout birth;
    private TextView birth_info;

    private EditText editText_residence;

    private RelativeLayout sex;
    private TextView sexText;
    private Button button;
    private ImageView back;

    private MyApplication application;




    private String[] sexArray = new String[]{"不告诉你！", "男", "女","跨性别男性","跨性别女性"};// 性别选择

    private final int SUCCESS=1;
    private final int FAILURE=2;
    private final int SERVICE_NO_RESPONSE=3;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        application=(MyApplication)this.getApplication();

        name=findViewById(R.id.edit_username);
        name.setText(LoginBean.getInstance().getUsername());
        signature=findViewById(R.id.edit_signature);
        signature.setText(LoginBean.getInstance().getSignature());

        editText_residence=findViewById(R.id.editText_residence);
        editText_residence.setText(LoginBean.getInstance().getResidence());

        getLocation();

        birth=findViewById(R.id.edit_birth_layout);
        birth_info=findViewById(R.id.birth_info);

        Date date=LoginBean.getInstance().getBirthday();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        birth_info.setText(simpleDateFormat.format(date));
        birth.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditInfoActivity.this);
            View view = getLayoutInflater().inflate(R.layout.date_dialog, null);
            final DatePicker datePicker = view.findViewById(R.id.date_picker);
            //设置日期简略显示 否则详细显示 包括:星期\周
            datePicker.setCalendarViewShown(false);
            //设置date布局
            builder.setView(view);
            builder.setTitle("选择出生日期");
            builder.setPositiveButton("确 定", (dialog, which) -> {
                //日期格式
                int year = datePicker.getYear();
                int month = datePicker.getMonth()+1;
                int dayOfMonth = datePicker.getDayOfMonth();
                birth_info.setText(year+"-"+month+"-"+dayOfMonth);
                dialog.cancel();
            });
            builder.setNegativeButton("取 消", (dialog, which) -> dialog.cancel());
            builder.create().show();
        });

        sex=findViewById(R.id.edit_sex_layout);
        sexText=findViewById(R.id.edit_sex);
        sexText.setText(LoginBean.getInstance().getSex());
        sex.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(EditInfoActivity.this);// 自定义对话框
            // 2默认的选中
            builder.setSingleChoiceItems(sexArray, 0, (dialog, which) -> {// which是被选中的位置
                sexText.setText(sexArray[which]);
                dialog.dismiss();// 随便点击一个item消失对话框，不用点击确认取消
            });
            builder.show();// 让弹出框显示
        });

        button= findViewById(R.id.save_info);
        button.setOnClickListener(v -> saveInfo());

        signature= findViewById(R.id.edit_signature);
        name= findViewById(R.id.edit_username);

        back=findViewById(R.id.back);
        back.setOnClickListener(v -> finish());

    }

    private void saveInfo(){
        String email=LoginBean.getInstance().getEmail();
        String password=LoginBean.getInstance().getPassword();
        String username=name.getText().toString();
        String sign=signature.getText().toString();
        String birth= birth_info.getText().toString();
        String residence=editText_residence.getText().toString();
        String sex=sexText.getText().toString();

        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"editUserInfo.action?" +
                "email="+ email +
                "&password="+ password+
                "&username="+ username+
                "&signature="+ sign+
                "&birthday="+ birth+
                "&residence="+ residence+
                "&sex="+ sex).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                Message message=new Message();
                if(res.equals("1")){
                    message.what=1;
                    LoginBean loginBean=LoginBean.getInstance();
                    loginBean.setUsername(username);
                    loginBean.setSignature(sign);

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        Date newTime = format.parse(birth);
                        loginBean.setBirthday(newTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    loginBean.setResidence(residence);
                    loginBean.setSex(sex);
                    finish();
                }else{
                    message.what=2;
                }
                handler.sendMessage(message);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                Message message=new Message();
                message.what=3;
                handler.sendMessage(message);
            }
        });
    }

    private void getLocation(){
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( EditInfoActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(EditInfoActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditInfoActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            LocationUtils.getInstance(getApplicationContext()).setAddressCallback(new LocationUtils.AddressCallback() {
                @Override
                public void onGetAddress(Address address) {
                    String countryName = address.getCountryName();//国家
                    String adminArea = address.getAdminArea();//省
                    String locality = address.getLocality();//市
                    String subLocality = address.getSubLocality();//区
                    String featureName = address.getFeatureName();//街道
                    //Log.d("nmsl",countryName+adminArea+locality+subLocality+featureName);
                    editText_residence.setText(adminArea+"-"+locality+"-"+subLocality);

                }
                @Override
                public void onGetLocation(double lat, double lng) {
                    //Log.d("定位经纬度",lat+" "+lng);
                }
            });
        }
    }

    private Handler handler=new Handler() {
        @Override
        public void handleMessage(@NotNull Message msg) {
            switch (msg.what) {
                case SUCCESS:
                    Toast.makeText(EditInfoActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                    break;
                case FAILURE:
                    Toast.makeText(EditInfoActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
                    break;
                case SERVICE_NO_RESPONSE:
                    Toast.makeText(EditInfoActivity.this, "服务器无响应", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
