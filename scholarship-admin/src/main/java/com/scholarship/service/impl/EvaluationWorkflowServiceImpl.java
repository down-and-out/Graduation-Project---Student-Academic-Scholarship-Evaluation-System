package com.scholarship.service.impl;

import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.LockConstants;
import com.scholarship.config.ScholarshipProperties;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.EvaluationCalculationService;
import com.scholarship.service.EvaluationRankService;
import com.scholarship.service.EvaluationWorkflowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationWorkflowServiceImpl implements EvaluationWorkflowService {

    private final EvaluationCalculationService evaluationCalculationService;
    private final EvaluationRankService evaluationRankService;
    private final AwardAllocationService awardAllocationService;
    private final EvaluationBatchService evaluationBatchService;
    private final ScholarshipProperties scholarshipProperties;
    private final RedissonClient redissonClient;

    @Override
    public Map<String, Object> evaluateBatch(Long batchId) {
        String lockKey = LockConstants.BATCH_EVALUATE + batchId;
        RLock lock = redissonClient.getLock(lockKey);
        if (!lock.tryLock()) {
            throw new BusinessException("该批次正在评定中，请勿重复操作");
        }

        try {
            evaluationBatchService.validateForEvaluation(batchId);
            log.info("Start evaluation workflow for batchId={}", batchId);

            Map<String, Object> result = new HashMap<>();
            BatchCalculationSummary calcSummary = evaluationCalculationService.calculateBatchApplications(batchId);
            result.put("calculatedCount", calcSummary.getWrittenCount());
            result.put("calculationPageCount", calcSummary.getPageCount());

            Map<Long, EvaluationResult> rankResults = evaluationRankService.generateBatchRanks(batchId);
            result.put("rankedCount", rankResults.size());

            AwardAllocationService.AwardAllocationResult awardResult = awardAllocationService.allocateAwards(batchId);
            result.put("awardResult", awardResult);
            result.put("batchId", batchId);
            result.put("status", "completed");

            log.info("Evaluation workflow completed for batchId={}", batchId);
            return result;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
