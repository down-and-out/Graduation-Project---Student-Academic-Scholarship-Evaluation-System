package com.scholarship.service.impl;

import com.scholarship.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务实现类
 * <p>
 * 使用 Redis 存储已注销的 Token，在 Token 自然过期前拒绝其访问
 * 支持最大容量限制，超过容量时异步清理即将过期的 Token
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";
    private static final String BLACKLIST_COUNT_KEY = "token:blacklist:count";

    private final StringRedisTemplate redisTemplate;

    @Value("${token.blacklist.max-capacity:10000}")
    private int maxCapacity;

    @Value("${token.blacklist.cleanup-count:50}")
    private int cleanupCount;

    public TokenBlacklistServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addToBlacklist(String token, long expireTime) {
        // 1. 存入 Token
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "BLACKLISTED", expireTime, TimeUnit.SECONDS);

        // 2. 计数器 +1
        Long count = redisTemplate.opsForValue().increment(BLACKLIST_COUNT_KEY);

        log.debug("Token 已加入黑名单：token={}, expireTime={}s, total={}", token, expireTime, count);

        // 3. 检查是否超过容量，触发异步清理
        if (count != null && count > maxCapacity) {
            log.warn("Token 黑名单超过容量限制：current={}, max={}，触发异步清理", count, maxCapacity);
            asyncCleanupExpiringTokens();
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            redisTemplate.opsForValue().decrement(BLACKLIST_COUNT_KEY);
            log.debug("Token 已从黑名单移除：token={}", token);
        }
    }

    @Override
    public long getBlacklistSize() {
        String count = redisTemplate.opsForValue().get(BLACKLIST_COUNT_KEY);
        return count != null ? Long.parseLong(count) : 0;
    }

    /**
     * 异步清理即将过期的 Token
     * <p>
     * 使用 @Async 注解实现异步执行，避免阻塞主线程
     * 清理策略：按 TTL 升序排序，删除即将过期的 cleanupCount 个 Token
     * </p>
     */
    @Async
    public void asyncCleanupExpiringTokens() {
        log.info("开始异步清理 Token 黑名单，目标清理数量：{}", cleanupCount);

        try {
            // 1. 扫描黑名单 Key 并收集 TTL 信息
            List<TokenTTL> tokenTTLList = new ArrayList<>();

            try (Cursor<String> cursor = redisTemplate.scan(
                    ScanOptions.scanOptions().match(BLACKLIST_PREFIX + "*").count(1000).build()
            )) {
                while (cursor.hasNext()) {
                    String key = cursor.next();
                    Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                    if (ttl != null && ttl > 0) {
                        tokenTTLList.add(new TokenTTL(key, ttl));
                    }
                }
            }

            // 2. 按 TTL 升序排序（即将过期的在前）
            Collections.sort(tokenTTLList);

            // 3. 删除前 cleanupCount 个
            int deletedCount = 0;
            int toDelete = Math.min(cleanupCount, tokenTTLList.size());

            for (int i = 0; i < toDelete; i++) {
                String key = tokenTTLList.get(i).key;
                Boolean deleted = redisTemplate.delete(key);
                if (Boolean.TRUE.equals(deleted)) {
                    deletedCount++;
                }
            }

            // 4. 更新计数器
            if (deletedCount > 0) {
                redisTemplate.opsForValue().decrement(BLACKLIST_COUNT_KEY, deletedCount);
            }

            log.info("Token 黑名单清理完成：删除 {} 个，剩余 {} 个", deletedCount, getBlacklistSize());

        } catch (Exception e) {
            log.error("Token 黑名单清理失败", e);
        }
    }

    /**
     * Token TTL 辅助类
     */
    private static class TokenTTL implements Comparable<TokenTTL> {
        final String key;
        final long ttl;

        TokenTTL(String key, long ttl) {
            this.key = key;
            this.ttl = ttl;
        }

        @Override
        public int compareTo(TokenTTL other) {
            return Long.compare(this.ttl, other.ttl);
        }
    }
}
