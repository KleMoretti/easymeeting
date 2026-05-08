package com.easymeeting.websocket.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MeetingRuntimeMetricsTest {

    @Test
    void recordsMessageLifecycleCounters() {
        MeetingRuntimeMetrics metrics = new MeetingRuntimeMetrics();

        metrics.recordPublishSuccess();
        metrics.recordPublishFailure();
        metrics.recordConsumeSuccess();
        metrics.recordConsumeFailure();
        metrics.recordDuplicateMessage();
        metrics.recordDeadLetterMessage();

        assertEquals(1L, metrics.snapshot().get("publishSuccessCount"));
        assertEquals(1L, metrics.snapshot().get("publishFailureCount"));
        assertEquals(1L, metrics.snapshot().get("consumeSuccessCount"));
        assertEquals(1L, metrics.snapshot().get("consumeFailureCount"));
        assertEquals(1L, metrics.snapshot().get("duplicateMessageCount"));
        assertEquals(1L, metrics.snapshot().get("deadLetterMessageCount"));
    }
}
