package com.scholarship.service.impl;

import com.scholarship.common.support.LockConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LoginAttemptServiceImpl 登录限流服务测试
 */
@DisplayName("LoginAttemptServiceImpl 登录限流服务测试")
class LoginAttemptServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LoginAttemptServiceImpl loginAttemptService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(loginAttemptService, "accountMaxAttempts", 5);
        ReflectionTestUtils.setField(loginAttemptService, "accountLockDurationMinutes", 30);
        ReflectionTestUtils.setField(loginAttemptService, "accountAttemptExpireHours", 24);
        ReflectionTestUtils.setField(loginAttemptService, "ipMaxAttempts", 20);
        ReflectionTestUtils.setField(loginAttemptService, "ipLockDurationMinutes", 10);
        ReflectionTestUtils.setField(loginAttemptService, "ipAttemptExpireHours", 1);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
    }

    @Test
    @DisplayName("测试记录登录失败 - 未达到账号锁定阈值")
    void testRecordFailureWithoutLock() {
        String username = "testuser";
        String clientIp = "192.168.1.1";

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(3L);

        loginAttemptService.recordFailure(username, clientIp);

        verify(redisTemplate, atLeastOnce()).execute(any(DefaultRedisScript.class), anyList(), anyString());
        verify(valueOperations, never()).set(anyString(), eq("LOCKED"), anyLong(), any());
    }

    @Test
    @DisplayName("测试记录登录失败 - 达到账号锁定阈值")
    void testRecordFailureWithLock() {
        String username = "testuser";
        String clientIp = "192.168.1.1";

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(5L);

        loginAttemptService.recordFailure(username, clientIp);

        verify(redisTemplate, atLeastOnce()).execute(any(DefaultRedisScript.class), anyList(), anyString());
        verify(valueOperations, atLeastOnce()).set(
                contains(LockConstants.LOGIN_LOCK_ACCOUNT + username),
                eq("LOCKED"),
                anyLong(),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("测试记录登录失败 - 账号已锁定")
    void testRecordFailureAlreadyLocked() {
        String username = "testuser";
        String clientIp = "192.168.1.1";

        when(redisTemplate.hasKey(LockConstants.LOGIN_LOCK_ACCOUNT + username)).thenReturn(true);

        loginAttemptService.recordFailure(username, clientIp);

        verify(redisTemplate, never()).execute(any(DefaultRedisScript.class), anyList(), eq(LockConstants.LOGIN_ATTEMPT_ACCOUNT + username));
    }

    @Test
    @DisplayName("测试重置登录失败次数")
    void testResetFailures() {
        String username = "testuser";
        String clientIp = "192.168.1.1";

        loginAttemptService.resetFailures(username, clientIp);

        verify(redisTemplate, times(1)).delete(LockConstants.LOGIN_ATTEMPT_ACCOUNT + username);
        verify(redisTemplate, times(1)).delete(LockConstants.LOGIN_LOCK_ACCOUNT + username);
        verify(redisTemplate, times(1)).delete(LockConstants.LOGIN_ATTEMPT_IP + clientIp);
        verify(redisTemplate, times(1)).delete(LockConstants.LOGIN_LOCK_IP + clientIp);
    }

    @Test
    @DisplayName("测试检查账号是否被锁定 - 已锁定")
    void testIsAccountLockedTrue() {
        String username = "testuser";

        when(redisTemplate.hasKey(LockConstants.LOGIN_LOCK_ACCOUNT + username)).thenReturn(true);

        assertTrue(loginAttemptService.isAccountLocked(username));
    }

    @Test
    @DisplayName("测试检查账号是否被锁定 - 未锁定")
    void testIsAccountLockedFalse() {
        String username = "testuser";

        when(redisTemplate.hasKey(LockConstants.LOGIN_LOCK_ACCOUNT + username)).thenReturn(false);

        assertFalse(loginAttemptService.isAccountLocked(username));
    }

    @Test
    @DisplayName("测试检查IP是否被锁定 - 已锁定")
    void testIsIpLockedTrue() {
        String clientIp = "192.168.1.1";

        when(redisTemplate.hasKey(LockConstants.LOGIN_LOCK_IP + clientIp)).thenReturn(true);

        assertTrue(loginAttemptService.isIpLocked(clientIp));
    }

    @Test
    @DisplayName("测试检查IP是否被锁定 - 未锁定")
    void testIsIpLockedFalse() {
        String clientIp = "192.168.1.1";

        when(redisTemplate.hasKey(LockConstants.LOGIN_LOCK_IP + clientIp)).thenReturn(false);

        assertFalse(loginAttemptService.isIpLocked(clientIp));
    }

    @Test
    @DisplayName("测试获取账号剩余锁定时间 - 已锁定")
    void testGetAccountRemainingLockTimeLocked() {
        String username = "testuser";
        long expectedTtl = 1800L;

        when(redisTemplate.getExpire(LockConstants.LOGIN_LOCK_ACCOUNT + username, TimeUnit.SECONDS))
                .thenReturn(expectedTtl);

        assertEquals(expectedTtl, loginAttemptService.getAccountRemainingLockTime(username));
    }

    @Test
    @DisplayName("测试获取账号剩余锁定时间 - 未锁定")
    void testGetAccountRemainingLockTimeUnlocked() {
        String username = "testuser";

        when(redisTemplate.getExpire(LockConstants.LOGIN_LOCK_ACCOUNT + username, TimeUnit.SECONDS))
                .thenReturn(null);

        assertEquals(0, loginAttemptService.getAccountRemainingLockTime(username));
    }

    @Test
    @DisplayName("测试获取IP剩余锁定时间 - 已锁定")
    void testGetIpRemainingLockTimeLocked() {
        String clientIp = "192.168.1.1";
        long expectedTtl = 600L;

        when(redisTemplate.getExpire(LockConstants.LOGIN_LOCK_IP + clientIp, TimeUnit.SECONDS))
                .thenReturn(expectedTtl);

        assertEquals(expectedTtl, loginAttemptService.getIpRemainingLockTime(clientIp));
    }

    @Test
    @DisplayName("测试获取IP剩余锁定时间 - 未锁定")
    void testGetIpRemainingLockTimeUnlocked() {
        String clientIp = "192.168.1.1";

        when(redisTemplate.getExpire(LockConstants.LOGIN_LOCK_IP + clientIp, TimeUnit.SECONDS))
                .thenReturn(null);

        assertEquals(0, loginAttemptService.getIpRemainingLockTime(clientIp));
    }

    @Test
    @DisplayName("测试连续失败后锁定")
    void testMultipleFailuresThenLock() {
        String username = "testuser";
        String clientIp = "192.168.1.1";

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(1L, 2L, 3L, 4L);

        for (int i = 0; i < 4; i++) {
            loginAttemptService.recordFailure(username, clientIp);
        }

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(5L);

        loginAttemptService.recordFailure(username, clientIp);

        verify(valueOperations, atLeastOnce()).set(
                contains("login:lock:"),
                eq("LOCKED"),
                anyLong(),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("测试登录成功后重置")
    void testSuccessResetsCounter() {
        String username = "testuser";
        String clientIp = "192.168.1.1";

        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString()))
                .thenReturn(1L, 2L, 3L);

        for (int i = 0; i < 3; i++) {
            loginAttemptService.recordFailure(username, clientIp);
        }

        loginAttemptService.resetFailures(username, clientIp);

        verify(redisTemplate, atLeastOnce()).delete(anyString());
    }
}
