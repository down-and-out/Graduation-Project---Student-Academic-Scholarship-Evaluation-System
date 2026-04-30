package com.scholarship.common.support;

import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.result.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

/**
 * 游标分页辅助工具
 * <p>
 * 使用 {@code WHERE id > lastId ORDER BY id ASC LIMIT size} 模式替代
 * {@code LIMIT offset, size}，避免深分页时扫描大量无用行，适合大数据量遍历场景。
 * </p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * CursorPageResult<SomeEntity> result = CursorPageHelper.fetchPage(lastId, pageSize,
 *     (cursorId, size) -> mapper.selectByCursor(cursorId, size));
 * }</pre>
 */
public final class CursorPageHelper {

    private CursorPageHelper() {
    }

    /**
     * 通用游标分页获取数据
     *
     * @param lastId    上一页最后一条记录的 ID，首次请求传 null
     * @param size      每页记录数
     * @param fetcher   数据获取函数，参数为 (lastId, limit)
     * @param <T>       数据类型
     * @return 游标分页结果，包含当前页数据和下一页游标
     */
    public static <T> CursorPageResult<T> fetchPage(Long lastId, Long size,
                                                     BiFunction<Long, Long, List<T>> fetcher) {
        if (size == null || size < 1 || size > 200) {
            size = 50L;
        }

        // 多取一条用于判断是否有下一页
        List<T> records = fetcher.apply(lastId, size + 1);

        boolean hasMore = records.size() > size;
        if (hasMore) {
            records = records.subList(0, size.intValue());
        }

        Long nextCursor = null;
        if (hasMore && !records.isEmpty()) {
            nextCursor = extractId(records.get(records.size() - 1));
        }

        return new CursorPageResult<>(records, nextCursor, hasMore);
    }

    private static Long extractId(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            return (Long) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            throw new IllegalArgumentException("Entity must have getId() method returning Long", e);
        }
    }

    @Data
    @AllArgsConstructor
    public static class CursorPageResult<T> {
        private List<T> records;
        private Long nextCursor;
        private boolean hasMore;
    }

    /**
     * 分页偏移量校验，防止深分页拖垮数据库
     *
     * @param current       当前页码
     * @param size          每页记录数
     * @param maxOffsetRows 允许的最大偏移行数
     * @throws BusinessException 偏移量超出限制时抛出
     */
    public static void validateOffset(Long current, Long size, long maxOffsetRows) {
        if (current == null || size == null || current < 1 || size < 1) {
            return;
        }
        long offset = (current - 1) * size;
        if (offset > maxOffsetRows) {
            throw new BusinessException(ResultCode.BAD_REQUEST,
                    "分页请求超出当前允许的深度，请缩小筛选范围后重试");
        }
    }
}
