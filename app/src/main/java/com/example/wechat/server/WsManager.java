package com.example.wechat.server;
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
import com.example.wechat.Adapter.ContactAdapter;
import com.example.wechat.R;
import com.example.wechat.SQLite.SQLiteHelper;
import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;
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

    private static WebSocket ws;
    private WebSocket freeWs;
    private FileManager fileManager;
    private LoginBean loginBean;
    private List<ContactBean> contactBeanList;
    private List<ChatBean> chatBeanList;

    private ChatBean chatBean;
    private String contact_head;
    private SQLiteHelper helper;
    private Context context;

    //接受两个适配器，用于更新列表
    private ContactAdapter contactAdapter;
    private ChatAdapter chatAdapter;

    private HomeFragment homeFragment;

    //获取当前时间
    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private WsManager(){
        this.fileManager=FileManager.getInstance();
        this.loginBean=LoginBean.getInstance();
        this.contactBeanList=ContactBean.getInstance();
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
            Log.d("nmsl", "onConnected: 连接成功");
            freeWs=websocket;
            JSONObject json = new JSONObject();
            json.put("type", 0);
            json.put("email", loginBean.getEmail());
            json.put("username", loginBean.getMyName());
            json.put("head", loginBean.getHead());
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
            String externalPath=context.getExternalFilesDir(null).getPath();
            JSONObject json = new JSONObject(text);
            chatBean = new ChatBean();
            chatBean.setState(chatBean.RECEIVE);
            chatBean.setHeadDetail(contact_head);
            //处理文字消息
            if(json.getString("type").equals("1")){
                Log.d("nmsl","确定是文本消息");
                Log.d("nmsl", "onTextMessage: 收到消息:" + text);
                chatBean.setMessageType(chatBean.TEXT);
                chatBean.setMessage(json.getString("text"));
                helper.insert(json.getString("sendUser"),json.getString("receiveUser"),json.getString("text"),3,df.format(new Date()));
                //将联系人的最新消息更新
                for(int i=0;i<contactBeanList.size();i++){
                    if(contactBeanList.get(i).getContact_email().equals(json.getString("sendUser"))){
                        contactBeanList.get(i).setContact_last_message(json.getString("text"));

                        generateNotification(contactBeanList.get(i).getContact_name(),json.getString("text"),contactBeanList.get(i).getContact_email(),contactBeanList.get(i).getContact_head());
                        contactBeanList.get(i).setLast_time(df.format(new Date()));
                    }
                }

            }else if(json.getString("type").equals("2")){
                Log.d("nmsl","确定是图片消息");
                chatBean.setMessageType(chatBean.PIC);
                chatBean.setMessage(externalPath+"/"+json.getString("fileName"));
                helper.insert(json.getString("sendUser"),json.getString("receiveUser"),externalPath+"/"+json.getString("fileName"),4,df.format(new Date()));
                toFile(json.getString("text"),externalPath+"/"+json.getString("fileName"));
                //将联系人的最新消息更新
                for(int i=0;i<contactBeanList.size();i++){
                    if(contactBeanList.get(i).getContact_email().equals(json.getString("sendUser"))){
                        contactBeanList.get(i).setContact_last_message("[图片]");
                        generateNotification(contactBeanList.get(i).getContact_name(),"[图片]",contactBeanList.get(i).getContact_email(),contactBeanList.get(i).getContact_head());
                        contactBeanList.get(i).setLast_time(df.format(new Date()));
                    }
                }
            }else if(json.getString("type").equals("3")){
                Log.d("nmsl","确定是文件消息");
                chatBean.setMessageType(chatBean.FILE);
                chatBean.setMessage(externalPath+"/"+json.getString("fileName"));
                helper.insert(json.getString("sendUser"),json.getString("receiveUser"),externalPath+"/"+json.getString("fileName"),5,df.format(new Date()));
                downLoadFile(json.getString("text"),externalPath,json.getString("fileName"),json.getString("sendUser"));
                return ;
                //下载完成
            }
            chatBeanList.add(chatBean);
            contactAdapter.notifyDataSetChanged();
            chatAdapter.notifyDataSetChanged();
        }

        @Override
        public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
            super.onBinaryMessage(websocket, binary);
        }

        public void toFile(String base64Code,String targetPath) throws Exception {
            byte[] buffer = Base64.decode(base64Code,1);
            FileOutputStream out = new FileOutputStream(targetPath);
            out.write(buffer);
            out.close();
        }
    }

    public void sendInfo(String sendUser,String receiveUser,String text,int type,String... fileName) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("sendUser", sendUser);
        json.put("receiveUser", receiveUser);
        json.put("text", text);
        if(type!=1) {
            json.put("fileName", fileName[0]);
            Log.d("nmsl",fileName[0]+"文件名");
        }
        freeWs.sendText(json.toString());


        for(int i=0;i<contactBeanList.size();i++){
            if(contactBeanList.get(i).getContact_email().equals(receiveUser)){
                if(type==3){
                    contactBeanList.get(i).setContact_last_message(json.getString("text"));
                }else if(type==4){
                    contactBeanList.get(i).setContact_last_message("[图片]");
                }
                contactBeanList.get(i).setLast_time(df.format(new Date()));
                break;
            }
        }
    }


    //get和set方法
    public ContactAdapter getContactAdapter() {
        return contactAdapter;
    }

    public void setContactAdapter(ContactAdapter contactAdapter) {
        this.contactAdapter = contactAdapter;
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

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public void setHomeFragment(HomeFragment homeFragment) {
        this.homeFragment = homeFragment;
    }

    public void generateNotification(String name, String message, String email, String head){
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("contact_email", email);
        intent.putExtra("my_email", loginBean.getEmail());
        intent.putExtra("my_name", loginBean.getMyName());
        intent.putExtra("contact_head",head);
        intent.putExtra("myHead", loginBean.getHead());



        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "8897")
                .setSmallIcon(R.mipmap.robot_icon)
                .setContentTitle(name)
                .setContentText(message)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            Log.d("nmsl","大于");
            NotificationChannel channel = new NotificationChannel("8897", "channel", importance);
            notificationManager.createNotificationChannel(channel);
        }
        notificationManager.notify(8897,builder.build());
    }





    public void downLoadFile(String url,String destFileDir,String destFileName,String sendUser) {

        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();

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
                    for(int i=0;i<contactBeanList.size();i++){
                        if(contactBeanList.get(i).getContact_email().equals(sendUser)){
                            contactBeanList.get(i).setContact_last_message("[文件]");
                            generateNotification(contactBeanList.get(i).getContact_name(),"[文件]",contactBeanList.get(i).getContact_email(),contactBeanList.get(i).getContact_head());
                            contactBeanList.get(i).setLast_time(df.format(new Date()));
                        }
                    }
                    chatBeanList.add(chatBean);
                    contactAdapter.notifyDataSetChanged();
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