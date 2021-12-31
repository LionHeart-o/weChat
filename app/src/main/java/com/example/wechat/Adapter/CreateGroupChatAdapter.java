package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wechat.Activity.AddActivity;
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
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CreateGroupChatAdapter extends BaseAdapter {
    private Context mContext;

    private List<ContactBean> sb1;
    private Set<Integer> selectPosition;

    public CreateGroupChatAdapter(Context context){
        this.mContext=context;
    }
    public void setData(List<ContactBean> sb1,Set<Integer> selectPosition){
        this.sb1=sb1;
        this.selectPosition=selectPosition;
    }
    public void modifyData(){
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
            convertView= LayoutInflater.from(mContext).inflate(R.layout.contact_checkbox_item,null);
            vh.contact_name=(TextView) convertView.findViewById(R.id.contact_name);
            vh.contact_head=(ImageView) convertView.findViewById(R.id.contact_head);
            vh.checkBox=(CheckBox) convertView.findViewById(R.id.check_member);
            convertView.setTag(vh);
        }else {
            vh=(ViewHolder) convertView.getTag();
        }

        final ContactBean bean=getItem(position);

        if(bean!=null){
            vh.contact_name.setText(bean.getUsername());
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

            if(bean.isChecked()){
                vh.checkBox.setChecked(true);
            }else{
                vh.checkBox.setChecked(false);
            }

            convertView.setOnClickListener(view -> {
                if(vh.checkBox.isChecked()){
                    vh.checkBox.setChecked(false);
                    selectPosition.remove(position);
                    bean.setChecked(false);
                }else {
                    vh.checkBox.setChecked(true);
                    selectPosition.add(position);
                    bean.setChecked(true);
                }
            });

        }





        return convertView;
    }

    class ViewHolder{
        public TextView contact_name;
        public ImageView contact_head;
        public CheckBox checkBox;
    }



}
