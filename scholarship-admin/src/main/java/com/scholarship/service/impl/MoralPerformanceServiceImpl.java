package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.dto.query.MoralPerformanceQuery;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.entity.MoralPerformance;
import com.scholarship.enums.AuditStatusEnum;
import com.scholarship.exception.BusinessException;
import com.scholarship.mapper.MoralPerformanceMapper;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.MoralPerformanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public Map<Long, BigDecimal> mapTotalScoreByStudentIds(List<Long> studentIds, Long batchId) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        log.debug("批量计算德育总分，studentCount={}, batchId={}", studentIds.size(), batchId);

        // 获取批次的学年
        String academicYear = null;
        if (batchId != null) {
            EvaluationBatch batch = evaluationBatchService.getById(batchId);
            if (batch != null) {
                academicYear = batch.getAcademicYear();
            }
        }

        // 批量查询所有学生的德育表现
        LambdaQueryWrapper<MoralPerformance> wrapper = new LambdaQueryWrapper<MoralPerformance>()
            .in(MoralPerformance::getStudentId, studentIds)
            .eq(MoralPerformance::getAuditStatus, 1); // 审核通过
        if (academicYear != null) {
            wrapper.eq(MoralPerformance::getAcademicYear, academicYear);
        }
        List<MoralPerformance> allPerformances = list(wrapper);

        // 按学生 ID 分组
        Map<Long, List<MoralPerformance>> performancesByStudent = allPerformances.stream()
            .collect(Collectors.groupingBy(MoralPerformance::getStudentId));

        // 计算每个学生的德育总分
        Map<Long, BigDecimal> result = new HashMap<>();
        for (Long studentId : studentIds) {
            List<MoralPerformance> performances = performancesByStudent.getOrDefault(studentId, List.of());

            BigDecimal totalScore = BigDecimal.ZERO;
            for (MoralPerformance performance : performances) {
                if (performance.getScore() != null) {
                    totalScore = totalScore.add(performance.getScore());
                }
            }
            result.put(studentId, totalScore);
        }

        log.debug("批量计算德育总分完成，resultCount={}", result.size());
        return result;
    }

    @Override
    public IPage<MoralPerformance> queryPage(MoralPerformanceQuery query) {
        log.debug("分页查询德育表现，query={}", query);

        Page<MoralPerformance> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<MoralPerformance> wrapper = new LambdaQueryWrapper<>();

        if (query.getStudentId() != null) {
            wrapper.eq(MoralPerformance::getStudentId, query.getStudentId());
        }
        if (query.getPerformanceType() != null) {
            wrapper.eq(MoralPerformance::getPerformanceType, query.getPerformanceType());
        }
        if (query.getAcademicYear() != null) {
            wrapper.eq(MoralPerformance::getAcademicYear, query.getAcademicYear());
        }
        if (query.getAuditStatus() != null) {
            wrapper.eq(MoralPerformance::getAuditStatus, query.getAuditStatus());
        }

        wrapper.orderByDesc(MoralPerformance::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional
    public boolean audit(Long id, Integer auditStatus, String auditComment, Long auditorId) {
        log.info("审核德育表现，id={}, status={}, auditorId={}", id, auditStatus, auditorId);

        // 验证审核状态是否有效
        if (!AuditStatusEnum.isValid(auditStatus)) {
            throw new BusinessException("无效的审核状态");
        }

        MoralPerformance performance = getById(id);
        if (performance == null) {
            throw new BusinessException("记录不存在");
        }

        performance.setAuditStatus(auditStatus);
        performance.setAuditComment(auditComment);
        performance.setAuditorId(auditorId);
        performance.setAuditTime(LocalDateTime.now());

        return updateById(performance);
    }
}
