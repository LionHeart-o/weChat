package com.example.wechat.javaBean;

import java.io.Serializable;
import java.sql.Timestamp;

public class CommentBean implements Serializable {
    private String comment_name;
    private String send_email;
    private String reply_name;
    private String reply_email;
    private String message;
    private Timestamp createTime;


    public String getComment_name() {
        return comment_name;
    }

    public void setComment_name(String comment_name) {
        this.comment_name = comment_name;
    }

    public String getSend_email() {
        return send_email;
    }

    public void setSend_email(String send_email) {
        this.send_email = send_email;
    }

    public String getReply_name() {
        return reply_name;
    }

    public void setReply_name(String reply_name) {
        this.reply_name = reply_name;
    }

    public String getReply_email() {
        return reply_email;
    }

    public void setReply_email(String reply_email) {
        this.reply_email = reply_email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }


}
