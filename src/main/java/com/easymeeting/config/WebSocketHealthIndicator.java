package com.easymeeting.config;

import com.easymeeting.websocket.ChannelContextUtils;
import com.easymeeting.websocket.netty.NettyWebSocketStarter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component("nettyWebSocket")
public class WebSocketHealthIndicator implements HealthIndicator {

    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;

    @Override
    public Health health() {
        Health.Builder builder = nettyWebSocketStarter.isRunning() ? Health.up() : Health.down();
        return builder.withDetail("onlineUserCount", ChannelContextUtils.getOnlineUserCount())
                .withDetail("meetingRoomCount", ChannelContextUtils.getMeetingRoomCount())
                .build();
    }
}
