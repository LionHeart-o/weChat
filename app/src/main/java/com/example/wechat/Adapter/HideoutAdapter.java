package com.example.wechat.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wechat.Activity.ui.hideout.HideoutFragment;
import com.example.wechat.R;
import com.example.wechat.application.MyApplication;
import com.example.wechat.javaBean.CommentBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.javaBean.ThoughtBean;
import com.example.wechat.Utils.FileManager;
import com.example.wechat.Utils.FullyGridLayoutManager;
import com.example.wechat.customView.ContactListView;
import com.example.wechat.upload.GlideEngine;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.decoration.GridSpacingItemDecoration;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.ScreenUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class HideoutAdapter extends BaseAdapter {
    private Context mContext;
    private Activity mActivity;

    private List<ThoughtBean> sb1;
    private LoginBean loginBean=LoginBean.getInstance();
    private MyApplication application;
    private CommentAdapter commentAdapter;
    private FileManager fileManager=FileManager.getInstance();
    private ContactListView thought_list;

    public HideoutAdapter(Context context, MyApplication application, Activity activity, ContactListView thought_list){
        this.mContext=context;
        this.application=application;
        this.mActivity=activity;
        this.thought_list=thought_list;
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
        return sb1==null?null:(sb1.size()<position?null:sb1.get(position));
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





        //加载文本，名字，时间
        vh.message=convertView.findViewById(R.id.thought_message);
        vh.name=convertView.findViewById(R.id.contact_name);
        vh.time=convertView.findViewById(R.id.publish_time);
        vh.message.setText(bean.getMessage());
        vh.name.setText(bean.getUsername());
        vh.time.setText(bean.getCreateTime().toString());

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
        FullyGridLayoutManager manager = null;
        if(bean.getPic().size()%2==0){
            manager = new FullyGridLayoutManager(mContext,
                    2, GridLayoutManager.VERTICAL, false);
        }else {
            manager = new FullyGridLayoutManager(mContext,
                    3, GridLayoutManager.VERTICAL, false);
        }
        vh.mRecyclerView.setLayoutManager(manager);
        if(vh.mRecyclerView.getItemDecorationCount()==0) vh.mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, ScreenUtils.dip2px(mContext, 1), true));

        ThoughtImageAdapter mAdapter= new ThoughtImageAdapter(mContext);
        mAdapter.setList(bean.getPic());
        mAdapter.setSelectMax(9);

        vh.mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((v, p) -> {
            //Log.d("nmsl","点击图片！");
            List<LocalMedia> selectList = mAdapter.getLocalMedia();
            if (selectList.size() > 0) {
                PictureSelector.create(mActivity)
                        .themeStyle(R.style.picture_default_style) // xml设置主题
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// 设置相册Activity方向，不设置默认使用系统
                        .isNotPreviewDownload(true)// 预览图片长按是否可以下载
                        .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .openExternalPreview(p, selectList);
            }
        });

        //给评论区域设置适配器，以便批量加载评论
        vh.comment_list=convertView.findViewById(R.id.comment_list);
        if(bean.getComments().size()>=0){
            commentAdapter=new CommentAdapter(mContext,vh.faker_editText);
            commentAdapter.setData(bean.getComments());
            vh.comment_list.setAdapter(commentAdapter);
        }

        vh.send_button=convertView.findViewById(R.id.send_comment_btn);

        vh.send_button.setOnClickListener(view -> {
            InputMethodManager imm = (InputMethodManager)mContext.getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(),0);

            if(vh.faker_editText.getText().toString().equals("")) return;
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
            temp.setComment_name(loginBean.getUsername());
            temp.setSend_email(loginBean.getEmail());
            temp.setReply_email(reply_email);
            temp.setMessage(vh.faker_editText.getText().toString());
            vh.faker_editText.setText("");
            bean.getComments().add(temp);
            commentAdapter.notifyDataSetChanged();
        });


        if(!sb1.isEmpty()&&sb1.get(position).getSend_email().equals(LoginBean.getInstance().getEmail())){
            convertView.setOnLongClickListener(new thoughtOnLongClick(position));
        }

        return convertView;
    }


    //上传说说信息到数据库
    private void sendComment(String share_life_id,String send_email,String reply_email,String message){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"sendComment.action?" +
                "share_life_id="+share_life_id+
                "&send_email="+ send_email+
                "&reply_email="+ reply_email+
                "&message="+ message).build();//在这里将用户发送的信息通过url发送给机器人
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {

            }
            @Override
            public void onFailure(Call call, IOException e) {

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
    private class thoughtOnLongClick implements View.OnLongClickListener {

        private int position;
        public thoughtOnLongClick(int position) {
            this.position=position;
        }

        @Override
        public boolean onLongClick(View view){
            View view1=view.findViewById(R.id.popupMenuShowPosition);
            PopupMenu popupMenu = new PopupMenu(mActivity, view1);
            //设置PopupMenu对象的布局
            popupMenu.inflate(R.menu.thought_menu);
            //设置PopupMenu的点击事件
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if(menuItem.getTitle().equals("编辑说说")){

                    }else if(menuItem.getTitle().equals("删除说说")){
                        StringBuffer filename=new StringBuffer();
                        List<String> urls =sb1.get(position).getPic() ;
                        for(int i=0;i<urls.size();i++){
                            String url[]=urls.get(i).split("/");
                            if(i!=0) filename.append("&path="+url[url.length-1]);
                            else filename.append("?path="+url[url.length-1]);
                        }

                        deleteThought(sb1.get(position).getId(),filename.toString());
                        mActivity.recreate();
                    }
                    return true;
                }
            });
            popupMenu.show();
            return true;
        }
        private void deleteThought(String share_life_id,String filename){
            OkHttpClient okHttpClient = new OkHttpClient();

            Request request = new Request.Builder().url(MyApplication.BACK_URL+"deleteThoughts.action?" +
                    "share_life_id="+share_life_id).build();//在这里将用户发送的信息通过url发送给机器人
            Call call = okHttpClient.newCall(request);
            // 开启异步线程访问网络
            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    fileManager.deleteFile(filename);
                }
                @Override
                public void onFailure(Call call, IOException e) {

                }
            });
        }

    }
}
