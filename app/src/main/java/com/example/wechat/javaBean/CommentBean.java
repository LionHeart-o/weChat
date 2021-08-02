package com.example.wechat.javaBean;

import java.io.Serializable;
import java.sql.Timestamp;

public class CommentBean implements Serializable {
    private String comment_name;
    private String comment_email;
    private String reply_name;
    private String reply_email;
    private String comment_text;
    private Timestamp createTime;


    public String getComment_name() {
        return comment_name;
    }

    public void setComment_name(String comment_name) {
        this.comment_name = comment_name;
    }

    public String getComment_email() {
        return comment_email;
    }

    public void setComment_email(String comment_email) {
        this.comment_email = comment_email;
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

    public String getComment_text() {
        return comment_text;
    }

    public void setComment_text(String comment_text) {
        this.comment_text = comment_text;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CommentBean{" +
                "comment_name='" + comment_name + '\'' +
                ", comment_email='" + comment_email + '\'' +
                ", reply_name='" + reply_name + '\'' +
                ", reply_email='" + reply_email + '\'' +
                ", comment_text='" + comment_text + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
