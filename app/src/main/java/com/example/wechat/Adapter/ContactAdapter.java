package com.example.wechat.Adapter;

import android.content.Context;
import android.content.Intent;
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

import java.util.List;

public class ContactAdapter extends BaseAdapter {
    private Context mContext;
    private List<ContactBean> sb1;

    public ContactAdapter(Context context){
        this.mContext=context;
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
            convertView= LayoutInflater.from(mContext).inflate(R.layout.contact_item,null);

            vh.contact_name=(TextView) convertView.findViewById(R.id.contact_name);
            vh.contact_email=(TextView) convertView.findViewById(R.id.contact_email);
            vh.contact_head=(ImageView) convertView.findViewById(R.id.contact_head);
            convertView.setTag(vh);
        }else {
            vh=(ViewHolder) convertView.getTag();
        }

        final ContactBean bean=getItem(position);

        if(bean!=null){
            vh.contact_name.setText(bean.getContact_name());
            vh.contact_email.setText(bean.getContact_email());
            Glide.with(mContext).load(bean.getHead()).error(R.mipmap.user).into(vh.contact_head);

        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bean == null) return;
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("contact_email", bean.getContact_email());
                intent.putExtra("my_email", bean.getMy_email());
                intent.putExtra("my_name", bean.getMy_name());
                intent.putExtra("contact_head", bean.getHead());
                intent.putExtra("myHead", bean.getOurHead());
                mContext.startActivity(intent);

            }
        });
        return convertView;
    }

    class ViewHolder{
        public TextView contact_name,contact_email;
        public ImageView contact_head;
    }

}
