package com.scholarship.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarship.service.AsyncTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 异步任务服务实现（统一管理导出和导入任务）。
 *
 * <p>使用 Redis 存储任务状态，使用 exportTaskExecutor / batchImportTaskExecutor 异步执行。</p>
 */
@Slf4j
@Service
public class AsyncTaskServiceImpl implements AsyncTaskService {

    private static final String TASK_KEY_PREFIX = "export:task:";
    private static final String IMPORT_TASK_PREFIX = "import:task:";
    private static final Duration TASK_TTL = Duration.ofMinutes(30);
    private static final String EXPORT_TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "scholarship-exports";

    private final StringRedisTemplate redisTemplate;
    private final ThreadPoolTaskExecutor exportTaskExecutor;
    private final ThreadPoolTaskExecutor batchImportTaskExecutor;
    private final ObjectMapper objectMapper;

    public AsyncTaskServiceImpl(StringRedisTemplate redisTemplate,
                                @Qualifier("exportTaskExecutor") ThreadPoolTaskExecutor exportTaskExecutor,
                                @Qualifier("batchImportTaskExecutor") ThreadPoolTaskExecutor batchImportTaskExecutor,
                                ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.exportTaskExecutor = exportTaskExecutor;
        this.batchImportTaskExecutor = batchImportTaskExecutor;
        this.objectMapper = objectMapper;
        File dir = new File(EXPORT_TEMP_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            log.warn("Failed to create export temp directory: {}", EXPORT_TEMP_DIR);
        }
    }

    @Override
    public String submitExport(String taskType, String fileName, Consumer<TaskContext> exportTask) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> initialState = new HashMap<>();
        initialState.put("status", Status.PROCESSING.name());
        initialState.put("fileName", fileName);
        initialState.put("taskType", taskType);

        writeTaskState(TASK_KEY_PREFIX, taskId, initialState);
        log.info("Export task created: taskId={}, taskType={}", taskId, taskType);

        TaskContext ctx = new TaskContext() {
            @Override
            public String getTaskId() {
                return taskId;
            }
            @Override
            public String buildFilePath(String extension) {
                return EXPORT_TEMP_DIR + File.separator + taskId + "." + extension;
            }
            @Override
            public void markCompleted(String fn, String fp) {
                AsyncTaskServiceImpl.this.markCompleted(taskId, fn, fp);
            }
            @Override
            public void markFailed(String errorMsg) {
                AsyncTaskServiceImpl.this.markFailed(taskId, errorMsg);
            }
        };
        exportTaskExecutor.execute(() -> exportTask.accept(ctx));

        return taskId;
    }

    private void markCompleted(String taskId, String fileName, String filePath) {
        Map<String, Object> state = new HashMap<>();
        state.put("status", Status.COMPLETED.name());
        state.put("fileName", fileName);
        state.put("filePath", filePath);
        writeTaskState(TASK_KEY_PREFIX, taskId, state);
        log.info("Export task completed: taskId={}, filePath={}", taskId, filePath);
    }

    private void markFailed(String taskId, String errorMsg) {
        Map<String, Object> state = new HashMap<>();
        state.put("status", Status.FAILED.name());
        state.put("errorMsg", errorMsg != null ? errorMsg : "Unknown error");
        writeTaskState(TASK_KEY_PREFIX, taskId, state);
    }

    @Override
    public Map<String, Object> getExportStatus(String taskId) {
        return readTaskState(TASK_KEY_PREFIX, taskId);
    }

    @Override
    public String getFilePath(String taskId) {
        Map<String, Object> state = readTaskState(TASK_KEY_PREFIX, taskId);
        if (state == null || !Status.COMPLETED.name().equals(state.get("status"))) {
            return null;
        }
        return (String) state.get("filePath");
    }

    @Override
    public String submitImport(String taskType, Consumer<String> importTask) {
        String taskId = UUID.randomUUID().toString().replace("-", "");
        Map<String, Object> initialState = new HashMap<>();
        initialState.put("status", Status.PROCESSING.name());
        initialState.put("taskType", taskType);
        writeTaskState(IMPORT_TASK_PREFIX, taskId, initialState);
        log.info("Import task created: taskId={}, taskType={}", taskId, taskType);

        batchImportTaskExecutor.execute(() -> {
            try {
                importTask.accept(taskId);
                Map<String, Object> completeState = new HashMap<>();
                completeState.put("status", Status.COMPLETED.name());
                writeTaskState(IMPORT_TASK_PREFIX, taskId, completeState);
            } catch (Exception e) {
                log.error("Import task failed: taskId={}", taskId, e);
                Map<String, Object> failState = new HashMap<>();
                failState.put("status", Status.FAILED.name());
                failState.put("errorMsg", e.getMessage());
                writeTaskState(IMPORT_TASK_PREFIX, taskId, failState);
            }
        });

        return taskId;
    }

    @Override
    public Map<String, Object> getImportStatus(String taskId) {
        return readTaskState(IMPORT_TASK_PREFIX, taskId);
    }

    private void writeTaskState(String prefix, String taskId, Map<String, Object> state) {
        try {
            String json = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(prefix + taskId, json, TASK_TTL);
        } catch (Exception e) {
            log.warn("Failed to write task state: prefix={}, taskId={}", prefix, taskId, e);
        }
    }

    private Map<String, Object> readTaskState(String prefix, String taskId) {
        try {
            String json = redisTemplate.opsForValue().get(prefix + taskId);
            if (json == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> state = objectMapper.readValue(json, Map.class);
            return state;
        } catch (Exception e) {
            log.warn("Failed to read task state: prefix={}, taskId={}", prefix, taskId, e);
            return null;
        }
    }

    @Scheduled(fixedDelay = 3_600_000)
    public void cleanTempExportFiles() {
        File dir = new File(EXPORT_TEMP_DIR);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        long cutoff = System.currentTimeMillis() - Duration.ofHours(1).toMillis();
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        int deletedCount = 0;
        for (File file : files) {
            if (file.isFile() && file.lastModified() < cutoff) {
                if (file.delete()) {
                    deletedCount++;
                } else {
                    log.warn("Failed to delete expired temp export file: {}", file.getAbsolutePath());
                }
            }
        }
        if (deletedCount > 0) {
            log.info("Cleaned {} expired temp export files from {}", deletedCount, EXPORT_TEMP_DIR);
        }
    }
}
