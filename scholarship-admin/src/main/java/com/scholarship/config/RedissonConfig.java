package com.scholarship.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁配置
 * <p>
 * 使用单机模式连接 Redis，复用已有 spring.data.redis 连接信息。
 * Redisson 提供 Watchdog 自动续期机制，适合长时间异步任务（如评定计算）。
 * </p>
 */
@Configuration
public class RedissonConfig {

    private static final String REDIS_PROTOCOL_PREFIX = "redis://";

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Bean
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = REDIS_PROTOCOL_PREFIX + host + ":" + port;
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setPassword(password.isEmpty() ? null : password);
        return Redisson.create(config);
    }
}
