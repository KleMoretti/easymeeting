package com.easymeeting.entity.dto;

import java.util.List;

public class MeetingExitDto {
    private String exitUserId;
    private List<MeetingMemberDto> meetingMemberList;
    private Integer exitStatus;

    public String getExitUserId() {
        return exitUserId;
    }

    public void setExitUserId(String exitUserId) {
        this.exitUserId = exitUserId;
    }

    public List<MeetingMemberDto> getMeetingMemberList() {
        return meetingMemberList;
    }

    public void setMeetingMemberList(List<MeetingMemberDto> meetingMemberList) {
        this.meetingMemberList = meetingMemberList;
    }

    public Integer getExitStatus() {
        return exitStatus;
    }

    public void setExitStatus(Integer exitStatus) {
        this.exitStatus = exitStatus;
    }
}
