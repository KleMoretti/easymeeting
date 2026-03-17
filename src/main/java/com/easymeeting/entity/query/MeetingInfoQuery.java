package com.easymeeting.entity.query;

import java.util.Date;


/**
 * @Description: 查询对象
 * @author: klein
 * @data: 2026/03/16
 */
public class MeetingInfoQuery extends BaseQuery{
	/**
	 * 
	 */
	private String meetingId;

	private String meetingIdFuzzy;

	/**
	 * 
	 */
	private String meetingNo;

	private String meetingNoFuzzy;

	/**
	 * 
	 */
	private String meetingName;

	private String meetingNameFuzzy;

	/**
	 * 
	 */
	private Date createTime;

	private String createTimeStart;
	private String createTimeEnd;

	/**
	 * 
	 */
	private String createUserId;

	private String createUserIdFuzzy;

	/**
	 * 
	 */
	private Integer joinType;

	/**
	 * 
	 */
	private String joinPassword;

	private String joinPasswordFuzzy;

	/**
	 * 
	 */
	private Date startTime;

	private String startTimeStart;
	private String startTimeEnd;

	/**
	 * 
	 */
	private Date endTime;

	private String endTimeStart;
	private String endTimeEnd;

	/**
	 * 
	 */
	private Integer status;

	private String userId;
	private Boolean queryMemberCount;

	public Boolean getQueryMemberCount() {
		return queryMemberCount;
	}

	public void setQueryMemberCount(Boolean queryMemberCount) {
		this.queryMemberCount = queryMemberCount;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void setMeetingId(String meetingId) {
		this.meetingId = meetingId;
	}

	public String getMeetingId(){
		return this.meetingId;
	}

	public void setMeetingNo(String meetingNo) {
		this.meetingNo = meetingNo;
	}

	public String getMeetingNo(){
		return this.meetingNo;
	}

	public void setMeetingName(String meetingName) {
		this.meetingName = meetingName;
	}

	public String getMeetingName(){
		return this.meetingName;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime(){
		return this.createTime;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getCreateUserId(){
		return this.createUserId;
	}

	public void setJoinType(Integer joinType) {
		this.joinType = joinType;
	}

	public Integer getJoinType(){
		return this.joinType;
	}

	public void setJoinPassword(String joinPassword) {
		this.joinPassword = joinPassword;
	}

	public String getJoinPassword(){
		return this.joinPassword;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getStartTime(){
		return this.startTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getEndTime(){
		return this.endTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setMeetingIdFuzzy(String meetingIdFuzzy) {
		this.meetingIdFuzzy = meetingIdFuzzy;
	}

	public String getMeetingIdFuzzy(){
		return this.meetingIdFuzzy;
	}

	public void setMeetingNoFuzzy(String meetingNoFuzzy) {
		this.meetingNoFuzzy = meetingNoFuzzy;
	}

	public String getMeetingNoFuzzy(){
		return this.meetingNoFuzzy;
	}

	public void setMeetingNameFuzzy(String meetingNameFuzzy) {
		this.meetingNameFuzzy = meetingNameFuzzy;
	}

	public String getMeetingNameFuzzy(){
		return this.meetingNameFuzzy;
	}

	public void setCreateTimeStart(String createTimeStart) {
		this.createTimeStart = createTimeStart;
	}

	public String getCreateTimeStart(){
		return this.createTimeStart;
	}

	public void setCreateTimeEnd(String createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}

	public String getCreateTimeEnd(){
		return this.createTimeEnd;
	}

	public void setCreateUserIdFuzzy(String createUserIdFuzzy) {
		this.createUserIdFuzzy = createUserIdFuzzy;
	}

	public String getCreateUserIdFuzzy(){
		return this.createUserIdFuzzy;
	}

	public void setJoinPasswordFuzzy(String joinPasswordFuzzy) {
		this.joinPasswordFuzzy = joinPasswordFuzzy;
	}

	public String getJoinPasswordFuzzy(){
		return this.joinPasswordFuzzy;
	}

	public void setStartTimeStart(String startTimeStart) {
		this.startTimeStart = startTimeStart;
	}

	public String getStartTimeStart(){
		return this.startTimeStart;
	}

	public void setStartTimeEnd(String startTimeEnd) {
		this.startTimeEnd = startTimeEnd;
	}

	public String getStartTimeEnd(){
		return this.startTimeEnd;
	}

	public void setEndTimeStart(String endTimeStart) {
		this.endTimeStart = endTimeStart;
	}

	public String getEndTimeStart(){
		return this.endTimeStart;
	}

	public void setEndTimeEnd(String endTimeEnd) {
		this.endTimeEnd = endTimeEnd;
	}

	public String getEndTimeEnd(){
		return this.endTimeEnd;
	}

}