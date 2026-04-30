package com.scholarship.service.impl;

import com.scholarship.common.support.CacheConstants;
import com.scholarship.service.CacheEvictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Redis SCAN+DELETE 的缓存失效实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheEvictionServiceImpl implements CacheEvictionService {

    private static final int SCAN_COUNT = 100;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void evictEvaluationResultsForBatch(Long batchId) {
        if (batchId == null) {
            return;
        }
        String batchSuffix = ":batch:" + batchId;
        scanAndDelete(CacheConstants.evalPrefix(), batchSuffix);
        // 管理员结果分页既可能按 batchId 查询，也可能按学年/学期汇总查询，批次重算后保守清理全部分页缓存。
        scanAndDelete(CacheConstants.evalPagePrefix(), null);
        scanByPattern(CacheConstants.evalPrefix() + "*:batch:latest");
        // 评定结果重建后，旧的详情缓存可能引用已被覆盖的结果记录，这里保守清空详情缓存。
        scanByPattern(CacheConstants.evalAdminPrefix() + "*");
        scanByPattern(CacheConstants.evalRankPrefix() + batchId + ":*");
    }

    @Override
    public void evictApplicationAchievementsForUser(Long userId) {
        if (userId == null) {
            return;
        }
        deleteExact(CacheConstants.appAchievementsPrefix() + userId);
    }

    @Override
    public void evictApplicationDetail(Long applicationId) {
        if (applicationId == null) {
            return;
        }
        deleteExact(CacheConstants.appDetailPrefix() + applicationId);
    }

    @Override
    public void evictApplicationPages() {
        scanAndDelete(CacheConstants.appPagePrefix(), null);
    }

    @Override
    public void evictTaskDetail(Long taskId) {
        if (taskId == null) {
            return;
        }
        deleteExact(CacheConstants.taskDetailPrefix() + taskId);
    }

    @Override
    public void evictBatchAvailable() {
        deleteExact(CacheConstants.batchAvailablePrefix() + "available");
    }

    @Override
    public void evictBatchDetail(Long batchId) {
        if (batchId == null) {
            return;
        }
        deleteExact(CacheConstants.batchDetailPrefix() + batchId);
    }

    @Override
    public void evictRuleCaches() {
        scanAndDelete(CacheConstants.ruleAvailablePrefix(), null);
        scanAndDelete(CacheConstants.ruleDetailPrefix(), null);
    }

    @Override
    public void evictAdminResult(Long resultId) {
        if (resultId == null) {
            return;
        }
        deleteExact(CacheConstants.evalAdminPrefix() + resultId);
    }

    private int scanAndDelete(String prefix, String suffix) {
        try {
            String pattern = prefix + "*";
            Integer deletedCount = redisTemplate.execute((RedisCallback<Integer>) connection -> {
                int count = 0;
                ScanOptions options = ScanOptions.scanOptions()
                        .match(pattern)
                        .count(SCAN_COUNT)
                        .build();
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    List<byte[]> batchKeys = new ArrayList<>();
                    while (cursor.hasNext()) {
                        byte[] rawKey = cursor.next();
                        String key = new String(rawKey, StandardCharsets.UTF_8);
                        if (suffix != null && !key.endsWith(suffix)) {
                            continue;
                        }
                        batchKeys.add(rawKey);
                    }
                    if (!batchKeys.isEmpty()) {
                        byte[][] keysArray = batchKeys.toArray(new byte[0][]);
                        Long deleted = connection.del(keysArray);
                        if (deleted != null) {
                            count = deleted.intValue();
                        }
                    }
                }
                return count;
            });
            int result = deletedCount != null ? deletedCount : 0;
            if (result > 0) {
                log.debug("Cache evicted: pattern={}, suffix={}, deleted={}", pattern, suffix, result);
            }
            return result;
        } catch (DataAccessException e) {
            log.warn("Cache eviction scan failed: prefix={}, suffix={}", prefix, suffix, e);
            return 0;
        }
    }

    private int scanByPattern(String pattern) {
        try {
            Integer deletedCount = redisTemplate.execute((RedisCallback<Integer>) connection -> {
                int count = 0;
                ScanOptions options = ScanOptions.scanOptions()
                        .match(pattern)
                        .count(SCAN_COUNT)
                        .build();
                try (Cursor<byte[]> cursor = connection.scan(options)) {
                    List<byte[]> batchKeys = new ArrayList<>();
                    while (cursor.hasNext()) {
                        batchKeys.add(cursor.next());
                    }
                    if (!batchKeys.isEmpty()) {
                        byte[][] keysArray = batchKeys.toArray(new byte[0][]);
                        Long deleted = connection.del(keysArray);
                        if (deleted != null) {
                            count = deleted.intValue();
                        }
                    }
                }
                return count;
            });
            int result = deletedCount != null ? deletedCount : 0;
            if (result > 0) {
                log.debug("Cache evicted by pattern: pattern={}, deleted={}", pattern, result);
            }
            return result;
        } catch (DataAccessException e) {
            log.warn("Cache eviction scan failed: pattern={}", pattern, e);
            return 0;
        }
    }

    private void deleteExact(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Cache key evicted: {}", key);
            }
        } catch (DataAccessException e) {
            log.warn("Cache eviction delete failed: key={}", key, e);
        }
    }
}
