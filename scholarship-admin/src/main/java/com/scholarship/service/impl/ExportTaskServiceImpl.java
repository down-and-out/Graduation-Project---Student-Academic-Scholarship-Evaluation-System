package com.scholarship.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarship.service.ExportTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 异步导出任务服务实现。
 *
 * <p>使用 Redis 存储任务状态，使用 exportTaskExecutor 线程池异步执行导出。</p>
 */
@Slf4j
@Service
public class ExportTaskServiceImpl implements ExportTaskService {

    private static final String TASK_KEY_PREFIX = "export:task:";
    private static final Duration TASK_TTL = Duration.ofMinutes(30);
    private static final String EXPORT_TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "scholarship-exports";

    private final StringRedisTemplate redisTemplate;
    private final ThreadPoolTaskExecutor exportTaskExecutor;
    private final ObjectMapper objectMapper;

    public ExportTaskServiceImpl(StringRedisTemplate redisTemplate,
                                 @Qualifier("exportTaskExecutor") ThreadPoolTaskExecutor exportTaskExecutor) {
        this.redisTemplate = redisTemplate;
        this.exportTaskExecutor = exportTaskExecutor;
        this.objectMapper = new ObjectMapper();
        // 确保临时目录存在
        File dir = new File(EXPORT_TEMP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public String submit(String taskType, String fileName, Consumer<String> exportTask) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> initialState = new HashMap<>();
        initialState.put("status", Status.PROCESSING.name());
        initialState.put("fileName", fileName);
        initialState.put("taskType", taskType);

        writeTaskState(taskId, initialState);
        log.info("Export task created: taskId={}, taskType={}", taskId, taskType);

        exportTaskExecutor.execute(() -> {
            try {
                exportTask.accept(taskId);
            } catch (Exception e) {
                log.error("Export task failed: taskId={}", taskId, e);
                markFailed(taskId, e.getMessage());
            }
        });

        return taskId;
    }

    /**
     * 由导出任务在完成时调用，标记任务完成并记录文件路径。
     */
    public void markCompleted(String taskId, String fileName, String filePath) {
        Map<String, Object> state = new HashMap<>();
        state.put("status", Status.COMPLETED.name());
        state.put("fileName", fileName);
        state.put("filePath", filePath);
        writeTaskState(taskId, state);
        log.info("Export task completed: taskId={}, filePath={}", taskId, filePath);
    }

    @Override
    public void markFailed(String taskId, String errorMsg) {
        Map<String, Object> state = new HashMap<>();
        state.put("status", Status.FAILED.name());
        state.put("errorMsg", errorMsg != null ? errorMsg : "Unknown error");
        writeTaskState(taskId, state);
    }

    @Override
    public Map<String, Object> getStatus(String taskId) {
        return readTaskState(taskId);
    }

    @Override
    public String getFilePath(String taskId) {
        Map<String, Object> state = readTaskState(taskId);
        if (state == null || !Status.COMPLETED.name().equals(state.get("status"))) {
            return null;
        }
        return (String) state.get("filePath");
    }

    private void writeTaskState(String taskId, Map<String, Object> state) {
        try {
            String json = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(buildKey(taskId), json, TASK_TTL);
        } catch (Exception e) {
            log.warn("Failed to write export task state: taskId={}", taskId, e);
        }
    }

    private Map<String, Object> readTaskState(String taskId) {
        try {
            String json = redisTemplate.opsForValue().get(buildKey(taskId));
            if (json == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> state = objectMapper.readValue(json, Map.class);
            return state;
        } catch (Exception e) {
            log.warn("Failed to read export task state: taskId={}", taskId, e);
            return null;
        }
    }

    private String buildKey(String taskId) {
        return TASK_KEY_PREFIX + taskId;
    }
}
