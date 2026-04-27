package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.enums.BatchStatusEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.mapper.EvaluationBatchMapper;
import com.scholarship.service.EvaluationBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class EvaluationBatchServiceImpl extends ServiceImpl<EvaluationBatchMapper, EvaluationBatch> implements EvaluationBatchService {

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

        if (currentStatus == targetStatus) {
            return true;
        }

        batch.setBatchStatus(targetStatus.getCode());
        return updateById(batch);
    }
}
