package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.event.CacheEvictionEvent;
import com.scholarship.common.event.CacheEvictionOperation;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.CacheConstants;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.EvaluationTask;
import com.scholarship.enums.BatchStatusEnum;
import com.scholarship.mapper.EvaluationBatchMapper;
import com.scholarship.mapper.EvaluationTaskMapper;
import com.scholarship.service.EvaluationBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * 评定批次服务实现类
 *
 * 功能说明：
 * - 分页查询评定批次（继承 BaseMapper 的自带的分页查询）
 * - 带缓存查询批次详情
 * - 新增/更新/删除评定批次（操作后清除缓存）
 * - 批次状态流转：开始申请、开始评审、开始公示、完成评定
 * - 查询申请中的批次列表（用于学生提交申请）
 * - 校验批次是否可评定
 *
 * 批次状态流转：
 * 1 未开始 → 2 申请中 → 3 评审中 → 4 公示中 → 5 已完成
 *
 * 缓存说明：
 * - 批次详情使用 @Cacheable 缓存（key: 批次ID）
 * - 可用批次列表使用 @Cacheable 缓存（key: 'available'）
 * - 新增、更新、删除操作后发布缓存清除事件
 *
 * 事务说明：
 * - 所有状态流转操作使用 @Transactional 保证一致性
 */
@Slf4j
@Service
public class EvaluationBatchServiceImpl extends ServiceImpl<EvaluationBatchMapper, EvaluationBatch>
        implements EvaluationBatchService {

    /** 评定任务类型标识 */
    private static final String TASK_TYPE_BATCH_EVALUATION = "BATCH_EVALUATION";
    /** 任务状态：待执行 */
    private static final int TASK_STATUS_PENDING = 0;
    /** 任务状态：执行中 */
    private static final int TASK_STATUS_RUNNING = 1;

    private final EvaluationTaskMapper evaluationTaskMapper;
    private final ApplicationEventPublisher eventPublisher;

    public EvaluationBatchServiceImpl(EvaluationTaskMapper evaluationTaskMapper,
                                      ApplicationEventPublisher eventPublisher) {
        this.evaluationTaskMapper = evaluationTaskMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 校验批次是否可评定
     * - 批次必须存在
     * - 批次状态必须为"评审中"
     *
     * @param batchId 批次ID
     */
    @Override
    public void validateForEvaluation(Long batchId) {
        EvaluationBatch batch = getById(batchId);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }
        if (!BatchStatusEnum.REVIEWING.getCode().equals(batch.getBatchStatus())) {
            throw new BusinessException("当前批次状态不允许评定");
        }
    }

    /**
     * 带缓存查询批次详情
     * 缓存键：批次ID
     */
    @Override
    @Cacheable(value = CacheConstants.BATCH_DETAIL, key = "#id")
    public EvaluationBatch getById(Serializable id) {
        return super.getById(id);
    }

    /**
     * 新增评定批次
     * 保存后清除缓存
     */
    @Override
    @Transactional
    public boolean save(EvaluationBatch entity) {
        boolean success = super.save(entity);
        if (success) {
            evictBatchCaches(entity.getId());
        }
        return success;
    }

    /**
     * 更新评定批次
     * 更新后清除缓存
     */
    @Override
    @Transactional
    public boolean updateById(EvaluationBatch entity) {
        boolean success = super.updateById(entity);
        if (success) {
            evictBatchCaches(entity.getId());
        }
        return success;
    }

    /**
     * 删除评定批次
     * 删除后清除缓存
     */
    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        boolean success = super.removeById(id);
        if (success && id instanceof Long batchId) {
            evictBatchCaches(batchId);
        }
        return success;
    }

    /**
     * 开始申请
     * 将批次从未开始流转为申请中
     */
    @Override
    @Transactional
    public boolean startBatch(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.APPLYING, "开始申请");
    }

    /**
     * 发布批次（兼容旧入口）
     * 将批次流转为未开始状态
     */
    @Override
    @Transactional
    public boolean publishBatch(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.NOT_STARTED, "发布批次");
    }

    /**
     * 完成评定
     * 将批次从公示中流转为已完成
     */
    @Override
    @Transactional
    public boolean closeBatch(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.COMPLETED, "完成评定");
    }

    /**
     * 开始评审
     * 将批次从申请中流转为评审中
     */
    @Override
    @Transactional
    public boolean startReview(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.REVIEWING, "开始评审");
    }

    /**
     * 开始公示
     * 将批次从评审中流转为公示中
     * 注意：如果仍有进行中的评定任务，不允许开始公示
     */
    @Override
    @Transactional
    public boolean startPublicity(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.PUBLICITY, "开始公示");
    }

    /**
     * 查询申请中的批次列表（用于学生提交申请）
     * 缓存键：'available'，同步刷新
     */
    @Override
    @Cacheable(value = CacheConstants.BATCH_AVAILABLE, key = "'available'", sync = true)
    public List<EvaluationBatch> listAvailableForApplication() {
        log.debug("查询申请中的批次列表");

        LambdaQueryWrapper<EvaluationBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationBatch::getBatchStatus, BatchStatusEnum.APPLYING.getCode());
        wrapper.orderByDesc(EvaluationBatch::getCreateTime);
        return list(wrapper);
    }

    /**
     * 更新批次状态（核心状态流转逻辑）
     *
     * @param id          批次ID
     * @param targetStatus 目标状态
     * @param actionName   操作名称（用于日志）
     * @return 操作结果
     */
    private boolean updateBatchStatus(Long id, BatchStatusEnum targetStatus, String actionName) {
        log.info("{}，id={}, targetStatus={}", actionName, id, targetStatus);

        // 1. 获取批次
        EvaluationBatch batch = getById(id);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        // 2. 获取当前状态
        BatchStatusEnum currentStatus = BatchStatusEnum.getByCode(batch.getBatchStatus());
        if (currentStatus == null) {
            throw new BusinessException("批次状态非法，无法执行" + actionName);
        }

        // 3. 校验状态流转是否合法
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new BusinessException("当前状态不允许执行" + actionName + "：" + currentStatus.getDescription() + " -> " + targetStatus.getDescription());
        }

        // 4. 如果是开始公示，校验是否有进行中的评定任务
        if (BatchStatusEnum.PUBLICITY == targetStatus && hasActiveEvaluationTask(id)) {
            throw new BusinessException("当前批次仍有评定任务执行中，暂不能开始公示");
        }

        // 5. 如果状态未变化，直接返回成功（幂等）
        if (currentStatus == targetStatus) {
            return true;
        }

        // 6. 更新状态
        batch.setBatchStatus(targetStatus.getCode());
        return updateById(batch);
    }

    /**
     * 判断批次是否有进行中的评定任务
     * 用于在开始公示前检查是否有异步评定任务还在执行
     */
    private boolean hasActiveEvaluationTask(Long batchId) {
        Long activeCount = evaluationTaskMapper.selectCount(new LambdaQueryWrapper<EvaluationTask>()
                .eq(EvaluationTask::getBatchId, batchId)
                .eq(EvaluationTask::getTaskType, TASK_TYPE_BATCH_EVALUATION)
                .in(EvaluationTask::getStatus, TASK_STATUS_PENDING, TASK_STATUS_RUNNING));
        return activeCount != null && activeCount > 0;
    }

    /**
     * 清除批次相关缓存
     * 发布缓存清除事件
     */
    private void evictBatchCaches(Long batchId) {
        eventPublisher.publishEvent(new CacheEvictionEvent(this, CacheEvictionOperation.EVICT_BATCH_CACHES, batchId));
    }
}
