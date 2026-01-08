package com.example.card_processing_app.services;

import com.example.card_processing_app.dto.IdempotencyResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Log4j2
@Service
@RequiredArgsConstructor
public class IdempotencyKeyService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String PREFIX = "idempotency:";

    @Value("${app.idempotency.ttl-in-hours:24}")
    private int ttlInHours;

    public IdempotencyResult checkAndLock(String key) {
        String fullKey = PREFIX + key;

        String script =
                "local val = redis.call('get', KEYS[1]) " +
                        "if val then return val end " +
                        "redis.call('setex', KEYS[1], ARGV[1], 'PROCESSING') " +
                        "return nil";

        String result = redisTemplate.execute(
                new DefaultRedisScript<>(script, String.class),
                Collections.singletonList(fullKey),
                "300"
        );

        if (result == null) {
            return IdempotencyResult.newRequest();
        }

        if ("PROCESSING".equals(result)) {
            return IdempotencyResult.processing();
        }

        return IdempotencyResult.completed(result);
    }

    public void markAsComplete(String key, Object response) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(response);

            redisTemplate.opsForValue().set(
                    PREFIX + key,
                    jsonResponse,
                    Duration.ofHours(ttlInHours)
            );
        } catch (JsonProcessingException e) {
            log.error("Idempotency serialization error for key: {}", key, e);
        }
    }

    public void deleteKey(String key) {
        redisTemplate.delete(PREFIX + key);
    }
}
