package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.EvaluationResult;

/**
 * 评定结果服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface EvaluationResultService extends IService<EvaluationResult> {

    /**
     * 分页查询评定结果
     *
     * @param current   当前页
     * @param size      每页大小
     * @param batchId   批次 ID
     * @param studentId 学生 ID
     * @param status    状态（1-公示中, 2-已确定, 3-有异议）
     * @param keyword   关键词（学号/姓名模糊查询）
     * @return 分页结果
     */
    IPage<EvaluationResult> pageResults(Long current, Long size, Long batchId, Long studentId, Integer status, String keyword);
}
