package com.example.wechat.javaBean;

import java.io.Serializable;

public class LoginBean implements Serializable {
    private String email;
    private String password;
    private String head;
    private String myName;

    private static LoginBean singleton;

    private LoginBean () {

    }

    public String getMyName() {
        return myName;
    }

    public void setMyName(String myName) {
        this.myName = myName;
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




    public static synchronized LoginBean getInstance() {
        if (singleton == null) {
            singleton = new LoginBean();
        }
        return singleton;
    }

    public void setAllNull(){
        this.singleton=new LoginBean();
    }
}
