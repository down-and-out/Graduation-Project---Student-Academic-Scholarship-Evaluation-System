package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.ReviewRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评审记录服务接口
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
public interface ReviewRecordService extends IService<ReviewRecord> {

    /**
     * 根据申请 ID 查询评审记录
     *
     * @param applicationId 申请 ID
     * @return 评审记录列表
     */
    List<ReviewRecord> listByApplicationId(Long applicationId);

    /**
     * 根据评审人 ID 查询评审记录
     *
     * @param reviewerId 评审人 ID
     * @return 评审记录列表
     */
    List<ReviewRecord> listByReviewerId(Long reviewerId);

    /**
     * 添加评审记录
     *
     * @param applicationId 申请 ID
     * @param reviewStage 评审阶段
     * @param reviewerId 评审人 ID
     * @param reviewerName 评审人姓名
     * @param reviewResult 评审结果
     * @param reviewScore 评审分数
     * @param reviewComment 评审意见
     * @return 是否成功
     */
    boolean addReviewRecord(Long applicationId, Integer reviewStage, Long reviewerId,
                           String reviewerName, Integer reviewResult, BigDecimal reviewScore,
                           String reviewComment);

    /**
     * 获取申请的最新评审记录
     *
     * @param applicationId 申请 ID
     * @return 最新评审记录
     */
    ReviewRecord getLatestRecord(Long applicationId);
}
