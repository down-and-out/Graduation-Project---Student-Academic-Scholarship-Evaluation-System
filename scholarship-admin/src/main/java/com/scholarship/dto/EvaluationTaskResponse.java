package com.scholarship.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EvaluationTaskResponse {

    private Long taskId;
    private Long batchId;
    private String taskType;
    private Integer status;
    private String statusText;
    private String message;
    private String errorMessage;
    private Boolean reusedActiveTask;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private Long durationMillis;
    private TaskSummary summary;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TaskSummary {
        private Integer processedCount;
        private Integer writtenCount;
        private Integer pageCount;
        private Integer rankedCount;
        private Integer awardedCount;
    }
}
