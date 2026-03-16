package com.easymeeting.mappers;

import org.apache.ibatis.annotations.Param;

/**
 * @Description:  Mapper接口
 * @author: klein
 * @data: 2026/03/16
 */
public interface MeetingInfoMapper<T, P> extends BaseMapper {

	/**
	 * 根据MeetingId查询
	 */
	T selectByMeetingId(@Param("meetingId") String meetingId);

	/**
	 * 根据MeetingId更新
	 */
	Integer updateByMeetingId(@Param("bean") T t, @Param("meetingId") String meetingId);

	/**
	 * 根据MeetingId删除
	 */
	Integer deleteByMeetingId(@Param("meetingId") String meetingId);

}