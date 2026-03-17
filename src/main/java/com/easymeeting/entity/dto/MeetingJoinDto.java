package com.easymeeting.entity.dto;

import com.easymeeting.entity.po.MeetingMember;

import java.util.List;

public class MeetingJoinDto {
    private MeetingMemberDto newMember;
    private List<MeetingMemberDto> meetingMemberList;
    public MeetingMemberDto getNewMember() {
        return newMember;
    }

    public void setNewMember(MeetingMemberDto newMember) {
        this.newMember = newMember;
    }

    public List<MeetingMemberDto> getMeetingMemberList() {
        return meetingMemberList;
    }

    public void setMeetingMemberList(List<MeetingMemberDto> meetingMemberList) {
        this.meetingMemberList = meetingMemberList;
    }

}
