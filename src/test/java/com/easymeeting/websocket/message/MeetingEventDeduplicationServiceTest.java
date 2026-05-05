package com.easymeeting.websocket.message;

import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.redis.RedisUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MeetingEventDeduplicationServiceTest {

    @Test
    void firstMessageIdIsAcceptedAndDuplicateIsRejected() {
        RedisUtils<Object> redisUtils = mock(RedisUtils.class);
        when(redisUtils.setIfAbsent(eq("easymeeting:meeting:event:processed:1001"), eq("1"), anyLong()))
                .thenReturn(true)
                .thenReturn(false);

        MeetingEventDeduplicationService service = new MeetingEventDeduplicationService(redisUtils);
        MessageSendDto<Object> message = new MessageSendDto<>();
        message.setMessageId(1001L);

        assertTrue(service.markProcessingIfAbsent(message));
        assertFalse(service.markProcessingIfAbsent(message));
    }

    @Test
    void messageWithoutIdIsAccepted() {
        RedisUtils<Object> redisUtils = mock(RedisUtils.class);
        MeetingEventDeduplicationService service = new MeetingEventDeduplicationService(redisUtils);

        assertTrue(service.markProcessingIfAbsent(new MessageSendDto<>()));
    }
}
