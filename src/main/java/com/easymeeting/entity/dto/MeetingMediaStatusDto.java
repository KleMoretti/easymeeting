package com.easymeeting.entity.dto;

import java.util.List;

public class MeetingMediaStatusDto {
    private String meetingId;
    private String userId;
    private Boolean openVideo;
    private Boolean openAudio;
    private List<MeetingMemberDto> meetingMemberList;

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getOpenVideo() {
        return openVideo;
    }

    public void setOpenVideo(Boolean openVideo) {
        this.openVideo = openVideo;
    }

    public Boolean getOpenAudio() {
        return openAudio;
    }

    public void setOpenAudio(Boolean openAudio) {
        this.openAudio = openAudio;
    }

    public List<MeetingMemberDto> getMeetingMemberList() {
        return meetingMemberList;
    }

    public void setMeetingMemberList(List<MeetingMemberDto> meetingMemberList) {
        this.meetingMemberList = meetingMemberList;
    }
}
