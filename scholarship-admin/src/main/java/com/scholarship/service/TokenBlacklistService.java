package com.scholarship.service;

/**
 * Token 黑名单服务接口
 * <p>
 * 用于管理已注销的 JWT Token，实现 Token 主动失效功能
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface TokenBlacklistService {

    /**
     * 将 Token 加入黑名单
     *
     * @param token JWT Token
     * @param expireTime Token 剩余有效时间（秒）
     */
    void addToBlacklist(String token, long expireTime);

    /**
     * 判断 Token 是否在黑名单中
     *
     * @param token JWT Token
     * @return true-在黑名单中，false-不在黑名单中
     */
    boolean isBlacklisted(String token);

    /**
     * 从黑名单中移除 Token
     *
     * @param token JWT Token
     */
    void removeFromBlacklist(String token);

    /**
     * 获取黑名单当前大小
     *
     * @return 黑名单中的 Token 数量
     */
    long getBlacklistSize();

    /**
     * 定时校准黑名单计数器。
     *
     * <p>Redis key TTL 过期不会触发计数器递减，长时间运行会导致计数器虚高。
     * 每小时 SCAN 一次全量 key 数量并校正。</p>
     */
    void reconcileBlacklistCount();
}
