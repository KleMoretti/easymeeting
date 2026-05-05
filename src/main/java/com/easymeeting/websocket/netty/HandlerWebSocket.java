package com.easymeeting.websocket.netty;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.entity.dto.PeerConnectionDataDto;
import com.easymeeting.entity.dto.PeerMessageDto;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.po.UserInfo;
import com.easymeeting.entity.query.UserInfoQuery;
import com.easymeeting.enums.MeetingMemberStatusEnum;
import com.easymeeting.enums.MessageSend2TypeEnum;
import com.easymeeting.enums.MessageTypesEnum;
import com.easymeeting.mappers.UserInfoMapper;
import com.easymeeting.redis.RedisComponent;
import com.easymeeting.service.MeetingInfoService;
import com.easymeeting.utils.JsonUtils;
import com.easymeeting.utils.StringTools;
import com.easymeeting.websocket.ChannelContextUtils;
import com.easymeeting.websocket.message.MessageHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@ChannelHandler.Sharable
@Slf4j
public class HandlerWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Resource
    private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;
    @Resource
    private RedisComponent redisComponent;
    @Resource
    private MessageHandler messageHandler;
    @Resource
    private MeetingInfoService meetingInfoService;
    @Resource
    private ChannelContextUtils channelContextUtils;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的链接加入");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有链接断开");
        Attribute<String> attribute = ctx.channel().attr(AttributeKey.valueOf(ctx.channel().id().toString()));
        String userId = attribute.get();
        if (!StringTools.isEmpty(userId)) {
            UserInfo userInfo = new UserInfo();
            userInfo.setLastOffTime(System.currentTimeMillis());
            userInfoMapper.updateByUserId(userInfo, userId);
            redisComponent.cleanUserHeartbeat(userId);

            TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDtoByUserId(userId);
            if (tokenUserInfoDto != null && !StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())) {
                try {
                    meetingInfoService.exitMeetingRoom(tokenUserInfoDto, MeetingMemberStatusEnum.EXIT_MEETING);
                } catch (Exception e) {
                    log.error("用户掉线退会失败,userId={}", userId, e);
                }
            }
            channelContextUtils.closeContext(userId);
        }
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame)
            throws Exception {
        String text = textWebSocketFrame.text();

        if (Constants.PING.equals(text)) {
            Attribute<String> attribute = channelHandlerContext.channel()
                    .attr(AttributeKey.valueOf(channelHandlerContext.channel().id().toString()));
            redisComponent.saveUserHeartbeat(attribute.get());
            return;
        }
        log.info("收到ws消息{}", text);
        PeerConnectionDataDto dataDto = JsonUtils.convertJson2Obj(text, PeerConnectionDataDto.class);
        if (dataDto == null || StringTools.isEmpty(dataDto.getToken())) {
            return;
        }
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDto(dataDto.getToken());
        if (tokenUserInfoDto == null || StringTools.isEmpty(tokenUserInfoDto.getCurrentMeetingId())) {
            return;
        }
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypesEnum.PEER.getType());
        PeerMessageDto peerMessageDto = new PeerMessageDto();
        peerMessageDto.setSignalType(dataDto.getSignalType());
        peerMessageDto.setSignalData(dataDto.getSignalData());

        messageSendDto.setMessageContent(peerMessageDto);

        messageSendDto.setMeetingId(tokenUserInfoDto.getCurrentMeetingId());
        messageSendDto.setSendUserId(tokenUserInfoDto.getUserId());
        messageSendDto.setReceiveUserId(dataDto.getReceiveUserId());
        if (StringTools.isEmpty(dataDto.getReceiveUserId())) {
            messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.GROUP.getType());
        } else {
            messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());
        }

        messageHandler.sendMessage(messageSendDto);
    }

}
