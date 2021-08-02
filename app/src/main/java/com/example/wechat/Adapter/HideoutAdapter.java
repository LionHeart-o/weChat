package com.example.wechat.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wechat.R;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.CommentBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.javaBean.ThoughtBean;
import com.example.wechat.server.FullyGridLayoutManager;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.tools.ScreenUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HideoutAdapter extends BaseAdapter {
    private Context mContext;
    private List<ThoughtBean> sb1;
    private LoginBean loginBean=LoginBean.getInstance();
    private MyApplication application;
    private CommentAdapter commentAdapter;

    public HideoutAdapter(Context context,MyApplication application){
        this.mContext=context;
        this.application=application;
    }
    public void setData(List<ThoughtBean> sb1){
        this.sb1=sb1;
    }

    @Override
    public int getCount(){
        return sb1==null?0:sb1.size();
    }

    @Override
    public ThoughtBean getItem(int position){
        return sb1==null?null:sb1.get(position);
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        final ViewHolder vh;
        final ThoughtBean bean=getItem(position);


        if(convertView==null){
            vh=new ViewHolder();
            convertView= LayoutInflater.from(mContext).inflate(R.layout.dynamics_item,null);
            convertView.setTag(vh);
        }else {
            vh=(ViewHolder) convertView.getTag();
        }

        vh.faker_editText=convertView.findViewById(R.id.default_send_message);
        vh.send_button=convertView.findViewById(R.id.send_comment_btn);

        vh.send_button.setOnClickListener(view -> {
            String reply_email=  vh.faker_editText.getHint().toString();
            String info[]=reply_email.split(" ");
            CommentBean temp=new CommentBean();
            if(!reply_email.equals("评论")) {
                reply_email=info[2];
                temp.setReply_name(info[1]);
            }else{
                reply_email="";
            }
            sendComment(bean.getId(),loginBean.getEmail(),reply_email,vh.faker_editText.getText().toString());
            temp.setComment_name(loginBean.getMyName());
            temp.setComment_email(loginBean.getEmail());
            temp.setReply_email(reply_email);
            temp.setComment_text(vh.faker_editText.getText().toString());
            bean.getCommentBeanList().add(temp);

            ViewGroup.LayoutParams params = vh.comment_list.getLayoutParams();
            params.height = bean.getCommentBeanList().size()*100;
            vh.comment_list.setLayoutParams(params);
            commentAdapter.notifyDataSetChanged();
        });



        //加载文本，名字，时间
        vh.message=convertView.findViewById(R.id.thought_message);
        vh.name=convertView.findViewById(R.id.contact_name);
        vh.time=convertView.findViewById(R.id.publish_time);
        vh.message.setText(bean.getMessage());
        vh.name.setText(bean.getName());
        vh.time.setText(bean.getTimestamp());

        //加载头像
        vh.head=convertView.findViewById(R.id.contact_head);
        Glide.with(vh.head.getContext())
                .load(bean.getHead())
                .centerCrop()
                .placeholder(R.color.app_color_f6)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(vh.head);


        //给图片区域设置适配器，以便批量加载图片
        vh.mRecyclerView = convertView.findViewById(R.id.recycler);
        FullyGridLayoutManager manager = new FullyGridLayoutManager(mContext,
                3, GridLayoutManager.VERTICAL, false);
        vh.mRecyclerView.setLayoutManager(manager);
        vh.mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3,
                ScreenUtils.dip2px(mContext, 8), false));
        ThoughtImageAdapter mAdapter= new ThoughtImageAdapter(mContext);
        mAdapter.setList(bean.getPic());
        mAdapter.setSelectMax(9);
        vh.mRecyclerView.setAdapter(mAdapter);

        //给评论区域设置适配器，以便批量加载评论
        vh.comment_list=convertView.findViewById(R.id.comment_list);
        if(bean.getCommentBeanList().size()>0){
            commentAdapter=new CommentAdapter(mContext,vh.faker_editText);
            commentAdapter.setData(bean.getCommentBeanList());
            vh.comment_list.setAdapter(commentAdapter);
            ViewGroup.LayoutParams params = vh.comment_list.getLayoutParams();
            params.height = bean.getCommentBeanList().size()*100;
            vh.comment_list.setLayoutParams(params);
        }
        return convertView;
    }
    //上传说说信息到数据库
    private void sendComment(String share_life_id,String send_email,String reply_email,String message){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(application.getBack_end_url()+"sendComment.action?" +
                "share_life_id="+share_life_id+"&send_email="+ send_email+"&reply_email="+ reply_email+"&message="+ message).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("nmsl","说说发表失败:"+e.getMessage());
            }
        });
    }

    class ViewHolder{
        ImageView head;
        TextView message,name,time;
        RecyclerView mRecyclerView;
        EditText faker_editText;
        ListView comment_list;
        Button send_button;

    }


}
