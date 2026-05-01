package com.scholarship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarship.common.result.Result;
import com.scholarship.dto.StudentImportDTO;
import com.scholarship.service.BatchImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/batch")
@Tag(name = "12-批量导入管理", description = "批量导入学生信息等")
public class BatchImportController {

    private static final String IMPORT_TASK_PREFIX = "import:task:";
    private static final Duration IMPORT_TASK_TTL = Duration.ofMinutes(30);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final BatchImportService batchImportService;
    private final StringRedisTemplate redisTemplate;
    private final ThreadPoolTaskExecutor batchImportTaskExecutor;

    public BatchImportController(BatchImportService batchImportService,
                                  StringRedisTemplate redisTemplate,
                                  @Qualifier("batchImportTaskExecutor") ThreadPoolTaskExecutor batchImportTaskExecutor) {
        this.batchImportService = batchImportService;
        this.redisTemplate = redisTemplate;
        this.batchImportTaskExecutor = batchImportTaskExecutor;
    }

    @PostMapping("/import/students")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "批量导入学生信息", description = "通过 Excel 文件批量导入学生信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导入完成"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "500", description = "导入失败")
    })
    public Result<Map<String, Object>> importStudents(
            @Parameter(description = "Excel 文件") @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.error("请上传 Excel 文件（.xlsx 或 .xls）");
        }

        try {
            List<StudentImportDTO> students = com.alibaba.excel.EasyExcel.read(file.getInputStream())
                    .head(StudentImportDTO.class)
                    .sheet("学生信息导入模板")
                    .headRowNumber(1)
                    .doReadSync();

            if (students == null || students.isEmpty()) {
                return Result.error("Excel 文件中没有数据");
            }

            Map<String, Object> result = batchImportService.importStudents(students);
            return Result.success("导入完成", result);

        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            return Result.error("读取文件失败：" + e.getMessage());
        }
    }

    @PostMapping("/import/students/async")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "异步批量导入学生信息", description = "提交异步导入任务，返回 taskId 用于轮询状态")
    public Result<Map<String, Object>> importStudentsAsync(
            @Parameter(description = "Excel 文件") @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return Result.error("请上传 Excel 文件（.xlsx 或 .xls）");
        }

        List<StudentImportDTO> students;
        try {
            students = com.alibaba.excel.EasyExcel.read(file.getInputStream())
                    .head(StudentImportDTO.class)
                    .sheet("学生信息导入模板")
                    .headRowNumber(1)
                    .doReadSync();
        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            return Result.error("读取文件失败：" + e.getMessage());
        }

        if (students == null || students.isEmpty()) {
            return Result.error("Excel 文件中没有数据");
        }

        String taskId = UUID.randomUUID().toString().replace("-", "");
        writeImportTaskState(taskId, Map.of("status", "PROCESSING", "total", String.valueOf(students.size())));

        batchImportTaskExecutor.execute(() -> {
            try {
                Map<String, Object> result = batchImportService.importStudents(students);
                Map<String, Object> state = new HashMap<>();
                state.put("status", "COMPLETED");
                state.put("result", result);
                writeImportTaskState(taskId, state);
            } catch (Exception e) {
                log.error("Async import failed: taskId={}", taskId, e);
                writeImportTaskState(taskId, Map.of("status", "FAILED", "errorMsg", e.getMessage()));
            }
        });

        return Result.success("导入任务已提交", Map.of("taskId", taskId, "total", students.size()));
    }

    @GetMapping("/import/students/status/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "查询异步导入任务状态")
    public Result<Map<String, Object>> importStatus(@PathVariable String taskId) {
        Map<String, Object> state = readImportTaskState(taskId);
        if (state == null) {
            return Result.error("任务不存在或已过期");
        }
        return Result.success(state);
    }

    @GetMapping("/import/students/template")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "下载学生信息导入模板")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "模板下载成功")
    })
    public void downloadTemplate(HttpServletResponse response) {
        try {
            byte[] template = batchImportService.getImportTemplate();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("学生信息导入模板", "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            response.getOutputStream().write(template);
            response.getOutputStream().flush();

        } catch (IOException e) {
            log.error("下载模板失败", e);
        }
    }

    private void writeImportTaskState(String taskId, Map<String, Object> state) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(state);
            redisTemplate.opsForValue().set(IMPORT_TASK_PREFIX + taskId, json, IMPORT_TASK_TTL);
        } catch (Exception e) {
            log.warn("Failed to write import task state: taskId={}", taskId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readImportTaskState(String taskId) {
        try {
            String json = redisTemplate.opsForValue().get(IMPORT_TASK_PREFIX + taskId);
            if (json == null) {
                return null;
            }
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            log.warn("Failed to read import task state: taskId={}", taskId, e);
            return null;
        }
    }
}
