package com.scholarship.service.impl;

import com.scholarship.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录限流服务实现类
 * <p>
 * 使用 Redis 记录登录失败次数，防止暴力破解
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final String ATTEMPT_KEY_PREFIX = "login:attempt:";
    private static final String LOCK_KEY_PREFIX = "login:lock:";

    /**
     * 最大失败尝试次数
     */
    @Value("${security.login.max-attempts:5}")
    private int maxAttempts;

    /**
     * 锁定时长（分钟）
     */
    @Value("${security.login.lock-duration:30}")
    private int lockDuration;

    /**
     * 失败记录过期时间（小时）
     */
    @Value("${security.login.attempt-expire:24}")
    private int attemptExpire;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void recordFailure(String identifier) {
        String attemptKey = ATTEMPT_KEY_PREFIX + identifier;
        String lockKey = LOCK_KEY_PREFIX + identifier;

        // 检查是否已被锁定
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            log.warn("账户已被锁定：identifier={}", identifier);
            return;
        }

        // 增加失败次数
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);
        if (attempts == null) {
            attempts = 1L;
            redisTemplate.opsForValue().set(attemptKey, "1", attemptExpire, TimeUnit.HOURS);
        }

        log.info("登录失败：identifier={}, attempts={}", identifier, attempts);

        // 检查是否达到锁定阈值
        if (attempts >= maxAttempts) {
            lockAccount(identifier, attempts);
        }
    }

    @Override
    public void resetFailures(String identifier) {
        String attemptKey = ATTEMPT_KEY_PREFIX + identifier;
        String lockKey = LOCK_KEY_PREFIX + identifier;

        redisTemplate.delete(attemptKey);
        redisTemplate.delete(lockKey);
        log.info("登录失败次数已重置：identifier={}", identifier);
    }

    @Override
    public boolean isLocked(String identifier) {
        String lockKey = LOCK_KEY_PREFIX + identifier;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }

    @Override
    public long getRemainingLockTime(String identifier) {
        String lockKey = LOCK_KEY_PREFIX + identifier;
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    /**
     * 锁定账户
     *
     * @param identifier 标识符
     * @param attempts 失败次数
     */
    private void lockAccount(String identifier, Long attempts) {
        String lockKey = LOCK_KEY_PREFIX + identifier;
        String attemptKey = ATTEMPT_KEY_PREFIX + identifier;

        // 设置锁定
        redisTemplate.opsForValue().set(lockKey, "LOCKED", lockDuration, TimeUnit.MINUTES);
        // 重置失败次数
        redisTemplate.delete(attemptKey);

        log.warn("账户已被锁定：identifier={}, attempts={}, lockDuration={}min",
                identifier, attempts, lockDuration);
    }
}
