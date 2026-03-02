package com.scholarship.service.impl;

import com.scholarship.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务实现类
 * <p>
 * 使用 Redis 存储已注销的 Token，在 Token 自然过期前拒绝其访问
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    private final StringRedisTemplate redisTemplate;

    @Override
    public void addToBlacklist(String token, long expireTime) {
        String key = BLACKLIST_PREFIX + token;
        // 将 Token 存入 Redis，设置剩余有效期
        redisTemplate.opsForValue().set(key, "BLACKLISTED", expireTime, TimeUnit.SECONDS);
        log.debug("Token 已加入黑名单：token={}, expireTime={}s", token, expireTime);
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
        redisTemplate.delete(key);
        log.debug("Token 已从黑名单移除：token={}", token);
    }
}
