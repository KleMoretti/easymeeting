package com.easymeeting.service.impl;

import com.easymeeting.entity.po.MeetingChatMessage;
import com.easymeeting.entity.query.MeetingChatMessageQuery;
import com.easymeeting.entity.query.SimplePage;
import com.easymeeting.entity.vo.PageinationResultVO;
import com.easymeeting.enums.PageSize;
import com.easymeeting.exception.BusinessException;
import com.easymeeting.mappers.MeetingChatMessageMapper;
import com.easymeeting.service.MeetingChatMessageService;
import com.easymeeting.utils.StringTools;
import com.easymeeting.utils.TableSplitUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("meetingChatMessageService")
public class MeetingChatMessageServiceImpl implements MeetingChatMessageService {

    @Resource
    private MeetingChatMessageMapper<MeetingChatMessage, MeetingChatMessageQuery> meetingChatMessageMapper;

    private String getTableName(String meetingId) {
        if (StringTools.isEmpty(meetingId)) {
            throw new BusinessException("meetingId不能为空");
        }
        return TableSplitUtils.getMeetingChatMessageTable(meetingId);
    }

    @Override
    public List<MeetingChatMessage> findListByParam(String meetingId, MeetingChatMessageQuery param) {
        return this.meetingChatMessageMapper.selectList(getTableName(meetingId), param);
    }

    @Override
    public Integer findCountByParam(String meetingId, MeetingChatMessageQuery param) {
        return this.meetingChatMessageMapper.selectCount(getTableName(meetingId), param);
    }

    @Override
    public PageinationResultVO<MeetingChatMessage> findListByPage(String meetingId, MeetingChatMessageQuery param) {
        Integer count = this.findCountByParam(meetingId, param);
        Integer pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<MeetingChatMessage> list = this.findListByParam(meetingId, param);
        return new PageinationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public Integer add(String meetingId, MeetingChatMessage bean) {
        return this.meetingChatMessageMapper.insert(getTableName(meetingId), bean);
    }

    @Override
    public Integer addBatch(String meetingId, List<MeetingChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.meetingChatMessageMapper.insertBatch(getTableName(meetingId), listBean);
    }

    @Override
    public Integer addOrUpdateBatch(String meetingId, List<MeetingChatMessage> listBean) {
        if (listBean == null || listBean.isEmpty()) {
            return 0;
        }
        return this.meetingChatMessageMapper.insertOrUpdateBatch(getTableName(meetingId), listBean);
    }

    @Override
    public Integer updateByParam(String meetingId, MeetingChatMessage bean, MeetingChatMessageQuery query) {
        return this.meetingChatMessageMapper.updateByParam(getTableName(meetingId), bean, query);
    }

    @Override
    public Integer deleteByParam(String meetingId, MeetingChatMessageQuery query) {
        return this.meetingChatMessageMapper.deleteByParam(getTableName(meetingId), query);
    }
}
