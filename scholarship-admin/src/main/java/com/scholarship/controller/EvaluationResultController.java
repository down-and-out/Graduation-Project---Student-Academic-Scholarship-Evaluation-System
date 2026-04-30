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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/evaluation-result")
@RequiredArgsConstructor
@Tag(name = "评审结果管理", description = "评审结果查询、调整、导出与异步评审任务接口")
public class EvaluationResultController {

    private final EvaluationResultService evaluationResultService;
    private final EvaluationCalculationService evaluationCalculationService;
    private final EvaluationRankService evaluationRankService;
    private final AwardAllocationService awardAllocationService;
    private final EvaluationTaskService evaluationTaskService;
    private final StudentInfoService studentInfoService;
    private final ScholarshipProperties scholarshipProperties;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STUDENT')")
    @Operation(summary = "分页查询评审结果")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "查询成功"))
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
    @Operation(summary = "获取评审结果详情")
    public Result<AdminEvaluationResultVO> getById(@PathVariable Long id,
                                                   @AuthenticationPrincipal LoginUser loginUser) {
        AdminEvaluationResultVO result = evaluationResultService.getAdminResultById(id);
        if (result == null) {
            return Result.error("评审结果不存在");
        }

        if (loginUser != null && UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
            if (studentInfo == null || !studentInfo.getId().equals(result.getStudentId())) {
                return Result.error(403, "无权访问该评审结果");
            }
        }
        return Result.success(result);
    }

    @PutMapping("/adjust/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "调整评审结果")
    public Result<Void> adjustResult(@PathVariable Long id,
                                     @Valid @RequestBody EvaluationResultAdjustRequest request) {
        boolean success = evaluationResultService.adjustResult(id, request);
        return success ? Result.success("调整成功") : Result.error("调整失败");
    }

    @GetMapping("/my-result")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "获取当前学生评审结果")
    public Result<EvaluationResult> getMyResult(@RequestParam(required = false) Long batchId,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("未登录");
        }

        StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
        if (studentInfo == null) {
            return Result.error("学生信息不存在");
        }

        EvaluationResult result = evaluationResultService.getStudentResult(studentInfo.getId(), batchId);
        if (result == null) {
            return Result.error("暂无评审结果");
        }
        return Result.success(result);
    }

    @PostMapping("/calculate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "同步计算批次评审结果")
    public Result<BatchCalculationSummary> calculateBatch(@PathVariable Long batchId) {
        ensureSyncEvaluationEndpointsAllowed();
        BatchCalculationSummary summary = evaluationCalculationService.calculateBatchApplications(batchId);
        return Result.success("评审计算完成", summary);
    }

    @PostMapping("/generate-ranks/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "同步生成排名")
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
    @Operation(summary = "同步生成奖项")
    public Result<AwardAllocationService.AwardAllocationResult> generateAwards(@PathVariable Long batchId) {
        ensureSyncEvaluationEndpointsAllowed();
        AwardAllocationService.AwardAllocationResult result = awardAllocationService.allocateAwards(batchId);
        return Result.success("奖项分配完成", result);
    }

    @PutMapping("/confirm/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "确认评审结果")
    public Result<Void> confirmResult(@PathVariable Long id) {
        boolean success = evaluationResultService.confirmResult(id);
        return success ? Result.success("确认成功") : Result.error("确认失败");
    }

    @PutMapping("/object/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "标记评审结果有异议")
    public Result<Void> objectResult(@PathVariable Long id) {
        boolean success = evaluationResultService.objectResult(id);
        return success ? Result.success("标记成功") : Result.error("标记失败");
    }

    @GetMapping("/batch/{batchId}/ranks")
    @Operation(summary = "查询批次排名列表")
    public Result<List<EvaluationResult>> getBatchRanks(@PathVariable Long batchId,
                                                        @Parameter(description = "排名维度", example = "department")
                                                        @RequestParam(defaultValue = "department") String type) {
        List<EvaluationResult> results = evaluationResultService.getBatchRanks(batchId, type);
        return Result.success(results);
    }

    @PostMapping("/evaluate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "创建异步评审任务")
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
    @Operation(summary = "查询评审任务状态")
    public Result<EvaluationTaskResponse> getEvaluationTask(@PathVariable Long taskId) {
        EvaluationTaskResponse taskResponse = evaluationTaskService.getTaskById(taskId);
        if (taskResponse == null) {
            return Result.error("评审任务不存在");
        }
        return Result.success(taskResponse);
    }

    @GetMapping("/tasks/latest/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "查询批次最近一次评审任务")
    public Result<EvaluationTaskResponse> getLatestEvaluationTask(@PathVariable Long batchId) {
        EvaluationTaskResponse taskResponse = evaluationTaskService.getLatestTaskByBatchId(batchId);
        return Result.success(taskResponse);
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "导出评审结果")
    public void export(@RequestParam(required = false) Long batchId,
                       @RequestParam(required = false) String academicYear,
                       @RequestParam(required = false) Integer semester,
                       HttpServletResponse response) throws IOException {
        List<EvaluationResultExportVO> exportData = evaluationResultService.exportBatchResults(batchId, academicYear, semester);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StringBuilder fileNameBuilder = new StringBuilder("评审结果");
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
                .sheet("评审结果")
                .doWrite(exportData);
    }

    private void ensureSyncEvaluationEndpointsAllowed() {
        if (!scholarshipProperties.getEvaluation().isAllowSyncEndpoints()) {
            throw new BusinessException("同步评审入口已关闭，请使用异步任务接口");
        }
    }
}
