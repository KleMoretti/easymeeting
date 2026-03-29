package com.easymeeting.entity.dto;

public class MeetingInviteDto {
    private String meetingId;
    private String meetingNo;
    private String meetingName;
    private String inviteUserId;
    private String inviteMessage;

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingNo() {
        return meetingNo;
    }

    public void setMeetingNo(String meetingNo) {
        this.meetingNo = meetingNo;
    }

    public String getMeetingName() {
        return meetingName;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public String getInviteUserId() {
        return inviteUserId;
    }

    public void setInviteUserId(String inviteUserId) {
        this.inviteUserId = inviteUserId;
    }

    public String getInviteMessage() {
        return inviteMessage;
    }

    public void setInviteMessage(String inviteMessage) {
        this.inviteMessage = inviteMessage;
    }
}
