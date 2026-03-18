package com.easymeeting.websocket.test;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitMqSubscriberAutoAck {
    private static final String EXCHANGE_NAME = "fanout_exchange";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        try {
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            String queuqName = channel.queueDeclare().getQueue();
            channel.queueBind(queuqName, EXCHANGE_NAME, "");
            System.out.println("等待消息中...");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    log.info("收到消息{}", message + System.currentTimeMillis());
                    System.out.println("收到消息" + message + System.currentTimeMillis());
                } catch (Exception ignored) {
                }
            };
            channel.basicConsume(queuqName, true, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            log.error("处理消息失败", e);
        }
    }
}
