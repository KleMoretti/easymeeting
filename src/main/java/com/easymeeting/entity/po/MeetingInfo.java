package com.easymeeting.entity.po;

import com.easymeeting.enums.DateTimePatternEnum;
import com.easymeeting.utils.DataUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;
import java.io.Serializable;


/**
 * @Description:
 * @author: klein
 * @data: 2026/03/16
 */
public class MeetingInfo implements Serializable {
    /**
     *
     */
    private String meetingId;

    /**
     *
     */
    private String meetingNo;

    /**
     *
     */
    private String meetingName;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     *
     */
    private String createUserId;

    /**
     *
     */
    private Integer joinType;

    /**
     *
     */
    private String joinPassword;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     *
     */
    @JsonIgnore
    private Integer status;

    private Integer memberCount;

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingId() {
        return this.meetingId;
    }

    public void setMeetingNo(String meetingNo) {
        this.meetingNo = meetingNo;
    }

    public String getMeetingNo() {
        return this.meetingNo;
    }

    public void setMeetingName(String meetingName) {
        this.meetingName = meetingName;
    }

    public String getMeetingName() {
        return this.meetingName;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateUserId(String createUserId) {
        this.createUserId = createUserId;
    }

    public String getCreateUserId() {
        return this.createUserId;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public Integer getJoinType() {
        return this.joinType;
    }

    public void setJoinPassword(String joinPassword) {
        this.joinPassword = joinPassword;
    }

    public String getJoinPassword() {
        return this.joinPassword;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return this.status;
    }

    @Override
    public String toString() {
        return ": " + (meetingId == null ? "空" : meetingId) + ",: " + (meetingNo == null ? "空" : meetingNo) + ",: " + (meetingName == null ? "空" : meetingName) + ",: " + (createTime == null ? "空" : DataUtils.format(createTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ",: " + (createUserId == null ? "空" : createUserId) + ",: " + (joinType == null ? "空" : joinType) + ",: " + (joinPassword == null ? "空" : joinPassword) + ",: " + (startTime == null ? "空" : DataUtils.format(startTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ",: " + (endTime == null ? "空" : DataUtils.format(endTime, DateTimePatternEnum.YYYY_MM_DD_HH_MM_SS.getPattern())) + ",: " + (status == null ? "空" : status);
    }
}