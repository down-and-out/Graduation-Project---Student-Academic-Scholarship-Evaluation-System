package com.scholarship.service.impl;

import com.scholarship.service.TokenBlacklistService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务实现类
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
        String key = BLACKLIST_PREFIX + token;
        try {
            redisTemplate.opsForValue().set(key, "BLACKLISTED", expireTime, TimeUnit.SECONDS);
            Long count = redisTemplate.opsForValue().increment(BLACKLIST_COUNT_KEY);

            log.debug("Token added to blacklist, token={}, expireTime={}s, total={}", token, expireTime, count);

            if (count != null && count > maxCapacity) {
                log.warn("Token blacklist capacity exceeded, current={}, max={}", count, maxCapacity);
                asyncCleanupExpiringTokens();
            }
        } catch (Exception e) {
            log.error("Failed to add token to blacklist, key={}, expireTime={}s", key, expireTime, e);
            throw e;
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
            log.debug("Token removed from blacklist, token={}", token);
        }
    }

    @Override
    public long getBlacklistSize() {
        String count = redisTemplate.opsForValue().get(BLACKLIST_COUNT_KEY);
        return count != null ? Long.parseLong(count) : 0;
    }

    @Async
    public void asyncCleanupExpiringTokens() {
        log.info("Start async token blacklist cleanup, targetCount={}", cleanupCount);

        try {
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

            Collections.sort(tokenTTLList);

            int deletedCount = 0;
            int toDelete = Math.min(cleanupCount, tokenTTLList.size());

            for (int i = 0; i < toDelete; i++) {
                String key = tokenTTLList.get(i).key;
                Boolean deleted = redisTemplate.delete(key);
                if (Boolean.TRUE.equals(deleted)) {
                    deletedCount++;
                }
            }

            if (deletedCount > 0) {
                redisTemplate.opsForValue().decrement(BLACKLIST_COUNT_KEY, deletedCount);
            }

            log.info("Token blacklist cleanup finished, deleted={}, remaining={}", deletedCount, getBlacklistSize());
        } catch (Exception e) {
            log.error("Token blacklist cleanup failed", e);
        }
    }

    /**
     * 定时校准黑名单计数器。
     *
     * <p>Redis key TTL 过期不会触发计数器递减，长时间运行会导致计数器虚高。
     * 每 3 小时 SCAN 校正一次，单次最多扫描 5000 个 key 防止 Redis 阻塞。</p>
     */
    @Override
    @Scheduled(fixedDelay = 10_800_000)
    public void reconcileBlacklistCount() {
        try {
            int actualCount = 0;
            int maxScanKeys = 5000;
            try (Cursor<String> cursor = redisTemplate.scan(
                    ScanOptions.scanOptions().match(BLACKLIST_PREFIX + "*").count(500).build())) {
                while (cursor.hasNext() && actualCount < maxScanKeys) {
                    cursor.next();
                    actualCount++;
                }
            }
            long oldCount = getBlacklistSize();
            if (oldCount != actualCount) {
                redisTemplate.opsForValue().set(BLACKLIST_COUNT_KEY, String.valueOf(actualCount));
                log.info("Token blacklist count reconciled: {} → {}{}", oldCount, actualCount,
                        actualCount >= maxScanKeys ? " (capped)" : "");
            }
        } catch (Exception e) {
            log.warn("Token blacklist count reconciliation failed", e);
        }
    }

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
