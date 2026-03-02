package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.MoralPerformance;

import java.math.BigDecimal;
import java.util.List;

/**
 * 德育表现服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface MoralPerformanceService extends IService<MoralPerformance> {

    /**
     * 查询学生的德育表现列表
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID（可选，用于筛选特定学年的记录）
     * @return 德育表现列表
     */
    List<MoralPerformance> listByStudentId(Long studentId, Long batchId);

    /**
     * 计算学生的德育总分
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID
     * @return 德育总分
     */
    BigDecimal calculateTotalScore(Long studentId, Long batchId);

    /**
     * 按类型统计分数
     *
     * @param studentId 学生 ID
     * @param batchId 批次 ID
     * @param performanceType 表现类型
     * @return 该类型的总分
     */
    BigDecimal calculateScoreByType(Long studentId, Long batchId, Integer performanceType);
}
