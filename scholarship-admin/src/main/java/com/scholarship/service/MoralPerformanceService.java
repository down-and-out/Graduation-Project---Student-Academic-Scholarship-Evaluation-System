package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.dto.query.MoralPerformanceQuery;
import com.scholarship.entity.MoralPerformance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 德育表现服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface MoralPerformanceService extends IService<MoralPerformance> {

    /**
     * 分页查询德育表现
     *
     * @param query 查询参数
     * @return 分页结果
     */
    com.baomidou.mybatisplus.core.metadata.IPage<MoralPerformance> queryPage(MoralPerformanceQuery query);

    /**
     * 审核德育表现
     *
     * @param id 记录 ID
     * @param auditStatus 审核状态
     * @param auditComment 审核意见
     * @param auditorId 审核人 ID
     * @return 是否成功
     */
    boolean audit(Long id, Integer auditStatus, String auditComment, Long auditorId);

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

    /**
     * 批量计算学生的德育总分
     *
     * @param studentIds 学生 ID 列表
     * @param batchId 批次 ID
     * @return 按学生 ID 分组的德育总分
     */
    Map<Long, BigDecimal> mapTotalScoreByStudentIds(List<Long> studentIds, Long batchId);
}
