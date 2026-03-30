package com.easymeeting.mappers;

import com.easymeeting.entity.po.MeetingInviteRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MeetingInviteRecordMapper {

    Integer insert(@Param("bean") MeetingInviteRecord meetingInviteRecord);

    MeetingInviteRecord selectByInviteId(@Param("inviteId") String inviteId);

    MeetingInviteRecord selectPendingByMeetingIdAndReceiveUserId(@Param("meetingId") String meetingId,
            @Param("receiveUserId") String receiveUserId);

    List<MeetingInviteRecord> selectPendingByReceiveUserId(@Param("receiveUserId") String receiveUserId);

    Integer updateByInviteId(@Param("bean") MeetingInviteRecord meetingInviteRecord,
            @Param("inviteId") String inviteId);

    Integer updateByMeetingIdAndReceiveUserIdAndStatus(@Param("bean") MeetingInviteRecord meetingInviteRecord,
            @Param("meetingId") String meetingId, @Param("receiveUserId") String receiveUserId,
            @Param("status") Integer status);
}
