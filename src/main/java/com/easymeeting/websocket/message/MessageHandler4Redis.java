package com.easymeeting.websocket.message;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.websocket.ChannelContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@Component
@ConditionalOnProperty(name = Constants.MESSAGING_HANDLE_CHANNEL_KEY, havingValue = Constants.MESSAGING_HANDLE_CHANNEL_REDIS)
@Slf4j
public class MessageHandler4Redis implements MessageHandler {

    private static final String MESSAGE_TOPIC = "message.topic";

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ChannelContextUtils channelContextUtils;

    @Override
    public void listenMessage() {
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.addListener(MessageSendDto.class, (MessageSendDto, sendDto) -> {
            try {
                if (sendDto == null) {
                    return;
                }
                log.info("redis收到消息, messageType={}, meetingId={}, sendUserId={}", sendDto.getMessageType(),
                        sendDto.getMeetingId(), sendDto.getSendUserId());
                channelContextUtils.sendMessage(sendDto);
            } catch (Exception e) {
                log.error("处理消息失败", e);
            }
        });
    }

    @Override
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
        RTopic rTopic = redissonClient.getTopic(MESSAGE_TOPIC);
        rTopic.publish(messageSendDto);
    }

    @PreDestroy
    public void destroy() {
        redissonClient.shutdown();
    }
}
