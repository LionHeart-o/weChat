package com.example.wechat.javaBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactBean implements Serializable {

    private String contact_head;
    private String contact_name;
    private String contact_email;
    private String contact_last_message;
    private String last_time;
    private String signature;
    private static List<ContactBean> singleton;
    private static Map<String,Integer> contactIndex;

    public static synchronized List<ContactBean> getInstance() {
        if (singleton == null) {
            singleton = new ArrayList<>();
        }
        return singleton;
    }

    public static synchronized Map<String,Integer> getIndexInstance() {
        if (contactIndex == null) {
            contactIndex = new HashMap<>();
        }
        return contactIndex;
    }

    public void setAllNull(){
        this.singleton=new ArrayList<>();
    }

    public String getContact_last_message() {
        return contact_last_message;
    }

    public void setContact_last_message(String contact_last_message) {
        this.contact_last_message = contact_last_message;
    }

    public String getLast_time() {
        return last_time;
    }

    public void setLast_time(String last_time) {
        this.last_time = last_time;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getContact_head() {
        return contact_head;
    }

    public void setContact_head(String contact_head) {
        this.contact_head = contact_head;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_email() {
        return contact_email;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }
}
