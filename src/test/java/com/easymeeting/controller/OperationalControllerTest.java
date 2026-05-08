package com.easymeeting.controller;

import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.websocket.ChannelContextUtils;
import com.easymeeting.websocket.message.MeetingRuntimeMetrics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OperationalControllerTest {

    @Test
    void meetingMetricsContainRuntimeAndMessageCounters() {
        MeetingRuntimeMetrics metrics = new MeetingRuntimeMetrics();
        metrics.recordPublishSuccess();
        OperationalController controller = new OperationalController(mock(HealthEndpoint.class), metrics);

        ResponseVO response = controller.meetingMetrics();
        Map<String, Object> data = (Map<String, Object>) response.getData();

        assertTrue(data.containsKey("onlineUserCount"));
        assertTrue(data.containsKey("meetingRoomCount"));
        assertEquals(ChannelContextUtils.getOnlineUserCount(), data.get("onlineUserCount"));
        assertEquals(1L, data.get("publishSuccessCount"));
    }

    @Test
    void healthDetailWrapsActuatorHealth() {
        HealthEndpoint healthEndpoint = mock(HealthEndpoint.class);
        HealthComponent health = Health.up().withDetail("rabbit", "up").build();
        when(healthEndpoint.health()).thenReturn(health);
        OperationalController controller = new OperationalController(healthEndpoint, new MeetingRuntimeMetrics());

        ResponseVO response = controller.healthDetail();

        assertEquals("success", response.getStatus());
        assertEquals(health, response.getData());
    }
}
