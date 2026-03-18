package com.easymeeting.websocket.test;

import com.google.protobuf.Message;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RabbitMqSubscriber {
    private static final String EXCHANGE_NAME = "fanout_exchange";

    private static final Integer MAX_RETRYTIMES = 3;

    private static final String RETRY_COUNT_KEY = "retryCount";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        String queuqName = channel.queueDeclare().getQueue();
        channel.queueBind(queuqName, EXCHANGE_NAME, "");
        Boolean autoAck = false;
        System.out.println("等待消息中...");
        try {
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    log.info("收到消息{}", message + System.currentTimeMillis());
                    System.out.println("收到消息" + message + System.currentTimeMillis());
                    if (Math.random() > 0.3) {
                        throw new RuntimeException("处理消息失败");
                    }
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (Exception e) {
                    log.info("处理消息失败");
                    try {
                        handleFailMessage(channel, delivery, queuqName);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            channel.basicConsume(queuqName, autoAck, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }

    private static void handleFailMessage(Channel channel, Delivery delivery, String queueName) throws Exception {
        Map<String, Object> headers = delivery.getProperties().getHeaders();
        if (headers == null) {
            headers = new HashMap<>();
        }
        Integer retryCount = 0;
        if (headers.containsKey(RETRY_COUNT_KEY)) {
            retryCount = (Integer) headers.get(RETRY_COUNT_KEY);
        }
        if (retryCount < MAX_RETRYTIMES) {
            headers.put(RETRY_COUNT_KEY, retryCount + 1);
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().headers(headers).build();
            channel.basicPublish("", queueName, properties, delivery.getBody());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }else{
            log.info("超过最大重试次数，放弃处理");
            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
