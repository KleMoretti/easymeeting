package com.easymeeting.websocket;

import com.alibaba.fastjson.JSON;
import com.easymeeting.entity.dto.MeetingExitDto;
import com.easymeeting.entity.dto.MeetingMemberDto;
import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.po.UserInfo;
import com.easymeeting.enums.MeetingMemberStatusEnum;
import com.easymeeting.enums.MessageSend2TypeEnum;
import com.easymeeting.enums.MessageTypesEnum;
import com.easymeeting.mappers.UserInfoMapper;
import com.easymeeting.redis.RedisComponent;
import com.easymeeting.utils.JsonUtils;
import com.easymeeting.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class ChannelContextUtils {
    public static final ConcurrentHashMap<String, Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();

    public static final ConcurrentHashMap<String, ChannelGroup> MEETING_ROOM_CONTEXT_MAP = new ConcurrentHashMap<>();
    private final UserInfoMapper userInfoMapper;
    private final RedisComponent redisComponent;

    public ChannelContextUtils(UserInfoMapper userInfoMapper, RedisComponent redisComponent) {
        this.userInfoMapper = userInfoMapper;
        this.redisComponent = redisComponent;
    }

    public void addContext(String userId, Channel channel) {
        try {
            String channelId = channel.id().toString();
            AttributeKey attributeKey = null;
            if (!AttributeKey.exists(channelId)) {
                attributeKey = AttributeKey.newInstance(channelId);
            } else {
                attributeKey = AttributeKey.valueOf(channelId);
            }
            channel.attr(attributeKey).set(userId);
            USER_CONTEXT_MAP.put(userId, channel);

            UserInfo userInfo = new UserInfo();
            userInfo.setLastLoginTime(System.currentTimeMillis());
            userInfoMapper.updateByUserId(userInfo, userId);

            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDtoByUserId(userId);
            if (tokenUserInfoDto == null || tokenUserInfoDto.getCurrentMeetingId() == null) {
                return;
            }
            addMeetingRoom(tokenUserInfoDto.getCurrentMeetingId(), userId);

        } catch (Exception e) {
            log.error("初始化连接失败", e);
        }
    }

    public void addMeetingRoom(String meetingId, String userId) {
        Channel context = USER_CONTEXT_MAP.get(userId);
        if (null == context) {
            return;
        }
        ChannelGroup group = MEETING_ROOM_CONTEXT_MAP.get(meetingId);
        if (group == null) {
            group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            MEETING_ROOM_CONTEXT_MAP.put(meetingId, group);
        }
        Channel channel = group.find(context.id());
        if (channel == null) {
            group.add(context);
        }
    }

    public void sendMessage(MessageSendDto messageSendDto) {
        if (messageSendDto == null) {
            return;
        }
        if (messageSendDto.getMessageId() == null) {
            messageSendDto.setMessageId(System.currentTimeMillis());
        }
        if (messageSendDto.getSendTime() == null) {
            messageSendDto.setSendTime(System.currentTimeMillis());
        }
        if (MessageSend2TypeEnum.USER.getType().equals(messageSendDto.getMessageSend2Type())) {
            sendMsg2User(messageSendDto);
        } else {
            sendMsg2Group(messageSendDto);
        }

    }

    private void sendMsg2Group(MessageSendDto messageSendDto) {
        ChannelGroup group = MEETING_ROOM_CONTEXT_MAP.get(messageSendDto.getMeetingId());
        if (group == null) {
            return;
        }
        group.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageSendDto)));

        if (MessageTypesEnum.EXIT_MEETING_ROOM.getType().equals(messageSendDto.getMessageType())) {
            MeetingExitDto exitDto;
            if (messageSendDto.getMessageContent() instanceof String) {
                exitDto = JsonUtils.convertJson2Obj((String) messageSendDto.getMessageContent(), MeetingExitDto.class);
            } else {
                exitDto = JsonUtils.convertJson2Obj(JsonUtils.convertObj2Json(messageSendDto.getMessageContent()),
                        MeetingExitDto.class);
            }
            if (exitDto == null) {
                return;
            }
            removeContextFromGroup(messageSendDto.getMeetingId(), exitDto.getExitUserId());
            List<MeetingMemberDto> meetingMemberList = redisComponent
                    .getMeetingMemberList(messageSendDto.getMeetingId());
            if (meetingMemberList == null) {
                meetingMemberList = java.util.Collections.emptyList();
            }
            List<MeetingMemberDto> onLineMemberList = meetingMemberList.stream()
                    .filter(item -> MeetingMemberStatusEnum.NORMAL.getStatus().equals(item.getStatus()))
                    .collect(Collectors.toList());
            if (onLineMemberList.isEmpty()) {
                removeContextGroup(messageSendDto.getMeetingId());
            }
            return;
        }
        if (MessageTypesEnum.FINIS_MEETING.getType().equals(messageSendDto.getMessageType())) {
            List<MeetingMemberDto> meetingMemberList = redisComponent
                    .getMeetingMemberList(messageSendDto.getMeetingId());
            if (meetingMemberList != null) {
                meetingMemberList
                        .forEach(item -> removeContextFromGroup(messageSendDto.getMeetingId(), item.getUserId()));
            }
            removeContextGroup(messageSendDto.getMeetingId());
        }
    }

    private void sendMsg2User(MessageSendDto messageSendDto) {
        if (messageSendDto.getReceiveUserId() == null) {
            return;
        }
        Channel channel = USER_CONTEXT_MAP.get(messageSendDto.getReceiveUserId());
        if (channel == null) {
            return;
        }
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(messageSendDto)));
    }

    private void removeContextFromGroup(String meetingId, String userId) {
        Channel context = USER_CONTEXT_MAP.get(userId);
        if (null == context) {
            return;
        }
        ChannelGroup group = MEETING_ROOM_CONTEXT_MAP.get(meetingId);
        if (group != null) {
            group.remove(context);
        }
    }

    private void removeContextGroup(String meetingId) {
        MEETING_ROOM_CONTEXT_MAP.remove(meetingId);
    }

    public void closeContext(String userId) {
        if (StringTools.isEmpty(userId)) {
            return;
        }
        Channel channel = USER_CONTEXT_MAP.get(userId);
        USER_CONTEXT_MAP.remove(userId);
        MEETING_ROOM_CONTEXT_MAP.forEach((meetingId, group) -> {
            if (group != null && channel != null) {
                group.remove(channel);
            }
        });
        if (channel != null) {
            channel.close();
        }
    }

}
