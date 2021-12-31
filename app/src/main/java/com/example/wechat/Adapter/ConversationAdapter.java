package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wechat.Activity.ChatActivity;
import com.example.wechat.R;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.ConversationBean;
import com.example.wechat.javaBean.GroupBean;
import com.example.wechat.javaBean.LoginBean;

import java.util.List;

public class ConversationAdapter extends BaseAdapter {
    private Context mContext;
    private List<ConversationBean> sb1;

    private LoginBean loginBean=LoginBean.getInstance();

    public ConversationAdapter(Context context){
        this.mContext=context;
    }
    public void setData(List<ConversationBean> sb1){
        this.sb1=sb1;
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return sb1==null?0:sb1.size();
    }

    @Override
    public ConversationBean getItem(int position){
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
            convertView= LayoutInflater.from(mContext).inflate(R.layout.conversation_item,null);

            vh.conversation_name=(TextView) convertView.findViewById(R.id.conversation_name);
            vh.conversation_time=(TextView) convertView.findViewById(R.id.conversation_time);
            vh.conversation_last_message=(TextView) convertView.findViewById(R.id.conversation_message);

            vh.conversation_head=(ImageView) convertView.findViewById(R.id.conversation_head);
            convertView.setTag(vh);
        }else {
            vh=(ViewHolder) convertView.getTag();
        }

        final ConversationBean bean=getItem(position);

        if(bean!=null){
            vh.conversation_name.setText(bean.getConversation_name());
            vh.conversation_time.setText(bean.getLast_time());
            vh.conversation_last_message.setText(bean.getLast_message());

            Glide.with(mContext).load(bean.getConversation_cover()).error(R.mipmap.user).into(vh.conversation_head);
        }
        convertView.setOnClickListener(view -> {
            if (bean == null) return;
            Intent intent = new Intent(mContext, ChatActivity.class);
            int position1;
            if(bean.getConversation_type()==ConversationBean.PEOPLE){
                position1 = loginBean.getContactIndex().get(bean.getAccountNumber());
                ContactBean contactBean=loginBean.getContacts().get(position1);
                intent.putExtra("sessionId",contactBean.getEmail());
                intent.putExtra("sessionType",ConversationBean.PEOPLE);
                intent.putExtra("contact",contactBean);
            }else{
                position1 = loginBean.getGroupIndex().get(bean.getAccountNumber());
                GroupBean groupBean=loginBean.getGroups().get(position1);
                intent.putExtra("sessionId",groupBean.getGroupId());
                intent.putExtra("sessionType",ConversationBean.GROUP);
                intent.putExtra("group",groupBean);
            }
            mContext.startActivity(intent);
        });
        return convertView;
    }

    class ViewHolder{
        public TextView conversation_name,conversation_time,conversation_last_message;
        public ImageView conversation_head;
    }

}
