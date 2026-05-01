package com.scholarship.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.result.Result;
import com.scholarship.config.ScholarshipProperties;
import com.scholarship.dto.BatchCalculationSummary;
import com.scholarship.dto.EvaluationTaskResponse;
import com.scholarship.dto.param.EvaluationResultAdjustRequest;
import com.scholarship.dto.param.EvaluationResultQueryParam;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.EvaluationCalculationService;
import com.scholarship.service.EvaluationRankService;
import com.scholarship.service.EvaluationResultService;
import com.scholarship.service.EvaluationTaskService;
import com.scholarship.service.ExportTaskService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.vo.AdminEvaluationResultVO;
import com.scholarship.vo.EvaluationResultExportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/evaluation-result")
@RequiredArgsConstructor
@Tag(name = "Evaluation Result", description = "Scholarship evaluation result APIs")
public class EvaluationResultController {

    private final EvaluationResultService evaluationResultService;
    private final EvaluationCalculationService evaluationCalculationService;
    private final EvaluationRankService evaluationRankService;
    private final AwardAllocationService awardAllocationService;
    private final EvaluationTaskService evaluationTaskService;
    private final StudentInfoService studentInfoService;
    private final ExportTaskService exportTaskService;
    private final ScholarshipProperties scholarshipProperties;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STUDENT')")
    @Operation(summary = "Page evaluation results")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Query succeeded"))
    public Result<IPage<AdminEvaluationResultVO>> page(@Valid @ModelAttribute EvaluationResultQueryParam queryParam,
                                                       @AuthenticationPrincipal LoginUser loginUser) {
        Long studentId = queryParam.getStudentId();
        if (loginUser != null && UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
            if (studentInfo == null) {
                return Result.success(new Page<>(queryParam.getCurrent(), queryParam.getSize(), 0));
            }
            studentId = studentInfo.getId();
        }

        IPage<AdminEvaluationResultVO> result = evaluationResultService.pageAdminResults(
                queryParam.getCurrent(),
                queryParam.getSize(),
                queryParam.getBatchId(),
                queryParam.getAcademicYear(),
                queryParam.getSemester(),
                studentId,
                queryParam.getStatus(),
                queryParam.getKeyword()
        );
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STUDENT')")
    @Operation(summary = "Get evaluation result detail")
    public Result<AdminEvaluationResultVO> getById(@PathVariable Long id,
                                                   @AuthenticationPrincipal LoginUser loginUser) {
        AdminEvaluationResultVO result = evaluationResultService.getAdminResultById(id);
        if (result == null) {
            return Result.error("评定结果不存在");
        }

        if (loginUser != null && UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
            if (studentInfo == null || !studentInfo.getId().equals(result.getStudentId())) {
                return Result.error(403, "无权访问该评定结果");
            }
        }
        return Result.success(result);
    }

    @PutMapping("/adjust/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Adjust evaluation result")
    public Result<Void> adjustResult(@PathVariable Long id,
                                     @Valid @RequestBody EvaluationResultAdjustRequest request) {
        boolean success = evaluationResultService.adjustResult(id, request);
        return success ? Result.success("调整成功") : Result.error("调整失败");
    }

    @GetMapping("/my-result")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "Get current student's latest result")
    public Result<EvaluationResult> getMyResult(@RequestParam(required = false) Long batchId,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("未登录");
        }

        StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
        if (studentInfo == null) {
            return Result.error("未找到学生信息");
        }

        EvaluationResult result = evaluationResultService.getStudentResult(studentInfo.getId(), batchId);
        return result == null
                ? Result.success("暂无评审结果", null)
                : Result.success(result);
    }

    @PostMapping("/calculate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Calculate batch evaluation scores")
    public Result<BatchCalculationSummary> calculateBatch(@PathVariable Long batchId) {
        ensureSyncEvaluationEndpointsAllowed();
        BatchCalculationSummary summary = evaluationCalculationService.calculateBatchApplications(batchId);
        return Result.success("计算完成", summary);
    }

    @PostMapping("/generate-ranks/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Generate batch ranks")
    public Result<Map<String, Object>> generateRanks(@PathVariable Long batchId) {
        ensureSyncEvaluationEndpointsAllowed();
        Map<Long, EvaluationResult> rankResults = evaluationRankService.generateBatchRanks(batchId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("batchId", batchId);
        stats.put("rankedCount", rankResults.size());
        return Result.success("排名生成完成", stats);
    }

    @PostMapping("/generate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Generate batch awards")
    public Result<AwardAllocationService.AwardAllocationResult> generateAwards(@PathVariable Long batchId) {
        ensureSyncEvaluationEndpointsAllowed();
        AwardAllocationService.AwardAllocationResult result = awardAllocationService.allocateAwards(batchId);
        return Result.success("奖项分配完成", result);
    }

    @PutMapping("/confirm/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Confirm evaluation result")
    public Result<Void> confirmResult(@PathVariable Long id) {
        boolean success = evaluationResultService.confirmResult(id);
        return success ? Result.success("确认成功") : Result.error("确认失败");
    }

    @PutMapping("/object/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Mark evaluation result as objected")
    public Result<Void> objectResult(@PathVariable Long id) {
        boolean success = evaluationResultService.objectResult(id);
        return success ? Result.success("已标记异议") : Result.error("标记异议失败");
    }

    @GetMapping("/batch/{batchId}/ranks")
    @Operation(summary = "Get batch ranks")
    public Result<List<EvaluationResult>> getBatchRanks(@PathVariable Long batchId,
                                                        @Parameter(description = "Ranking type", example = "department")
                                                        @RequestParam(defaultValue = "department") String type) {
        List<EvaluationResult> results = evaluationResultService.getBatchRanks(batchId, type);
        return Result.success(results);
    }

    @PostMapping("/evaluate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Create async evaluation task")
    public Result<EvaluationTaskResponse> evaluateBatch(@PathVariable Long batchId,
                                                        @AuthenticationPrincipal LoginUser loginUser) {
        EvaluationTaskResponse taskResponse = evaluationTaskService.createEvaluationTask(
                batchId,
                loginUser != null ? loginUser.getUserId() : null,
                loginUser != null ? loginUser.getRealName() : null
        );
        if (!Boolean.TRUE.equals(taskResponse.getReusedActiveTask())) {
            evaluationTaskService.executeTaskAsync(taskResponse.getTaskId());
        }
        return Result.success(taskResponse.getMessage(), taskResponse);
    }

    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get evaluation task detail")
    public Result<EvaluationTaskResponse> getEvaluationTask(@PathVariable Long taskId) {
        EvaluationTaskResponse taskResponse = evaluationTaskService.getTaskById(taskId);
        if (taskResponse == null) {
            return Result.error("评定任务不存在");
        }
        return Result.success(taskResponse);
    }

    @GetMapping("/tasks/latest/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Get latest batch evaluation task")
    public Result<EvaluationTaskResponse> getLatestEvaluationTask(@PathVariable Long batchId) {
        EvaluationTaskResponse taskResponse = evaluationTaskService.getLatestTaskByBatchId(batchId);
        return Result.success(taskResponse);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Export evaluation results")
    public void export(@RequestParam(required = false) Long batchId,
                       @RequestParam(required = false) String academicYear,
                       @RequestParam(required = false) Integer semester,
                       HttpServletResponse response) throws IOException {
        List<EvaluationResultExportVO> exportData = evaluationResultService.exportBatchResults(batchId, academicYear, semester);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StringBuilder fileNameBuilder = new StringBuilder("evaluation_results");
        if (batchId != null) {
            fileNameBuilder.append("_").append(batchId);
        }
        if (academicYear != null && !academicYear.isBlank()) {
            fileNameBuilder.append("_").append(academicYear);
        }
        if (semester != null) {
            fileNameBuilder.append("_").append(semester);
        }
        String fileName = URLEncoder.encode(fileNameBuilder.toString(), StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), EvaluationResultExportVO.class)
                .sheet("evaluation_results")
                .doWrite(exportData);
    }

    private static final int EXPORT_MAX_ROWS = 5000;

    @PostMapping("/export/submit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Submit async evaluation result export")
    public Result<Map<String, Object>> exportAsync(@RequestParam(required = false) Long batchId,
                                                   @RequestParam(required = false) String academicYear,
                                                   @RequestParam(required = false) Integer semester,
                                                   @RequestParam(defaultValue = "5000") int maxRows) {
        String fileName = "evaluation_results"
                + (batchId != null ? "_" + batchId : "")
                + (academicYear != null && !academicYear.isBlank() ? "_" + academicYear : "")
                + (semester != null ? "_" + semester : "");

        int effectiveMaxRows = Math.min(maxRows, EXPORT_MAX_ROWS);

        String taskId = exportTaskService.submit("evaluation-result", fileName, id -> {
            try {
                List<EvaluationResultExportVO> data = evaluationResultService.exportBatchResults(batchId, academicYear, semester)
                        .stream().limit(effectiveMaxRows).toList();
                String tempDir = exportTaskService.getTempDir();
                String filePath = tempDir + File.separator + id + ".xlsx";
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    EasyExcel.write(fos, EvaluationResultExportVO.class)
                            .sheet("evaluation_results")
                            .doWrite(data);
                }
                exportTaskService.markCompleted(id, fileName + ".xlsx", filePath);
            } catch (Exception e) {
                log.error("Async export failed: taskId={}", id, e);
                exportTaskService.markFailed(id, e.getMessage());
            }
        });

        return Result.success("导出任务已提交", Map.of("taskId", taskId));
    }

    @GetMapping("/export/status/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Query export task status")
    public Result<Map<String, Object>> exportStatus(@PathVariable String taskId) {
        Map<String, Object> status = exportTaskService.getStatus(taskId);
        if (status == null) {
            return Result.error("任务不存在或已过期");
        }
        return Result.success(status);
    }

    @GetMapping("/export/download/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "Download async export file")
    public void exportDownload(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        String filePath = exportTaskService.getFilePath(taskId);
        if (filePath == null) {
            response.setStatus(404);
            response.getWriter().write("{\"message\":\"文件不存在或导出未完成\"}");
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            response.setStatus(404);
            response.getWriter().write("{\"message\":\"文件已过期，请重新导出\"}");
            return;
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encodedFileName = URLEncoder.encode(new File(filePath).getName(), StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName);
        Files.copy(file.toPath(), response.getOutputStream());
        response.getOutputStream().flush();
    }

    private void ensureSyncEvaluationEndpointsAllowed() {
        if (!scholarshipProperties.getEvaluation().isAllowSyncEndpoints()) {
            throw new BusinessException("Synchronous evaluation endpoints are disabled. Use async tasks instead.");
        }
    }
}
