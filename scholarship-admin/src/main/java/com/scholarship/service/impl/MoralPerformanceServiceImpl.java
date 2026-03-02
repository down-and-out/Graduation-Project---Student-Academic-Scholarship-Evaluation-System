package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.MoralPerformance;
import com.scholarship.mapper.MoralPerformanceMapper;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.MoralPerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 德育表现服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class MoralPerformanceServiceImpl extends ServiceImpl<MoralPerformanceMapper, MoralPerformance>
        implements MoralPerformanceService {

    private final EvaluationBatchService evaluationBatchService;

    public MoralPerformanceServiceImpl(EvaluationBatchService evaluationBatchService) {
        this.evaluationBatchService = evaluationBatchService;
    }

    @Override
    public List<MoralPerformance> listByStudentId(Long studentId, Long batchId) {
        log.debug("查询德育表现，studentId={}, batchId={}", studentId, batchId);

        LambdaQueryWrapper<MoralPerformance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MoralPerformance::getStudentId, studentId)
                .eq(MoralPerformance::getAuditStatus, 1); // 只查询审核通过的

        // 如果指定了批次 ID，根据批次的学年筛选
        if (batchId != null) {
            EvaluationBatch batch = evaluationBatchService.getById(batchId);
            if (batch != null && batch.getAcademicYear() != null) {
                wrapper.eq(MoralPerformance::getAcademicYear, batch.getAcademicYear());
            }
        }

        wrapper.orderByDesc(MoralPerformance::getCreateTime);
        return list(wrapper);
    }

    @Override
    public BigDecimal calculateTotalScore(Long studentId, Long batchId) {
        log.debug("计算德育总分，studentId={}, batchId={}", studentId, batchId);

        List<MoralPerformance> performances = listByStudentId(studentId, batchId);

        if (performances.isEmpty()) {
            log.warn("未找到德育表现记录，studentId={}", studentId);
            return BigDecimal.ZERO;
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        for (MoralPerformance performance : performances) {
            if (performance.getScore() != null) {
                totalScore = totalScore.add(performance.getScore());
            }
        }

        log.debug("德育总分计算完成，studentId={}, totalScore={}", studentId, totalScore);
        return totalScore;
    }

    @Override
    public BigDecimal calculateScoreByType(Long studentId, Long batchId, Integer performanceType) {
        log.debug("按类型统计分数，studentId={}, batchId={}, type={}", studentId, batchId, performanceType);

        List<MoralPerformance> performances = list(
            new LambdaQueryWrapper<MoralPerformance>()
                .eq(MoralPerformance::getStudentId, studentId)
                .eq(MoralPerformance::getAuditStatus, 1)
                .eq(MoralPerformance::getPerformanceType, performanceType)
        );

        if (batchId != null) {
            EvaluationBatch batch = evaluationBatchService.getById(batchId);
            if (batch != null && batch.getAcademicYear() != null) {
                performances = performances.stream()
                    .filter(p -> batch.getAcademicYear().equals(p.getAcademicYear()))
                    .toList();
            }
        }

        BigDecimal totalScore = BigDecimal.ZERO;
        for (MoralPerformance performance : performances) {
            if (performance.getScore() != null) {
                totalScore = totalScore.add(performance.getScore());
            }
        }

        log.debug("类型分数统计完成，studentId={}, type={}, score={}", studentId, performanceType, totalScore);
        return totalScore;
    }
}
