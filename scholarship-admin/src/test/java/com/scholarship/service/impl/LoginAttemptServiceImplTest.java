package com.scholarship.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

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
        // 设置测试用的配置值
        ReflectionTestUtils.setField(loginAttemptService, "maxAttempts", 5);
        ReflectionTestUtils.setField(loginAttemptService, "lockDuration", 30);
        ReflectionTestUtils.setField(loginAttemptService, "attemptExpire", 24);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("测试记录登录失败 - 未达到锁定阈值")
    void testRecordFailureWithoutLock() {
        String identifier = "testuser";

        when(valueOperations.increment(anyString())).thenReturn(3L);

        loginAttemptService.recordFailure(identifier);

        verify(valueOperations, times(1)).increment("login:attempt:" + identifier);
        verify(valueOperations, never()).set(anyString(), eq("LOCKED"), anyLong(), any());
    }

    @Test
    @DisplayName("测试记录登录失败 - 达到锁定阈值")
    void testRecordFailureWithLock() {
        String identifier = "testuser";

        when(valueOperations.increment(anyString())).thenReturn(5L);

        loginAttemptService.recordFailure(identifier);

        verify(valueOperations, times(1)).increment("login:attempt:" + identifier);
        verify(valueOperations, times(1)).set(
            eq("login:lock:" + identifier),
            eq("LOCKED"),
            eq(30L),
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("测试记录登录失败 - 已锁定账户")
    void testRecordFailureAlreadyLocked() {
        String identifier = "testuser";

        when(redisTemplate.hasKey("login:lock:" + identifier)).thenReturn(true);

        loginAttemptService.recordFailure(identifier);

        verify(valueOperations, never()).increment(anyString());
        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any());
    }

    @Test
    @DisplayName("测试重置登录失败次数")
    void testResetFailures() {
        String identifier = "testuser";

        loginAttemptService.resetFailures(identifier);

        verify(redisTemplate, times(1)).delete("login:attempt:" + identifier);
        verify(redisTemplate, times(1)).delete("login:lock:" + identifier);
    }

    @Test
    @DisplayName("测试检查是否被锁定 - 已锁定")
    void testIsLockedTrue() {
        String identifier = "testuser";

        when(redisTemplate.hasKey("login:lock:" + identifier)).thenReturn(true);

        assertTrue(loginAttemptService.isLocked(identifier));
    }

    @Test
    @DisplayName("测试检查是否被锁定 - 未锁定")
    void testIsLockedFalse() {
        String identifier = "testuser";

        when(redisTemplate.hasKey("login:lock:" + identifier)).thenReturn(false);

        assertFalse(loginAttemptService.isLocked(identifier));
    }

    @Test
    @DisplayName("测试获取剩余锁定时间 - 已锁定")
    void testGetRemainingLockTimeLocked() {
        String identifier = "testuser";
        long expectedTtl = 1800L; // 30 minutes

        when(redisTemplate.getExpire("login:lock:" + identifier, TimeUnit.SECONDS))
            .thenReturn(expectedTtl);

        assertEquals(expectedTtl, loginAttemptService.getRemainingLockTime(identifier));
    }

    @Test
    @DisplayName("测试获取剩余锁定时间 - 未锁定")
    void testGetRemainingLockTimeUnlocked() {
        String identifier = "testuser";

        when(redisTemplate.getExpire("login:lock:" + identifier, TimeUnit.SECONDS))
            .thenReturn(null);

        assertEquals(0, loginAttemptService.getRemainingLockTime(identifier));
    }

    @Test
    @DisplayName("测试多次失败后锁定")
    void testMultipleFailuresThenLock() {
        String identifier = "testuser";

        // 模拟连续失败 4 次
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L, 4L);

        for (int i = 0; i < 4; i++) {
            loginAttemptService.recordFailure(identifier);
        }

        // 第 5 次失败应该触发锁定
        when(valueOperations.increment(anyString())).thenReturn(5L);
        loginAttemptService.recordFailure(identifier);

        verify(valueOperations, times(1)).set(
            eq("login:lock:" + identifier),
            eq("LOCKED"),
            eq(30L),
            eq(TimeUnit.MINUTES)
        );
    }

    @Test
    @DisplayName("测试登录成功后重置")
    void testSuccessResetsCounter() {
        String identifier = "testuser";

        // 先失败 3 次
        when(valueOperations.increment(anyString())).thenReturn(1L, 2L, 3L);
        for (int i = 0; i < 3; i++) {
            loginAttemptService.recordFailure(identifier);
        }

        // 登录成功，重置计数
        loginAttemptService.resetFailures(identifier);

        // 验证 resetFailures 调用了 delete 方法两次（attempt 和 lock）
        verify(redisTemplate, times(2)).delete(anyString());
    }
}
