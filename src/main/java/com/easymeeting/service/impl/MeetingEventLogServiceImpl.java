package com.easymeeting.service.impl;

import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.entity.po.MeetingEventLog;
import com.easymeeting.enums.MeetingEventStatusEnum;
import com.easymeeting.mappers.MeetingEventLogMapper;
import com.easymeeting.service.MeetingEventLogService;
import com.easymeeting.utils.StringTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class MeetingEventLogServiceImpl implements MeetingEventLogService {

    @Resource
    private MeetingEventLogMapper meetingEventLogMapper;

    @Override
    public void recordPublished(MessageSendDto<?> messageSendDto) {
        if (messageSendDto == null || messageSendDto.getMessageId() == null) {
            return;
        }
        try {
            MeetingEventLog eventLog = new MeetingEventLog();
            eventLog.setEventId(StringTools.getRandomNumber(20));
            eventLog.setMessageId(messageSendDto.getMessageId());
            eventLog.setMeetingId(messageSendDto.getMeetingId());
            eventLog.setUserId(messageSendDto.getSendUserId());
            eventLog.setEventType(messageSendDto.getMessageType());
            eventLog.setSendToType(messageSendDto.getMessageSend2Type());
            eventLog.setReceiveUserId(messageSendDto.getReceiveUserId());
            eventLog.setStatus(MeetingEventStatusEnum.PUBLISHED.getStatus());
            eventLog.setRetryCount(0);
            eventLog.setCreateTime(new Date());
            eventLog.setUpdateTime(new Date());
            meetingEventLogMapper.insertOrUpdate(eventLog);
        } catch (Exception e) {
            log.warn("record meeting event publish failed, messageId={}", messageSendDto.getMessageId(), e);
        }
    }

    @Override
    public void recordConsumed(MessageSendDto<?> messageSendDto) {
        updateStatus(messageSendDto, MeetingEventStatusEnum.CONSUMED, null, null);
    }

    @Override
    public void recordFailed(MessageSendDto<?> messageSendDto, Integer retryCount, Exception e) {
        updateStatus(messageSendDto, MeetingEventStatusEnum.FAILED, retryCount, e);
    }

    @Override
    public void recordDeadLetter(MessageSendDto<?> messageSendDto, Integer retryCount, Exception e) {
        updateStatus(messageSendDto, MeetingEventStatusEnum.DEAD_LETTER, retryCount, e);
    }

    private void updateStatus(MessageSendDto<?> messageSendDto, MeetingEventStatusEnum statusEnum, Integer retryCount,
            Exception e) {
        if (messageSendDto == null || messageSendDto.getMessageId() == null) {
            return;
        }
        try {
            String errorReason = e == null ? null : e.getMessage();
            if (errorReason != null && errorReason.length() > 500) {
                errorReason = errorReason.substring(0, 500);
            }
            meetingEventLogMapper.updateStatusByMessageId(messageSendDto.getMessageId(), statusEnum.getStatus(),
                    retryCount, errorReason);
        } catch (Exception ex) {
            log.warn("update meeting event log failed, messageId={}, status={}", messageSendDto.getMessageId(),
                    statusEnum.getStatus(), ex);
        }
    }
}
