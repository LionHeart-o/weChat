package com.example.wechat.javaBean;

import java.io.Serializable;

public class NotificationBean implements Serializable {

    public static final int DEFAULT=0;
    public static final int ACCEPT=1;
    public static final int REFUSE=2;

    private String head;
    private String name;
    private String email;
    private int state;

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
