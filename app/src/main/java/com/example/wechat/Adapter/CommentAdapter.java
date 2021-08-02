package com.example.wechat.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wechat.R;
import com.example.wechat.javaBean.CommentBean;

import java.util.List;

public class CommentAdapter extends BaseAdapter {
    private List<CommentBean> commentBeanList;
    private Context context;
    EditText father_edit;

    public void setData(List<CommentBean> commentBeanList){
        this.commentBeanList=commentBeanList;
        notifyDataSetChanged();
    }

    public CommentAdapter(Context context,EditText father_edit){
        this.context=context;
        this.father_edit=father_edit;
    }

    @Override
    public int getCount() {
        return commentBeanList==null?0:commentBeanList.size();
    }

    @Override
    public CommentBean getItem(int position) {
        return commentBeanList==null?null:commentBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        CommentBean commentBean=commentBeanList.get(position);
        Log.d("nmsl","position:"+position);

        if(view==null){
            viewHolder=new ViewHolder();
            view= LayoutInflater.from(context).inflate(R.layout.comment_item,null);
            view.setTag(viewHolder);
        }else {
            viewHolder=(ViewHolder)view.getTag();
        }
        viewHolder.textView=view.findViewById(R.id.comment);

        //设置部分文字点击事件
        //设置部分文字颜色
        //给部分文字添加点击事件以及更换颜色
        SpannableStringBuilder style = new SpannableStringBuilder();
        if(commentBean.getReply_name()==null||commentBean.getReply_name().equals("null")||commentBean.getReply_name().equals("")){

            style.append(commentBean.getComment_name()+"："+commentBean.getComment_text());
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {

                }
            };
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#0c6c8e"));
            style.setSpan(foregroundColorSpan, 0, commentBean.getComment_name().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }else {
            style.append(commentBean.getComment_name()+" 回复 "+commentBean.getReply_name()+"："+commentBean.getComment_text());
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {

                }
            };
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#0c6c8e"));
            style.setSpan(foregroundColorSpan, 0, commentBean.getComment_name().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#0c6c8e"));
            style.setSpan(foregroundColorSpan, commentBean.getComment_name().length()+4,commentBean.getComment_name().length()+4+commentBean.getReply_name().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        //配置给TextView
        viewHolder.textView.setMovementMethod(LinkMovementMethod.getInstance());
        viewHolder.textView.setText(style);
        viewHolder.textView.setOnClickListener(view1 -> {
            SpannableStringBuilder style1 = new SpannableStringBuilder();
            //father_edit.setHint("回复 "+commentBean.getComment_name());
            style1.append("回复 "+commentBean.getComment_name()+" "+commentBean.getComment_email());
            ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#f3f8f8"));
            style1.setSpan(foregroundColorSpan, 3+commentBean.getComment_name().length()+1, 3+commentBean.getComment_name().length()+1+commentBean.getComment_email().length() , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            father_edit.setHint(style1);
        });

        return view;
    }

    private class ViewHolder{

        TextView textView;
    }
}
