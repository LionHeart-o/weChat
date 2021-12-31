package com.example.wechat.Activity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.wechat.Adapter.CreateGroupChatAdapter;
import com.example.wechat.R;
import com.example.wechat.Utils.FileManager;
import com.example.wechat.Utils.MD5Utils;
import com.example.wechat.application.MyApplication;
import com.example.wechat.customView.CircleImageViewDrawable;
import com.example.wechat.customView.ContactListView;
import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.ConversationBean;
import com.example.wechat.javaBean.GroupBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.upload.GlideEngine;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.wechat.application.MyApplication.FILE_SAVE_URL;

public class CreateGroupChatActivity extends AppCompatActivity {

    private List<ContactBean> contactBeanList;
    private CreateGroupChatAdapter adapter;
    private ContactListView checkboxList;
    private EditText search;
    private Button createGroup;
    private Set<Integer> selectPosition;
    private PopupWindow editGroupInfo;

    private ImageView groupHead;
    private EditText groupName;
    private EditText groupDetail;
    private Button sure;
    private File headPath;

    private FileManager fileManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkbox_contacts);
        fileManager=FileManager.getInstance();

        checkboxList=findViewById(R.id.v_list);
        search=findViewById(R.id.search_bar);
        createGroup=findViewById(R.id.create);
        contactBeanList=new ArrayList<>();
        contactBeanList.addAll(LoginBean.getInstance().getContacts());
        selectPosition=new LinkedHashSet<>();

        adapter=new CreateGroupChatAdapter(getApplicationContext());

        adapter.setData(contactBeanList,selectPosition);
        adapter.modifyData();

        checkboxList.setAdapter(adapter);


        Drawable icon = getResources().getDrawable(R.mipmap.search);
        icon.setBounds(0, 0, 80, 80);
        search.setCompoundDrawables(icon, null, null, null);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                contactBeanList.clear();
                List<ContactBean> temp=LoginBean.getInstance().getContacts();
                for(int i=0;i<temp.size();i++){
                    if(temp.get(i).getUsername().contains(s.toString())){
                        contactBeanList.add(temp.get(i));
                    }
                }
                adapter.modifyData();
            }
        });

        View contentView = LayoutInflater.from(this).inflate(R.layout.create_group, null);
        groupHead=contentView.findViewById(R.id.create_group_head);
        groupName=contentView.findViewById(R.id.edit_group_name);
        groupName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                groupName.setHint("");
            } else {
                groupName.setHint("为你的群取一个名字吧~");
            }
        });
        groupDetail=contentView.findViewById(R.id.edit_group_signature);
        groupDetail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                groupDetail.setHint("");
            } else {
                groupDetail.setHint("说说群的主题吧~");
            }
        });

        sure=contentView.findViewById(R.id.sure);

        editGroupInfo = new PopupWindow(getApplicationContext());
        editGroupInfo.setContentView(contentView);
        editGroupInfo.setBackgroundDrawable(null);
        editGroupInfo.setOutsideTouchable(true);
        editGroupInfo.setTouchable(true);
        editGroupInfo.setFocusable(true);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        editGroupInfo.setOnDismissListener(() -> {
            lp.alpha = 1.0f; //0.0-1.0
            getWindow().setAttributes(lp);
        });


        createGroup.setOnClickListener(v -> {
            if(editGroupInfo.isShowing()){
                editGroupInfo.dismiss();
            }
            else {
                lp.alpha = 0.5f;
                getWindow().setAttributes(lp);
                editGroupInfo.showAtLocation(CreateGroupChatActivity.this.getWindow().getDecorView(),Gravity.CENTER,0,0);
            }
        });




        groupHead.setOnClickListener(view -> PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .selectionMode(PictureConfig.SINGLE)//单选
                .isEnableCrop(true)//是否开启裁剪
                .imageEngine(GlideEngine.createGlideEngine())
                .isWeChatStyle(true)
                .showCropFrame(true)// 是否显示裁剪矩形边框 圆形裁剪时建议设为false
                .scaleEnabled(true)//裁剪是否可放大缩小图片
                .isDragFrame(true)//是否可拖动裁剪框(固定)
                .rotateEnabled(false) // 裁剪是否可旋转图片
                .withAspectRatio(1,1)//裁剪比例
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        headPath=new File(result.get(0).getCutPath());
                        modifyHead(result.get(0).getCutPath());
                    }

                    @Override
                    public void onCancel() {
                        // 取消
                    }
                }));

        sure.setOnClickListener(v -> {

            if(headPath==null){
                Toast.makeText(CreateGroupChatActivity.this,"请选择头像！",Toast.LENGTH_SHORT).show();
                return;
            }
            if(selectPosition.size()==0){
                Toast.makeText(CreateGroupChatActivity.this,"请选择好友！",Toast.LENGTH_SHORT).show();
                return;
            }
            if(groupName.getText().toString().equals("")){
                Toast.makeText(CreateGroupChatActivity.this,"请输入群名！",Toast.LENGTH_SHORT).show();
                return;
            }
            if(groupDetail.getText().toString().equals("")){
                Toast.makeText(CreateGroupChatActivity.this,"请输入群描述！",Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuffer member=new StringBuffer();
            for (Integer position : selectPosition) {
                member.append("&contactEmails="+contactBeanList.get(position).getEmail());
            }
            fileManager.uploadFile(headPath, MD5Utils.stringToMD5(headPath.getName()),null,"",-1,"",-1,null);
            generateGroup(LoginBean.getInstance().getEmail(),FILE_SAVE_URL+MD5Utils.stringToMD5(headPath.getName()),groupName.getText().toString(),groupDetail.getText().toString(),member.toString());
        });

    }

    private void generateGroup(String email,String head,String name,String detail,String member){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(MyApplication.BACK_URL+"createGroup.action?" +
                "email="+email+"&head="+head+"&name="+name+"&detail="+detail+member).build();
        Call call = okHttpClient.newCall(request);
        // 开启异步线程访问网络
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()));
                Gson gson = builder.setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                GroupBean groupBean= gson.fromJson(res,GroupBean.class);

                ConversationBean temp=new ConversationBean();
                temp.setConversation_cover(groupBean.getGroupCover());
                temp.setConversation_name(groupBean.getGroupName());
                temp.setAccountNumber(groupBean.getGroupId().toString());
                temp.setConversation_type(ConversationBean.GROUP);
                LoginBean.getInstance().getConversations().add(temp);

                Message message=new Message();
                if(groupBean==null){
                    message.what=2;
                }else{
                    message.what=1;
                }
                handler.sendMessage(message);


            }
            @Override
            public void onFailure(Call call, IOException e) {

            }
        });

    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    Toast.makeText(CreateGroupChatActivity.this,"创建成功！",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                default:
                    Toast.makeText(CreateGroupChatActivity.this,"创建失败！",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void modifyHead(String head){
        Glide.with(getApplicationContext()).asBitmap().load(head).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                Drawable drawable = new CircleImageViewDrawable(resource);
                groupHead.setImageDrawable(drawable);
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }
}
