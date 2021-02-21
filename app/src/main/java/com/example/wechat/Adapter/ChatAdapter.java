package com.example.wechat.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wechat.R;
import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.ContactBean;

import java.util.List;

public class ChatAdapter extends BaseAdapter {
    private Context mContext;
    private List<ChatBean> chatBeanList; //聊天数据
    private LayoutInflater layoutInflater;

    public ChatAdapter(List<ChatBean> chatBeanList, Context context) {
        this.mContext=context;
        this.chatBeanList = chatBeanList;
        layoutInflater = LayoutInflater.from(context);
    }
    public void setData(List<ChatBean> sb1){
        this.chatBeanList = sb1;
        notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return chatBeanList.size();
    }
    @Override
    public Object getItem(int position) {
        return chatBeanList.get(position);
    }
    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View contentView, ViewGroup viewGroup) {

        Holder holder = new Holder();
        //判断当前的信息是发送的信息还是接收到的信息，不同信息加载不同的view
        if (chatBeanList.get(position).getState() == ChatBean.RECEIVE) {
            //加载左边布局，也就是机器人对应的布局信息
            contentView = layoutInflater.inflate(R.layout.chatting_left_item,
                    null);
        } else {
            //加载右边布局，也就是用户对应的布局信息
            contentView = layoutInflater.inflate(R.layout.chatting_right_item,
                    null);
        }
        holder.tv_chat_content = (TextView) contentView.findViewById(R.id.tv_chat_content);
        holder.tv_chat_content.setText(chatBeanList.get(position).getMessage());
        holder.head=(ImageView) contentView.findViewById(R.id.iv_head);
        Glide.with(mContext).load(chatBeanList.get(position).getHeadDetail()).error(R.mipmap.user).into(holder.head);
        return contentView;
    }
    class Holder {
        public TextView tv_chat_content; // 聊天内容
        public ImageView head;
    }
}

