package com.example.wechat.Utils;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.wechat.Activity.ChatActivity;
import com.example.wechat.Activity.ui.home.HomeFragment;
import com.example.wechat.Adapter.ChatAdapter;
import com.example.wechat.Adapter.ConversationAdapter;
import com.example.wechat.R;
import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.ConversationBean;
import com.example.wechat.javaBean.GroupBean;
import com.example.wechat.javaBean.LoginBean;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class WsManager {

    private static final String TAG="WsManager";

    private static WebSocket ws;
    private WebSocket freeWs;
    private FileManager fileManager;

    private LoginBean loginBean;

    private List<ConversationBean> conversationBeanList;


    private List<ChatBean> chatBeanList;
    private List<LocalMedia> localMediaList;

    private ChatBean chatBean;

    private SQLiteHelper helper;
    private Context context;

    //接受两个适配器，用于更新列表
    private ConversationAdapter conversationAdapter;
    private ChatAdapter chatAdapter;

    private HomeFragment homeFragment;

    //获取当前时间
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private WsManager(){
        this.fileManager=FileManager.getInstance();
        this.loginBean=LoginBean.getInstance();
        this.conversationBeanList=LoginBean.getInstance().getConversations();
    }

    private static WsManager singleton;

    public static synchronized WsManager getInstance() {
        if (singleton == null) {
            singleton = new WsManager();
        }
        return singleton;
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
            Log.d(TAG, "onConnected: 连接成功");
            freeWs=websocket;
            JSONObject json = new JSONObject();
            json.put("messageType", 0);
            json.put("email", loginBean.getEmail());
            json.put("username", loginBean.getUsername());
            json.put("head", loginBean.getHead());
            websocket.sendText(json.toString());
        }

        @Override
        public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
            Log.d(TAG, "onConnectError: 连接失败");
        }

        @Override
        public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame,
                                   WebSocketFrame clientCloseFrame,
                                   boolean closedByServer) throws Exception {
            Log.d(TAG, "onDisconnected: 断开连接");

        }

        @Override
        public void onTextMessage(WebSocket websocket, String text) throws Exception {
            String externalPath=context.getExternalFilesDir(null).getPath();
            JSONObject json = new JSONObject(text);
            chatBean = new ChatBean();
            chatBean.setState(chatBean.RECEIVE);
            chatBean.setEmail(json.getString("sendUser"));

            Log.d(TAG, "onTextMessage: 收到消息:" + text);

            int sessionType=json.getInt("sessionType");
            String head;
            String sessionId;

            if(sessionType==ConversationBean.PEOPLE){
                sessionId=json.getString("sendUser");
                head= loginBean.getContacts().get(loginBean.getContactIndex().get(sessionId)).getHead();
            }else{
                sessionId=json.getString("sessionId");
                GroupBean groupBean=loginBean.getGroups().get(loginBean.getGroupIndex().get(sessionId));
                head= groupBean.getGroupMembers().get(groupBean.getMemberIndex().get(json.getString("sendUser"))).getHead();
            }


            //处理文字消息
            if(json.getInt("messageType")==ChatBean.TEXT){
                Log.d(TAG,"确定是文本消息");
                chatBean.setMessageType(chatBean.TEXT);
                chatBean.setMessage(json.getString("message"));
                helper.insert(sessionId,ConversationBean.PEOPLE,json.getString("sendUser"),json.getString("message"),ChatBean.TEXT,df.format(new Date()));
                generateNotification(
                        sessionId,
                        json.getString("message"),
                        sessionType
                );
                for(int i=0;i<conversationBeanList.size();i++){
                    if(conversationBeanList.get(i).getAccountNumber().equals(sessionId)){
                        conversationBeanList.get(i).setLast_message(json.getString("message"));
                        conversationBeanList.get(i).setLast_time(df.format(new Date()));
                    }
                }

            }else if(json.getInt("messageType")==ChatBean.PIC){
                Log.d(TAG,"确定是图片消息");
                chatBean.setMessageType(chatBean.PIC);
                chatBean.setMessage(externalPath+"/"+json.getString("fileName"));

                helper.insert(sessionId,sessionType,json.getString("sendUser"),externalPath+"/"+json.getString("fileName"),ChatBean.PIC,df.format(new Date()));

                downLoadFile(json.getString("message"),externalPath,json.getString("fileName"),sessionId,"[图片]",sessionType);
                //将媒体信息更新
                LocalMedia temp=new LocalMedia(externalPath+"/"+json.getString("fileName"),0, PictureMimeType.ofImage(),json.getString("fileName"));
                localMediaList.add(temp);
                chatBean.setMedia_position(localMediaList.size()-1);
            }else if(json.getInt("messageType")==ChatBean.FILE){
                Log.d(TAG,"确定是文件消息");
                chatBean.setMessageType(chatBean.FILE);
                chatBean.setMessage(externalPath+"/"+json.getString("fileName"));
                helper.insert(sessionId,sessionType,json.getString("sendUser"),externalPath+"/"+json.getString("fileName"),ChatBean.FILE,df.format(new Date()));

                downLoadFile(json.getString("message"),externalPath,json.getString("fileName"),sessionId,"[文件]",sessionType);
                //下载完成
            }

            chatBean.setHead(head);
            chatBeanList.add(chatBean);
            conversationAdapter.notifyDataSetChanged();
            chatAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
            super.onBinaryMessage(websocket, binary);
        }
    }

    public void sendInfo(String sessionId,int sessionType,String sendUser,String message,int messageType,String... fileName) throws JSONException {
        conversationBeanList=LoginBean.getInstance().getConversations();
        Log.d(TAG,"正在发送消息");
        JSONObject json = new JSONObject();

        json.put("sessionId",sessionId);
        json.put("sessionType",sessionType);
        json.put("sendUser",sendUser);
        json.put("message", message);
        json.put("messageType", messageType);
        //设置日期格式

        if(messageType!=ChatBean.TEXT) {
            json.put("fileName", fileName[0]);
            Log.d(TAG,fileName[0]+"文件名");
        }
        freeWs.sendText(json.toString());


        //下面是添加联系人界面的最新消息
        if(conversationBeanList!=null){
            for(int i=0;i<conversationBeanList.size();i++){
                if(conversationBeanList.get(i).getAccountNumber().equals(sessionId)){
                    if(messageType==ChatBean.TEXT){
                        conversationBeanList.get(i).setLast_message(json.getString("message"));
                    }else if(messageType==ChatBean.FILE){
                        conversationBeanList.get(i).setLast_message("[图片]");
                    }
                    conversationBeanList.get(i).setLast_time(df.format(new Date()));
                    break;
                }
            }
        }
    }


    //get和set方法
    public ConversationAdapter getConversationAdapter() {
        return conversationAdapter;
    }

    public void setConversationAdapter(ConversationAdapter conversationAdapter) {
        this.conversationAdapter = conversationAdapter;
    }

    public ChatAdapter getChatAdapter() {
        return chatAdapter;
    }

    public void setChatAdapter(ChatAdapter chatAdapter) {
        this.chatAdapter = chatAdapter;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        helper = new SQLiteHelper(context);
        SQLiteDatabase database = helper.getReadableDatabase();
        helper.onCreate(database);
    }

    public List<ChatBean> getChatBeanList() {
        return chatBeanList;
    }

    public void setChatBeanList(List<ChatBean> chatBeanList) {
        this.chatBeanList = chatBeanList;
    }

    public void setLocalMediaList(List<LocalMedia> localMediaList) {
        this.localMediaList = localMediaList;
    }

    public void generateNotification(String sessionId,String message,int sessionType){
        conversationBeanList=LoginBean.getInstance().getConversations();

        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        String title;


        if(sessionType==ConversationBean.PEOPLE){
            ContactBean contactBean=loginBean.getContacts().get(loginBean.getContactIndex().get(sessionId));
            intent.putExtra("sessionId",contactBean.getEmail());
            intent.putExtra("sessionType",ConversationBean.PEOPLE);
            intent.putExtra("contact",contactBean);
            title=contactBean.getUsername();
        }else {
            GroupBean groupBean=loginBean.getGroups().get(loginBean.getGroupIndex().get(sessionId));
            intent.putExtra("sessionId",groupBean.getGroupId());
            intent.putExtra("sessionType",ConversationBean.GROUP);
            intent.putExtra("group",groupBean);
            title=groupBean.getGroupName();
        }


        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "8897")
                .setSmallIcon(R.mipmap.robot_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("8897", "channel", importance);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(8897,builder.build());
    }





    public void downLoadFile(String url,String destFileDir,String destFileName,String sessionId,String message,int sessionType) {
        conversationBeanList=LoginBean.getInstance().getConversations();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;

                //储存下载文件的目录
                File dir = new File(destFileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, destFileName);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        //下载中更新进度条

                    }
                    fos.flush();

                    for(int i=0;i<conversationBeanList.size();i++){
                        if(conversationBeanList.get(i).getAccountNumber().equals(sessionId)){
                            conversationBeanList.get(i).setLast_message(message);
                            generateNotification(sessionId,message,sessionType);
                            conversationBeanList.get(i).setLast_time(df.format(new Date()));
                        }
                    }

                    chatBeanList.add(chatBean);
                    conversationAdapter.notifyDataSetChanged();
                    chatAdapter.notifyDataSetChanged();
                } catch (Exception e) {

                }finally {

                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {

                    }

                }


            }
        });
    }
}