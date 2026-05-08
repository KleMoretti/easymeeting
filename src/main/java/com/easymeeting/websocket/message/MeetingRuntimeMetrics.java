package com.easymeeting.websocket.message;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MeetingRuntimeMetrics {

    private final AtomicLong publishSuccessCount = new AtomicLong();
    private final AtomicLong publishFailureCount = new AtomicLong();
    private final AtomicLong consumeSuccessCount = new AtomicLong();
    private final AtomicLong consumeFailureCount = new AtomicLong();
    private final AtomicLong duplicateMessageCount = new AtomicLong();
    private final AtomicLong deadLetterMessageCount = new AtomicLong();

    public void recordPublishSuccess() {
        publishSuccessCount.incrementAndGet();
    }

    public void recordPublishFailure() {
        publishFailureCount.incrementAndGet();
    }

    public void recordConsumeSuccess() {
        consumeSuccessCount.incrementAndGet();
    }

    public void recordConsumeFailure() {
        consumeFailureCount.incrementAndGet();
    }

    public void recordDuplicateMessage() {
        duplicateMessageCount.incrementAndGet();
    }

    public void recordDeadLetterMessage() {
        deadLetterMessageCount.incrementAndGet();
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("publishSuccessCount", publishSuccessCount.get());
        data.put("publishFailureCount", publishFailureCount.get());
        data.put("consumeSuccessCount", consumeSuccessCount.get());
        data.put("consumeFailureCount", consumeFailureCount.get());
        data.put("duplicateMessageCount", duplicateMessageCount.get());
        data.put("deadLetterMessageCount", deadLetterMessageCount.get());
        return data;
    }
}
