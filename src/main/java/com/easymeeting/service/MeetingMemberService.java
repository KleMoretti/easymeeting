package com.easymeeting.service;

import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.entity.po.MeetingMember;
import com.easymeeting.entity.query.MeetingMemberQuery;
import java.util.List;

/**
 * @Description: Service
 * @author: klein
 * @data: 2026/03/16
 */
public interface MeetingMemberService{

	/**
	 * 根据条件查询列表
	 */
	List<MeetingMember> findListByParam(MeetingMemberQuery param);

	/**
	 * 根据条件查询数量
	 */
	Integer findCountByParam(MeetingMemberQuery param);

	/**
	 * 分页查询
	 */
	PageinationResultVO<MeetingMember> findPageByPage(MeetingMemberQuery param);

	/**
	 * 新增
	 */
	Integer add(MeetingMember bean);

	/**
	 * 批量新增
	 */
	Integer addBatch(List<MeetingMember> listBean );

	/**
	 * 批量新增或修改
	 */
	Integer addOrUpdateBatch(List<MeetingMember> listBean );


	/**
	 * 根据MeetingIdAndUserId查询
	 */
	MeetingMember getMeetingMemberByMeetingIdAndUserId(String meetingId, String userId);

	/**
	 * 根据MeetingIdAndUserId更新
	 */
	Integer updateMeetingMemberByMeetingIdAndUserId( MeetingMember bean, String meetingId, String userId);

	/**
	 * 根据MeetingIdAndUserId删除
	 */
	Integer deleteMeetingMemberByMeetingIdAndUserId(String meetingId, String userId);
}
