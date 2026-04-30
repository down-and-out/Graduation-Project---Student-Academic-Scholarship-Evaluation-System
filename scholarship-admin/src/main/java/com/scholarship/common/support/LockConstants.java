package com.scholarship.common.support;

/**
 * 分布式锁 Key 常量。
 *
 * <p>全项目集中管理所有 Redis/Redisson 分布式锁 Key 前缀，
 * 避免散落在各个 ServiceImpl 中难以维护。</p>
 *
 * @author Scholarship Development Team
 */
public final class LockConstants {

    private LockConstants() {
        // 工具类禁止实例化
    }

    // ======================== 评定模块 (Redisson) ========================

    /** 评定任务创建锁，key 格式: lock:evaluation:task:create:{batchId} */
    public static final String TASK_CREATE = "lock:evaluation:task:create:";
    /** 评定任务执行锁，key 格式: lock:evaluation:task:execute:{taskId} */
    public static final String TASK_EXECUTE = "lock:evaluation:task:execute:";
    /** 批次评定锁，key 格式: lock:evaluation:batch:{batchId} */
    public static final String BATCH_EVALUATE = "lock:evaluation:batch:";

    // ======================== 申请模块 (Redisson) ========================

    /** 申请提交锁，key 格式: lock:application:submit:{studentId}:{batchId} */
    public static final String APPLICATION_SUBMIT = "lock:application:submit:";
    /** 申请审核锁，key 格式: lock:review:application:{applicationId} */
    public static final String REVIEW_APPLICATION = "lock:review:application:";

    // ======================== 登录安全模块 (StringRedisTemplate + Lua) ========================

    /** 账号登录尝试计数，key 格式: login:attempt:account:{username} */
    public static final String LOGIN_ATTEMPT_ACCOUNT = "login:attempt:account:";
    /** 账号锁定标记，key 格式: login:lock:account:{username} */
    public static final String LOGIN_LOCK_ACCOUNT = "login:lock:account:";
    /** IP 登录尝试计数，key 格式: login:attempt:ip:{clientIp} */
    public static final String LOGIN_ATTEMPT_IP = "login:attempt:ip:";
    /** IP 锁定标记，key 格式: login:lock:ip:{clientIp} */
    public static final String LOGIN_LOCK_IP = "login:lock:ip:";
}
