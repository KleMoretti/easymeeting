package com.easymeeting.task;

import com.easymeeting.entity.dto.MeetingMemberDto;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.po.MeetingInfo;
import com.easymeeting.entity.po.MeetingMember;
import com.easymeeting.entity.query.MeetingInfoQuery;
import com.easymeeting.entity.query.MeetingMemberQuery;
import com.easymeeting.enums.MeetingMemberStatusEnum;
import com.easymeeting.enums.MeetingStatusEnum;
import com.easymeeting.mappers.MeetingInfoMapper;
import com.easymeeting.mappers.MeetingMemberMapper;
import com.easymeeting.redis.RedisComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MeetingStateRepairTask {

    @Value("${meeting.state.repair.enabled:true}")
    private Boolean enabled;

    @Resource
    private MeetingInfoMapper<MeetingInfo, MeetingInfoQuery> meetingInfoMapper;
    @Resource
    private MeetingMemberMapper<MeetingMember, MeetingMemberQuery> meetingMemberMapper;
    @Resource
    private RedisComponent redisComponent;

    @Scheduled(fixedDelayString = "${meeting.state.repair.fixed-delay-ms:60000}")
    public void repairRunningMeetingState() {
        if (!Boolean.TRUE.equals(enabled)) {
            return;
        }
        try {
            MeetingInfoQuery meetingInfoQuery = new MeetingInfoQuery();
            meetingInfoQuery.setStatus(MeetingStatusEnum.RUNNING.getStatus());
            List<MeetingInfo> runningMeetings = meetingInfoMapper.selectList(meetingInfoQuery);
            if (runningMeetings == null || runningMeetings.isEmpty()) {
                return;
            }
            for (MeetingInfo meetingInfo : runningMeetings) {
                repairMeetingMembers(meetingInfo.getMeetingId());
            }
        } catch (Exception e) {
            log.warn("repair running meeting state failed", e);
        }
    }

    private void repairMeetingMembers(String meetingId) {
        List<MeetingMemberDto> redisMembers = redisComponent.getMeetingMemberList(meetingId);
        if (redisMembers == null) {
            redisMembers = Collections.emptyList();
        }
        Set<String> onlineUserIds = redisMembers.stream()
                .filter(item -> MeetingMemberStatusEnum.NORMAL.getStatus().equals(item.getStatus()))
                .map(MeetingMemberDto::getUserId)
                .collect(Collectors.toCollection(HashSet::new));

        MeetingMemberQuery memberQuery = new MeetingMemberQuery();
        memberQuery.setMeetingId(meetingId);
        memberQuery.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        memberQuery.setMeetingStatus(MeetingStatusEnum.RUNNING.getStatus());
        List<MeetingMember> dbMembers = meetingMemberMapper.selectList(memberQuery);
        if (dbMembers == null || dbMembers.isEmpty()) {
            return;
        }

        for (MeetingMember member : dbMembers) {
            if (onlineUserIds.contains(member.getUserId())) {
                continue;
            }
            MeetingMember update = new MeetingMember();
            update.setStatus(MeetingMemberStatusEnum.EXIT_MEETING.getStatus());
            meetingMemberMapper.updateByMeetingIdAndUserId(update, meetingId, member.getUserId());

            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDtoByUserId(member.getUserId());
            if (tokenUserInfoDto != null && meetingId.equals(tokenUserInfoDto.getCurrentMeetingId())) {
                tokenUserInfoDto.setCurrentMeetingId(null);
                redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
            }
            log.info("repair stale meeting member, meetingId={}, userId={}", meetingId, member.getUserId());
        }
    }
}
