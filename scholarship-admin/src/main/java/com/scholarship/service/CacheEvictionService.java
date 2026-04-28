package com.scholarship.service;

/**
 * 缓存失效服务。
 *
 * <p>使用 Redis SCAN+DELETE 做模糊匹配批量清除，
 * 弥补 Spring {@code @CacheEvict} 不支持模糊匹配的不足。</p>
 *
 * <p>所有操作设计为 best-effort：缓存失效失败不影响主业务流程。</p>
 *
 * @author Scholarship Development Team
 */
public interface CacheEvictionService {

    /**
     * 清除指定批次的所有评定相关缓存。
     * <p>覆盖：eval:student、eval:page、eval:admin、eval:rank</p>
     *
     * @param batchId 批次 ID
     */
    void evictEvaluationResultsForBatch(Long batchId);

    /**
     * 清除指定用户的成果缓存（app:achievements）。
     *
     * @param userId 用户 ID
     */
    void evictApplicationAchievementsForUser(Long userId);

    /**
     * 清除申请详情缓存。
     *
     * @param applicationId 申请 ID
     */
    void evictApplicationDetail(Long applicationId);

    /**
     * 清除所有申请分页缓存。
     */
    void evictApplicationPages();

    /**
     * 清除任务轮询缓存。
     *
     * @param taskId 任务 ID
     */
    void evictTaskDetail(Long taskId);

    /**
     * 清除可用批次列表缓存。
     */
    void evictBatchAvailable();

    /**
     * 清除指定结果的 admin 详情缓存（key 格式：eval:admin::{resultId}）。
     *
     * @param resultId 评定结果 ID
     */
    void evictAdminResult(Long resultId);
}
