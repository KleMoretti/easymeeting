package com.easymeeting.controller;

import com.easymeeting.annotation.GlobalInterceptor;
import com.easymeeting.entity.vo.ResponseVO;
import com.easymeeting.websocket.ChannelContextUtils;
import com.easymeeting.websocket.message.MeetingRuntimeMetrics;
import com.easymeeting.websocket.message.RabbitMqDeadLetterService;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@Validated
public class OperationalController extends ABaseController {

    private final HealthEndpoint healthEndpoint;
    private final MeetingRuntimeMetrics meetingRuntimeMetrics;

    @Resource
    private RabbitMqDeadLetterService rabbitMqDeadLetterService;

    public OperationalController(HealthEndpoint healthEndpoint, MeetingRuntimeMetrics meetingRuntimeMetrics) {
        this.healthEndpoint = healthEndpoint;
        this.meetingRuntimeMetrics = meetingRuntimeMetrics;
    }

    @RequestMapping("/health/detail")
    public ResponseVO healthDetail() {
        HealthComponent health = healthEndpoint.health();
        return getSuccessResponseVO(health);
    }

    @RequestMapping("/admin/metrics/meeting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO meetingMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("onlineUserCount", ChannelContextUtils.getOnlineUserCount());
        metrics.put("meetingRoomCount", ChannelContextUtils.getMeetingRoomCount());
        metrics.putAll(meetingRuntimeMetrics.snapshot());
        return getSuccessResponseVO(metrics);
    }

    @RequestMapping("/admin/message/retryDeadLetter")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO retryDeadLetter(Integer maxCount) {
        int retryCount = rabbitMqDeadLetterService.retryDeadLetterMessages(maxCount == null ? 10 : maxCount);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("retryCount", retryCount);
        return getSuccessResponseVO(result);
    }
}
