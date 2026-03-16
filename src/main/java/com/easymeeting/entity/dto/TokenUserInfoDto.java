package com.easymeeting.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenUserInfoDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private String token;
    private String userId;
    private String nickName;
    private Integer sex;
    private String currentMeeting;
    private String currentNickName;

    private String myMeetingNo;
    private Boolean admin;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getCurrentMeeting() {
        return currentMeeting;
    }

    public void setCurrentMeeting(String currentMeeting) {
        this.currentMeeting = currentMeeting;
    }

    public String getCurrentNickName() {
        return currentNickName;
    }

    public void setCurrentNickName(String currentNickName) {
        this.currentNickName = currentNickName;
    }

    public String getMyMeetingNo() {
        return myMeetingNo;
    }

    public void setMyMeetingNo(String myMeetingNo) {
        this.myMeetingNo = myMeetingNo;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
}
