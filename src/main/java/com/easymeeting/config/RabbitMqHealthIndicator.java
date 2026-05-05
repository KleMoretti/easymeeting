package com.easymeeting.config;

import com.easymeeting.websocket.message.MessageHandler4rRabbitMq;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("rabbitMq")
public class RabbitMqHealthIndicator implements HealthIndicator {

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

    @Override
    public Health health() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            MessageHandler4rRabbitMq.declareTopology(channel);
            return Health.up()
                    .withDetail("host", host)
                    .withDetail("port", port)
                    .withDetail("exchange", MessageHandler4rRabbitMq.EXCHANGE_NAME)
                    .withDetail("deadLetterQueue", MessageHandler4rRabbitMq.DEAD_LETTER_QUEUE_NAME)
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .withDetail("host", host)
                    .withDetail("port", port)
                    .build();
        }
    }
}
