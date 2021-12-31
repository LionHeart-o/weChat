package com.example.wechat.javaBean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupBean implements Serializable {
    public static final int ADMIN=1;
    public static final int MEMBER=2;

    private Long groupId;
    private String groupCover;
    private String groupName;
    private String groupAdmin;
    private Date createTime;
    private List<GroupMember> groupMembers;
    private Map<String,Integer> memberIndex=new ConcurrentHashMap<>();


    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupAdmin() {
        return groupAdmin;
    }

    public void setGroupAdmin(String groupAdmin) {
        this.groupAdmin = groupAdmin;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<GroupMember> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(List<GroupMember> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public String getGroupCover() {
        return groupCover;
    }

    public void setGroupCover(String groupCover) {
        this.groupCover = groupCover;
    }

    public Map<String, Integer> getMemberIndex() {
        return memberIndex;
    }

    public void setMemberIndex(Map<String, Integer> memberIndex) {
        this.memberIndex = memberIndex;
    }
}
