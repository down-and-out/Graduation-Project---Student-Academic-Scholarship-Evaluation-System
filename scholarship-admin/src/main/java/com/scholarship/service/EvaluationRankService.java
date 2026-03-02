package com.scholarship.service;

import com.scholarship.entity.EvaluationResult;

import java.util.List;
import java.util.Map;

/**
 * 排名生成服务接口
 * <p>
 * 负责生成学生的各类排名，包括院系排名、专业排名等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface EvaluationRankService {

    /**
     * 生成某批次的所有排名
     * <p>
     * 包括院系排名和专业排名
     * </p>
     *
     * @param batchId 批次 ID
     * @return 排名结果，key 为学生 ID，value 为包含排名的 EvaluationResult
     */
    Map<Long, EvaluationResult> generateBatchRanks(Long batchId);

    /**
     * 计算院系排名
     * <p>
     * 按院系统计学生排名
     * </p>
     *
     * @param batchId 批次 ID
     * @param department 院系名称
     * @return 排名列表，按排名升序排列
     */
    List<EvaluationResult> calculateDepartmentRank(Long batchId, String department);

    /**
     * 计算专业排名
     * <p>
     * 按专业统计学生排名
     * </p>
     *
     * @param batchId 批次 ID
     * @param major 专业名称
     * @return 排名列表，按排名升序排列
     */
    List<EvaluationResult> calculateMajorRank(Long batchId, String major);

    /**
     * 获取学生的院系排名
     *
     * @param batchId 批次 ID
     * @param studentId 学生 ID
     * @return 院系排名，未找到返回 null
     */
    Integer getDepartmentRank(Long batchId, Long studentId);

    /**
     * 获取学生的专业排名
     *
     * @param batchId 批次 ID
     * @param studentId 学生 ID
     * @return 专业排名，未找到返回 null
     */
    Integer getMajorRank(Long batchId, Long studentId);

    /**
     * 批量更新排名
     * <p>
     * 根据计算结果批量更新评定结果表中的排名字段
     * </p>
     *
     * @param batchId 批次 ID
     * @param results 评定结果列表
     * @return 更新的数量
     */
    int updateRanksBatch(Long batchId, List<EvaluationResult> results);
}
