package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.CourseScore;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.mapper.CourseScoreMapper;
import com.scholarship.service.CourseScoreService;
import com.scholarship.service.EvaluationBatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 课程成绩服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class CourseScoreServiceImpl extends ServiceImpl<CourseScoreMapper, CourseScore>
        implements CourseScoreService {

    private final EvaluationBatchService evaluationBatchService;

    public CourseScoreServiceImpl(EvaluationBatchService evaluationBatchService) {
        this.evaluationBatchService = evaluationBatchService;
    }

    @Override
    public List<CourseScore> listByStudentId(Long studentId, Long batchId) {
        log.debug("查询学生成绩，studentId={}, batchId={}", studentId, batchId);

        LambdaQueryWrapper<CourseScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseScore::getStudentId, studentId);

        // 如果指定了批次 ID，根据批次的学年筛选成绩
        if (batchId != null) {
            EvaluationBatch batch = evaluationBatchService.getById(batchId);
            if (batch != null && batch.getAcademicYear() != null) {
                wrapper.eq(CourseScore::getAcademicYear, batch.getAcademicYear());
            }
        }

        wrapper.orderByDesc(CourseScore::getExamDate);
        return list(wrapper);
    }

    @Override
    public BigDecimal calculateWeightedAverage(Long studentId, Long batchId) {
        log.debug("计算加权平均分，studentId={}, batchId={}", studentId, batchId);

        List<CourseScore> scores = listByStudentId(studentId, batchId);

        if (scores.isEmpty()) {
            log.warn("未找到学生成绩，studentId={}", studentId);
            return BigDecimal.ZERO;
        }

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (CourseScore score : scores) {
            BigDecimal credit = score.getCredit();
            BigDecimal scoreValue = score.getScore();

            if (credit != null && scoreValue != null) {
                totalWeightedScore = totalWeightedScore.add(scoreValue.multiply(credit));
                totalCredits = totalCredits.add(credit);
            }
        }

        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal weightedAverage = totalWeightedScore.divide(totalCredits, 2, RoundingMode.HALF_UP);
        log.debug("加权平均分计算完成，studentId={}, weightedAverage={}", studentId, weightedAverage);

        return weightedAverage;
    }

    @Override
    public BigDecimal calculateTotalCredits(Long studentId) {
        log.debug("计算总学分，studentId={}", studentId);

        List<CourseScore> scores = list(
            new LambdaQueryWrapper<CourseScore>()
                .eq(CourseScore::getStudentId, studentId)
                .eq(CourseScore::getDeleted, 0)
        );

        BigDecimal totalCredits = BigDecimal.ZERO;
        for (CourseScore score : scores) {
            if (score.getCredit() != null && score.getScore() != null && score.getScore().compareTo(BigDecimal.valueOf(60)) >= 0) {
                totalCredits = totalCredits.add(score.getCredit());
            }
        }

        log.debug("总学分计算完成，studentId={}, totalCredits={}", studentId, totalCredits);
        return totalCredits;
    }

    @Override
    public BigDecimal calculateAverageGPA(Long studentId) {
        log.debug("计算平均绩点，studentId={}", studentId);

        List<CourseScore> scores = list(
            new LambdaQueryWrapper<CourseScore>()
                .eq(CourseScore::getStudentId, studentId)
                .eq(CourseScore::getDeleted, 0)
        );

        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalGPA = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (CourseScore score : scores) {
            BigDecimal credit = score.getCredit();
            BigDecimal gpa = score.getGpa();

            if (credit != null && gpa != null) {
                totalGPA = totalGPA.add(gpa.multiply(credit));
                totalCredits = totalCredits.add(credit);
            }
        }

        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal averageGPA = totalGPA.divide(totalCredits, 2, RoundingMode.HALF_UP);
        log.debug("平均绩点计算完成，studentId={}, averageGPA={}", studentId, averageGPA);

        return averageGPA;
    }

    @Override
    public Map<Long, BigDecimal> mapWeightedAverageByStudentIds(List<Long> studentIds, Long batchId) {
        if (studentIds == null || studentIds.isEmpty()) {
            return Map.of();
        }

        log.debug("批量计算加权平均分，studentCount={}, batchId={}", studentIds.size(), batchId);

        // 获取批次的学年
        String academicYear = null;
        if (batchId != null) {
            EvaluationBatch batch = evaluationBatchService.getById(batchId);
            if (batch != null) {
                academicYear = batch.getAcademicYear();
            }
        }

        // 批量查询所有学生的成绩
        LambdaQueryWrapper<CourseScore> wrapper = new LambdaQueryWrapper<CourseScore>()
            .in(CourseScore::getStudentId, studentIds);
        if (academicYear != null) {
            wrapper.eq(CourseScore::getAcademicYear, academicYear);
        }
        List<CourseScore> allScores = list(wrapper);

        // 按学生 ID 分组
        Map<Long, List<CourseScore>> scoresByStudent = allScores.stream()
            .collect(java.util.stream.Collectors.groupingBy(CourseScore::getStudentId));

        // 计算每个学生的加权平均分
        Map<Long, BigDecimal> result = new HashMap<>();
        for (Long studentId : studentIds) {
            List<CourseScore> scores = scoresByStudent.getOrDefault(studentId, List.of());

            if (scores.isEmpty()) {
                result.put(studentId, BigDecimal.ZERO);
                continue;
            }

            BigDecimal totalWeightedScore = BigDecimal.ZERO;
            BigDecimal totalCredits = BigDecimal.ZERO;

            for (CourseScore score : scores) {
                BigDecimal credit = score.getCredit();
                BigDecimal scoreValue = score.getScore();

                if (credit != null && scoreValue != null) {
                    totalWeightedScore = totalWeightedScore.add(scoreValue.multiply(credit));
                    totalCredits = totalCredits.add(credit);
                }
            }

            if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
                result.put(studentId, BigDecimal.ZERO);
            } else {
                result.put(studentId, totalWeightedScore.divide(totalCredits, 2, RoundingMode.HALF_UP));
            }
        }

        log.debug("批量计算加权平均分完成，resultCount={}", result.size());
        return result;
    }
}
