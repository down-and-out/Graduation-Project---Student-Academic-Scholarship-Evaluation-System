package com.scholarship.common.support;

import com.scholarship.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@Deprecated
public class RedisLockSupport {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(String key, String value, long ttlSeconds) {
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, value, ttlSeconds, TimeUnit.SECONDS);
            return Boolean.TRUE.equals(locked);
        } catch (DataAccessException e) {
            log.error("Redis lock acquisition failed: key={}", key, e);
            throw new BusinessException("锁服务暂不可用，请稍后再试");
        }
    }

    public boolean unlock(String key, String value) {
        try {
            Long result = redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), value);
            return result != null && result > 0;
        } catch (DataAccessException e) {
            log.warn("Redis lock release failed: key={}", key, e);
            return false;
        }
    }
}
