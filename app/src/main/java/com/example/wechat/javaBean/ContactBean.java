package com.example.wechat.javaBean;

public class ContactBean{

    private String ourHead;
    private String head;
    private String my_name;
    private String contact_name;
    private String my_email;

    public String getMy_email() {
        return my_email;
    }

    public void setMy_email(String my_email) {
        this.my_email = my_email;
    }

    private String contact_email;


    public void setOurHead(String ourHead) {
        this.ourHead = ourHead;
    }

    public String getMy_name() {
        return my_name;
    }

    public void setMy_name(String my_name) {
        this.my_name = my_name;
    }

    public ContactBean(String ourHeadDetail){
        this.ourHead=ourHeadDetail;
    }

    public String getOurHead() {
        return ourHead;
    }

    public String getContact_email() {
        return contact_email;
    }

    public void setContact_email(String contact_email) {
        this.contact_email = contact_email;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

}
