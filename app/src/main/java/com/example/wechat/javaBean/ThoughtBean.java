package com.example.wechat.javaBean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ThoughtBean implements Serializable {
    private String id;
    private String send_email;
    private String message;
    private String head;
    private String name;
    private List<String> pic;
    private List<CommentBean> commentBeanList;
    private String timestamp;



    private static List<ThoughtBean> singleton;

    public static synchronized List<ThoughtBean> getInstance() {
        if (singleton == null) {
            singleton = new ArrayList<>();
        }
        return singleton;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSend_email() {
        return send_email;
    }

    public void setSend_email(String send_email) {
        this.send_email = send_email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getPic() {
        return pic;
    }

    public void setPic(List<String> pic) {
        this.pic = pic;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

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

    public List<CommentBean> getCommentBeanList() {
        return commentBeanList;
    }

    public void setCommentBeanList(List<CommentBean> commentBeanList) {
        this.commentBeanList = commentBeanList;
    }

    @Override
    public String toString() {
        return "ThoughtBean{" +
                "id='" + id + '\'' +
                ", send_email='" + send_email + '\'' +
                ", message='" + message + '\'' +
                ", head='" + head + '\'' +
                ", name='" + name + '\'' +
                ", pic=" + pic +
                ", commentBeanList=" + commentBeanList +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
