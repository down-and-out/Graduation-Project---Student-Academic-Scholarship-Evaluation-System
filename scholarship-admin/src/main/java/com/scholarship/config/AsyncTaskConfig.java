package com.scholarship.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncTaskConfig {

    private final ScholarshipProperties scholarshipProperties;

    public AsyncTaskConfig(ScholarshipProperties scholarshipProperties) {
        this.scholarshipProperties = scholarshipProperties;
    }

    @Bean("evaluationTaskExecutor")
    public Executor evaluationTaskExecutor() {
        return buildExecutor(scholarshipProperties.getAsync().getEvaluation());
    }

    @Bean("exportTaskExecutor")
    public Executor exportTaskExecutor() {
        return buildExecutor(scholarshipProperties.getAsync().getExport());
    }

    @Bean("batchImportTaskExecutor")
    public Executor batchImportTaskExecutor() {
        return buildExecutor(scholarshipProperties.getAsync().getBatchImport());
    }

    private Executor buildExecutor(ScholarshipProperties.ExecutorConfig config) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(config.getAwaitTerminationSeconds());
        executor.setRejectedExecutionHandler(resolveRejectedPolicy(config.getRejectedExecutionPolicy()));
        executor.initialize();
        return executor;
    }

    private RejectedExecutionHandler resolveRejectedPolicy(String policy) {
        String normalized = policy == null ? "" : policy.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "ABORT" -> new ThreadPoolExecutor.AbortPolicy();
            case "DISCARD" -> new ThreadPoolExecutor.DiscardPolicy();
            case "DISCARD_OLDEST" -> new ThreadPoolExecutor.DiscardOldestPolicy();
            default -> new ThreadPoolExecutor.CallerRunsPolicy();
        };
    }
}
