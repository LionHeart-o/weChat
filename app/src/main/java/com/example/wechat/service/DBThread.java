package com.example.wechat.service;

/*在客户端连接数据库的方法，已废弃。

import android.content.Context;


import com.example.wechat.javaBean.ChatBean;
import com.example.wechat.javaBean.ContactBean;
import com.example.wechat.javaBean.LoginBean;

import java.util.List;

import static com.example.wechat.methods.getMysql.addContact;
import static com.example.wechat.methods.getMysql.updateHead;

public class DBThread implements Runnable {




    private String email;
    private String contactEmail;
    private String message;
    private String password;
    private String action;
    private String my_head;
    private String contact_head;
    private String user_name;

    private Context context;
    private LoginBean loginBean;
    private List<ContactBean> pythonList;
    private List<ChatBean> chatBeanList;

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMy_head(String my_head) {
        this.my_head = my_head;
    }

    public void setContact_head(String contact_head) {
        this.contact_head = contact_head;
    }


    public List<ChatBean> getChatBeanList() {
        return chatBeanList;
    }

    public void setChatBeanList(List<ChatBean> chatBeanList) {
        this.chatBeanList = chatBeanList;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    public void setPythonList(List<ContactBean> pythonList) {
        this.pythonList = pythonList;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setContext(Context context) {
        this.context = context;
    }



    @Override
    public void run() {
        //if(action.equals("获取联系人")) getContacts(email,loginBean,pythonList,context);
        //else if(action.equals("获取聊天记录")) getMessages(email,contactEmail,chatBeanList,my_head,contact_head,context);
        //else if(action.equals("发送信息")) sendMessage(email,contactEmail,message);
        if(action.equals("添加好友")) addContact(email,contactEmail);
        //else if(action.equals("更改头像")) updateHead(email,my_head);
        //Log.i("???","线程里的"+loginBean.isLogin());
    }

}*/
