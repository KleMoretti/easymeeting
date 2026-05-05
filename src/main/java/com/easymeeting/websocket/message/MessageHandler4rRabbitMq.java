package com.easymeeting.websocket.message;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.service.MeetingEventLogService;
import com.easymeeting.utils.JsonUtils;
import com.easymeeting.websocket.ChannelContextUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
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
@ConditionalOnProperty(name = Constants.MESSAGING_HANDLE_CHANNEL_KEY, havingValue = Constants.MESSAGING_HANDLE_CHANNEL_RABBITMQ,
        matchIfMissing = true)
@Slf4j
public class MessageHandler4rRabbitMq implements MessageHandler {

    public static final String EXCHANGE_NAME = "easymeeting.meeting.event.exchange";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "easymeeting.meeting.event.dlx";
    public static final String DEAD_LETTER_QUEUE_NAME = "easymeeting.meeting.event.dlq";

    private static final String QUEUE_NAME_PREFIX = "easymeeting.meeting.event.queue.";
    private static final Integer MAX_RETRY_TIMES = 3;
    private static final String RETRY_COUNT_KEY = "retryCount";

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
    @Value("${app.instance-id:${random.uuid}}")
    private String instanceId;

    @Resource
    private ChannelContextUtils channelContextUtils;
    @Resource
    private MeetingEventDeduplicationService meetingEventDeduplicationService;
    @Resource
    private MeetingRuntimeMetrics meetingRuntimeMetrics;
    @Resource
    private MeetingEventLogService meetingEventLogService;

    private Connection connection;
    private Channel channel;

    private ConnectionFactory buildConnectionFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        return connectionFactory;
    }

    public static void declareTopology(Channel channel) throws IOException {
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true);
        channel.exchangeDeclare(DEAD_LETTER_EXCHANGE_NAME, BuiltinExchangeType.FANOUT, true);
        channel.queueDeclare(DEAD_LETTER_QUEUE_NAME, true, false, false, null);
        channel.queueBind(DEAD_LETTER_QUEUE_NAME, DEAD_LETTER_EXCHANGE_NAME, "");
    }

    private String declareConsumerQueue(Channel channel) throws IOException {
        declareTopology(channel);
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE_NAME);
        String queueName = QUEUE_NAME_PREFIX + instanceId;
        channel.queueDeclare(queueName, true, false, false, args);
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        return queueName;
    }

    @Override
    public void listenMessage() {
        try {
            connection = buildConnectionFactory().newConnection();
            channel = connection.createChannel();
            channel.basicQos(20);
            String queueName = declareConsumerQueue(channel);
            log.info("RabbitMQ meeting event consumer started, queueName={}", queueName);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> consumeMessage(queueName, delivery);
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            log.error("RabbitMQ listen meeting event failed", e);
        }
    }

    private void consumeMessage(String queueName, Delivery delivery) throws IOException {
        MessageSendDto<?> messageSendDto = null;
        try {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            messageSendDto = JsonUtils.convertJson2Obj(message, MessageSendDto.class);
            log.info("RabbitMQ receive meeting event, meetingId={}, userId={}, messageId={}, type={}",
                    messageSendDto == null ? null : messageSendDto.getMeetingId(),
                    messageSendDto == null ? null : messageSendDto.getSendUserId(),
                    messageSendDto == null ? null : messageSendDto.getMessageId(),
                    messageSendDto == null ? null : messageSendDto.getMessageType());

            if (!meetingEventDeduplicationService.markProcessingIfAbsent(messageSendDto)) {
                meetingRuntimeMetrics.recordDuplicateMessage();
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                return;
            }

            channelContextUtils.sendMessage(messageSendDto);
            meetingEventLogService.recordConsumed(messageSendDto);
            meetingRuntimeMetrics.recordConsumeSuccess();
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("RabbitMQ handle meeting event failed", e);
            meetingRuntimeMetrics.recordConsumeFailure();
            handleFailMessage(queueName, delivery, messageSendDto, e);
        }
    }

    private void handleFailMessage(String queueName, Delivery delivery, MessageSendDto<?> messageSendDto, Exception cause)
            throws IOException {
        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
        }
        Integer retryCount = parseRetryCount(headers);
        if (retryCount < MAX_RETRY_TIMES) {
            headers.put(RETRY_COUNT_KEY, retryCount + 1);
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().headers(headers).build();
            channel.basicPublish("", queueName, properties, delivery.getBody());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            meetingEventLogService.recordFailed(messageSendDto, retryCount + 1, cause);
            return;
        }

        log.error("meeting event retry exceeded, send to dead letter, queueName={}, retryCount={}", queueName,
                retryCount);
        channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
        meetingRuntimeMetrics.recordDeadLetterMessage();
        meetingEventLogService.recordDeadLetter(messageSendDto, retryCount, cause);
    }

    private Integer parseRetryCount(Map<String, Object> headers) {
        Object retryValue = headers.get(RETRY_COUNT_KEY);
        if (retryValue instanceof Number) {
            return ((Number) retryValue).intValue();
        }
        return 0;
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
        try (Connection publishConnection = buildConnectionFactory().newConnection();
                Channel publishChannel = publishConnection.createChannel()) {
            declareTopology(publishChannel);
            publishChannel.confirmSelect();
            publishChannel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
            publishChannel.waitForConfirmsOrDie(5000);
            meetingEventLogService.recordPublished(messageSendDto);
            meetingRuntimeMetrics.recordPublishSuccess();
            log.info("RabbitMQ publish meeting event success, meetingId={}, userId={}, messageId={}, type={}",
                    messageSendDto.getMeetingId(), messageSendDto.getSendUserId(), messageSendDto.getMessageId(),
                    messageSendDto.getMessageType());
        } catch (Exception e) {
            meetingRuntimeMetrics.recordPublishFailure();
            meetingEventLogService.recordFailed(messageSendDto, 0, e);
            log.error("RabbitMQ publish meeting event failed, meetingId={}, userId={}, messageId={}",
                    messageSendDto.getMeetingId(), messageSendDto.getSendUserId(), messageSendDto.getMessageId(), e);
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
