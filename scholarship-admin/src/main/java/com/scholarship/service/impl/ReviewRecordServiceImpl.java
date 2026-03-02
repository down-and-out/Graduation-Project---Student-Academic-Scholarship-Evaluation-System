package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.ReviewRecord;
import com.scholarship.mapper.ReviewRecordMapper;
import com.scholarship.service.ReviewRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评审记录服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Slf4j
@Service
public class ReviewRecordServiceImpl extends ServiceImpl<ReviewRecordMapper, ReviewRecord>
        implements ReviewRecordService {

    @Override
    public List<ReviewRecord> listByApplicationId(Long applicationId) {
        log.debug("根据申请 ID 查询评审记录，applicationId={}", applicationId);

        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewRecord::getApplicationId, applicationId)
                .orderByAsc(ReviewRecord::getReviewStage);

        return list(wrapper);
    }

    @Override
    public List<ReviewRecord> listByReviewerId(Long reviewerId) {
        log.debug("根据评审人 ID 查询评审记录，reviewerId={}", reviewerId);

        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewRecord::getReviewerId, reviewerId)
                .orderByDesc(ReviewRecord::getReviewTime);

        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addReviewRecord(Long applicationId, Integer reviewStage, Long reviewerId,
                                   String reviewerName, Integer reviewResult, BigDecimal reviewScore,
                                   String reviewComment) {
        log.info("添加评审记录，applicationId={}, stage={}, reviewerId={}, result={}",
                applicationId, reviewStage, reviewerId, reviewResult);

        ReviewRecord record = new ReviewRecord();
        record.setApplicationId(applicationId);
        record.setReviewStage(reviewStage);
        record.setReviewerId(reviewerId);
        record.setReviewerName(reviewerName);
        record.setReviewResult(reviewResult);
        record.setReviewScore(reviewScore);
        record.setReviewComment(reviewComment);
        record.setReviewTime(LocalDateTime.now());

        boolean success = save(record);
        log.info("评审记录添加{}", success ? "成功" : "失败");
        return success;
    }

    @Override
    public ReviewRecord getLatestRecord(Long applicationId) {
        log.debug("获取最新评审记录，applicationId={}", applicationId);

        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewRecord::getApplicationId, applicationId)
                .orderByDesc(ReviewRecord::getReviewTime)
                .last("LIMIT 1");

        return getOne(wrapper);
    }
}
