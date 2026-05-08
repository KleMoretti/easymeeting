package com.easymeeting.service;

import com.easymeeting.entity.dto.MessageSendDto;

public interface MeetingEventLogService {
    void recordPublished(MessageSendDto<?> messageSendDto);

    void recordConsumed(MessageSendDto<?> messageSendDto);

    void recordFailed(MessageSendDto<?> messageSendDto, Integer retryCount, Exception e);

    void recordDeadLetter(MessageSendDto<?> messageSendDto, Integer retryCount, Exception e);
}
