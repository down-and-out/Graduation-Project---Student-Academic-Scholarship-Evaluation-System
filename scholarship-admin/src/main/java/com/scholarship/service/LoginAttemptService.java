package com.scholarship.service;

/**
 * 登录限流服务接口
 * <p>
 * 用于防止暴力破解密码，限制同一 IP 或用户名的登录尝试次数
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface LoginAttemptService {

    /**
     * 记录登录失败
     *
     * @param identifier 标识符（IP 地址或用户名）
     */
    void recordFailure(String identifier);

    /**
     * 重置登录失败次数
     *
     * @param identifier 标识符（IP 地址或用户名）
     */
    void resetFailures(String identifier);

    /**
     * 检查是否已被锁定
     *
     * @param identifier 标识符（IP 地址或用户名）
     * @return true-已锁定，false-未锁定
     */
    boolean isLocked(String identifier);

    /**
     * 获取剩余锁定时间（秒）
     *
     * @param identifier 标识符（IP 地址或用户名）
     * @return 剩余锁定时间（秒），0 表示未锁定
     */
    long getRemainingLockTime(String identifier);
}
