package com.scholarship.service;

import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.ScholarshipApplication;

import java.math.BigDecimal;

/**
 * 评定计算服务接口
 * <p>
 * 负责计算学生的各项评分，包括课程成绩、科研成果、竞赛获奖、综合素质等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface EvaluationCalculationService {

    /**
     * 计算单个申请的总分
     * <p>
     * 根据评分规则计算该申请的各项分数和总分
     * </p>
     *
     * @param application 申请记录
     * @return 计算结果，包含各项分数和总分
     */
    EvaluationResult calculateApplication(ScholarshipApplication application);

    /**
     * 计算某批次所有申请的分数
     * <p>
     * 批量计算指定批次下所有申请的评分
     * </p>
     *
     * @param batchId 批次 ID
     * @return 计算结果 map，key 为申请 ID，value 为 EvaluationResult
     */
    BatchCalculationSummary calculateBatchApplications(Long batchId);

    /**
     * 计算课程成绩分数
     * <p>
     * 根据学生的课程成绩和评分规则计算课程成绩分数
     * </p>
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID
     * @return 课程成绩分数
     */
    BigDecimal calculateCourseScore(Long studentId, Long batchId);

    /**
     * 计算科研成果分数
     * <p>
     * 根据学生的科研论文、专利、项目等成果计算科研分数
     * </p>
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID
     * @return 科研成果分数
     */
    BigDecimal calculateResearchScore(Long studentId, Long batchId);

    /**
     * 计算竞赛获奖分数
     * <p>
     * 根据学生的竞赛获奖记录计算竞赛分数
     * </p>
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID
     * @return 竞赛获奖分数
     */
    BigDecimal calculateCompetitionScore(Long studentId, Long batchId);

    /**
     * 计算综合素质分数
     * <p>
     * 根据学生的德育表现等计算综合素质分数
     * </p>
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID
     * @return 综合素质分数
     */
    BigDecimal calculateQualityScore(Long studentId, Long batchId);

    /**
     * 计算总分
     * <p>
     * 根据各项分数计算总分（支持加权计算）
     * </p>
     *
     * @param courseScore 课程成绩分数
     * @param researchScore 科研分数
     * @param competitionScore 竞赛分数
     * @param qualityScore 综合素质分数
     * @return 总分
     */
    BigDecimal calculateTotalScore(BigDecimal courseScore, BigDecimal researchScore,
                                   BigDecimal competitionScore, BigDecimal qualityScore);
}
