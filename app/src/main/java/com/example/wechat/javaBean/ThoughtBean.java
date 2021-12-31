package com.example.wechat.javaBean;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ThoughtBean implements Serializable {
    private String id="";
    private String send_email="";
    private String message="";
    private Timestamp createTime;
    private List<String> pic;
    private List<CommentBean> comments;

    private String head="";
    private String name="";


    private static List<ThoughtBean> singleton;

    public static synchronized List<ThoughtBean> getInstance() {
        if (singleton == null) {
            singleton = new ArrayList<>();
        }
        return singleton;
    }
    public static synchronized void setInstance(List<ThoughtBean> thoughtBeanList) {
        singleton=thoughtBeanList;
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


    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getUsername() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<CommentBean> getComments() {
        return comments;
    }

    public void setComments(List<CommentBean> comments) {
        this.comments = comments;
    }
}
