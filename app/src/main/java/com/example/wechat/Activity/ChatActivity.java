package com.example.wechat.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.wechat.Adapter.ChatAdapter;
import com.example.wechat.R;
import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.javaBean.ChatBean;

import com.example.wechat.server.FileManager;
import com.example.wechat.upload.GlideEngine;
import com.example.wechat.server.WsManager;
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

import static com.yalantis.ucrop.util.FileUtils.getPath;


public class ChatActivity extends AppCompatActivity {

    private ListView listView;
    private TextView user_name;
    private ChatAdapter adpter;
    private List<ChatBean> chatBeanList; //存放所有聊天数据的集合
    private EditText et_send_msg;
    private Button btn_send;
    private ImageView send_pic;


    private String sendMsg;    //发送的信息

    private SQLiteHelper helper;
    private ChatBean chatBean;

    private String sendUser;
    private String receiveUser;
    private String message;
    private int sqlMessageType;

    private String ownEmail;
    private String ownHead;
    private String ownName;
    private String contactHead;
    private String contactEmail;


    private ChatBean receiveBean;

    private WsManager wsManager;
    private FileManager fileManager;


    protected BottomSheetLayout bottomSheetLayout;//底部功能菜单

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_chat);

        //创建聊天数据类，每一个ChatBean封装一条消息
        chatBeanList = new ArrayList<ChatBean>();


        //获取本地聊天信息
        helper = new SQLiteHelper(this);
        SQLiteDatabase database = helper.getReadableDatabase();
        helper.onCreate(database);

        Intent intent = getIntent();
        ownEmail=intent.getStringExtra("my_email");
        ownHead=intent.getStringExtra("myHead");
        ownName=intent.getStringExtra("my_name");
        contactHead=intent.getStringExtra("contact_head");
        contactEmail=intent.getStringExtra("contact_email");

        Log.d("nmsl",ownEmail+ownHead+ownName);
        Cursor cursor = helper.query(ownEmail,contactEmail);
        if (cursor.moveToFirst()) {
            do {
                sendUser = cursor.getString(cursor.getColumnIndex("sendUser"));
                receiveUser = cursor.getString(cursor.getColumnIndex("receiveUser"));
                message = cursor.getString(cursor.getColumnIndex("message"));
                sqlMessageType = cursor.getInt(cursor.getColumnIndex("type"));

                chatBean = new ChatBean();
                //从数据库获取到用户名、密码
                if (sendUser.equals(ownEmail)) {
                    chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
                    chatBean.setMessageType(sqlMessageType);//表示数据类型，3代表文字，4代表图片
                    chatBean.setHeadDetail(ownHead);
                } else if (receiveUser.equals(ownEmail)) {
                    chatBean.setState(chatBean.RECEIVE);
                    chatBean.setMessageType(sqlMessageType);
                    chatBean.setHeadDetail(contactHead);
                }
                chatBean.setMessage(message);
                chatBeanList.add(chatBean);
            } while (cursor.moveToNext());
            //关闭游标
            cursor.close();
        }


        //建立websocket连接
        receiveBean=new ChatBean();
        receiveBean.setState(chatBean.RECEIVE);
        receiveBean.setHeadDetail(contactHead);

        wsManager=WsManager.getInstance();
        wsManager.setChatAdapter(adpter);
        wsManager.setChatBeanList(chatBeanList);
        wsManager.connect();

        fileManager = FileManager.getInstance();

        initView(); //初始化界面控件
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
        user_name=(TextView)findViewById(R.id.user_name);

        bottomSheetLayout = (BottomSheetLayout) findViewById(R.id.bottomsheet);
        bottomSheetLayout.setPeekOnDismiss(true);


        user_name.setText((String)getIntent().getSerializableExtra("name"));

        adpter = new ChatAdapter(chatBeanList, this);
        listView.setAdapter(adpter);

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
        et_send_msg.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() ==
                        KeyEvent.ACTION_DOWN) {
                    try {
                        sendData();//点击Enter键也可以发送信息
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        send_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                showMenuSheet(MenuSheetView.MenuType.GRID);
            }
        });

    }
    private void sendData() throws JSONException {//点击发送后触发的事件，主要是判断信息合法性，确认信息合法之后调用websocket的信息发送方法
        Intent intent = getIntent();
        sendMsg = et_send_msg.getText().toString(); //获取你输入的信息
        if (TextUtils.isEmpty(sendMsg)) {             //判断是否为空
            Toast.makeText(this, "您还未输任何信息哦", Toast.LENGTH_LONG).show();
            return;
        }
        et_send_msg.setText("");
        //替换空格和换行
        ChatBean chatBean = new ChatBean();
        chatBean.setMessage(sendMsg);
        chatBean.setHeadDetail(intent.getStringExtra("myHead"));
        chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
        chatBean.setMessageType(chatBean.TEXT);
        chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
        adpter.notifyDataSetChanged();    //更新ListView列表
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        helper.insert(ownEmail,contactEmail,sendMsg,3,df.format(new Date()));//将该信息插入到本地数据库

        wsManager.sendInfo(ownEmail,contactEmail,sendMsg,1);//1代表发送的是文本消息

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        finish();//一点击返回就立马销毁当前页面
        return super.onKeyDown(keyCode, event);
    }


    /** * 删除方法 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理 * * @param directory */
    private static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            Log.i("nmsl",directory.getAbsolutePath());
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
    }

    /*public static byte[] fileToByteArray(String FilePath){
        //1.创建源与目的的
        File src=new File(FilePath);
        byte[] dest=null;//在字节数组输出的时候是不需要源的。


        //2.选择流，选择文件输入流
        InputStream is=null;//方便在finally中使用，设置为全局变量
        ByteArrayOutputStream os=null;//新增方法

        try {

            is=new FileInputStream(src);
            os=new ByteArrayOutputStream();
            //3.操作,读文件
            byte[] flush=new byte[1024*10];//10k，创建读取数据时的缓冲，每次读取的字节个数。
            int len=-1;//接受长度；
            while((len=is.read(flush))!=-1) {
                //表示当还没有到文件的末尾时
                //字符数组-->字符串，即是解码。
                os.write(flush,0,len);//将文件内容写出字节数组

            }
            os.flush();
            return os.toByteArray();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally {
            //4.释放资源
            try {
                if(is!=null) {//表示当文打开时，才需要通知操作系统关闭
                    is.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return null;
    }*/
    public static String encodeBase64File(String path) {
        File file = new File(path);
        FileInputStream inputFile = null;
        try {
            inputFile = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        byte[] buffer = new byte[(int) file.length()];
        try {
            inputFile.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputFile != null) {
                    inputFile.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Base64.encodeToString(buffer,1);
    }

    public static void toFile(String base64Code,String targetPath) throws Exception {
        byte[] buffer = Base64.decode(base64Code,1);
        FileOutputStream out = new FileOutputStream(targetPath);
        out.write(buffer);
        out.close();
    }

    private void showMenuSheet(final MenuSheetView.MenuType menuType) {
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
                                Log.d("nmsl",realPath+"虚无");
                                //2.将图片转为字节数据，发送给好友
                                try {
                                    ChatBean chatBean = new ChatBean();
                                    chatBean.setMessage(result.get(0).getRealPath());
                                    chatBean.setHeadDetail(ownHead);
                                    chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
                                    chatBean.setMessageType(chatBean.PIC);
                                    chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
                                    adpter.notifyDataSetChanged();    //更新ListView列表
                                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    helper.insert(ownEmail,contactEmail,realPath,4,df.format(new Date()));//将该信息插入到本地数据库
                                    wsManager.sendInfo(ownEmail,contactEmail,encodeBase64File(realPath),2,file.getName());//2代表的是发送的是非文本消息
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            //3.适配器更新视图
                            adpter.notifyDataSetChanged();
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
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())) {//使用第三方应用打开
                path = uri.getPath();
                Toast.makeText(this, path + "11111", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = getPath(this, uri);
                Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                // 结果回调,这里写发送文件给好友的方法
                //1.获取文件
                File file = new File(path);
                //2.将文件转为字节数据，发送给好友
                ChatBean chatBean = new ChatBean();
                chatBean.setMessage(path);
                chatBean.setHeadDetail(ownHead);
                chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
                chatBean.setMessageType(chatBean.FILE);
                chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
                adpter.notifyDataSetChanged();    //更新ListView列表
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Log.d("nmsl",path);
                helper.insert(ownEmail,contactEmail,path,5,df.format(new Date()));//将该信息插入到本地数据库，5表示文件消息，代表文件地址
                //这里要由发送文本信息改为上传文件
                fileManager.uploadFile(file,file.getName(),ownEmail,contactEmail,wsManager);

                //3.适配器更新视图
                adpter.notifyDataSetChanged();

            }
        }
    }
}
