package com.easymeeting.entity.dto;

public class MeetingMemberDto {
    private Boolean openVideo;
    private String avatar;
    private String nickName;
    private Integer sex;
    private Long joinTime;
    private Integer status;
    private String userId;
    private Integer memberType;

    public Integer getMemberType() {
        return memberType;
    }

    public void setMemberType(Integer memberType) {
        this.memberType = memberType;
    }

    public Boolean getOpenVideo() {
        return openVideo;
    }

    public void setOpenVideo(Boolean openVideo) {
        this.openVideo = openVideo;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(Long joinTime) {
        this.joinTime = joinTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
