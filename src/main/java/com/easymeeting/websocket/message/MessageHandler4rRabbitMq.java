package com.easymeeting.websocket.message;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.utils.JsonUtils;
import com.easymeeting.websocket.ChannelContextUtils;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
@ConditionalOnProperty(name = Constants.MESSAGING_HANDLE_CHANNEL_KEY, havingValue = Constants.MESSAGING_HANDLE_CHANNEL_RABBITMQ)
@Slf4j
public class MessageHandler4rRabbitMq implements MessageHandler {

    private static final String EXCHANGE_NAME = "fanout_exchange";

    private static final Integer MAX_RETRYTIMES = 3;

    private static final String RETRY_COUNT_KEY = "retryCount";

    @Value("${spring.rabbitmq.host:}")
    private String host;
    @Value("${spring.rabbitmq.port:}")
    private Integer port;

    @Resource
    private ChannelContextUtils channelContextUtils;

    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    private ConnectionFactory buildConnectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        return connectionFactory;
    }

    @Override
    public void listenMessage() {
        factory = buildConnectionFactory();
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String queuqName = channel.queueDeclare().getQueue();
            channel.queueBind(queuqName, EXCHANGE_NAME, "");
            Boolean autoAck = false;
            System.out.println("等待消息中...");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    log.info("RabbitMq收到消息, payload={}", message);
                    channelContextUtils.sendMessage(JsonUtils.convertJson2Obj(message, MessageSendDto.class));
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    log.error("RabbitMq处理消息失败", e);
                    handleFailMessage(channel, delivery, queuqName);
                }
            };
            channel.basicConsume(queuqName, autoAck, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            log.error("RabbitMq监听消息失败", e);
        }
    }

    private static void handleFailMessage(Channel channel, Delivery delivery, String queueName) throws IOException {
        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
        }
        Integer retryCount = 0;
        if (headers.containsKey(RETRY_COUNT_KEY)) {
            Object retryValue = headers.get(RETRY_COUNT_KEY);
            if (retryValue instanceof Number) {
                retryCount = ((Number) retryValue).intValue();
            }
        }
        if (retryCount < MAX_RETRYTIMES) {
            headers.put(RETRY_COUNT_KEY, retryCount + 1);
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().headers(headers).build();
            channel.basicPublish("", queueName, properties, delivery.getBody());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } else {
            log.error("超过最大重试次数，放弃处理, queueName={}", queueName);
            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
        }
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
        String message = JsonUtils.convertObj2Json(messageSendDto);
        ConnectionFactory connectionFactory = buildConnectionFactory();
        try (Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("RabbiMq发送消息失败", e);
        }
    }

    @PreDestroy
    public void destroy() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
}
