package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wechat.Activity.AddActivity;
import com.example.wechat.Activity.ChatActivity;
import com.example.wechat.Activity.UserInfoActivity;
import com.example.wechat.R;
import com.example.wechat.application.MyApplication;
import com.example.wechat.customView.CircleImageViewDrawable;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.wechat.Activity.AddActivity.CANT_ADD_YOURSELF;

public class SearchAdapter extends BaseAdapter {
    private Context mContext;

    private List<ContactBean> sb1;
    private LoginBean loginBean=LoginBean.getInstance();
    private Handler addActivityHandle;

    public SearchAdapter(Context context,Handler addActivityHandle){
        this.mContext=context;
        this.addActivityHandle=addActivityHandle;
    }
    public void setData(List<ContactBean> sb1){
        this.sb1=sb1;
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return sb1==null?0:sb1.size();
    }

    @Override
    public ContactBean getItem(int position){
        return sb1==null?null:sb1.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        final ViewHolder vh;

        if(convertView==null){
            vh=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(R.layout.search_people_item,null);
            vh.contact_name=(TextView) convertView.findViewById(R.id.contact_name);
            vh.contact_head=(ImageView) convertView.findViewById(R.id.contact_head);
            vh.contact_detail=(TextView) convertView.findViewById(R.id.contact_detail);
            vh.add_button=(Button) convertView.findViewById(R.id.add_friend_button);
            convertView.setTag(vh);
        }else {
            vh=(ViewHolder) convertView.getTag();
        }

        final ContactBean bean=getItem(position);

        if(bean!=null){
            vh.contact_name.setText(bean.getUsername());
            vh.contact_detail.setText(
                    bean.getSex()+" "+
                    bean.getContact_age()+"岁 | "+
                    bean.getBirthdayString()+" "+
                    bean.getContact_constellation());

            Glide.with(mContext).asBitmap().load(bean.getHead()).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Drawable drawable = new CircleImageViewDrawable(resource);
                    vh.contact_head.setImageDrawable(drawable);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }

        convertView.setOnClickListener(view -> {
            if (bean == null) return;
            Intent intent = new Intent(mContext, UserInfoActivity.class);
            intent.putExtra("userInfo",bean);
            mContext.startActivity(intent);
        });

        vh.add_button.setOnClickListener(v -> {
            addFriend(loginBean.getEmail(),bean.getEmail());
        });
        return convertView;
    }

    class ViewHolder{
        public TextView contact_name,contact_detail;
        public ImageView contact_head;
        public Button add_button;
    }

    private void addFriend(String email,String contactEmail){
        Message message=new Message();

        if(email.equals(contactEmail)){
            message.what= AddActivity.CANT_ADD_YOURSELF;
            addActivityHandle.sendMessage(message);
            return;
        }else if(loginBean.getContactIndex().containsKey(contactEmail)){
            message.what=AddActivity.IS_YOUR_FRIEND;
            addActivityHandle.sendMessage(message);
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"addContact.action?" +
                "email="+email+"&contactEmail="+ contactEmail).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                try {
                    JSONObject json=new JSONObject(res);
                    message.what=AddActivity.BACK_MESSAGE;
                    Bundle bundle=new Bundle();
                    bundle.putString("message",json.getString("message"));
                    message.setData(bundle);
                    addActivityHandle.sendMessage(message);
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
