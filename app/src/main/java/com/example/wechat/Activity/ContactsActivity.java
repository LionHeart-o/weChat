package com.example.wechat.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.os.Parcel;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.wechat.Adapter.ContactAdapter;
import com.example.wechat.Adapter.ContactListView;
import com.example.wechat.R;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.service.CommonDialog;
import com.example.wechat.service.DBThread;
import com.example.wechat.upload.GlideEngine;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE;


public class ContactsActivity extends AppCompatActivity {
    private ContactListView slv_list;              //列表控件
    private ContactAdapter adapter;                //列表的适配器
    private ImageView addContact;
    private ImageView editInformation;

    private LoginBean loginBean;
    private int time = 10;
    private List<ContactBean> pythonList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        loginBean = (LoginBean)getIntent().getSerializableExtra("LoginBean");
        init();
    }
    /**
     * 初始化界面控件
     */
    private void init(){

        slv_list= (ContactListView) findViewById(R.id.v_list);

        addContact=(ImageView) findViewById(R.id.add_contact);
        addContact.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final CommonDialog dialog=new CommonDialog(ContactsActivity.this);
                dialog.setTitle("请输入联系人邮箱：");

                dialog.setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        DBThread dt = new DBThread();
                        dt.setEmail(loginBean.getEmail());
                        dt.setContactEmail(dialog.getText());
                        dt.setContext(ContactsActivity.this);
                        dt.setAction("添加好友");
                        Thread thread = new Thread(dt);
                        thread.start();
                        dialog.dismiss();
                        Intent intent = new Intent(ContactsActivity.this, ContactsActivity.class);
                        intent.putExtra("LoginBean",loginBean);
                        startActivity(intent);
                    }

                });
                dialog.show();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
        });

        editInformation=(ImageView)findViewById(R.id.edit_information);
        editInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*final CommonDialog dialog=new CommonDialog(ContactsActivity.this);
                dialog.setTitle("请输入头像图片地址：");
                dialog.setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        if(!dialog.getText().equals("")){
                            DBThread dt = new DBThread();
                            dt.setEmail(loginBean.getEmail());
                            dt.setMy_head(dialog.getText());
                            dt.setContext(ContactsActivity.this);
                            dt.setAction("更改头像");
                            Thread thread = new Thread(dt);
                            thread.start();
                            Intent intent = new Intent(ContactsActivity.this, ContactsActivity.class);
                            loginBean.setHead(dialog.getText());
                            intent.putExtra("LoginBean",loginBean);
                            startActivity(intent);
                        }
                        finish();
                        dialog.dismiss();
                    }
                });
                dialog.show();*/
                PictureSelector.create(ContactsActivity.this)
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)//单选
                        .isEnableCrop(true)//是否开启裁剪
                        .imageEngine(GlideEngine.createGlideEngine())
                        .isWeChatStyle(true)
                        .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                        .scaleEnabled(true)//裁剪是否可放大缩小图片
                        .isDragFrame(true)//是否可拖动裁剪框(固定)
                        .withAspectRatio(1,1)//裁剪比例
                        .forResult(new OnResultCallbackListener<LocalMedia>() {
                            @Override
                            public void onResult(List<LocalMedia> result) {
                                // 结果回调,这里写上传到服务器的方法
                                File file=new File(result.get(0).getCutPath());
                                uploadFile(file);

                            }

                            @Override
                            public void onCancel() {
                                // 取消
                            }
                        });

            }
        });

        adapter=new ContactAdapter(this);
        slv_list.setAdapter(adapter);

        DBThread dt = new DBThread();
        dt.setEmail(loginBean.getEmail());
        dt.setContext(ContactsActivity.this);
        dt.setAction("获取联系人");
        dt.setLoginBean(loginBean);
        dt.setPythonList(pythonList);
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
                    tipToast("联系人读取失败");
                    System.gc();
                    cancel();
                }
                if(pythonList.size()!=0){
                    //因为不是创建这个东西的view不能直接给适配器设置数据，所以需要执行这个方法。
                    ContactsActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            adapter.setData(pythonList);
                        }
                    });
                    System.gc();
                    cancel();
                }
            }
        };
        timer.schedule(task,0,1000);

    }

    protected long exitTime;//记录第一次点击时的时间
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(ContactsActivity.this, "再按一次退出程序",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                ContactsActivity.this.finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void tipToast(String information){
        Looper.prepare();
        Toast.makeText(ContactsActivity.this,information,Toast.LENGTH_SHORT).show();
        Looper.loop();
    }
    // 使用OkHttp上传文件
    public void uploadFile(File file) {
        OkHttpClient client = new OkHttpClient();
        MediaType contentType = MediaType.parse("text/plain"); // 上传文件的Content-Type
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", loginBean.getEmail(),//设置param的名字，到时候取这个值的时候需要这个参数
                        RequestBody.create(MediaType.parse("multipart/form-data"), file))
                .build();

        Request request = new Request.Builder()
                .url("http://159.75.27.108:3300/upload") // 上传地址
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 文件上传成功
                if (response.isSuccessful()) {
                    Log.i("nmsl", "onResponse: " + response.body().string());
                } else {
                    Log.i("nmsl", "onResponse: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 文件上传失败
                Log.i("Haoxueren", "onFailure: " + e.getMessage());
            }
        });
    }

}
