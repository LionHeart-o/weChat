package com.example.wechat.websocket;

import android.util.Log;

import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.javaBean.ChatBean;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/*
* 此类是废类，仅做测试使用
* */
public class WsManager {

    private volatile static WsManager wsManger;
    private WebSocket ws;
    private WebSocket freeWs;
    private ChatBean chatBean;
    private List<ChatBean> chatBeanList;
    private SQLiteHelper helper;
    private String ownEmail;
    private String contactEmail;


    public WsManager(ChatBean bean,List<ChatBean> beanList,SQLiteHelper helper,String ownEmail,String contactEmail) {
        this.chatBean=bean;
        this.chatBeanList=beanList;
        this.helper=helper;
        this.ownEmail=ownEmail;
        this.contactEmail=contactEmail;
    }

    public static WsManager getWsManger(ChatBean bean,List<ChatBean> beanList,SQLiteHelper helper,String ownEmail,String contactEmail) {
        if (wsManger == null) {
            synchronized (WsManager.class) {
                if (wsManger == null) {
                    wsManger = new WsManager(bean,beanList,helper,ownEmail,contactEmail);
                }
            }
        }
        return wsManger;
    }

    public enum WsStatus {
        //连接成功
        CONNECT_SUCCESS,
        //连接失败
        CONNECT_FAIL,
        //正在连接
        CONNECTING;
    }
    /**
     * 连接方法
     */
    public void connect() {
        //WEB_SOCKET_API 是连接的url地址，
        // CONNECT_TIMEOUT是连接的超时时间 这里是 5秒
        try {
            ws = new WebSocketFactory().createSocket("ws://159.75.27.108:3000", 5000)
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
            chatBean.setMessage(json.getString("message"));
            chatBeanList.add(chatBean);
            helper.insert(json.getString("sendUser"),json.getString("receiveUser"),json.getString("message"));
            Log.d("nmsl", "4" );
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