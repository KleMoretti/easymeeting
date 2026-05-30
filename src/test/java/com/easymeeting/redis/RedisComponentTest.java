package com.easymeeting.redis;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MeetingMemberDto;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.enums.MeetingMemberStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RedisComponentTest {

    private RedisUtils redisUtils;
    private RedisComponent redisComponent;

    @BeforeEach
    void setUp() {
        redisUtils = mock(RedisUtils.class);
        redisComponent = new RedisComponent();
        ReflectionTestUtils.setField(redisComponent, "redisUtils", redisUtils);
    }

    @Test
    void saveTokenUserInfoDtoWritesTokenAndUserIdIndexes() {
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setToken("token-1");
        tokenUserInfoDto.setUserId("user-1");

        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        verify(redisUtils).setex(Constants.REDIS_KEY_WS_TOKEN + "token-1", tokenUserInfoDto,
                Constants.REDIS_KEY_EXPIRES_DAY);
        verify(redisUtils).setex(Constants.REDIS_KEY_WS_TOKEN_USERID + "user-1", "token-1",
                Constants.REDIS_KEY_EXPIRES_DAY);
    }

    @Test
    void saveTokenUserInfoDtoSkipsInvalidTokenPayloads() {
        redisComponent.saveTokenUserInfoDto(null);

        TokenUserInfoDto missingToken = new TokenUserInfoDto();
        missingToken.setUserId("user-1");
        redisComponent.saveTokenUserInfoDto(missingToken);

        TokenUserInfoDto missingUserId = new TokenUserInfoDto();
        missingUserId.setToken("token-1");
        redisComponent.saveTokenUserInfoDto(missingUserId);

        verifyNoInteractions(redisUtils);
    }

    @Test
    void getTokenUserInfoDtoByUserIdResolvesUserIndexBeforeTokenPayload() {
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setToken("token-1");
        tokenUserInfoDto.setUserId("user-1");
        when(redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + "user-1")).thenReturn("token-1");
        when(redisUtils.get(Constants.REDIS_KEY_WS_TOKEN + "token-1")).thenReturn(tokenUserInfoDto);

        TokenUserInfoDto result = redisComponent.getTokenUserInfoDtoByUserId("user-1");

        assertSame(tokenUserInfoDto, result);
    }

    @Test
    void getTokenUserInfoDtoByUserIdReturnsNullWhenIndexMissing() {
        when(redisUtils.get(Constants.REDIS_KEY_WS_TOKEN_USERID + "user-1")).thenReturn(null);

        TokenUserInfoDto result = redisComponent.getTokenUserInfoDtoByUserId("user-1");

        assertNull(result);
        verify(redisUtils, never()).get(Constants.REDIS_KEY_WS_TOKEN + "token-1");
    }

    @Test
    void getMeetingMemberListReturnsEmptyListWhenRedisHasNoMembers() {
        when(redisUtils.hvals(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1")).thenReturn(null);

        List<MeetingMemberDto> result = redisComponent.getMeetingMemberList("meeting-1");

        assertTrue(result.isEmpty());
    }

    @Test
    void getMeetingMemberListSortsMembersByJoinTime() {
        MeetingMemberDto later = member("user-2", 200L);
        MeetingMemberDto earlier = member("user-1", 100L);
        when(redisUtils.hvals(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1"))
                .thenReturn(Arrays.asList(later, earlier));

        List<MeetingMemberDto> result = redisComponent.getMeetingMemberList("meeting-1");

        assertEquals(Arrays.asList(earlier, later), result);
    }

    @Test
    void exitMeetingReturnsFalseWhenMemberDoesNotExist() {
        when(redisUtils.hget(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1", "user-1")).thenReturn(null);

        Boolean result = redisComponent.exitMeeting("meeting-1", "user-1", MeetingMemberStatusEnum.EXIT_MEETING);

        assertFalse(result);
        verify(redisUtils, never()).hset(anyString(), anyString(), any());
    }

    @Test
    void exitMeetingUpdatesStatusAndPersistsMember() {
        MeetingMemberDto member = member("user-1", 100L);
        member.setStatus(MeetingMemberStatusEnum.NORMAL.getStatus());
        when(redisUtils.hget(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1", "user-1")).thenReturn(member);

        Boolean result = redisComponent.exitMeeting("meeting-1", "user-1", MeetingMemberStatusEnum.EXIT_MEETING);

        assertTrue(result);
        assertEquals(MeetingMemberStatusEnum.EXIT_MEETING.getStatus(), member.getStatus());
        verify(redisUtils).hset(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1", "user-1", member);
    }

    @Test
    void updateMeetingMemberMediaStatusOnlyChangesProvidedFlags() {
        MeetingMemberDto member = member("user-1", 100L);
        member.setOpenVideo(true);
        member.setOpenAudio(false);
        when(redisUtils.hget(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1", "user-1")).thenReturn(member);

        MeetingMemberDto result = redisComponent.updateMeetingMemberMediaStatus("meeting-1", "user-1", null, true);

        assertSame(member, result);
        assertTrue(result.getOpenVideo());
        assertTrue(result.getOpenAudio());
        verify(redisUtils).hset(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1", "user-1", member);
    }

    @Test
    void removeAllMeetingMemberDeletesEachExistingMemberFromMeetingHash() {
        MeetingMemberDto first = member("user-1", 100L);
        MeetingMemberDto second = member("user-2", 200L);
        when(redisUtils.hvals(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1"))
                .thenReturn(Arrays.asList(first, second));

        redisComponent.removeAllMeetingMember("meeting-1");

        verify(redisUtils).hdel(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1", "user-1", "user-2");
    }

    @Test
    void removeAllMeetingMemberSkipsRedisDeleteWhenMeetingIsEmpty() {
        when(redisUtils.hvals(Constants.REDIS_KEY_MEETING_ROOM + "meeting-1")).thenReturn(Collections.emptyList());

        redisComponent.removeAllMeetingMember("meeting-1");

        verify(redisUtils, never()).hdel(anyString(), any());
    }

    private static MeetingMemberDto member(String userId, Long joinTime) {
        MeetingMemberDto member = new MeetingMemberDto();
        member.setUserId(userId);
        member.setJoinTime(joinTime);
        return member;
    }
}
