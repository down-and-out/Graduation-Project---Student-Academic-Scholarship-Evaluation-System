package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.CourseScore;

import java.math.BigDecimal;
import java.util.List;

/**
 * 课程成绩服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface CourseScoreService extends IService<CourseScore> {

    /**
     * 查询学生的课程成绩列表
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID（可选，用于筛选特定学年的成绩）
     * @return 课程成绩列表
     */
    List<CourseScore> listByStudentId(Long studentId, Long batchId);

    /**
     * 计算学生的加权平均分
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID
     * @return 加权平均分
     */
    BigDecimal calculateWeightedAverage(Long studentId, Long batchId);

    /**
     * 计算学生的总学分
     *
     * @param studentId 学生 ID
     * @return 总学分
     */
    BigDecimal calculateTotalCredits(Long studentId);

    /**
     * 计算学生的平均绩点
     *
     * @param studentId 学生 ID
     * @return 平均绩点
     */
    BigDecimal calculateAverageGPA(Long studentId);
}
