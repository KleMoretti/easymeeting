package com.easymeeting.websocket.message;

import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.service.MeetingEventLogService;
import com.easymeeting.utils.JsonUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class RabbitMqDeadLetterService {

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;
    @Value("${spring.rabbitmq.port:5672}")
    private Integer port;
    @Value("${spring.rabbitmq.username:guest}")
    private String username;
    @Value("${spring.rabbitmq.password:guest}")
    private String password;
    @Value("${spring.rabbitmq.virtual-host:/}")
    private String virtualHost;

    @Resource
    private MeetingRuntimeMetrics meetingRuntimeMetrics;
    @Resource
    private MeetingEventLogService meetingEventLogService;

    private ConnectionFactory buildConnectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        return connectionFactory;
    }

    public int retryDeadLetterMessages(int maxCount) {
        int retryLimit = maxCount <= 0 ? 10 : maxCount;
        int retryCount = 0;
        try (Connection connection = buildConnectionFactory().newConnection();
                Channel channel = connection.createChannel()) {
            MessageHandler4rRabbitMq.declareTopology(channel);
            for (int i = 0; i < retryLimit; i++) {
                GetResponse response = channel.basicGet(MessageHandler4rRabbitMq.DEAD_LETTER_QUEUE_NAME, false);
                if (response == null) {
                    break;
                }
                String message = new String(response.getBody(), StandardCharsets.UTF_8);
                MessageSendDto<?> messageSendDto = JsonUtils.convertJson2Obj(message, MessageSendDto.class);
                AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().build();
                channel.basicPublish(MessageHandler4rRabbitMq.EXCHANGE_NAME, "", properties, response.getBody());
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                meetingEventLogService.recordPublished(messageSendDto);
                meetingRuntimeMetrics.recordPublishSuccess();
                retryCount++;
            }
            return retryCount;
        } catch (Exception e) {
            log.error("retry dead letter messages failed", e);
            meetingRuntimeMetrics.recordPublishFailure();
            return retryCount;
        }
    }
}
