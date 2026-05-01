package com.scholarship.common.support;

import com.scholarship.common.exception.BusinessException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redisson 分布式锁操作工具类，统一封装 tryLock / unlock 模板逻辑。
 *
 * <p>覆盖阻塞锁、tryLock、tryLock+fallback 三种模式，每种模式提供有返回值和无返回值两个变体。</p>
 *
 * @author Scholarship Development Team
 */
public final class RedissonLockSupport {

    private RedissonLockSupport() {
        // 工具类禁止实例化
    }

    /**
     * 阻塞锁 + 有返回值。
     *
     * @param client   RedissonClient
     * @param key      锁 Key
     * @param supplier 业务逻辑（有返回值）
     * @param <T>      返回值类型
     * @return 业务逻辑的返回值
     */
    public static <T> T executeWithLock(RedissonClient client, String key, Supplier<T> supplier) {
        RLock lock = client.getLock(key);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 阻塞锁 + 无返回值。
     *
     * @param client   RedissonClient
     * @param key      锁 Key
     * @param runnable 业务逻辑（无返回值）
     */
    public static void executeWithLock(RedissonClient client, String key, Runnable runnable) {
        RLock lock = client.getLock(key);
        lock.lock();
        try {
            runnable.run();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * tryLock + 有返回值。
     *
     * @param client          RedissonClient
     * @param key             锁 Key
     * @param waitTime        等待获取锁的时间
     * @param leaseTime       锁租约时间
     * @param unit            时间单位
     * @param supplier        业务逻辑（有返回值）
     * @param lockBusyMessage 锁被占用时抛出的异常消息
     * @param <T>             返回值类型
     * @return 业务逻辑的返回值
     */
    public static <T> T executeWithTryLock(RedissonClient client, String key,
                                           long waitTime, long leaseTime, TimeUnit unit,
                                           Supplier<T> supplier, String lockBusyMessage) {
        RLock lock = client.getLock(key);
        boolean locked;
        try {
            locked = lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(lockBusyMessage);
        }
        if (!locked) {
            throw new BusinessException(lockBusyMessage);
        }
        try {
            return supplier.get();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * tryLock + 无返回值。
     *
     * @param client          RedissonClient
     * @param key             锁 Key
     * @param waitTime        等待获取锁的时间
     * @param leaseTime       锁租约时间
     * @param unit            时间单位
     * @param runnable        业务逻辑（无返回值）
     * @param lockBusyMessage 锁被占用时抛出的异常消息
     */
    public static void executeWithTryLock(RedissonClient client, String key,
                                          long waitTime, long leaseTime, TimeUnit unit,
                                          Runnable runnable, String lockBusyMessage) {
        RLock lock = client.getLock(key);
        boolean locked;
        try {
            locked = lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(lockBusyMessage);
        }
        if (!locked) {
            throw new BusinessException(lockBusyMessage);
        }
        try {
            runnable.run();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * tryLock + 锁失败时执行 fallback。
     *
     * @param client          RedissonClient
     * @param key             锁 Key
     * @param waitTime        等待获取锁的时间
     * @param leaseTime       锁租约时间
     * @param unit            时间单位
     * @param supplier        获取锁成功后执行的业务逻辑
     * @param fallback        获取锁失败后执行的降级逻辑
     * @param lockBusyMessage 锁被占用且 fallback 抛出异常时的消息
     * @param <T>             返回值类型
     * @return supplier 或 fallback 的返回值
     */
    public static <T> T executeWithTryLockOrFallback(RedissonClient client, String key,
                                                      long waitTime, long leaseTime, TimeUnit unit,
                                                      Supplier<T> supplier, Supplier<T> fallback,
                                                      String lockBusyMessage) {
        RLock lock = client.getLock(key);
        boolean locked;
        try {
            locked = lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(lockBusyMessage);
        }
        if (!locked) {
            return fallback.get();
        }
        try {
            return supplier.get();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
