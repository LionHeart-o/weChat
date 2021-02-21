package com.example.wechat.Activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.wechat.Adapter.ChatAdapter;
import com.example.wechat.R;
import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.javaBean.ChatBean;

import com.example.wechat.websocket.WsManager;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ChatActivity extends AppCompatActivity {

    private ListView listView;
    private TextView user_name;
    private ChatAdapter adpter;
    private List<ChatBean> chatBeanList; //存放所有聊天数据的集合
    private EditText et_send_msg;
    private Button btn_send;

    private String sendMsg;    //发送的信息

    private SQLiteHelper helper;
    private ChatBean chatBean;

    private String sendUser;
    private String receiveUser;
    private String message;

    private String ownEmail;
    private String ownHead;
    private String contactHead;
    private String contactEmail;


    private ChatBean receiveBean;
    private static WebSocket ws;
    private WebSocket freeWs;

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
        contactHead=intent.getStringExtra("contact_head");
        contactEmail=intent.getStringExtra("contact_email");

        Cursor cursor = helper.query(ownEmail,contactEmail);
        if (cursor.moveToFirst()) {
            do {
                sendUser = cursor.getString(cursor.getColumnIndex("sendUser"));
                receiveUser = cursor.getString(cursor.getColumnIndex("receiveUser"));
                message = cursor.getString(cursor.getColumnIndex("message"));

                chatBean = new ChatBean();
                //从数据库获取到用户名、密码
                if (sendUser.equals(ownEmail)) {
                    chatBean.setState(chatBean.SEND); //SEND表示自己发送的信息
                    chatBean.setHeadDetail(ownHead);
                } else if (receiveUser.equals(ownEmail)) {
                    chatBean.setState(chatBean.RECEIVE);
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
        connect();

        initView(); //初始化界面控件
        listView.setSelection(ListView.FOCUS_DOWN);
    }
    public void initView() {

        //获取页面组件
        listView = (ListView) findViewById(R.id.list);
        et_send_msg = (EditText) findViewById(R.id.et_send_msg);
        btn_send = (Button) findViewById(R.id.btn_send);
        user_name=(TextView)findViewById(R.id.user_name);
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

    }
    private void sendData() throws JSONException {//每一次发送信息都会向机器人请求一个回复
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
        chatBeanList.add(chatBean);        //将发送的信息添加到chatBeanList集合中
        adpter.notifyDataSetChanged();    //更新ListView列表
        helper.insert(ownEmail,contactEmail,sendMsg);//将该信息插入到本地数据库

        sendInfo(ownEmail,contactEmail,sendMsg);

    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        totalThread.stop();
        finish();//一点击返回就立马销毁当前页面
        ws.disconnect();
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 连接方法
     */
    public void connect() {
        //WEB_SOCKET_API 是连接的url地址，
        // CONNECT_TIMEOUT是连接的超时时间 这里是 5秒
        try {
            ws = new WebSocketFactory().createSocket("ws://192.168.1.2:3000", 5000)
                    //设置帧队列最大值为5
                    .setFrameQueueSize(5)
                    //设置不允许服务端关闭连接却未发送关闭帧
                    .setMissingCloseFrameAllowed(false)
                    //添加回调监听
                    .addListener(new WsListener())
                    //异步连接
                    .connectAsynchronously();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * websocket回调事件
     */
    private class WsListener extends WebSocketAdapter {
        @Override
        public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
            Log.d("nmsl", "onConnected: 连接成功");
            freeWs=websocket;
            JSONObject json = new JSONObject();
            json.put("type", 0);
            json.put("email", "1171654515@qq.com");
            json.put("username", "刘大西");
            json.put("head", "https://t9.baidu.com/it/u=3703311756,2653434749&fm=193&app=53&size=w414&n=0&g=0n&f=jpeg?sec=1610987543&t=6686bffd39d94de6a2480cd5ca4e1749");
            websocket.sendText(json.toString());
        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
            Log.d("nmsl", "onConnectError: 连接失败");
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                   WebSocketFrame clientCloseFrame,
                                   boolean closedByServer) throws Exception {
            Log.d("nmsl", "onDisconnected: 断开连接");

        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            Log.d("nmsl", "onTextMessage: 收到消息:" + text);
            JSONObject json = new JSONObject(text);
            chatBean = new ChatBean();
            chatBean.setState(chatBean.RECEIVE);
            chatBean.setHeadDetail(contactHead);
            chatBean.setMessage(json.getString("message"));
            chatBeanList.add(chatBean);
            helper.insert(json.getString("sendUser"),json.getString("receiveUser"),json.getString("message"));
            Log.d("nmsl", "4" );
            adpter.notifyDataSetChanged();    //更新ListView列表
        }
    }

    public void sendInfo(String sendUser,String receiveUser,String text) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", 1);
        json.put("sendUser", sendUser);
        json.put("receiveUser", receiveUser);
        json.put("text", text);
        freeWs.sendText(json.toString());
    }
}
