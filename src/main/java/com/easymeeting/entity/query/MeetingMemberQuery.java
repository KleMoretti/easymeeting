package com.easymeeting.entity.query;

import java.util.Date;


/**
 * @Description: 查询对象
 * @author: klein
 * @data: 2026/03/16
 */
public class MeetingMemberQuery extends BaseQuery{
	/**
	 * 
	 */
	private String meetingId;

	private String meetingIdFuzzy;

	/**
	 * 
	 */
	private String userId;

	private String userIdFuzzy;

	/**
	 * 
	 */
	private String nickName;

	private String nickNameFuzzy;

	/**
	 * 
	 */
	private Date lastJoinTime;

	private String lastJoinTimeStart;
	private String lastJoinTimeEnd;

	/**
	 * 
	 */
	private Integer status;

	/**
	 * 
	 */
	private Integer memberType;

	/**
	 * 
	 */
	private Integer meetingStatus;

	public void setMeetingId(String meetingId) {
		this.meetingId = meetingId;
	}

	public String getMeetingId(){
		return this.meetingId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId(){
		return this.userId;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getNickName(){
		return this.nickName;
	}

	public void setLastJoinTime(Date lastJoinTime) {
		this.lastJoinTime = lastJoinTime;
	}

	public Date getLastJoinTime(){
		return this.lastJoinTime;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getStatus(){
		return this.status;
	}

	public void setMemberType(Integer memberType) {
		this.memberType = memberType;
	}

	public Integer getMemberType(){
		return this.memberType;
	}

	public void setMeetingStatus(Integer meetingStatus) {
		this.meetingStatus = meetingStatus;
	}

	public Integer getMeetingStatus(){
		return this.meetingStatus;
	}

	public void setMeetingIdFuzzy(String meetingIdFuzzy) {
		this.meetingIdFuzzy = meetingIdFuzzy;
	}

	public String getMeetingIdFuzzy(){
		return this.meetingIdFuzzy;
	}

	public void setUserIdFuzzy(String userIdFuzzy) {
		this.userIdFuzzy = userIdFuzzy;
	}

	public String getUserIdFuzzy(){
		return this.userIdFuzzy;
	}

	public void setNickNameFuzzy(String nickNameFuzzy) {
		this.nickNameFuzzy = nickNameFuzzy;
	}

	public String getNickNameFuzzy(){
		return this.nickNameFuzzy;
	}

	public void setLastJoinTimeStart(String lastJoinTimeStart) {
		this.lastJoinTimeStart = lastJoinTimeStart;
	}

	public String getLastJoinTimeStart(){
		return this.lastJoinTimeStart;
	}

	public void setLastJoinTimeEnd(String lastJoinTimeEnd) {
		this.lastJoinTimeEnd = lastJoinTimeEnd;
	}

	public String getLastJoinTimeEnd(){
		return this.lastJoinTimeEnd;
	}

}