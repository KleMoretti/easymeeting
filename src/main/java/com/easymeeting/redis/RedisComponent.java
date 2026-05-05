package com.easymeeting.redis;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MeetingMemberDto;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.enums.MeetingMemberStatusEnum;
import com.easymeeting.utils.StringTools;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.Collections;
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
        if (tokenUserInfoDto == null || StringTools.isEmpty(tokenUserInfoDto.getToken())
                || StringTools.isEmpty(tokenUserInfoDto.getUserId())) {
            return;
        }
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN + tokenUserInfoDto.getToken(), tokenUserInfoDto,
                Constants.REDIS_KEY_EXPIRES_DAY);
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID + tokenUserInfoDto.getUserId(),
                tokenUserInfoDto.getToken(), Constants.REDIS_KEY_EXPIRES_DAY);

    }

    public TokenUserInfoDto getTokenUserInfoDto(String token) {
        if (StringTools.isEmpty(token)) {
            return null;
        }
        return (TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + token);
    }

    public TokenUserInfoDto getTokenUserInfoDtoByUserId(String userId) {
        if (StringTools.isEmpty(userId)) {
            return null;
        }
        String token = (String) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + userId);
        if (StringTools.isEmpty(token)) {
            return null;
        }
        return getTokenUserInfoDto(token);
    }

    public void saveUserHeartbeat(String userId) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        redisUtils.setex(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId, System.currentTimeMillis(),
                Constants.REDIS_KEY_EXPIRES_ONE_MIN);
    }

    public void cleanUserHeartbeat(String userId) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        redisUtils.delete(Constants.REDIS_KEY_WS_USER_HEART_BEAT + userId);
    }

    public void add2Meeting(String meetingId, MeetingMemberDto meetingMemberDto) {
        redisUtils.hset(Constants.REDIS_KEY_MEETING_ROOM + meetingId, meetingMemberDto.getUserId(), meetingMemberDto);
    }

    public List<MeetingMemberDto> getMeetingMemberList(String meetingId) {
        List<MeetingMemberDto> meetingMemberDtoList = redisUtils.hvals(Constants.REDIS_KEY_MEETING_ROOM + meetingId);
        if (meetingMemberDtoList == null) {
            return Collections.emptyList();
        }
        meetingMemberDtoList = meetingMemberDtoList.stream().sorted(Comparator.comparing(MeetingMemberDto::getJoinTime))
                .collect(Collectors.toList());
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

    public void removeAllMeetingMember(String meetingId) {
        List<MeetingMemberDto> meetingMemberDtoList = getMeetingMemberList(meetingId);
        if (meetingMemberDtoList == null || meetingMemberDtoList.isEmpty()) {
            return;
        }
        List<String> userId = meetingMemberDtoList.stream().map(MeetingMemberDto::getUserId)
                .collect(Collectors.toList());
        if (userId.isEmpty()) {
            return;
        }
        redisUtils.hdel(Constants.REDIS_KEY_MEETING_ROOM + meetingId, userId.toArray(new String[userId.size()]));

    }

    public MeetingMemberDto updateMeetingMemberMediaStatus(String meetingId, String userId, Boolean openVideo,
            Boolean openAudio) {
        MeetingMemberDto meetingMemberDto = getMeetingMember(meetingId, userId);
        if (meetingMemberDto == null) {
            return null;
        }
        if (openVideo != null) {
            meetingMemberDto.setOpenVideo(openVideo);
        }
        if (openAudio != null) {
            meetingMemberDto.setOpenAudio(openAudio);
        }
        add2Meeting(meetingId, meetingMemberDto);
        return meetingMemberDto;
    }
}
