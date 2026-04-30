package com.scholarship.service.impl;

import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.LockConstants;
import com.scholarship.config.ScholarshipProperties;
import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.entity.EvaluationBatch;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.EvaluationCalculationService;
import com.scholarship.service.EvaluationRankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("EvaluationWorkflowServiceImpl concurrency tests")
class EvaluationWorkflowServiceImplTest {

    @Mock
    private EvaluationCalculationService evaluationCalculationService;
    @Mock
    private EvaluationRankService evaluationRankService;
    @Mock
    private AwardAllocationService awardAllocationService;
    @Mock
    private EvaluationBatchService evaluationBatchService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;

    private EvaluationWorkflowServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ScholarshipProperties properties = new ScholarshipProperties();
        properties.getLock().setBatchEvaluateSeconds(1800L);
        service = new EvaluationWorkflowServiceImpl(
                evaluationCalculationService,
                evaluationRankService,
                awardAllocationService,
                evaluationBatchService,
                properties,
                redissonClient
        );
    }

    @Test
    @DisplayName("evaluate should fail when batch lock is occupied")
    void evaluateShouldFailWhenBatchLockIsOccupied() {
        when(redissonClient.getLock(eq(LockConstants.BATCH_EVALUATE + 6))).thenReturn(rLock);
        when(rLock.tryLock()).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.evaluateBatch(6L));

        assertEquals("该批次正在评定中，请勿重复操作", exception.getMessage());
        verify(evaluationBatchService, never()).getById(anyLong());
    }

    @Test
    @DisplayName("evaluate should reject non reviewing batch")
    void evaluateShouldRejectNonReviewingBatch() {
        when(redissonClient.getLock(eq(LockConstants.BATCH_EVALUATE + 9))).thenReturn(rLock);
        when(rLock.tryLock()).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        doThrow(new BusinessException("当前批次状态不允许评定"))
                .when(evaluationBatchService).validateForEvaluation(9L);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.evaluateBatch(9L));

        assertEquals("当前批次状态不允许评定", exception.getMessage());
        verify(evaluationCalculationService, never()).calculateBatchApplications(anyLong());
    }

    @Test
    @DisplayName("evaluate should expose calculation summary counts")
    void evaluateShouldExposeCalculationSummaryCounts() {
        EvaluationBatch batch = new EvaluationBatch();
        batch.setId(11L);
        batch.setBatchStatus(3);

        BatchCalculationSummary summary = new BatchCalculationSummary();
        summary.setBatchId(11L);
        summary.setWrittenCount(4);
        summary.setPageCount(2);

        when(redissonClient.getLock(eq(LockConstants.BATCH_EVALUATE + 11))).thenReturn(rLock);
        when(rLock.tryLock()).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        when(evaluationBatchService.getById(11L)).thenReturn(batch);
        when(evaluationCalculationService.calculateBatchApplications(11L)).thenReturn(summary);
        when(evaluationRankService.generateBatchRanks(11L)).thenReturn(java.util.Map.of());
        when(awardAllocationService.allocateAwards(11L)).thenReturn(new AwardAllocationService.AwardAllocationResult());

        var result = service.evaluateBatch(11L);

        assertEquals(4, result.get("calculatedCount"));
        assertEquals(2, result.get("calculationPageCount"));
    }
}
