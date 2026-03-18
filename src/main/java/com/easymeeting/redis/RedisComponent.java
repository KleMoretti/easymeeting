package com.easymeeting.redis;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MeetingMemberDto;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.enums.MeetingMemberStatusEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class RedisComponent {

    @Resource
    private RedisUtils redisUtils;

    public String saveCheckCode(String code) {
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey, code, 60 * 5);
        return checkCodeKey;
    }


    public String getCheckCode(String checkCodeKey) {
        return (String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void cleanCheckCode(String checkCodeKey) {
        redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE + checkCodeKey);
    }

    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto) {
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto, Constants.REDIS_KEY_EXPIRES_DAY);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getUserId(), tokenUserInfoDto.getToken(), Constants.REDIS_KEY_EXPIRES_DAY);

    }

    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
    }

    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) {
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        return getTokenUserInfoDto(token);
    }

    public void add2Meeting(String meetingId, MeetingMemberDto meetingMemberDto) {
        redisUtils.hset(Constants.REDIS_KEY_MEETING_ROOM + meetingId, meetingMemberDto.getUserId(), meetingMemberDto);
    }

    public List<MeetingMemberDto> getMeetingMemberList(String meetingId) {
        List<MeetingMemberDto> meetingMemberDtoList = redisUtils.hvals(Constants.REDIS_KEY_MEETING_ROOM + meetingId);
        meetingMemberDtoList = meetingMemberDtoList.stream().sorted(Comparator.comparing(MeetingMemberDto::getJoinTime)).collect(Collectors.toList());
        return meetingMemberDtoList;

    }

    public MeetingMemberDto getMeetingMember(String meetingId, String userId) {
        return (MeetingMemberDto) redisUtils.hget(Constants.REDIS_KEY_MEETING_ROOM + meetingId, userId);
    }

    public Boolean exitMeeting(String meetingId, String userId, MeetingMemberStatusEnum statusEnum) {
        MeetingMemberDto meetingMemberDto = getMeetingMember(meetingId, userId);
        if (meetingMemberDto == null) {
            return false;
        }
        meetingMemberDto.setStatus(statusEnum.getStatus());
        add2Meeting(meetingId, meetingMemberDto);

        return true;
    }
}