package com.scholarship.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 缓存失效事件，在事务成功提交后由 {@link com.scholarship.common.listener.CacheEvictionListener} 处理。
 *
 * <p>使用 Spring {@code @TransactionalEventListener(phase = AFTER_COMMIT)} 确保缓存
 * 仅在事务提交后才清除，避免事务回滚后缓存已误删导致的性能退化。</p>
 */
@Getter
public class CacheEvictionEvent extends ApplicationEvent {

    private final CacheEvictionOperation operation;
    private final Long target;

    /**
     * @param source    事件发布者（通常是 ServiceImpl）
     * @param operation 缓存清除操作类型
     * @param target    目标 ID（如 batchId、applicationId、userId），无目标时传 null
     */
    public CacheEvictionEvent(Object source, CacheEvictionOperation operation, Long target) {
        super(source);
        this.operation = operation;
        this.target = target;
    }
}
