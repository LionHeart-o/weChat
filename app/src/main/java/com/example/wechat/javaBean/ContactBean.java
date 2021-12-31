package com.example.wechat.javaBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactBean implements Serializable {

    //和后端一致的消息
    private String email;
    private String username;
    private String head;
    private String cover;//朋友圈封面
    private	String signature;//用户个性签名
    private String residence;//用户居住地
    private String sex;//用户性别
    private Date birthday;
    private List<ThoughtBean> thoughtBeanList=new ArrayList<>();

    //和后端不一致的消息，这些消息都可以从出生日推导
    private String birthdayString;//中文描述的出生日
    private int contact_age;//年龄
    private String contact_constellation;//星座

    //和后端不一致的消息，主要用于创建群聊
    private boolean isChecked;


    public String getBirthdayString() {
        return birthdayString;
    }

    public void setBirthdayString(String birthdayString) {
        this.birthdayString = birthdayString;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
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


    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public int getContact_age() {
        return contact_age;
    }

    public void setContact_age(int contact_age) {
        this.contact_age = contact_age;
    }

    public List<ThoughtBean> getThoughtBeanList() {
        return thoughtBeanList;
    }

    public void setThoughtBeanList(List<ThoughtBean> thoughtBeanList) {
        this.thoughtBeanList = thoughtBeanList;
    }

    public String getContact_constellation() {
        return contact_constellation;
    }

    public void setContact_constellation(String contact_constellation) {
        this.contact_constellation = contact_constellation;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }


}
