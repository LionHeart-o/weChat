package com.example.wechat.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.wechat.R;
import com.example.wechat.Utils.WsManager;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.service.WebSocketService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.wechat.application.MyApplication.ANNOUNCEMENT_URL;

public class MainActivity extends AppCompatActivity {

    private MyApplication application=MyApplication.getInstance();
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private long downloadLength=0;
    private long contentLength=0;

    private final int UPDATE_PROGRESSBAR=1;
    private final int SHOW_POPUP_WINDOW=2;

    private ProgressBar progressBar;
    private TextView progress_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        //将每个菜单ID作为一组ID传递，因为每个菜单应被视为顶级目的地。
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.item_bottom_1, R.id.item_bottom_2, R.id.item_bottom_3)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        NavigationUI.setupWithNavController(navView,navController);

        application.setContext(getApplicationContext());

        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        Intent intent=new Intent(this, WebSocketService.class);
        startService(intent);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        getTheLatestVersion();


    }
    protected long exitTime;//记录第一次点击时的时间
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序",Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                this.finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private Handler handler=new Handler(){

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case UPDATE_PROGRESSBAR:
                    int progress=(int) (downloadLength * 1.0f / contentLength * 100);
                    progressBar.setProgress(progress);
                    progress_text.setText(progress+"%");
                    break;
                case SHOW_POPUP_WINDOW:
                    Bundle data=msg.getData();
                    String res=data.getString("res");
                    try {
                        JSONObject result=new JSONObject(res);
                        popUpdateWindow(result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    break;
            }

        }
    };



    private void getTheLatestVersion(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(ANNOUNCEMENT_URL+"version.json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res=response.body().string();

                if (response.body()!=null) {

                    Message message=new Message();
                    message.what=SHOW_POPUP_WINDOW;
                    Bundle data=new Bundle();
                    data.putString("res",res);
                    message.setData(data);
                    handler.sendMessage(message);

                }else {
                    //返回数据错误
                    return;
                }
            }
        });
    }

    private void popUpdateWindow(JSONObject result){
        try {
            Double version = Double.parseDouble(getPackageManager().
                    getPackageInfo(getPackageName(), 0).versionName);
            String apkSize=result.getString("apkSize");
            String msg=result.getString("message");
            String downloadUrl=result.getString("downloadUrl");
            double versionCode= result.getDouble("version");
            String title="是否升级到"+versionCode+"版本？";
            //如果当前版本小于最新版本，则更新
            Log.d("nmsl",versionCode+" "+version);
            if (versionCode>version){
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View view = inflater.inflate(R.layout.update_dialog, null);
                AlertDialog.Builder mDialog = new AlertDialog.Builder(MainActivity.this,R.style.Translucent_NoTitle);
                mDialog.setView(view);
                mDialog.setCancelable(true);
                mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        return keyCode == KeyEvent.KEYCODE_BACK;
                    }
                });
                Button dialog_button= view.findViewById(R.id.dialog_button);
                TextView dialog_title= view.findViewById(R.id.dialog_title);
                TextView dialog_size= view.findViewById(R.id.dialog_size);
                TextView dialog_message= view.findViewById(R.id.dialog_message);

                progress_text= view.findViewById(R.id.progress_text);
                ImageView iv_close= view.findViewById(R.id.iv_close);
                progressBar= view.findViewById(R.id.progressBar);
                progressBar.setMax(100);

                dialog_title.setText(title);
                dialog_size.setText(apkSize);
                dialog_message.setText(msg);

                dialog_button.setOnClickListener(v -> {
                    //动态询问是否授权
                    int permission = ActivityCompat.checkSelfPermission(getApplication(),
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,
                                1);
                    }else {
                        dialog_button.setVisibility(View.INVISIBLE);
                        downLoadFile(downloadUrl);
                    }
                });
                final Dialog dialog;
                dialog= mDialog.show();
                iv_close.setOnClickListener(v -> {
                    dialog.dismiss();
                });
            }else {

            }
        } catch (PackageManager.NameNotFoundException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void downLoadFile(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.body() == null) {
                    //下载失败
                    return;
                }
                InputStream is = null;
                FileOutputStream fos = null;
                byte[] buff = new byte[2048];
                int len;
                try {
                    is = response.body().byteStream();
                    String root = MainActivity.this.getExternalFilesDir(null).getPath();
                    File file = new File(root,"fakeChatAPP.apk");
                    if (file.exists()){
                        file.delete();
                    }
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    fos = new FileOutputStream(file);
                    long total = response.body().contentLength();
                    contentLength=total;
                    long sum = 0;
                    while ((len = is.read(buff)) != -1) {
                        fos.write(buff,0,len);
                        sum+=len;
                        downloadLength=sum;
                        int progress = (int) (sum * 1.0f / total * 100);
                        //下载中，更新下载进度
                        Message message=new Message();
                        message.what=UPDATE_PROGRESSBAR;
                        handler.sendMessage(message);
                    }
                    fos.flush();
                    //4.下载完成，安装apk
                    installApk(MainActivity.this,file);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                        if (fos != null)
                            fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void installApk(Context context, File file) {
        if (context == null) {
            return;
        }
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri= FileProvider.getUriForFile(context,context.getPackageName()+".fileprovider",file);
        } else {
            apkUri = Uri.fromFile(file);
        }
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_VIEW);

        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            //这个方法的含义是，给packageName应用授予路径为uri的文件的FLAG_GRANT_READ_URI_PERMISSION权限

            context.grantUriPermission(packageName, apkUri,Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        //判读版本是否在7.0以上

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        //获取文件file的MIME类型
        context.startActivity(intent);
        //弹出安装窗口把原程序关闭。
        //避免安装完毕点击打开时没反应
        /*this.finish();
        System.exit(0);*/
    }

}