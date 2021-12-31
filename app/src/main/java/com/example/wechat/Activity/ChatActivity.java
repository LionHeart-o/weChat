package com.example.wechat.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.wechat.Adapter.ChatAdapter;
import com.example.wechat.R;
import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.javaBean.ChatBean;

import com.example.wechat.Utils.FileManager;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.ConversationBean;
import com.example.wechat.javaBean.GroupBean;
import com.example.wechat.javaBean.LoginBean;
import com.example.wechat.upload.GlideEngine;
import com.example.wechat.Utils.WsManager;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.util.Base64;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.example.wechat.Utils.MD5Utils.getGlide4_SafeKey;
import static com.yalantis.ucrop.util.FileUtils.getPath;


public class ChatActivity extends AppCompatActivity {

    private ListView listView;//聊天布局
    private TextView session_name;//会话窗口名字
    private ChatAdapter adapter;//
    private List<ChatBean> chatBeanList; //存放所有聊天数据的集合
    private List<LocalMedia> localMediaList;//存放所有媒体信息的集合

    private EditText et_send_msg;
    private Button btn_send;
    private ImageView send_pic;
    private LinearLayout chat_linearLayout;
    private RelativeLayout chat_relativeLayout;
    private ImageView chat_bg;
    protected BottomSheetLayout bottomSheetLayout;//底部功能菜单

    private String sendMsg;    //发送的信息

    private SQLiteHelper helper;
    private ChatBean chatBean;

    private String sendUser;

    private String sessionId;
    private int sessionType;

    private String message;
    private int messageType;


    private ChatBean receiveBean;
    private LoginBean loginBean=LoginBean.getInstance();


    private ContactBean contactBean;

    private GroupBean groupBean;
    private Map<String,Integer> memberIndex;


    private WsManager wsManager;
    private FileManager fileManager;

    private final String TAG="ChatActivity";


    /**
     * 上次更新时间的字符串常量，用于作为SharedPreferences的键值
     */
    private final String LOCAL_BG_KEY = "local_bg";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //创建聊天数据类，每一个ChatBean封装一条消息
        chatBeanList = new ArrayList<ChatBean>();
        localMediaList=new ArrayList<>();

        //获取本地聊天信息
        helper = new SQLiteHelper(this);
        SQLiteDatabase database = helper.getReadableDatabase();
        helper.onCreate(database);

        Intent intent = getIntent();
        sessionType=intent.getIntExtra("sessionType",-1);

        if(sessionType==ConversationBean.PEOPLE){
            contactBean= (ContactBean) intent.getSerializableExtra("contact");
            sessionId=contactBean.getEmail();
        }else if(sessionType==ConversationBean.GROUP){
            groupBean=(GroupBean) intent.getSerializableExtra("group");
            memberIndex=groupBean.getMemberIndex();
            sessionId=groupBean.getGroupId().toString();
        }


        Cursor cursor = helper.query(sessionId);
        if (cursor.moveToFirst()) {
            do {
                sendUser = cursor.getString(cursor.getColumnIndex("sendUser"));
                message = cursor.getString(cursor.getColumnIndex("message"));
                messageType = cursor.getInt(cursor.getColumnIndex("messageType"));

                chatBean = new ChatBean();

                //从数据库获取到用户名、密码
                if (sendUser.equals(loginBean.getEmail())) {
                    chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
                } else {
                    chatBean.setState(chatBean.RECEIVE);
                    if(sessionType==ConversationBean.PEOPLE){
                        chatBean.setHead(contactBean.getHead());
                        chatBean.setUsername(contactBean.getUsername());
                    }else{
                        chatBean.setHead(groupBean.getGroupMembers().get(groupBean.getMemberIndex().get(sendUser)).getHead());
                        chatBean.setUsername(groupBean.getGroupMembers().get(groupBean.getMemberIndex().get(sendUser)).getUsername());
                    }
                }

                chatBean.setMessageType(messageType);//表示数据类型，3代表文字，4代表图片

                if(messageType==ChatBean.PIC){
                    String name[]=message.split("/");
                    LocalMedia temp=new LocalMedia(message,0, PictureMimeType.ofImage(),name[name.length-1]);
                    localMediaList.add(temp);
                    chatBean.setMedia_position(localMediaList.size()-1);
                }

                chatBean.setMessage(message);

                chatBeanList.add(chatBean);

            } while (cursor.moveToNext());
            //关闭游标
            cursor.close();
        }


        receiveBean=new ChatBean();
        receiveBean.setState(chatBean.RECEIVE);

        wsManager=WsManager.getInstance();
        wsManager.setChatAdapter(adapter);
        wsManager.setChatBeanList(chatBeanList);
        wsManager.setLocalMediaList(localMediaList);
        wsManager.connect();

        fileManager = FileManager.getInstance();

        initView(); //初始化界面控件
        intListener();//初始化按钮监听
        listView.setSelection(ListView.FOCUS_DOWN);
    }
    public void initView() {

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{ "Manifest.permission.WRITE_EXTERNAL_STORAGE","Manifest.permission.READ_EXTERNAL_STORAGE"},1);
        }
        //获取页面组件
        listView = (ListView) findViewById(R.id.list);
        et_send_msg = (EditText) findViewById(R.id.et_send_msg);
        btn_send = (Button) findViewById(R.id.btn_send);
        send_pic=(ImageView) findViewById(R.id.btn_sendPic);
        session_name=(TextView)findViewById(R.id.session_name);

        bottomSheetLayout = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        chat_linearLayout=(LinearLayout) findViewById(R.id.chat_linerLayout);
        chat_relativeLayout=(RelativeLayout) findViewById(R.id.chat_relativeLayout);
        chat_bg=(ImageView) findViewById(R.id.chat_bg);


        if(sessionType==ConversationBean.PEOPLE){
            session_name.setText(contactBean.getUsername());
            adapter = new ChatAdapter(chatBeanList,localMediaList, this,contactBean);

        }
        else if(sessionType==ConversationBean.GROUP){
            session_name.setText(groupBean.getGroupName());
            adapter = new ChatAdapter(chatBeanList,localMediaList, this,groupBean);
        }

        listView.setAdapter(adapter);

    }

    private void intListener(){
        /*
         * 获取手机的高度，以便固定父容器和背景容器的高度，这样软键盘弹出时就不会影响背景图的布局
         * */
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        ViewGroup.LayoutParams paramsForBottomSheetLayout = bottomSheetLayout.getLayoutParams();
        paramsForBottomSheetLayout.height=metric.heightPixels;
        bottomSheetLayout.setLayoutParams(paramsForBottomSheetLayout);
        bottomSheetLayout.setPeekOnDismiss(true);//点击阴影处软键盘不消失
        ViewGroup.LayoutParams paramsForBG=chat_bg.getLayoutParams();
        paramsForBG.height=metric.heightPixels;
        paramsForBG.width=metric.widthPixels;
        chat_bg.setLayoutParams(paramsForBG);

        SharedPreferences bgInfo=getSharedPreferences(LOCAL_BG_KEY,MODE_PRIVATE);
                //百度上的方法已经过时，这个是新的方法
        Glide.with(this)
                .load(bgInfo.getString(LOCAL_BG_KEY,""))
                .centerCrop()
                .placeholder(R.color.app_color_f6)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.mipmap.default_bg)
                .into(chat_bg);

        /*
        * 获取包容信息列表和输入框的容器，以便下面进行动态高度更改
        * */
        ViewGroup.LayoutParams paramsForChatLinearLayout = chat_linearLayout.getLayoutParams();


        /*
        * 获取最外层容器的高度，用于触发软键盘时动态更改输入框和信息列表的位置
        * */
        ViewGroup parentContent=findViewById(android.R.id.content);
        parentContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                parentContent.getWindowVisibleDisplayFrame(r);
                int displayHeight = r.bottom - r.top;
                //Log.v(TAG, "displayHeight:" + displayHeight);
                paramsForChatLinearLayout.height=displayHeight;
                chat_linearLayout.setLayoutParams(paramsForChatLinearLayout);
                //int parentHeight = parentContent.getHeight();
                //Log.v(TAG, "parentHeight:" + parentHeight);

                //int softKeyHeight = paramsForBottomSheetLayout.height - displayHeight;
                //Log.v(TAG, "softKeyHeight:" + softKeyHeight);
            }
        });

        /*
         * 为发送信息的按钮添加监听
         * */

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    sendData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //点击发送按钮，发送信息,这个方法在下面封装
            }
        });

        /*
         * 为文本框添加监听
         * */
        et_send_msg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    try {
                        sendData();//点击Enter键也可以发送信息
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });

        /*
        * 为发送文件和图片的按钮添加监听
        * */
        send_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showMenuSheet(MenuSheetView.MenuType.GRID);
            }
        });
    }

    private void sendData() throws JSONException {//点击发送后触发的事件，主要是判断信息合法性，确认信息合法之后调用websocket的信息发送方法


        sendMsg = et_send_msg.getText().toString(); //获取你输入的信息
        if (TextUtils.isEmpty(sendMsg)) {             //判断是否为空
            Toast.makeText(this, "您还未输任何信息哦", Toast.LENGTH_LONG).show();
            return;
        }
        et_send_msg.setText("");
        //替换空格和换行
        ChatBean chatBean = new ChatBean();
        chatBean.setMessage(sendMsg);
        chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
        chatBean.setMessageType(chatBean.TEXT);
        chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
        adapter.notifyDataSetChanged();    //更新ListView列表
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        helper.insert(sessionId,ConversationBean.PEOPLE,loginBean.getEmail(),sendMsg,ChatBean.TEXT,df.format(new Date()));//将该信息插入到本地数据库

        wsManager.sendInfo(sessionId,sessionType,loginBean.getEmail(),sendMsg,ChatBean.TEXT);

    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();//一点击返回就立马销毁当前页面
        return super.onKeyDown(keyCode, event);
    }



    private void showMenuSheet(final MenuSheetView.MenuType menuType) {
        InputMethodManager manager = ((InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE));

        manager.hideSoftInputFromWindow(this.bottomSheetLayout.getContentView().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

        MenuSheetView menuSheetView =
                new MenuSheetView(ChatActivity.this, menuType, "请选择...", new MenuSheetView.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(ChatActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                        //因为点击了面板中的某一项，关闭这个面板
                        if (bottomSheetLayout.isSheetShowing()) {
                            bottomSheetLayout.dismissSheet();
                        }
                        //假设用户选择以另外一种方式打开
                        //则重新切换样式
                        if (item.getItemId() == R.id.send_picture) {
                            sendPicture();
                        }else if (item.getItemId() == R.id.send_file) {
                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("*/*");//无类型限制
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            startActivityForResult(intent, 1);
                        }
                        return true;
                    }
                });
        //此处构造展示出来的面板选项条目的数据源
        //从menu菜单的数据创建
        //类似适配器
        menuSheetView.inflateMenu(R.menu.bottom_function);

        //不要忘记这段代码，否则显示不出来
        bottomSheetLayout.showWithSheetView(menuSheetView);
    }

    private void sendPicture(){
        try {
            PictureSelector.create(ChatActivity.this)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.createGlideEngine())
                .isWeChatStyle(true)
                .forResult(new OnResultCallbackListener<LocalMedia>() {
                    @Override
                    public void onResult(List<LocalMedia> result) {
                        // 结果回调,这里写发送图片给好友的方法
                        //1.获取图片
                        for(int i=0;i<result.size();i++){
                            String realPath=result.get(i).getRealPath();
                            File file = new File(realPath);
                            //2.将图片转为字节数据，发送给好友
                            ChatBean chatBean = new ChatBean();
                            chatBean.setMessage(result.get(0).getRealPath());
                            chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
                            chatBean.setMessageType(chatBean.PIC);

                            chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
                            //更新聊天媒体信息
                            String name[]=result.get(0).getRealPath().split("/");
                            LocalMedia temp=new LocalMedia(result.get(0).getRealPath(),0, PictureMimeType.ofImage(),name[name.length-1]);
                            localMediaList.add(temp);
                            chatBean.setMedia_position(localMediaList.size()-1);

                            adapter.notifyDataSetChanged();    //更新ListView列表
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                            helper.insert(sessionId, sessionType,loginBean.getEmail(),realPath,ChatBean.PIC,df.format(new Date()));//将该信息插入到本地数据库

                            fileManager.uploadFile(file,file.getName(),null,sessionId,sessionType,loginBean.getEmail(),ChatBean.PIC,wsManager);
                        }
                        //3.适配器更新视图
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancel() {
                        // 取消
                    }
                });

        } finally {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path = "";

        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();

                if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                    path = uri.getPath();
                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                    path = getPath(this, uri);
                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                    //1.获取文件
                    File file = new File(path);

                    ChatBean chatBean = new ChatBean();
                    chatBean.setMessage(path);
                    chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
                    chatBean.setMessageType(chatBean.FILE);
                    chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
                    adapter.notifyDataSetChanged();    //更新ListView列表
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    helper.insert(sessionId,ConversationBean.PEOPLE,loginBean.getEmail(),path,ChatBean.FILE,df.format(new Date()));//将该信息插入到本地数据库
                    //这里要由发送文本信息改为上传文件
                    fileManager.uploadFile(file,file.getName(),null,sessionId,sessionType,loginBean.getEmail(),ChatBean.FILE,wsManager);
                    //3.适配器更新视图
                    adapter.notifyDataSetChanged();
                }
            }
        }catch (NullPointerException e){
            Toast.makeText(this, "不准发送空文件！", Toast.LENGTH_SHORT).show();
        }

    }
}
