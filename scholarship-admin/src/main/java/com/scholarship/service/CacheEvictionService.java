package com.scholarship.service;

/**
 * 缓存失效服务。
 *
 * <p>使用 Redis SCAN+DELETE 处理按前缀或模式的定向清理，
 * 弥补 {@code @CacheEvict} 不支持模糊删除的场景。</p>
 */
public interface CacheEvictionService {

    void evictEvaluationResultsForBatch(Long batchId);

    void evictApplicationAchievementsForUser(Long userId);

    void evictApplicationDetail(Long applicationId);

    void evictApplicationPages();

    void evictTaskDetail(Long taskId);

    void evictBatchAvailable();

    void evictBatchDetail(Long batchId);

    void evictRuleCaches();

    void evictAdminResult(Long resultId);
}
