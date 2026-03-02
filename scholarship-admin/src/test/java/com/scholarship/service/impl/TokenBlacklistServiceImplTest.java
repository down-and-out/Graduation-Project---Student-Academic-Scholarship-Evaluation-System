package com.scholarship.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TokenBlacklistServiceImpl Token 黑名单服务测试
 */
@DisplayName("TokenBlacklistServiceImpl Token 黑名单服务测试")
class TokenBlacklistServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistServiceImpl tokenBlacklistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("测试将 Token 加入黑名单")
    void testAddToBlacklist() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
        long expireTime = 3600L;

        tokenBlacklistService.addToBlacklist(token, expireTime);

        verify(valueOperations, times(1)).set(
            eq("token:blacklist:" + token),
            eq("BLACKLISTED"),
            eq(expireTime),
            eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("测试检查 Token 是否在黑名单中 - 存在")
    void testIsBlacklistedTrue() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

        when(redisTemplate.hasKey("token:blacklist:" + token)).thenReturn(true);

        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    @DisplayName("测试检查 Token 是否在黑名单中 - 不存在")
    void testIsBlacklistedFalse() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

        when(redisTemplate.hasKey("token:blacklist:" + token)).thenReturn(false);

        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    @DisplayName("测试从黑名单中移除 Token")
    void testRemoveFromBlacklist() {
        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";

        tokenBlacklistService.removeFromBlacklist(token);

        verify(redisTemplate, times(1)).delete("token:blacklist:" + token);
    }

    @Test
    @DisplayName("测试多个 Token 加入黑名单")
    void testMultipleTokensToBlacklist() {
        String token1 = "token1";
        String token2 = "token2";
        String token3 = "token3";

        tokenBlacklistService.addToBlacklist(token1, 3600L);
        tokenBlacklistService.addToBlacklist(token2, 7200L);
        tokenBlacklistService.addToBlacklist(token3, 1800L);

        verify(valueOperations, times(1)).set(eq("token:blacklist:" + token1), eq("BLACKLISTED"), eq(3600L), eq(TimeUnit.SECONDS));
        verify(valueOperations, times(1)).set(eq("token:blacklist:" + token2), eq("BLACKLISTED"), eq(7200L), eq(TimeUnit.SECONDS));
        verify(valueOperations, times(1)).set(eq("token:blacklist:" + token3), eq("BLACKLISTED"), eq(1800L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("测试登出场景：加入黑名单并检查")
    void testLogoutScenario() {
        String token = "logout_test_token";
        long expireTime = 1800L;

        // 用户登出，Token 加入黑名单
        tokenBlacklistService.addToBlacklist(token, expireTime);

        // 检查 Token 是否在黑名单中
        when(redisTemplate.hasKey("token:blacklist:" + token)).thenReturn(true);
        assertTrue(tokenBlacklistService.isBlacklisted(token));

        // Token 过期后从黑名单移除
        tokenBlacklistService.removeFromBlacklist(token);

        verify(redisTemplate, times(1)).delete("token:blacklist:" + token);
    }

    @Test
    @DisplayName("测试空 Token 处理")
    void testEmptyToken() {
        String emptyToken = "";

        tokenBlacklistService.addToBlacklist(emptyToken, 3600L);

        verify(valueOperations, times(1)).set(
            eq("token:blacklist:"),
            eq("BLACKLISTED"),
            eq(3600L),
            eq(TimeUnit.SECONDS)
        );
    }

    @Test
    @DisplayName("测试 null Token 处理")
    void testNullToken() {
        // 由于 Redis 模板通常会处理 null 值，这里只验证方法调用不会抛出异常
        assertDoesNotThrow(() -> {
            tokenBlacklistService.addToBlacklist(null, 3600L);
        });
    }
}
