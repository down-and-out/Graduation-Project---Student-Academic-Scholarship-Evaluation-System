package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.enums.BatchStatusEnum;
import com.scholarship.exception.BusinessException;
import com.scholarship.mapper.EvaluationBatchMapper;
import com.scholarship.service.EvaluationBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评定批次服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class EvaluationBatchServiceImpl extends ServiceImpl<EvaluationBatchMapper, EvaluationBatch> implements EvaluationBatchService {

    @Override
    @Transactional
    public boolean startBatch(Long id) {
        log.info("开启批次，id={}", id);

        EvaluationBatch batch = getById(id);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        batch.setBatchStatus(BatchStatusEnum.APPLYING.getCode());
        return updateById(batch);
    }

    @Override
    @Transactional
    public boolean publishBatch(Long id) {
        log.info("发布批次，id={}", id);

        EvaluationBatch batch = getById(id);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        batch.setBatchStatus(BatchStatusEnum.NOT_STARTED.getCode());
        return updateById(batch);
    }

    @Override
    @Transactional
    public boolean closeBatch(Long id) {
        log.info("关闭批次，id={}", id);

        EvaluationBatch batch = getById(id);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        batch.setBatchStatus(BatchStatusEnum.COMPLETED.getCode());
        return updateById(batch);
    }

    @Override
    @Transactional
    public boolean startReview(Long id) {
        log.info("开始评审，id={}", id);

        EvaluationBatch batch = getById(id);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        batch.setBatchStatus(BatchStatusEnum.REVIEWING.getCode());
        return updateById(batch);
    }

    @Override
    @Transactional
    public boolean startPublicity(Long id) {
        log.info("开始公示，id={}", id);

        EvaluationBatch batch = getById(id);
        if (batch == null) {
            throw new BusinessException("批次不存在");
        }

        batch.setBatchStatus(BatchStatusEnum.PUBLICITY.getCode());
        return updateById(batch);
    }

    @Override
    public List<EvaluationBatch> listAvailableForApplication() {
        log.debug("查询申请中的批次列表");

        LambdaQueryWrapper<EvaluationBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EvaluationBatch::getBatchStatus, BatchStatusEnum.APPLYING.getCode());
        return list(wrapper);
    }
}
