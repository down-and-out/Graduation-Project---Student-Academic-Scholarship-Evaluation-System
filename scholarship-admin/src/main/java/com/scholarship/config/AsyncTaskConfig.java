package com.scholarship.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class AsyncTaskConfig implements AsyncConfigurer {

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

    @Override
    public Executor getAsyncExecutor() {
        return buildExecutor(new ScholarshipProperties.ExecutorConfig(
                2, 4, 50, 60, "CALLER_RUNS", "async-default-"));
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (Throwable ex, Method method, Object... params) ->
                log.error("Async method [{}] threw exception with params={}",
                        method.getName(), Arrays.toString(params), ex);
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
