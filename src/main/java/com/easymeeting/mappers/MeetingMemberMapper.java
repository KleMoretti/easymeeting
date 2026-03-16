package com.easymeeting.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:  Mapper接口
 * @author: klein
 * @data: 2026/03/16
 */
public interface MeetingMemberMapper<T, P> extends BaseMapper {

	/**
	 * 根据MeetingIdAndUserId查询
	 */
	T selectByMeetingIdAndUserId(@Param("meetingId") String meetingId, @Param("userId") String userId);

	/**
	 * 根据MeetingIdAndUserId更新
	 */
	Integer updateByMeetingIdAndUserId(@Param("bean") T t, @Param("meetingId") String meetingId, @Param("userId") String userId);

	/**
	 * 根据MeetingIdAndUserId删除
	 */
	Integer deleteByMeetingIdAndUserId(@Param("meetingId") String meetingId, @Param("userId") String userId);

}