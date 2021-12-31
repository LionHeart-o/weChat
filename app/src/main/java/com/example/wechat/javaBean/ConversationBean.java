package com.example.wechat.javaBean;

import java.io.Serializable;

public class ConversationBean implements Serializable {
    public static final int PEOPLE=1;
    public static final int GROUP=2;

    private String conversation_cover;
    private String conversation_name;
    private String last_message;
    private String last_time;
    private int conversation_type;
    private String accountNumber;

    public String getConversation_cover() {
        return conversation_cover;
    }

    public void setConversation_cover(String conversation_cover) {
        this.conversation_cover = conversation_cover;
    }

    public String getConversation_name() {
        return conversation_name;
    }

    public void setConversation_name(String conversation_name) {
        this.conversation_name = conversation_name;
    }

    public String getLast_message() {
        return last_message;
    }

    public void setLast_message(String last_message) {
        this.last_message = last_message;
    }

    public String getLast_time() {
        return last_time;
    }

    public void setLast_time(String last_time) {
        this.last_time = last_time;
    }

    public int getConversation_type() {
        return conversation_type;
    }

    public void setConversation_type(int conversation_type) {
        this.conversation_type = conversation_type;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
}
