package com.easymeeting.service.impl;

import com.easymeeting.entity.query.SimplePage;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.enums.PageSize;
import com.easymeeting.entity.po.MeetingMember;
import com.easymeeting.entity.query.MeetingMemberQuery;
import com.easymeeting.mappers.MeetingMemberMapper;
import com.easymeeting.service.MeetingMemberService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: Service
 * @author: klein
 * @data: 2026/03/16
 */
@Service("meetingMemberService")
public class MeetingMemberServiceImpl implements MeetingMemberService {

	@Resource
	private MeetingMemberMapper<MeetingMember, MeetingMemberQuery> meetingMemberMapper;
	/**
	 * 根据条件查询列表
	 */
	public List<MeetingMember> findListByParam(MeetingMemberQuery param) {
		return this.meetingMemberMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	public Integer findCountByParam(MeetingMemberQuery param) {
		return this.meetingMemberMapper.selectCount(param);
	}
	/**
	 * 分页查询
	 */
	public PageinationResultVO<MeetingMember> findPageByPage(MeetingMemberQuery param ) {
		Integer count = this.findCountByParam(param);
		Integer pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<MeetingMember> list = this.findListByParam(param);
		PageinationResultVO<MeetingMember> result = new PageinationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;	}

	/**
	 * 新增
	 */
	public Integer add(MeetingMember bean) {
		return this.meetingMemberMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<MeetingMember> listBean ) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.meetingMemberMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	public Integer addOrUpdateBatch(List<MeetingMember> listBean ) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.meetingMemberMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据MeetingIdAndUserId查询
	 */
	public MeetingMember getMeetingMemberByMeetingIdAndUserId(String meetingId, String userId) {
		return this.meetingMemberMapper.selectByMeetingIdAndUserId( meetingId, userId );
	}

	/**
	 * 根据MeetingIdAndUserId更新
	 */
	public Integer updateMeetingMemberByMeetingIdAndUserId( MeetingMember bean, String meetingId, String userId) {
		return this.meetingMemberMapper.updateByMeetingIdAndUserId(bean, meetingId, userId);
	}

	/**
	 * 根据MeetingIdAndUserId删除
	 */
	public Integer deleteMeetingMemberByMeetingIdAndUserId(String meetingId, String userId) {
		return this.meetingMemberMapper.deleteByMeetingIdAndUserId(meetingId, userId);
	}
}
