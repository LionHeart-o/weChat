package com.example.wechat.Activity.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.wechat.Adapter.ContactAdapter;
import com.example.wechat.Adapter.ContactListView;
import com.example.wechat.R;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.service.CommonDialog;
import com.example.wechat.server.WsManager;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private ContactListView slv_list;              //列表控件
    private ContactAdapter adapter;                //列表的适配器
    private ImageView addContact;
    private int time = 10;
    private LoginBean loginBean=LoginBean.getInstance();
    private List<ContactBean> contactBeanList=ContactBean.getInstance();
    private WsManager wsManager=WsManager.getInstance();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.activity_contacts, container, false);
        init(root);
        return root;
    }
    private void init(View root){


        slv_list= (ContactListView) root.findViewById(R.id.v_list);

        addContact=(ImageView) root.findViewById(R.id.add_contact);
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final CommonDialog dialog=new CommonDialog(getActivity());
                dialog.setTitle("请输入联系人邮箱：");

                dialog.setOnClickBottomListener(new CommonDialog.OnClickBottomListener() {
                    @Override
                    public void onPositiveClick() {
                        addFriend(loginBean.getEmail(),dialog.getText());
                        dialog.dismiss();
                    }
                });
                dialog.show();
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
        });
        adapter=new ContactAdapter(getActivity());
        slv_list.setAdapter(adapter);
        adapter.setData(contactBeanList);

        if(wsManager.getContactAdapter()==null){
            wsManager.setContactAdapter(adapter);
        }

    }

    private void addFriend(String email,String contactEmail){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("http://192.168.123.22:8848/android_back_end_war_exploded/addContact.action?" +
                "email="+email+"&contactEmail="+ contactEmail).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject json=new JSONObject(res);
                    Looper.prepare();
                    Toast.makeText(getActivity(),json.getString("message"),Toast.LENGTH_SHORT).show();
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


    /** * 清除本应用内部缓存(/data/data/com.xxx.xxx/cache) * * @param context */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }


    /**
     * * 清除外部cache下的内容(/mnt/sdcard/android/data/com.xxx.xxx/cache) * * @param
     * context
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }

    /** * 清除自定义路径下的文件，使用需小心，请不要误删。而且只支持目录下的文件删除 * * @param filePath */
    public static void cleanCustomCache(String filePath) {
        deleteFilesByDirectory(new File(filePath));
    }

    /** * 清除本应用所有的数据 * * @param context * @param filepath */
    public static void cleanApplicationData(Context context, String... filepath) {
        cleanInternalCache(context);
        cleanExternalCache(context);
        for (String filePath : filepath) {
            cleanCustomCache(filePath);
        }
    }

    /** * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理 * * @param directory */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    @Override
    public void onStart() {
        Log.d("nmsl","开始");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d("nmsl","刷新");
        // TODO Auto-generated method stub
        super.onResume();

    }


}