package com.example.wechat.Utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.wechat.Activity.ui.hideout.HideoutFragment;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.wechat.application.MyApplication.FILE_SAVE_URL;

public class FileManager {

    private LoginBean loginBean=LoginBean.getInstance();
    private List<ContactBean> contactBeanList=loginBean.getContacts();
    private static FileManager fileManager;

    private FileManager(){
    }
    public static synchronized FileManager getInstance(){
        if(fileManager ==null){
            fileManager =new FileManager();
        }
        return fileManager;
    }

    // 使用OkHttp上传文件
    public void uploadFile(File file, String fileName, Handler handler,String sessionId,int sessionType, String sendUser,int messageType, WsManager wsManager) {
        OkHttpClient client = new OkHttpClient();
        //MediaType contentType = MediaType.parse("text/plain"); // 上传文件的Content-Type
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,//设置param的名字，到时候取这个值的时候需要这个参数
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
                    if(!(sessionId.equals("")&&sendUser.equals(""))){
                        try {
                            wsManager.sendInfo(sessionId,sessionType,sendUser,FILE_SAVE_URL+fileName,messageType,fileName);//1代表发送的是文本消息
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    if(handler!=null){
                        Message msg = new Message();
                        msg.what = HideoutFragment.UPDATE_COVER;
                        handler.sendMessage(msg);
                    }
                    Log.d("nmsl", "onResponse: " + response.body().string()+"上传成功");
                } else {
                    Log.d("nmsl", "onResponse: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                // 文件上传失败
                Log.i("nmsl", "onFailure: " + e.getMessage());
            }
        });
    }

    public void deleteFile(String filename) {
        Log.d("nmsl",filename);
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("names",filename)
                .setType(MultipartBody.FORM)
                .build();

        Request request = new Request.Builder()
                .url("http://159.75.27.108:3300/delete"+filename) // 上传地址
                .post(body)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.d("nmsl", "onResponse: " + response.body().string());
                } else {
                    Log.d("nmsl", "onResponse: " + response.message());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("nmsl", "onFailure: " + e.getMessage());
            }
        });
    }

}
