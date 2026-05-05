package com.easymeeting.websocket.message;

import com.easymeeting.entity.constants.Constants;
import com.easymeeting.entity.dto.MessageSendDto;
import com.easymeeting.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class MeetingEventDeduplicationService {

    private static final int MAX_LOCAL_KEYS = 4096;
    private static final long PROCESSED_MESSAGE_TTL_SECONDS = 60 * 60 * 24;

    private final RedisUtils<Object> redisUtils;
    private final Map<Long, Boolean> localProcessedMessages = Collections.synchronizedMap(
            new LinkedHashMap<Long, Boolean>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Long, Boolean> eldest) {
                    return size() > MAX_LOCAL_KEYS;
                }
            });

    public MeetingEventDeduplicationService(RedisUtils<Object> redisUtils) {
        this.redisUtils = redisUtils;
    }

    public boolean markProcessingIfAbsent(MessageSendDto<?> messageSendDto) {
        if (messageSendDto == null || messageSendDto.getMessageId() == null) {
            return true;
        }
        Long messageId = messageSendDto.getMessageId();
        String key = Constants.REDIS_KEY_MEETING_EVENT_PROCESSED + messageId;
        try {
            return redisUtils.setIfAbsent(key, "1", PROCESSED_MESSAGE_TTL_SECONDS);
        } catch (Exception e) {
            log.warn("redis dedup check failed, use local fallback, messageId={}", messageId, e);
            synchronized (localProcessedMessages) {
                if (localProcessedMessages.containsKey(messageId)) {
                    return false;
                }
                localProcessedMessages.put(messageId, Boolean.TRUE);
                return true;
            }
        }
    }
}
