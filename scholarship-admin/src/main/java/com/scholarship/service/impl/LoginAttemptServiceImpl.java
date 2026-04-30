package com.scholarship.service.impl;

import com.scholarship.common.support.LockConstants;
import com.scholarship.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final DefaultRedisScript<Long> INCR_WITH_EXPIRE_SCRIPT = new DefaultRedisScript<>(
            "local count = redis.call('INCR', KEYS[1]) " +
            "if count == 1 then " +
            "    redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
            "end " +
            "return count",
            Long.class
    );

    @Value("${security.login.max-attempts:5}")
    private int accountMaxAttempts;

    @Value("${security.login.lock-duration:30}")
    private int accountLockDurationMinutes;

    @Value("${security.login.attempt-expire:24}")
    private int accountAttemptExpireHours;

    @Value("${security.login.ip-max-attempts:20}")
    private int ipMaxAttempts;

    @Value("${security.login.ip-lock-duration:10}")
    private int ipLockDurationMinutes;

    @Value("${security.login.ip-attempt-expire:1}")
    private int ipAttemptExpireHours;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void recordFailure(String username, String clientIp) {
        incrementAndMaybeLock(
                LockConstants.LOGIN_ATTEMPT_ACCOUNT + username,
                LockConstants.LOGIN_LOCK_ACCOUNT + username,
                accountAttemptExpireHours,
                accountMaxAttempts,
                accountLockDurationMinutes,
                "account",
                username
        );
        incrementAndMaybeLock(
                LockConstants.LOGIN_ATTEMPT_IP + clientIp,
                LockConstants.LOGIN_LOCK_IP + clientIp,
                ipAttemptExpireHours,
                ipMaxAttempts,
                ipLockDurationMinutes,
                "ip",
                clientIp
        );
    }

    @Override
    public void resetFailures(String username, String clientIp) {
        redisTemplate.delete(LockConstants.LOGIN_ATTEMPT_ACCOUNT + username);
        redisTemplate.delete(LockConstants.LOGIN_LOCK_ACCOUNT + username);
        redisTemplate.delete(LockConstants.LOGIN_ATTEMPT_IP + clientIp);
        redisTemplate.delete(LockConstants.LOGIN_LOCK_IP + clientIp);
        log.info("Login failure counters reset, username={}, clientIp={}", username, clientIp);
    }

    @Override
    public boolean isAccountLocked(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LockConstants.LOGIN_LOCK_ACCOUNT + username));
    }

    @Override
    public boolean isIpLocked(String clientIp) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(LockConstants.LOGIN_LOCK_IP + clientIp));
    }

    @Override
    public long getAccountRemainingLockTime(String username) {
        return getRemainingLockTime(LockConstants.LOGIN_LOCK_ACCOUNT + username);
    }

    @Override
    public long getIpRemainingLockTime(String clientIp) {
        return getRemainingLockTime(LockConstants.LOGIN_LOCK_IP + clientIp);
    }

    private void incrementAndMaybeLock(String attemptKey,
                                       String lockKey,
                                       int attemptExpireHours,
                                       int maxAttempts,
                                       int lockDurationMinutes,
                                       String scope,
                                       String identifier) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            log.warn("Login scope already locked, scope={}, identifier={}", scope, identifier);
            return;
        }

        Long attempts = redisTemplate.execute(INCR_WITH_EXPIRE_SCRIPT,
                Collections.singletonList(attemptKey),
                String.valueOf(TimeUnit.HOURS.toSeconds(attemptExpireHours)));
        if (attempts == null) {
            attempts = 1L;
        }

        log.info("Login failure recorded, scope={}, identifier={}, attempts={}", scope, identifier, attempts);
        if (attempts >= maxAttempts) {
            redisTemplate.opsForValue().set(lockKey, "LOCKED", lockDurationMinutes, TimeUnit.MINUTES);
            redisTemplate.delete(attemptKey);
            log.warn("Login scope locked, scope={}, identifier={}, durationMinutes={}", scope, identifier, lockDurationMinutes);
        }
    }

    private long getRemainingLockTime(String lockKey) {
        Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
