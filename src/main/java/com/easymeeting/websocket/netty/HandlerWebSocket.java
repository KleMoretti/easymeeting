package com.easymeeting.websocket.netty;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.entity.dto.PeerConnectionDataDto;
import com.easymeeting.entity.dto.PeerMessageDto;
import com.easymeeting.entity.dto.TokenUserInfoDto;
import com.easymeeting.entity.po.UserInfo;
import com.easymeeting.entity.query.UserInfoQuery;
import com.easymeeting.enums.MessageSend2TypeEnum;
import com.easymeeting.enums.MessageTypesEnum;
import com.easymeeting.mappers.UserInfoMapper;
import com.easymeeting.redis.RedisComponent;
import com.easymeeting.utils.JsonUtils;
import com.easymeeting.websocket.message.MessageHandler;
import com.rabbitmq.tools.json.JSONUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("有新的链接加入");
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("有链接断开");
        //TODO 处理连接断开的逻辑
        Attribute<String> attribute = ctx.channel().attr(AttributeKey.valueOf(ctx.channel().id().toString()));
        String userId = attribute.get();
        UserInfo userInfo = new UserInfo();
        userInfo.setLastOffTime(System.currentTimeMillis());
        userInfoMapper.updateByUserId(userInfo, userId);
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String text = textWebSocketFrame.text();

        if(Constants.PING.equals(text)){
            return;
        }
        log.error("收到消息{}", text);
        PeerConnectionDataDto dataDto= JsonUtils.convertJson2Obj(text, PeerConnectionDataDto.class);
        TokenUserInfoDto tokenUserInfoDto=redisComponent.getTokenUserInfoDto(dataDto.getToken());
        if(tokenUserInfoDto==null){
            return;
        }
        MessageSendDto messageSendDto=new MessageSendDto();
        messageSendDto.setMessageType(MessageTypesEnum.PEER.getType());
        PeerMessageDto peerMessageDto=new PeerMessageDto();
        peerMessageDto.setSignalType(dataDto.getSignalType());
        peerMessageDto.setSignalData(dataDto.getSignalData());

        messageSendDto.setMessageContent(peerMessageDto);

        messageSendDto.setMeetingId(tokenUserInfoDto.getCurrentMeetingId());
        messageSendDto.setSendUserId(tokenUserInfoDto.getUserId());
        messageSendDto.setReceiveUserId(dataDto.getReceiveUserId());
        messageSendDto.setMessageSend2Type(MessageSend2TypeEnum.USER.getType());

        messageHandler.sendMessage(messageSendDto);
    }


}
