package com.scholarship.service.impl;

import com.scholarship.common.enums.EvaluationTaskStatusEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.LockConstants;
import com.scholarship.config.ScholarshipProperties;
import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.dto.EvaluationTaskResponse;
import com.scholarship.entity.EvaluationTask;
import com.scholarship.mapper.EvaluationResultMapper;
import com.scholarship.mapper.EvaluationTaskMapper;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.CacheEvictionService;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("EvaluationTaskServiceImpl tests")
class EvaluationTaskServiceImplTest {

    @Mock
    private EvaluationTaskMapper evaluationTaskMapper;
    @Mock
    private EvaluationResultMapper evaluationResultMapper;
    @Mock
    private EvaluationCalculationService evaluationCalculationService;
    @Mock
    private EvaluationRankService evaluationRankService;
    @Mock
    private AwardAllocationService awardAllocationService;
    @Mock
    private EvaluationBatchService evaluationBatchService;
    @Mock
    private CacheEvictionService cacheEvictionService;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLockCreate;
    @Mock
    private RLock rLockExecute;

    private EvaluationTaskServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ScholarshipProperties properties = new ScholarshipProperties();
        properties.getLock().setEvaluationTaskCreateSeconds(10L);
        service = new EvaluationTaskServiceImpl(
                evaluationTaskMapper,
                evaluationResultMapper,
                evaluationCalculationService,
                evaluationRankService,
                awardAllocationService,
                evaluationBatchService,
                cacheEvictionService,
                properties,
                redissonClient
        );
    }

    @Test
    @DisplayName("create task should return existing active task when create lock is occupied")
    void createTaskShouldReturnExistingTaskWhenLockIsOccupied() throws InterruptedException {
        EvaluationTask task = createPendingTask(7L, 10L);
        when(redissonClient.getLock(eq(LockConstants.TASK_CREATE + 10))).thenReturn(rLockCreate);
        when(rLockCreate.tryLock(0, 10L, TimeUnit.SECONDS)).thenReturn(false);
        when(evaluationTaskMapper.selectList(any())).thenReturn(java.util.List.of(task));

        EvaluationTaskResponse response = service.createEvaluationTask(10L, 1L, "admin");

        assertEquals(7L, response.getTaskId());
        assertTrue(Boolean.TRUE.equals(response.getReusedActiveTask()));
        verify(evaluationTaskMapper, never()).insert(any(EvaluationTask.class));
    }

    @Test
    @DisplayName("create task should return existing active task when active task already exists")
    void createTaskShouldReturnExistingTaskWhenActiveTaskExists() throws InterruptedException {
        when(redissonClient.getLock(eq(LockConstants.TASK_CREATE + 10))).thenReturn(rLockCreate);
        when(rLockCreate.tryLock(0, 10L, TimeUnit.SECONDS)).thenReturn(true);
        when(rLockCreate.isHeldByCurrentThread()).thenReturn(true);
        when(evaluationTaskMapper.selectList(any())).thenReturn(java.util.List.of(createPendingTask(1L, 10L)));

        EvaluationTaskResponse response = service.createEvaluationTask(10L, 1L, "admin");

        assertEquals(1L, response.getTaskId());
        assertTrue(Boolean.TRUE.equals(response.getReusedActiveTask()));
        verify(rLockCreate).unlock();
    }

    @Test
    @DisplayName("create task should fail when lock is occupied and no task is found")
    void createTaskShouldFailWhenLockIsOccupiedAndNoTaskFound() throws InterruptedException {
        when(redissonClient.getLock(eq(LockConstants.TASK_CREATE + 10))).thenReturn(rLockCreate);
        when(rLockCreate.tryLock(0, 10L, TimeUnit.SECONDS)).thenReturn(false);
        when(evaluationTaskMapper.selectList(any())).thenReturn(java.util.List.of());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createEvaluationTask(10L, 1L, "admin")
        );

        assertEquals("该批次已有评审任务正在创建，请勿重复操作", exception.getMessage());
    }

    @Test
    @DisplayName("task success should evict batch caches and task detail cache")
    void shouldEvictCachesOnSuccess() {
        Long taskId = 1L;
        Long batchId = 10L;

        EvaluationTask task = createPendingTask(taskId, batchId);

        when(evaluationTaskMapper.selectById(taskId)).thenReturn(task);
        when(evaluationTaskMapper.updateById(any(EvaluationTask.class))).thenReturn(1);
        when(redissonClient.getLock(eq(LockConstants.TASK_EXECUTE + 1))).thenReturn(rLockExecute);

        BatchCalculationSummary calcSummary = new BatchCalculationSummary();
        calcSummary.setBatchId(batchId);
        calcSummary.setProcessedCount(10);
        calcSummary.setWrittenCount(10);
        calcSummary.setPageCount(1);
        when(evaluationCalculationService.calculateBatchApplications(batchId)).thenReturn(calcSummary);
        when(evaluationResultMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(evaluationRankService.generateBatchRanks(eq(batchId), anyList())).thenReturn(Map.of());
        AwardAllocationService.AwardAllocationResult awardResult = new AwardAllocationService.AwardAllocationResult();
        when(awardAllocationService.allocateAwards(eq(batchId), anyList())).thenReturn(awardResult);
        when(rLockExecute.isHeldByCurrentThread()).thenReturn(true);

        service.executeTask(taskId);

        verify(cacheEvictionService).evictEvaluationResultsForBatch(batchId);
        verify(cacheEvictionService, org.mockito.Mockito.times(2)).evictTaskDetail(taskId);
        verify(rLockExecute).unlock();
    }

    @Test
    @DisplayName("task failure should still evict task detail cache")
    void shouldEvictTaskCacheOnFailure() {
        Long taskId = 2L;
        Long batchId = 20L;

        EvaluationTask task = createPendingTask(taskId, batchId);

        when(evaluationTaskMapper.selectById(taskId)).thenReturn(task);
        when(evaluationTaskMapper.updateById(any(EvaluationTask.class))).thenReturn(1);
        when(redissonClient.getLock(eq(LockConstants.TASK_EXECUTE + 2))).thenReturn(rLockExecute);
        when(evaluationCalculationService.calculateBatchApplications(batchId))
                .thenThrow(new RuntimeException("Calculation failed"));
        when(rLockExecute.isHeldByCurrentThread()).thenReturn(true);

        service.executeTask(taskId);

        verify(cacheEvictionService, never()).evictEvaluationResultsForBatch(anyLong());
        verify(cacheEvictionService, org.mockito.Mockito.times(2)).evictTaskDetail(taskId);
        verify(rLockExecute).unlock();
    }

    @Test
    @DisplayName("task should stop when another worker already claimed running state")
    void shouldStopWhenTaskAlreadyClaimed() {
        Long taskId = 3L;
        Long batchId = 30L;

        EvaluationTask task = createPendingTask(taskId, batchId);

        when(evaluationTaskMapper.selectById(taskId)).thenReturn(task);
        when(evaluationTaskMapper.updateById(any(EvaluationTask.class))).thenReturn(0);

        service.executeTask(taskId);

        verify(evaluationCalculationService, never()).calculateBatchApplications(anyLong());
        verify(evaluationRankService, never()).generateBatchRanks(anyLong());
        verify(awardAllocationService, never()).allocateAwards(anyLong());
        verify(cacheEvictionService, never()).evictTaskDetail(taskId);
    }

    private EvaluationTask createPendingTask(Long taskId, Long batchId) {
        EvaluationTask task = new EvaluationTask();
        task.setId(taskId);
        task.setBatchId(batchId);
        task.setTaskType("BATCH_EVALUATION");
        task.setStatus(EvaluationTaskStatusEnum.PENDING.getCode());
        task.setCreateTime(LocalDateTime.now());
        return task;
    }
}
