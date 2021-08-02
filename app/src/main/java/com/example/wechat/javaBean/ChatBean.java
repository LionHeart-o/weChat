package com.example.wechat.javaBean;

import java.io.Serializable;

public class ChatBean  implements Serializable {
    public static final int SEND = 1;     //发送消息
    public static final int RECEIVE = 2; //接收到的消息
    public static final int TEXT=3;
    public static final int PIC=4;
    public static final int FILE=5;

    private int state;       //消息的状态（是接收还是发送）
    private int messageType;
    private String message; //消息的内容，如果是文字消息就是纯文字意义，如果是图片消息就代表图片本地存储地址
    private String headDetail;

    public String getHeadDetail() {
        return headDetail;
    }

    public void setHeadDetail(String headDetail) {
        this.headDetail = headDetail;
    }

    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }


}
