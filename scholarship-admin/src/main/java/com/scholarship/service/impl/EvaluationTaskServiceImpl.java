package com.scholarship.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.common.enums.EvaluationTaskStatusEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.LockConstants;
import com.scholarship.config.ScholarshipProperties;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.dto.EvaluationTaskResponse;
import com.scholarship.dto.EvaluationTaskResponse.TaskSummary;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.EvaluationTask;
import com.scholarship.mapper.EvaluationTaskMapper;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.CacheEvictionService;
import com.scholarship.service.EvaluationBatchService;
import com.scholarship.service.EvaluationCalculationService;
import com.scholarship.service.EvaluationRankService;
import com.scholarship.service.EvaluationTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationTaskServiceImpl implements EvaluationTaskService {

    private static final String TASK_TYPE_BATCH_EVALUATION = "BATCH_EVALUATION";

    private final EvaluationTaskMapper evaluationTaskMapper;
    private final EvaluationCalculationService evaluationCalculationService;
    private final EvaluationRankService evaluationRankService;
    private final AwardAllocationService awardAllocationService;
    private final EvaluationBatchService evaluationBatchService;
    private final CacheEvictionService cacheEvictionService;
    private final ScholarshipProperties scholarshipProperties;
    private final RedissonClient redissonClient;

    @Override
    @Transactional
    public EvaluationTaskResponse createEvaluationTask(Long batchId, Long triggeredBy, String triggeredByName) {
        evaluationBatchService.validateForEvaluation(batchId);

        String lockKey = LockConstants.TASK_CREATE + batchId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked;
        try {
            locked = lock.tryLock(0, scholarshipProperties.getLock().getEvaluationTaskCreateSeconds(),
                    TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("该批次已有评审任务正在创建，请勿重复操作");
        }
        if (!locked) {
            EvaluationTask activeTask = findLatestActiveTask(batchId);
            if (activeTask != null) {
                return buildTaskResponse(activeTask, true, "该批次已有评审任务在执行，返回现有任务");
            }
            throw new BusinessException("该批次已有评审任务正在创建，请勿重复操作");
        }

        try {
            EvaluationTask activeTask = findLatestActiveTask(batchId);
            if (activeTask != null) {
                return buildTaskResponse(activeTask, true, "该批次已有评审任务在执行，返回现有任务");
            }

            EvaluationTask task = new EvaluationTask();
            task.setBatchId(batchId);
            task.setTaskType(TASK_TYPE_BATCH_EVALUATION);
            task.setStatus(EvaluationTaskStatusEnum.PENDING.getCode());
            task.setTriggeredBy(triggeredBy);
            task.setTriggeredByName(triggeredByName);
            task.setCreateTime(LocalDateTime.now());

            try {
                evaluationTaskMapper.insert(task);
            } catch (DataIntegrityViolationException ex) {
                EvaluationTask latestActiveTask = findLatestActiveTask(batchId);
                if (latestActiveTask != null) {
                    return buildTaskResponse(latestActiveTask, true, "该批次已有评审任务在执行，返回现有任务");
                }
                throw new BusinessException("该批次已有评审任务正在执行，请勿重复操作");
            }

            log.info("Created evaluation task, taskId={}, batchId={}, triggeredBy={}", task.getId(), batchId, triggeredBy);
            return buildTaskResponse(task, false, "评审任务已创建，正在等待执行");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public EvaluationTaskResponse getTaskById(Long taskId) {
        EvaluationTask task = evaluationTaskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }
        return buildTaskResponse(task);
    }

    @Override
    public EvaluationTaskResponse getLatestTaskByBatchId(Long batchId) {
        EvaluationTask task = evaluationTaskMapper.selectOne(
                new LambdaQueryWrapper<EvaluationTask>()
                        .eq(EvaluationTask::getBatchId, batchId)
                        .eq(EvaluationTask::getTaskType, TASK_TYPE_BATCH_EVALUATION)
                        .orderByDesc(EvaluationTask::getCreateTime)
                        .last("LIMIT 1")
        );
        return task == null ? null : buildTaskResponse(task);
    }

    @Override
    public void executeTask(Long taskId) {
        doExecuteTask(taskId);
    }

    @Async("evaluationTaskExecutor")
    @Override
    public void executeTaskAsync(Long taskId) {
        doExecuteTask(taskId);
    }

    private void doExecuteTask(Long taskId) {
        EvaluationTask task = evaluationTaskMapper.selectById(taskId);
        if (task == null) {
            log.error("Evaluation task not found, taskId={}", taskId);
            return;
        }

        if (!EvaluationTaskStatusEnum.PENDING.getCode().equals(task.getStatus())) {
            log.warn("Skip non-pending evaluation task, taskId={}, status={}", taskId, task.getStatus());
            return;
        }

        task.setStatus(EvaluationTaskStatusEnum.RUNNING.getCode());
        task.setStartedAt(LocalDateTime.now());
        boolean claimed = evaluationTaskMapper.updateById(task) > 0;
        if (!claimed) {
            log.warn("Skip evaluation task because another worker already claimed it, taskId={}", taskId);
            return;
        }
        cacheEvictionService.evictTaskDetail(taskId);

        String executeLockKey = LockConstants.TASK_EXECUTE + taskId;
        RLock executeLock = redissonClient.getLock(executeLockKey);
        executeLock.lock();
        boolean succeeded = false;
        try {
            Long batchId = task.getBatchId();
            LocalDateTime startedAt = task.getStartedAt();
            log.info("Start evaluation task execution, taskId={}, batchId={}", taskId, batchId);

            BatchCalculationSummary calcSummary = evaluationCalculationService.calculateBatchApplications(batchId);
            Map<Long, EvaluationResult> rankResults = evaluationRankService.generateBatchRanks(batchId);
            AwardAllocationService.AwardAllocationResult awardResult = awardAllocationService.allocateAwards(batchId);

            TaskSummary summary = new TaskSummary();
            summary.setProcessedCount(calcSummary.getProcessedCount());
            summary.setWrittenCount(calcSummary.getWrittenCount());
            summary.setPageCount(calcSummary.getPageCount());
            summary.setRankedCount(rankResults.size());
            int totalAwarded = awardResult.getSpecialCount()
                    + awardResult.getFirstCount()
                    + awardResult.getSecondCount()
                    + awardResult.getThirdCount();
            summary.setAwardedCount(totalAwarded);

            task.setResultSummaryJson(JSON.toJSONString(summary));
            task.setStatus(EvaluationTaskStatusEnum.SUCCESS.getCode());
            task.setFinishedAt(LocalDateTime.now());
            evaluationTaskMapper.updateById(task);
            succeeded = true;

            log.info("Evaluation task completed, taskId={}, batchId={}, durationMs={}, summary={}",
                    taskId, batchId, durationMillis(startedAt, task.getFinishedAt()), summary);
        } catch (Exception ex) {
            log.error("Evaluation task failed, taskId={}, batchId={}", taskId, task.getBatchId(), ex);
            task.setStatus(EvaluationTaskStatusEnum.FAILED.getCode());
            task.setErrorMessage(truncateMessage(ex.getMessage()));
            task.setFinishedAt(LocalDateTime.now());
            evaluationTaskMapper.updateById(task);
        } finally {
            if (executeLock.isHeldByCurrentThread()) {
                executeLock.unlock();
            }
        }

        // 缓存清除在锁外执行，减小锁持有时间
        if (succeeded) {
            cacheEvictionService.evictEvaluationResultsForBatch(task.getBatchId());
        }
        cacheEvictionService.evictTaskDetail(taskId);
    }

    private EvaluationTask findLatestActiveTask(Long batchId) {
        List<EvaluationTask> activeTasks = evaluationTaskMapper.selectList(
                new LambdaQueryWrapper<EvaluationTask>()
                        .eq(EvaluationTask::getBatchId, batchId)
                        .eq(EvaluationTask::getTaskType, TASK_TYPE_BATCH_EVALUATION)
                        .in(EvaluationTask::getStatus,
                                EvaluationTaskStatusEnum.PENDING.getCode(),
                                EvaluationTaskStatusEnum.RUNNING.getCode())
                        .orderByDesc(EvaluationTask::getCreateTime)
                        .last("LIMIT 1")
        );
        return activeTasks.isEmpty() ? null : activeTasks.get(0);
    }

    private EvaluationTaskResponse buildTaskResponse(EvaluationTask task) {
        return buildTaskResponse(task, false, null);
    }

    private EvaluationTaskResponse buildTaskResponse(EvaluationTask task, boolean reusedActiveTask, String messageOverride) {
        EvaluationTaskResponse response = new EvaluationTaskResponse();
        response.setTaskId(task.getId());
        response.setBatchId(task.getBatchId());
        response.setTaskType(task.getTaskType());
        response.setStatus(task.getStatus());
        response.setStatusText(EvaluationTaskStatusEnum.getDescription(task.getStatus()));
        response.setErrorMessage(task.getErrorMessage());
        response.setReusedActiveTask(reusedActiveTask);
        response.setCreatedAt(task.getCreateTime());
        response.setStartedAt(task.getStartedAt());
        response.setFinishedAt(task.getFinishedAt());
        response.setDurationMillis(durationMillis(task.getStartedAt(), task.getFinishedAt()));

        EvaluationTaskStatusEnum status = EvaluationTaskStatusEnum.valueOfCode(task.getStatus());
        if (messageOverride != null && !messageOverride.isBlank()) {
            response.setMessage(messageOverride);
        } else if (status == EvaluationTaskStatusEnum.PENDING) {
            response.setMessage("评审任务已创建，正在等待执行");
        } else if (status == EvaluationTaskStatusEnum.RUNNING) {
            response.setMessage("评审任务正在执行中");
        } else if (status == EvaluationTaskStatusEnum.SUCCESS) {
            response.setMessage("评审任务执行成功");
        } else if (status == EvaluationTaskStatusEnum.FAILED) {
            response.setMessage("评审任务执行失败");
        } else if (status == EvaluationTaskStatusEnum.CANCELLED) {
            response.setMessage("评审任务已取消");
        }

        if (task.getResultSummaryJson() != null && !task.getResultSummaryJson().isBlank()) {
            try {
                TaskSummary summary = JSON.parseObject(task.getResultSummaryJson(), TaskSummary.class);
                response.setSummary(summary);
            } catch (Exception ignored) {
                // ignore parse errors
            }
        }
        return response;
    }

    private Long durationMillis(LocalDateTime startedAt, LocalDateTime finishedAt) {
        if (startedAt == null || finishedAt == null) {
            return null;
        }
        return Duration.between(startedAt, finishedAt).toMillis();
    }

    private String truncateMessage(String message) {
        if (message == null) {
            return null;
        }
        return message.length() > 1000 ? message.substring(0, 1000) : message;
    }
}
