package com.scholarship.service.impl;

import com.scholarship.common.support.CacheConstants;
import com.scholarship.service.CacheEvictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 缓存失效服务实现：使用 Redis SCAN+DELETE 批量清除缓存。
 *
 * <p>统一使用 {@link StringRedisTemplate#execute(RedisCallback)} 方式
 * 执行 SCAN 命令，避免 {@code KEYS *} 阻塞 Redis。</p>
 *
 * @author Scholarship Development Team
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
        // 使用分隔符包围的精确匹配，避免 batch:1 误匹配 batch:10
        String batchSuffix = ":batch:" + batchId;
        scanAndDelete(CacheConstants.evalPrefix(), batchSuffix);
        scanAndDelete(CacheConstants.evalPagePrefix(), batchSuffix);
        // 清除所有学生的"最新批次"缓存（对应前端不传 batchId 的场景），
        // 因为任何批次数据变更后，"最新"可能已变化
        scanAndDelete(CacheConstants.evalPrefix(), ":batch:latest");
        // eval:admin key 不含 batchId（key 为 resultId），由调用方通过 evictAdminResult 精确清除
        // eval:rank key 格式为 {batchId}:{type}，使用直接 pattern 扫描
        String rankPattern = CacheConstants.evalRankPrefix() + batchId + ":*";
        scanByPattern(rankPattern);
    }

    @Override
    public void evictApplicationAchievementsForUser(Long userId) {
        if (userId == null) {
            return;
        }
        String keyPattern = CacheConstants.appAchievementsPrefix() + userId;
        deleteExact(keyPattern);
    }

    @Override
    public void evictApplicationDetail(Long applicationId) {
        if (applicationId == null) {
            return;
        }
        String keyPattern = CacheConstants.appDetailPrefix() + applicationId;
        deleteExact(keyPattern);
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
        String keyPattern = CacheConstants.taskDetailPrefix() + taskId;
        deleteExact(keyPattern);
    }

    @Override
    public void evictBatchAvailable() {
        String keyPattern = CacheConstants.batchAvailablePrefix() + "available";
        deleteExact(keyPattern);
    }

    @Override
    public void evictAdminResult(Long resultId) {
        if (resultId == null) {
            return;
        }
        deleteExact(CacheConstants.evalAdminPrefix() + resultId);
    }

    // ======================== internal helpers ========================

    /**
     * SCAN 匹配前缀并删除包含 suffix 的 key。
     * <p>若 suffix 为 null 则删除所有匹配前缀的 key。</p>
     *
     * @param prefix 缓存 key 前缀
     * @param suffix 可选后缀过滤，为 null 时不过滤
     * @return 删除数量
     */
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
                        if (suffix != null && !(key + ":").contains(suffix + ":")) {
                            continue;
                        }
                        batchKeys.add(rawKey);
                    }

                    // Delete in batch if possible (RedisConnection.del supports byte[]...)
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

    /**
     * 使用精确 pattern SCAN 并删除所有匹配的 key（不做 suffix 过滤）。
     *
     * @param pattern Redis SCAN pattern
     * @return 删除数量
     */
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

    /**
     * 精确删除指定 key（Spring Cache 格式为 cacheName::key，可直接拼出完整 key）。
     */
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
