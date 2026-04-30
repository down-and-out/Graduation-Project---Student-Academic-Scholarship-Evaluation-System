package com.scholarship.common.event;

import com.scholarship.service.CacheEvictionService;

/**
 * 缓存失效操作枚举，替换 {@link CacheEvictionEvent} 中的魔法字符串。
 *
 * <p>每个枚举值直接持有对 {@link CacheEvictionService} 对应方法的调用逻辑，
 * 消除 {@link com.scholarship.common.listener.CacheEvictionListener} 中的字符串 switch。</p>
 */
public enum CacheEvictionOperation {

    EVICT_EVALUATION_RESULTS_FOR_BATCH {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictEvaluationResultsForBatch(target);
        }
    },
    EVICT_APPLICATION_ACHIEVEMENTS_FOR_USER {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictApplicationAchievementsForUser(target);
        }
    },
    EVICT_APPLICATION_DETAIL {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictApplicationDetail(target);
        }
    },
    EVICT_APPLICATION_PAGES {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictApplicationPages();
        }
    },
    EVICT_TASK_DETAIL {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictTaskDetail(target);
        }
    },
    EVICT_BATCH_AVAILABLE {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictBatchAvailable();
        }
    },
    EVICT_BATCH_DETAIL {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictBatchDetail(target);
        }
    },
    EVICT_RULE_CACHES {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictRuleCaches();
        }
    },
    EVICT_ADMIN_RESULT {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictAdminResult(target);
        }
    },
    /** 复合操作：清除批次可用性缓存 + 批次详情缓存 */
    EVICT_BATCH_CACHES {
        @Override
        public void execute(CacheEvictionService service, Long target) {
            service.evictBatchAvailable();
            if (target != null) {
                service.evictBatchDetail(target);
            }
        }
    };

    /**
     * 对指定的 CacheEvictionService 执行驱逐操作。
     *
     * @param service 缓存驱逐服务
     * @param target  操作目标 ID（如 batchId、applicationId、userId），无参数时传 null
     */
    public abstract void execute(CacheEvictionService service, Long target);
}
