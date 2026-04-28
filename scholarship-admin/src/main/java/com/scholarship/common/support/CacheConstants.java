package com.scholarship.common.support;

/**
 * 缓存Key常量、TTL配置与工具类。
 *
 * <p>Spring Cache 生成的 Redis Key 格式为 {@code {cacheName}::{key}}，
 * 例如 {@code eval:student::student:100:batch:5}。</p>
 *
 * <p>eviction 前缀模式方法供 {@link com.scholarship.service.CacheEvictionService}
 * 做 SCAN+DELETE 批量清除使用。</p>
 *
 * @author Scholarship Development Team
 */
public final class CacheConstants {

    private CacheConstants() {
        // 工具类禁止实例化
    }

    // ======================== Cache Names ========================

    public static final String SYS_SETTING = "sys:settings";
    public static final String SYS_SETTING_ACTIVE = "sys:settings:active";
    public static final String SYS_SETTINGS_ALL = "sys:settings:all";
    public static final String BATCH_AVAILABLE = "batch:available";
    public static final String TASK_DETAIL = "task:detail";
    public static final String APP_DETAIL = "app:detail";
    public static final String APP_ACHIEVEMENTS = "app:achievements";
    public static final String APP_PAGE = "app:page";
    public static final String EVAL_STUDENT = "eval:student";
    public static final String EVAL_PAGE = "eval:page";
    public static final String EVAL_ADMIN = "eval:admin";
    public static final String EVAL_RANK = "eval:rank";

    // ======================== TTL Constants (seconds) ========================

    public static final long TTL_SYS_SETTING = 3600L;
    public static final long TTL_SYS_SETTING_ACTIVE = 3600L;
    public static final long TTL_SYS_SETTINGS_ALL = 3600L;
    public static final long TTL_BATCH_AVAILABLE = 300L;
    public static final long TTL_TASK_DETAIL = 10L;
    public static final long TTL_APP_DETAIL = 300L;
    public static final long TTL_APP_ACHIEVEMENTS = 300L;
    public static final long TTL_APP_PAGE = 120L;
    public static final long TTL_EVAL_STUDENT = 120L;
    public static final long TTL_EVAL_PAGE = 120L;
    public static final long TTL_EVAL_ADMIN = 120L;
    public static final long TTL_EVAL_RANK = 120L;

    // ======================== Key Builders (for @Cacheable SpEL) ========================

    /**
     * 构建评定结果分页缓存 key。
     */
    public static String evalPageKey(Long current, Long size, Long batchId, String academicYear,
                                     Integer semester, Long studentId, Integer status, String keyword) {
        StringBuilder sb = new StringBuilder();
        sb.append("page:").append(current).append(':').append(size);
        appendIfNotNull(sb, ":batch:", batchId);
        appendIfNotNull(sb, ":year:", academicYear);
        appendIfNotNull(sb, ":sem:", semester);
        appendIfNotNull(sb, ":stu:", studentId);
        appendIfNotNull(sb, ":status:", status);
        appendIfNotNull(sb, ":kw:", keyword);
        return sb.toString();
    }

    /**
     * 构建申请分页缓存 key。
     */
    public static String appPageKey(Long current, Long size, Long batchId, Long studentId, Integer status) {
        StringBuilder sb = new StringBuilder();
        sb.append("page:").append(current).append(':').append(size);
        appendIfNotNull(sb, ":batch:", batchId);
        appendIfNotNull(sb, ":stu:", studentId);
        appendIfNotNull(sb, ":status:", status);
        return sb.toString();
    }

    // ======================== Eviction Patterns (for SCAN+DELETE) ========================

    /**
     * 评定结果相关所有缓存前缀（按 batchId 批量清除时使用）。
     */
    public static String evalPrefix() {
        return EVAL_STUDENT + "::";
    }

    public static String evalPagePrefix() {
        return EVAL_PAGE + "::";
    }

    public static String evalAdminPrefix() {
        return EVAL_ADMIN + "::";
    }

    public static String evalRankPrefix() {
        return EVAL_RANK + "::";
    }

    /**
     * 申请详情缓存前缀（按 applicationId 清除）。
     */
    public static String appDetailPrefix() {
        return APP_DETAIL + "::";
    }

    /**
     * 申请分页缓存前缀（全量清除）。
     */
    public static String appPagePrefix() {
        return APP_PAGE + "::";
    }

    public static String appAchievementsPrefix() {
        return APP_ACHIEVEMENTS + "::";
    }

    public static String taskDetailPrefix() {
        return TASK_DETAIL + "::";
    }

    public static String batchAvailablePrefix() {
        return BATCH_AVAILABLE + "::";
    }

    // ======================== internal helpers ========================

    private static void appendIfNotNull(StringBuilder sb, String prefix, Object value) {
        if (value != null) {
            sb.append(prefix).append(value);
        }
    }
}
