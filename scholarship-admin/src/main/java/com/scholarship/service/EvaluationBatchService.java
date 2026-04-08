package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.EvaluationBatch;

import java.util.List;

/**
 * 评定批次服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface EvaluationBatchService extends IService<EvaluationBatch> {

    /**
     * 开启批次（设置为申请中状态）
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    boolean startBatch(Long id);

    /**
     * 发布批次（设置为未开始/已发布状态）
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    boolean publishBatch(Long id);

    /**
     * 关闭批次（设置为已完成状态）
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    boolean closeBatch(Long id);

    /**
     * 开始评审（设置为评审中状态）
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    boolean startReview(Long id);

    /**
     * 开始公示（设置为公示中状态）
     *
     * @param id 批次 ID
     * @return 是否成功
     */
    boolean startPublicity(Long id);

    /**
     * 获取申请中的批次列表
     *
     * @return 批次列表
     */
    List<EvaluationBatch> listAvailableForApplication();
}
