package com.easymeeting.mappers;

import com.easymeeting.entity.po.MeetingEventLog;
import org.apache.ibatis.annotations.Param;

public interface MeetingEventLogMapper {
    Integer insertOrUpdate(@Param("bean") MeetingEventLog meetingEventLog);

    Integer updateStatusByMessageId(@Param("messageId") Long messageId,
            @Param("status") Integer status,
            @Param("retryCount") Integer retryCount,
            @Param("errorReason") String errorReason);
}
