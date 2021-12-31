package com.example.wechat.javaBean;

import android.app.Notification;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginBean implements Serializable {
    private String email;
    private String password;
    private String registerState;
    private String loginState;
    private String username;
    private String head;
    private String cover;
    private	String signature;//用户个性签名
    private String residence;//用户居住地
    private String sex;//用户性别
    private Date birthday;//生日

    private List<ContactBean> contacts;
    private List<NotificationBean> notifications;
    private List<GroupBean> groups;

    //与后端不一致，用于整合联系人与群聊，合并显示
    private List<ConversationBean> conversations;


    private Map<String,Integer> contactIndex=new ConcurrentHashMap<>();
    private Map<String,Integer> groupIndex=new ConcurrentHashMap<>();

    private static LoginBean singleton;

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getRegisterState() {
        return registerState;
    }

    public void setRegisterState(String registerState) {
        this.registerState = registerState;
    }

    public String getLoginState() {
        return loginState;
    }

    public void setLoginState(String loginState) {
        this.loginState = loginState;
    }

    public List<ContactBean> getContacts() {
        return contacts;
    }

    public void setContacts(List<ContactBean> contacts) {
        this.contacts = contacts;
    }

    public List<NotificationBean> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationBean> notifications) {
        this.notifications = notifications;
    }

    public List<GroupBean> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupBean> groups) {
        this.groups = groups;
    }

    public Map<String, Integer> getContactIndex() {
        return contactIndex;
    }

    public void setContactIndex(Map<String, Integer> contactIndex) {
        this.contactIndex = contactIndex;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private boolean isLogin=false;

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isLogin() {
        return isLogin;
    }

    public void setLogin(boolean login) {
        isLogin = login;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public static synchronized LoginBean getInstance() {
        if (singleton == null) {
            singleton = new LoginBean();
        }
        return singleton;
    }

    public static synchronized void setInstance(LoginBean loginBean) {
        singleton=loginBean;
    }
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public List<ConversationBean> getConversations() {
        return conversations;
    }

    public void setConversations(List<ConversationBean> conversations) {
        this.conversations = conversations;
    }

    public Map<String, Integer> getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(Map<String, Integer> groupIndex) {
        this.groupIndex = groupIndex;
    }
}
