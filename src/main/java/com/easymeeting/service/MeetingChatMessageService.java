package com.easymeeting.service;

import com.easymeeting.entity.po.MeetingChatMessage;
import com.easymeeting.entity.query.MeetingChatMessageQuery;
import com.easymeeting.entity.vo.PageinationResultVO;

import java.util.List;

public interface MeetingChatMessageService {

    List<MeetingChatMessage> findListByParam(String meetingId, MeetingChatMessageQuery param);

    Integer findCountByParam(String meetingId, MeetingChatMessageQuery param);

    PageinationResultVO<MeetingChatMessage> findListByPage(String meetingId, MeetingChatMessageQuery param);

    Integer add(String meetingId, MeetingChatMessage bean);

    Integer addBatch(String meetingId, List<MeetingChatMessage> listBean);

    Integer addOrUpdateBatch(String meetingId, List<MeetingChatMessage> listBean);

    Integer updateByParam(String meetingId, MeetingChatMessage bean, MeetingChatMessageQuery query);

    Integer deleteByParam(String meetingId, MeetingChatMessageQuery query);
}
