package com.scholarship.service;

import com.scholarship.dto.EvaluationTaskResponse;
import com.scholarship.entity.EvaluationTask;

public interface EvaluationTaskService {

    /**
     * 为批次创建评定任务并返回任务信息
     * <p>
     * 若该批次已有未完成的任务（PENDING/RUNNING），则返回业务错误。
     * 校验通过后插入 PENDING 任务记录，提交后台执行并返回任务信息。
     * </p>
     *
     * @param batchId 批次ID
     * @param triggeredBy 触发人ID
     * @param triggeredByName 触发人姓名
     * @return 任务响应
     */
    EvaluationTaskResponse createEvaluationTask(Long batchId, Long triggeredBy, String triggeredByName);

    /**
     * 查询单个任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    EvaluationTaskResponse getTaskById(Long taskId);

    /**
     * 获取批次最近一个评定任务
     *
     * @param batchId 批次ID
     * @return 任务详情，若无则返回null
     */
    EvaluationTaskResponse getLatestTaskByBatchId(Long batchId);

    /**
     * 执行评定任务（后台异步调用）
     *
     * @param taskId 任务ID
     */
    void executeTask(Long taskId);

    /**
     * 异步触发评定任务执行
     *
     * @param taskId 任务ID
     */
    void executeTaskAsync(Long taskId);
}
