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
import static org.mockito.Mockito.times;
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
    @DisplayName("evictEvaluationResultsForBatch should scan all related result caches")
    void evictEvaluationResultsForBatchShouldDeleteAllEvalCaches() {
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(3);

        assertDoesNotThrow(() -> service.evictEvaluationResultsForBatch(5L));
        verify(redisTemplate, times(5)).execute(any(RedisCallback.class));
    }

    @Test
    @DisplayName("scanAndDelete should swallow data access exception")
    void scanAndDeleteShouldReturnZeroOnDataAccessException() {
        when(redisTemplate.execute(any(RedisCallback.class)))
                .thenThrow(new DataAccessException("Connection failed") { });

        assertDoesNotThrow(() -> service.evictEvaluationResultsForBatch(1L));
    }

    @Test
    @DisplayName("evictApplicationAchievementsForUser should delete correct key")
    void evictApplicationAchievementsForUserShouldDeleteCorrectKey() {
        String expectedKey = CacheConstants.APP_ACHIEVEMENTS + "::100";
        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        assertDoesNotThrow(() -> service.evictApplicationAchievementsForUser(100L));
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("evictBatchAvailable should delete correct key")
    void evictBatchAvailableShouldDeleteCorrectKey() {
        String expectedKey = CacheConstants.BATCH_AVAILABLE + "::available";
        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        assertDoesNotThrow(service::evictBatchAvailable);
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("evictBatchDetail should delete correct key")
    void evictBatchDetailShouldDeleteCorrectKey() {
        String expectedKey = CacheConstants.BATCH_DETAIL + "::8";
        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        assertDoesNotThrow(() -> service.evictBatchDetail(8L));
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("evictRuleCaches should scan rule caches")
    void evictRuleCachesShouldScanRuleCaches() {
        when(redisTemplate.execute(any(RedisCallback.class))).thenReturn(1);

        assertDoesNotThrow(service::evictRuleCaches);
        verify(redisTemplate, times(2)).execute(any(RedisCallback.class));
    }

    @Test
    @DisplayName("evictAdminResult should delete correct key")
    void evictAdminResultShouldDeleteCorrectKey() {
        String expectedKey = CacheConstants.EVAL_ADMIN + "::200";
        when(redisTemplate.delete(expectedKey)).thenReturn(true);

        assertDoesNotThrow(() -> service.evictAdminResult(200L));
        verify(redisTemplate).delete(expectedKey);
    }
}
