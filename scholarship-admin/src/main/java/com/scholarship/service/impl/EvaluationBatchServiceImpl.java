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

@Slf4j
@Service
public class EvaluationBatchServiceImpl extends ServiceImpl<EvaluationBatchMapper, EvaluationBatch>
        implements EvaluationBatchService {

    private static final String TASK_TYPE_BATCH_EVALUATION = "BATCH_EVALUATION";
    private static final int TASK_STATUS_PENDING = 0;
    private static final int TASK_STATUS_RUNNING = 1;

    private final EvaluationTaskMapper evaluationTaskMapper;
    private final ApplicationEventPublisher eventPublisher;

    public EvaluationBatchServiceImpl(EvaluationTaskMapper evaluationTaskMapper,
                                      ApplicationEventPublisher eventPublisher) {
        this.evaluationTaskMapper = evaluationTaskMapper;
        this.eventPublisher = eventPublisher;
    }

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

    @Override
    @Cacheable(value = CacheConstants.BATCH_DETAIL, key = "#id")
    public EvaluationBatch getById(Serializable id) {
        return super.getById(id);
    }

    @Override
    @Transactional
    public boolean save(EvaluationBatch entity) {
        boolean success = super.save(entity);
        if (success) {
            evictBatchCaches(entity.getId());
        }
        return success;
    }

    @Override
    @Transactional
    public boolean updateById(EvaluationBatch entity) {
        boolean success = super.updateById(entity);
        if (success) {
            evictBatchCaches(entity.getId());
        }
        return success;
    }

    @Override
    @Transactional
    public boolean removeById(Serializable id) {
        boolean success = super.removeById(id);
        if (success && id instanceof Long batchId) {
            evictBatchCaches(batchId);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean startBatch(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.APPLYING, "开始申请");
    }

    @Override
    @Transactional
    public boolean publishBatch(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.NOT_STARTED, "发布批次");
    }

    @Override
    @Transactional
    public boolean closeBatch(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.COMPLETED, "完成评定");
    }

    @Override
    @Transactional
    public boolean startReview(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.REVIEWING, "开始评审");
    }

    @Override
    @Transactional
    public boolean startPublicity(Long id) {
        return updateBatchStatus(id, BatchStatusEnum.PUBLICITY, "开始公示");
    }

    @Override
    @Cacheable(value = CacheConstants.BATCH_AVAILABLE, key = "'available'", sync = true)
    public List<EvaluationBatch> listAvailableForApplication() {
        log.debug("查询申请中的批次列表");

        LambdaQueryWrapper<EvaluationBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationBatch::getBatchStatus, BatchStatusEnum.APPLYING.getCode());
        wrapper.orderByDesc(EvaluationBatch::getCreateTime);
        return list(wrapper);
    }

    private boolean updateBatchStatus(Long id, BatchStatusEnum targetStatus, String actionName) {
        log.info("{}，id={}, targetStatus={}", actionName, id, targetStatus);

        EvaluationBatch batch = getById(id);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        BatchStatusEnum currentStatus = BatchStatusEnum.getByCode(batch.getBatchStatus());
        if (currentStatus == null) {
            throw new BusinessException("批次状态非法，无法执行" + actionName);
        }

        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new BusinessException("当前状态不允许执行" + actionName + "：" + currentStatus.getDescription() + " -> " + targetStatus.getDescription());
        }

        if (BatchStatusEnum.PUBLICITY == targetStatus && hasActiveEvaluationTask(id)) {
            throw new BusinessException("当前批次仍有评定任务执行中，暂不能开始公示");
        }

        if (currentStatus == targetStatus) {
            return true;
        }

        batch.setBatchStatus(targetStatus.getCode());
        return updateById(batch);
    }

    private boolean hasActiveEvaluationTask(Long batchId) {
        Long activeCount = evaluationTaskMapper.selectCount(new LambdaQueryWrapper<EvaluationTask>()
                .eq(EvaluationTask::getBatchId, batchId)
                .eq(EvaluationTask::getTaskType, TASK_TYPE_BATCH_EVALUATION)
                .in(EvaluationTask::getStatus, TASK_STATUS_PENDING, TASK_STATUS_RUNNING));
        return activeCount != null && activeCount > 0;
    }

    private void evictBatchCaches(Long batchId) {
        eventPublisher.publishEvent(new CacheEvictionEvent(this, CacheEvictionOperation.EVICT_BATCH_CACHES, batchId));
    }
}
