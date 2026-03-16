package com.easymeeting.service.impl;

import com.easymeeting.entity.query.SimplePage;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.enums.PageSize;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.mappers.MeetingInfoMapper;
import com.easymeeting.service.MeetingInfoService;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description: Service
 * @author: klein
 * @data: 2026/03/16
 */
@Service("meetingInfoService")
public class MeetingInfoServiceImpl implements MeetingInfoService {

	@Resource
	private MeetingInfoMapper<MeetingInfo, MeetingInfoQuery> meetingInfoMapper;
	/**
	 * 根据条件查询列表
	 */
	public List<MeetingInfo> findListByParam(MeetingInfoQuery param) {
		return this.meetingInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询数量
	 */
	public Integer findCountByParam(MeetingInfoQuery param) {
		return this.meetingInfoMapper.selectCount(param);
	}
	/**
	 * 分页查询
	 */
	public PageinationResultVO<MeetingInfo> findPageByPage(MeetingInfoQuery param ) {
		Integer count = this.findCountByParam(param);
		Integer pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<MeetingInfo> list = this.findListByParam(param);
		PageinationResultVO<MeetingInfo> result = new PageinationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;	}

	/**
	 * 新增
	 */
	public Integer add(MeetingInfo bean) {
		return this.meetingInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	public Integer addBatch(List<MeetingInfo> listBean ) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.meetingInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或修改
	 */
	public Integer addOrUpdateBatch(List<MeetingInfo> listBean ) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.meetingInfoMapper.insertOrUpdateBatch(listBean);
	}


	/**
	 * 根据MeetingId查询
	 */
	public MeetingInfo getMeetingInfoByMeetingId(String meetingId) {
		return this.meetingInfoMapper.selectByMeetingId( meetingId );
	}

	/**
	 * 根据MeetingId更新
	 */
	public Integer updateMeetingInfoByMeetingId( MeetingInfo bean, String meetingId) {
		return this.meetingInfoMapper.updateByMeetingId(bean, meetingId);
	}

	/**
	 * 根据MeetingId删除
	 */
	public Integer deleteMeetingInfoByMeetingId(String meetingId) {
		return this.meetingInfoMapper.deleteByMeetingId(meetingId);
	}
}
