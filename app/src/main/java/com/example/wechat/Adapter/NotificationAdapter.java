package com.example.wechat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wechat.Activity.LoginActivity;
import com.example.wechat.Activity.MainActivity;
import com.example.wechat.R;
import com.example.wechat.SQLite.SQLAutoLogin;
import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.javaBean.NotificationBean;
import com.example.wechat.javaBean.getStaticBean.getNotificationBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NotificationAdapter extends BaseAdapter {
    private Context mContext;
    private Activity activity;
    private List<NotificationBean> sb1;
    private LoginBean loginBean=LoginBean.getInstance();
    private List<ContactBean> contactBeanList=ContactBean.getInstance();
    private MyApplication application;

    public NotificationAdapter(Context context,Activity activity){
        this.mContext=context;
        this.activity=activity;
        application = (MyApplication)activity.getApplication();
    }
    public void setData(List<NotificationBean> sb1){
        this.sb1=sb1;
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return sb1==null?0:sb1.size();
    }

    @Override
    public NotificationBean getItem(int position){
        return sb1==null?null:sb1.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final NotificationAdapter.ViewHolder vh;

        if(convertView==null){
            vh=new NotificationAdapter.ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(R.layout.notifications_item,null);

            vh.contact_name=(TextView) convertView.findViewById(R.id.contact_name);
            vh.contact_email=(TextView) convertView.findViewById(R.id.contact_email);
            vh.contact_head=(ImageView) convertView.findViewById(R.id.contact_head);
            vh.textClass=(TextView)convertView.findViewById(R.id.tip);
            vh.refuse=(Button)convertView.findViewById(R.id.refuse);
            vh.accept=(Button)convertView.findViewById(R.id.accept);

            convertView.setTag(vh);
        }else {
            vh=(NotificationAdapter.ViewHolder) convertView.getTag();
        }

        final NotificationBean bean=getItem(position);
        if(bean!=null){
            vh.contact_name.setText(bean.getName());
            vh.contact_email.setText(bean.getEmail());
            Glide.with(mContext).load(bean.getHead()).error(R.mipmap.user).into(vh.contact_head);
            /*if(bean.getName().equals(getLoginBean.loginBean.getMyName())){
                vh.refuse.setVisibility(View.INVISIBLE);
                vh.accept.setVisibility(View.INVISIBLE);
            }*/

            if(bean.getState()==bean.REFUSE){
                vh.refuse.setVisibility(View.INVISIBLE);
                vh.accept.setVisibility(View.INVISIBLE);
                vh.textClass.setText("已拒绝");
            }else if(bean.getState()==bean.DEFAULT){
                vh.textClass.setText("请求添加你为好友");
            }else if(bean.getState()==bean.ACCEPT){
                vh.refuse.setVisibility(View.INVISIBLE);
                vh.accept.setVisibility(View.INVISIBLE);
                vh.textClass.setText("已添加");
            }
        }

        vh.refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(application.getBack_end_url()+ "handleNotification.action?" +
                        "email="+loginBean.getEmail()+"&contactEmail="+ vh.contact_email.getText()+"&handle=refuse").build();//在这里将用户发送的信息通过url发送给机器人
                Call call = okHttpClient.newCall(request);
                // 开启异步线程访问网络
                call.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        requireLogin();
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
                vh.refuse.setVisibility(View.INVISIBLE);
                vh.accept.setVisibility(View.INVISIBLE);
                vh.textClass.setText("已拒绝");
                activity.finish();
                Intent intent = new Intent();
                intent.setClass(mContext, MainActivity.class);
                mContext.startActivity(intent);
            }
        });

        vh.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder().url(application.getBack_end_url()+"handleNotification.action?" +
                        "email="+loginBean.getEmail()+"&contactEmail="+ vh.contact_email.getText()+"&handle=accept").build();//在这里将用户发送的信息通过url发送给机器人
                Call call = okHttpClient.newCall(request);
                // 开启异步线程访问网络
                call.enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        requireLogin();
                    }
                    @Override
                    public void onFailure(Call call, IOException e) {

                    }
                });
                vh.refuse.setVisibility(View.INVISIBLE);
                vh.accept.setVisibility(View.INVISIBLE);
                vh.textClass.setText("已接受");
                activity.finish();
                Intent intent = new Intent();
                intent.setClass(mContext, MainActivity.class);
                mContext.startActivity(intent);

            }
        });
        return convertView;
    }

    class ViewHolder{
        public TextView contact_name,contact_email,textClass;
        public ImageView contact_head;
        public Button refuse,accept;
    }

    public void requireLogin() {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(application.getBack_end_url()+"login.action?" +
                "email=" + loginBean.getEmail() + "&password=" + loginBean.getPassword()).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SQLiteHelper info_helper;//处理获取用户消息记录的数据库操作对象
                String res = response.body().string();
                contactBeanList.clear();
                getNotificationBean.pythonList.clear();
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
                            info_helper = new SQLiteHelper(activity);
                            SQLiteDatabase database = info_helper.getReadableDatabase();
                            info_helper.onCreate(database);

                            Cursor cursor = info_helper.queryLastMessage(loginBean.getEmail(),jsonTemp.getString("email"));
                            if(cursor.getCount()==0){
                                contactBean.setContact_last_message("");
                            }else {
                                cursor.moveToFirst();
                                contactBean.setLast_time(cursor.getString(cursor.getColumnIndex("createTime")));
                                if(cursor.getString(cursor.getColumnIndex("type")).equals("4")){
                                    contactBean.setContact_last_message("[图片]");
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
    }
}
