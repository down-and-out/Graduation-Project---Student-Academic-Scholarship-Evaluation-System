package com.scholarship.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis缓存配置类
 * <p>
 * 配置Redis相关的组件，包括：
 * - RedisTemplate: 用于操作Redis的模板类
 * - CacheManager: Spring Cache缓存管理器
 * - 序列化配置: 使用JSON序列化替代默认的JDK序列化
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching  // 启用Spring Cache缓存注解
public class RedisConfig {

    /**
     * 缓存过期时间常量（单位：秒）
     */
    private static final long CACHE_EXPIRATION_SECONDS = 3600;  // 1小时
    private static final long USER_CACHE_EXPIRATION_SECONDS = 1800;  // 30分钟

    /**
     * 配置RedisTemplate
     * <p>
     * RedisTemplate是Spring Data Redis提供的核心操作类
     * 这里配置了key和value的序列化方式
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return RedisTemplate对象
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 创建Jackson序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = createJacksonSerializer();

        // 使用String序列化器作为key的序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // 设置key的序列化方式
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // 设置value的序列化方式
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        // 设置默认的序列化方式
        template.setDefaultSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * 配置CacheManager
     * <p>
     * CacheManager是Spring Cache的缓存管理器，用于管理各种缓存
     * 这里配置了Redis缓存的过期时间和序列化方式
     * </p>
     *
     * @param connectionFactory Redis连接工厂
     * @return CacheManager对象
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建Jackson序列化器
        GenericJackson2JsonRedisSerializer jsonSerializer = createJacksonSerializer();

        // 配置缓存规则
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // 设置过期时间：1小时
                .entryTtl(Duration.ofSeconds(CACHE_EXPIRATION_SECONDS))
                // 设置key的序列化方式
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // 设置value的序列化方式
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                // 不缓存null值
                .disableCachingNullValues();

        // 构建CacheManager
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                // 可以针对特定的缓存名称设置不同的过期时间
                .withCacheConfiguration("users",
                        config.entryTtl(Duration.ofSeconds(USER_CACHE_EXPIRATION_SECONDS)))
                .build();
    }

    /**
     * 创建Jackson序列化器
     * <p>
     * 配置ObjectMapper以支持：
     * - 所有字段的可见性
     * - 类型信息的保存（用于多态序列化）
     * - Java 8日期时间类型的支持
     * </p>
     *
     * @return GenericJackson2JsonRedisSerializer对象
     */
    private GenericJackson2JsonRedisSerializer createJacksonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();

        // 设置所有字段可见（包括private字段）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用类型信息（用于多态序列化）
        // 使用 NON_CONCRETE 仅对非具体类型（抽象类和接口）添加类型信息，更安全
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_CONCRETE,
                JsonTypeInfo.As.PROPERTY
        );

        // 注册Java 8日期时间模块
        objectMapper.registerModule(new JavaTimeModule());

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }
}
