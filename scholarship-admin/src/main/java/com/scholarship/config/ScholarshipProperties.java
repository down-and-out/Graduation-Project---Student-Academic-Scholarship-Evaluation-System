package com.scholarship.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 奖学金业务配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "scholarship")
public class ScholarshipProperties {

    private final SystemConfig system = new SystemConfig();

    private final ScoreConfig score = new ScoreConfig();

    private final ApplicationConfig application = new ApplicationConfig();

    private final LockConfig lock = new LockConfig();

    private final EvaluationConfig evaluation = new EvaluationConfig();

    private final AsyncConfig async = new AsyncConfig();

    private final RateLimitConfig rateLimit = new RateLimitConfig();

    @Data
    public static class SystemConfig {
        private String defaultPassword;
        private String name;
        private String version;
    }

    @Data
    public static class ScoreConfig {
        private Integer totalScore;
        private Integer passScore;
    }

    @Data
    public static class ApplicationConfig {
        private String numberPrefix = "SCH";
    }

    @Data
    public static class LockConfig {
        private long applicationSubmitSeconds = 10L;
        private long batchEvaluateSeconds = 1800L;
        private long evaluationTaskCreateSeconds = 10L;
    }

    @Data
    public static class EvaluationConfig {
        private long readPageSize = 200L;
        private int writeBatchSize = 200;
        private long maxOffsetRows = 5000L;
        private boolean allowSyncEndpoints = false;
    }

    @Data
    public static class AsyncConfig {
        private final ExecutorConfig evaluation = new ExecutorConfig(2, 4, 20, 60, "CALLER_RUNS", "evaluation-task-");
        private final ExecutorConfig export = new ExecutorConfig(1, 2, 10, 60, "CALLER_RUNS", "export-task-");
        private final ExecutorConfig batchImport = new ExecutorConfig(1, 2, 10, 60, "CALLER_RUNS", "batch-import-task-");
    }

    @Data
    public static class ExecutorConfig {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
        private int awaitTerminationSeconds;
        private String rejectedExecutionPolicy;
        private String threadNamePrefix;

        public ExecutorConfig() {
        }

        public ExecutorConfig(int corePoolSize, int maxPoolSize, int queueCapacity, int awaitTerminationSeconds,
                              String rejectedExecutionPolicy, String threadNamePrefix) {
            this.corePoolSize = corePoolSize;
            this.maxPoolSize = maxPoolSize;
            this.queueCapacity = queueCapacity;
            this.awaitTerminationSeconds = awaitTerminationSeconds;
            this.rejectedExecutionPolicy = rejectedExecutionPolicy;
            this.threadNamePrefix = threadNamePrefix;
        }
    }

    @Data
    public static class RateLimitConfig {
        private boolean enabled = true;
        private final EndpointLimit login = new EndpointLimit(20, 60, 8, 300);
        private final EndpointLimit register = new EndpointLimit(3, 3600, 0, 0);
        private final EndpointLimit applicationSubmit = new EndpointLimit(10, 60, 5, 60);
        private final EndpointLimit evaluationTrigger = new EndpointLimit(5, 60, 2, 60);
        private final EndpointLimit export = new EndpointLimit(3, 60, 2, 60);
    }

    @Data
    public static class EndpointLimit {
        private int ipLimit;
        private int ipWindowSeconds;
        private int actorLimit;
        private int actorWindowSeconds;

        public EndpointLimit() {
        }

        public EndpointLimit(int ipLimit, int ipWindowSeconds, int actorLimit, int actorWindowSeconds) {
            this.ipLimit = ipLimit;
            this.ipWindowSeconds = ipWindowSeconds;
            this.actorLimit = actorLimit;
            this.actorWindowSeconds = actorWindowSeconds;
        }
    }
}
