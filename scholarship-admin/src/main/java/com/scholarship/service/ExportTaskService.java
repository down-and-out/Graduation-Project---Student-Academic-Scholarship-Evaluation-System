package com.scholarship.service;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 异步导出任务服务接口。
 *
 * <p>管理导出任务的全生命周期：创建任务 → 异步执行 → 状态查询 → 文件下载。</p>
 */
public interface ExportTaskService {

    /** 任务状态 */
    enum Status {
        PROCESSING, COMPLETED, FAILED
    }

    /**
     * 创建导出任务并异步执行。
     *
     * @param taskType   任务类型标识（如 "course-score", "evaluation-result"）
     * @param fileName   导出文件名（不含扩展名）
     * @param exportTask 导出执行逻辑，接收 taskId 参数，在 exportTaskExecutor 线程池中执行
     * @return 任务 ID
     */
    String submit(String taskType, String fileName, Consumer<String> exportTask);

    /**
     * 查询任务状态。
     *
     * @param taskId 任务 ID
     * @return 包含 status / fileName / errorMsg 的 Map，任务不存在返回 null
     */
    Map<String, Object> getStatus(String taskId);

    /**
     * 获取导出文件路径（仅在 COMPLETED 状态时可获取）。
     *
     * @param taskId 任务 ID
     * @return 文件路径，任务未完成或不存在返回 null
     */
    String getFilePath(String taskId);

    /**
     * 标记任务完成并记录文件信息。
     */
    void markCompleted(String taskId, String fileName, String filePath);

    /**
     * 更新任务状态为失败。
     */
    void markFailed(String taskId, String errorMsg);
}
