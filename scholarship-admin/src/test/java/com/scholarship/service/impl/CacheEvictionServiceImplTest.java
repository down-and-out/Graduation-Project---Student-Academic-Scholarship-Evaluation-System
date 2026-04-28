package com.scholarship.service.impl;

import com.scholarship.common.support.CacheConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("CacheEvictionServiceImpl tests")
class CacheEvictionServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private CacheEvictionServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CacheEvictionServiceImpl(redisTemplate);
    }

    @Test
    @DisplayName("evictEvaluationResultsForBatch 正确扫描并删除该批次所有 eval 缓存")
    void evictEvaluationResultsForBatchShouldDeleteAllEvalCaches() {
        Long batchId = 5L;

        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(3);

        // 不应抛异常（内部调用 3 次：evalPrefix + evalPagePrefix + scanByPattern for rank）
        assertDoesNotThrow(() -> service.evictEvaluationResultsForBatch(batchId));
        verify(redisTemplate, org.mockito.Mockito.times(4)).execute(any(RedisCallback.class));
    }

    @Test
    @DisplayName("evictEvaluationResultsForBatch 不同 batchId 的缓存不被误删（suffix 过滤正确）")
    void evictEvaluationResultsForBatchShouldNotDeleteOtherBatches() {
        Long batchId = 5L;

        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(2);

        assertDoesNotThrow(() -> service.evictEvaluationResultsForBatch(batchId));
        // 核心验证：scanAndDelete 使用分隔符包围的 suffix 精确过滤；新增 :batch:latest 扫描
        verify(redisTemplate, org.mockito.Mockito.times(4)).execute(any(RedisCallback.class));
    }

    @Test
    @DisplayName("scanAndDelete 遇到 DataAccessException 时返回 0 不抛异常")
    void scanAndDeleteShouldReturnZeroOnDataAccessException() {
        when(redisTemplate.execute(any(RedisCallback.class)))
                .thenThrow(new DataAccessException("Connection failed") {});

        // 不应抛异常，静默处理
        assertDoesNotThrow(() -> service.evictEvaluationResultsForBatch(1L));
    }

    @Test
    @DisplayName("evictApplicationAchievementsForUser 正确删除用户成果缓存")
    void evictApplicationAchievementsForUserShouldDeleteCorrectKey() {
        Long userId = 100L;
        String expectedKey = CacheConstants.APP_ACHIEVEMENTS + "::" + userId;

        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        assertDoesNotThrow(() -> service.evictApplicationAchievementsForUser(userId));
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("evictBatchAvailable 正确删除可用批次缓存")
    void evictBatchAvailableShouldDeleteCorrectKey() {
        String expectedKey = CacheConstants.BATCH_AVAILABLE + "::available";

        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        assertDoesNotThrow(() -> service.evictBatchAvailable());
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("evictAdminResult 正确删除指定 admin 缓存 key")
    void evictAdminResultShouldDeleteCorrectKey() {
        Long resultId = 200L;
        String expectedKey = CacheConstants.EVAL_ADMIN + "::" + resultId;

        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        assertDoesNotThrow(() -> service.evictAdminResult(resultId));
        verify(redisTemplate).delete(expectedKey);
    }
}
