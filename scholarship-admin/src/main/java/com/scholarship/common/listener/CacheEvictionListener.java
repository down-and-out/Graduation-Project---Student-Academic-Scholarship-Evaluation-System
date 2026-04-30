package com.scholarship.common.listener;

import com.scholarship.common.event.CacheEvictionEvent;
import com.scholarship.service.CacheEvictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 缓存失效监听器，在事务成功提交后执行缓存清除。
 *
 * <p>通过 {@code @TransactionalEventListener(phase = AFTER_COMMIT)} 保证
 * 清除操作推迟到事务成功提交之后执行，避免：
 * <ul>
 *   <li>事务回滚 → 缓存误删 → 重新查库（性能影响）</li>
 *   <li>缓存清除但事务未提交 → 其他请求在窗口期读到旧数据（脏读）</li>
 * </ul>
 *
 * 清除失败时记录 WARN 日志，避免静默吞异常。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheEvictionListener {

    private final CacheEvictionService cacheEvictionService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCacheEviction(CacheEvictionEvent event) {
        try {
            event.getOperation().execute(cacheEvictionService, event.getTarget());
        } catch (Exception e) {
            log.warn("Cache eviction failed after commit: operation={}, target={}",
                    event.getOperation(), event.getTarget(), e);
        }
    }
}
