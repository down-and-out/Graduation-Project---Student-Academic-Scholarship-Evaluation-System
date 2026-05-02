package com.scholarship.service;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 异步任务服务接口（统一管理导出和导入任务）。
 *
 * <p>管理任务的全生命周期：创建任务 → 异步执行 → 状态查询 → 文件下载。</p>
 */
public interface AsyncTaskService {

    /** 任务状态 */
    enum Status {
        PROCESSING, COMPLETED, FAILED
    }

    /**
     * 任务上下文，仅在 {@link #submitExport} 回调中暴露，
     * 外部代码无法直接调用 markCompleted/markFailed。
     */
    interface TaskContext {
        /** 返回当前任务 ID */
        String getTaskId();
        void markCompleted(String fileName, String filePath);
        void markFailed(String errorMsg);
        /** 构建导出文件完整路径（临时目录 + taskId + 扩展名） */
        String buildFilePath(String extension);
    }

    // ========== 导出任务 ==========

    /**
     * 创建导出任务并异步执行。
     *
     * @param taskType   任务类型标识（如 "course-score", "evaluation-result"）
     * @param fileName   导出文件名（不含扩展名）
     * @param exportTask 导出执行逻辑，接收 TaskContext 参数，在 exportTaskExecutor 线程池中执行
     * @return 任务 ID
     */
    String submitExport(String taskType, String fileName, Consumer<TaskContext> exportTask);

    /**
     * 查询导出任务状态。
     *
     * @param taskId 任务 ID
     * @return 包含 status / fileName / errorMsg 的 Map，任务不存在返回 null
     */
    Map<String, Object> getExportStatus(String taskId);

    /**
     * 获取导出文件路径（仅在 COMPLETED 状态时可获取）。
     *
     * @param taskId 任务 ID
     * @return 文件路径，任务未完成或不存在返回 null
     */
    String getFilePath(String taskId);

    // ========== 导入任务 ==========

    /**
     * 创建导入任务并异步执行。
     *
     * @param taskType   任务类型标识
     * @param importTask 导入执行逻辑，接收 taskId 参数
     * @return 任务 ID
     */
    String submitImport(String taskType, Consumer<String> importTask);

    /**
     * 查询导入任务状态。
     *
     * @param taskId 任务 ID
     * @return 状态 Map，任务不存在返回 null
     */
    Map<String, Object> getImportStatus(String taskId);
}
