package com.scholarship.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Locale;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AsyncTaskConfig {

    private final ScholarshipProperties scholarshipProperties;

    public AsyncTaskConfig(ScholarshipProperties scholarshipProperties) {
        this.scholarshipProperties = scholarshipProperties;
    }

    @Bean("evaluationTaskExecutor")
    public ThreadPoolTaskExecutor evaluationTaskExecutor() {
        return buildExecutor(scholarshipProperties.getAsync().getEvaluation());
    }

    @Bean("exportTaskExecutor")
    public ThreadPoolTaskExecutor exportTaskExecutor() {
        return buildExecutor(scholarshipProperties.getAsync().getExport());
    }

    @Bean("batchImportTaskExecutor")
    public ThreadPoolTaskExecutor batchImportTaskExecutor() {
        return buildExecutor(scholarshipProperties.getAsync().getBatchImport());
    }

    @Bean
    public MeterBinder executorMetrics(
            @Qualifier("evaluationTaskExecutor") ThreadPoolTaskExecutor evaluationExecutor,
            @Qualifier("exportTaskExecutor") ThreadPoolTaskExecutor exportExecutor,
            @Qualifier("batchImportTaskExecutor") ThreadPoolTaskExecutor batchImportExecutor) {
        return registry -> {
            registerExecutorMetrics(registry, evaluationExecutor, "evaluation");
            registerExecutorMetrics(registry, exportExecutor, "export");
            registerExecutorMetrics(registry, batchImportExecutor, "batch-import");
        };
    }

    private void registerExecutorMetrics(MeterRegistry registry, ThreadPoolTaskExecutor executor, String name) {
        ThreadPoolExecutor tpe = executor.getThreadPoolExecutor();
        Gauge.builder("executor." + name + ".active.count", tpe, ThreadPoolExecutor::getActiveCount)
                .description("Current active threads in " + name + " executor")
                .register(registry);
        Gauge.builder("executor." + name + ".queue.size", tpe, e -> e.getQueue().size())
                .description("Current queue size in " + name + " executor")
                .register(registry);
        Gauge.builder("executor." + name + ".pool.size", tpe, ThreadPoolExecutor::getPoolSize)
                .description("Current pool size in " + name + " executor")
                .register(registry);
        Gauge.builder("executor." + name + ".completed.tasks", tpe, ThreadPoolExecutor::getCompletedTaskCount)
                .description("Completed tasks in " + name + " executor")
                .register(registry);
    }

    private ThreadPoolTaskExecutor buildExecutor(ScholarshipProperties.ExecutorConfig config) {
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
