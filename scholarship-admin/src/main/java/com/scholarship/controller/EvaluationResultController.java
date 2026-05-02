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
import com.scholarship.service.AsyncTaskService;
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

/**
 * 评定结果管理控制器
 *
 * 功能说明：
 * - 分页查询评定结果（支持学年、学期、关键词、状态筛选）
 * - 获取评定结果详情
 * - 调整评定结果（用于异议处理）
 * - 获取当前学生最新评定结果
 * - 确认评定结果
 * - 标记异议
 * - 获取批次排名列表
 * - 创建异步评定任务
 * - 获取评定任务详情
 * - 获取最新批次评定任务
 * - 导出评定结果（同步/异步）
 *
 * 权限说明：
 * - 分页查询、结果详情：管理员和学生可访问（学生只能看自己的）
 * - 调整、确认、标记异议：仅管理员可访问
 * - 评定任务相关：仅管理员可访问
 */
@Slf4j
@RestController
@RequestMapping("/evaluation-result")
@RequiredArgsConstructor
@Tag(name = "06-评定结果管理", description = "奖学金评定结果的查询、调整、导出接口（管理员）")
public class EvaluationResultController {

    private final EvaluationResultService evaluationResultService;
    private final EvaluationCalculationService evaluationCalculationService;
    private final EvaluationRankService evaluationRankService;
    private final AwardAllocationService awardAllocationService;
    private final EvaluationTaskService evaluationTaskService;
    private final StudentInfoService studentInfoService;
    private final AsyncTaskService asyncTaskService;
    private final ScholarshipProperties scholarshipProperties;

    /**
     * 分页查询评定结果
     * - 管理员：可查看所有结果
     * - 学生：只能查看自己的结果（自动根据当前用户过滤）
     *
     * @param queryParam 查询参数
     * @param loginUser  当前登录用户
     * @return 分页后的评定结果列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STUDENT')")
    @Operation(summary = "分页查询评定结果", description = "管理员可查看所有结果，学生只能查看自己的")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "查询成功"))
    public Result<IPage<AdminEvaluationResultVO>> page(@Valid @ModelAttribute EvaluationResultQueryParam queryParam,
                                                       @AuthenticationPrincipal LoginUser loginUser) {
        Long studentId = queryParam.getStudentId();
        // 如果是学生，自动根据当前用户过滤
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

    /**
     * 获取评定结果详情
     * - 管理员：可查看任何结果
     * - 学生：只能查看自己的结果
     *
     * @param id        结果ID
     * @param loginUser 当前登录用户
     * @return 评定结果详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STUDENT')")
    @Operation(summary = "获取评定结果详情")
    public Result<AdminEvaluationResultVO> getById(@PathVariable Long id,
                                                   @AuthenticationPrincipal LoginUser loginUser) {
        AdminEvaluationResultVO result = evaluationResultService.getAdminResultById(id);
        if (result == null) {
            return Result.error("评定结果不存在");
        }

        // 学生只能查看自己的结果
        if (loginUser != null && UserTypeEnum.isStudent(loginUser.getUserType())) {
            StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
            if (studentInfo == null || !studentInfo.getId().equals(result.getStudentId())) {
                return Result.error(403, "无权访问该评定结果");
            }
        }
        return Result.success(result);
    }

    /**
     * 调整评定结果
     * 用于异议处理，管理员可调整奖项等级并填写调整原因
     *
     * @param id      结果ID
     * @param request 调整请求（奖项等级 + 调整原因）
     * @return 操作结果
     */
    @PutMapping("/adjust/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "调整评定结果", description = "用于异议处理，调整奖项等级并填写调整原因")
    public Result<Void> adjustResult(@PathVariable Long id,
                                     @Valid @RequestBody EvaluationResultAdjustRequest request) {
        boolean success = evaluationResultService.adjustResult(id, request);
        return success ? Result.success("调整成功") : Result.error("调整失败");
    }

    /**
     * 获取当前学生的最新评定结果
     *
     * @param batchId   可选，指定批次ID
     * @param loginUser 当前登录学生
     * @return 最新的评定结果
     */
    @GetMapping("/my-result")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "获取当前学生的最新结果")
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

    /**
     * 计算批次评定分数（同步）
     * 仅在配置允许的情况下可用，建议使用异步评定接口
     *
     * @param batchId 批次ID
     * @return 计算结果汇总
     */
    @PostMapping("/calculate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "计算批次评定分数（同步）", description = "仅在配置允许的情况下可用")
    public Result<BatchCalculationSummary> calculateBatch(@PathVariable Long batchId) {
        ensureSyncEvaluationEndpointsAllowed();
        BatchCalculationSummary summary = evaluationCalculationService.calculateBatchApplications(batchId);
        return Result.success("计算完成", summary);
    }

    /**
     * 生成批次排名（同步）
     *
     * @param batchId 批次ID
     * @return 排名结果统计
     */
    @PostMapping("/generate-ranks/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "生成批次排名（同步）")
    public Result<Map<String, Object>> generateRanks(@PathVariable Long batchId) {
        ensureSyncEvaluationEndpointsAllowed();
        Map<Long, EvaluationResult> rankResults = evaluationRankService.generateBatchRanks(batchId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("batchId", batchId);
        stats.put("rankedCount", rankResults.size());
        return Result.success("排名生成完成", stats);
    }

    /**
     * 生成批次奖项（同步）
     *
     * @param batchId 批次ID
     * @return 奖项分配结果
     */
    @PostMapping("/generate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "生成批次奖项（同步）")
    public Result<AwardAllocationService.AwardAllocationResult> generateAwards(@PathVariable Long batchId) {
        ensureSyncEvaluationEndpointsAllowed();
        AwardAllocationService.AwardAllocationResult result = awardAllocationService.allocateAwards(batchId);
        return Result.success("奖项分配完成", result);
    }

    /**
     * 确认评定结果
     * 将结果状态从公示中改为已确认
     *
     * @param id 结果ID
     * @return 操作结果
     */
    @PutMapping("/confirm/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "确认评定结果", description = "将结果状态从公示中改为已确认")
    public Result<Void> confirmResult(@PathVariable Long id) {
        boolean success = evaluationResultService.confirmResult(id);
        return success ? Result.success("确认成功") : Result.error("确认失败");
    }

    /**
     * 标记异议
     * 将结果状态标记为有异议
     *
     * @param id 结果ID
     * @return 操作结果
     */
    @PutMapping("/object/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "标记异议", description = "将结果状态标记为有异议")
    public Result<Void> objectResult(@PathVariable Long id) {
        boolean success = evaluationResultService.objectResult(id);
        return success ? Result.success("已标记异议") : Result.error("标记异议失败");
    }

    /**
     * 获取批次排名列表
     *
     * @param batchId 批次ID
     * @param type    排名类型（department=院系排名，major=专业排名）
     * @return 排名列表
     */
    @GetMapping("/batch/{batchId}/ranks")
    @Operation(summary = "获取批次排名列表")
    public Result<List<EvaluationResult>> getBatchRanks(@PathVariable Long batchId,
                                                         @Parameter(description = "排名类型", example = "department")
                                                         @RequestParam(defaultValue = "department") String type) {
        List<EvaluationResult> results = evaluationResultService.getBatchRanks(batchId, type);
        return Result.success(results);
    }

    /**
     * 创建异步评定任务
     * 推荐使用此接口进行评定，页面可通过轮询任务状态获取进度
     *
     * @param batchId   批次ID
     * @param loginUser 当前登录用户
     * @return 任务响应（含任务ID）
     */
    @PostMapping("/evaluate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "创建异步评定任务", description = "推荐使用此接口进行评定，页面可通过轮询任务状态获取进度")
    public Result<EvaluationTaskResponse> evaluateBatch(@PathVariable Long batchId,
                                                         @AuthenticationPrincipal LoginUser loginUser) {
        EvaluationTaskResponse taskResponse = evaluationTaskService.createEvaluationTask(
                batchId,
                loginUser != null ? loginUser.getUserId() : null,
                loginUser != null ? loginUser.getRealName() : null
        );
        // 如果不是复用已有任务，立即执行
        if (!Boolean.TRUE.equals(taskResponse.getReusedActiveTask())) {
            evaluationTaskService.executeTaskAsync(taskResponse.getTaskId());
        }
        return Result.success(taskResponse.getMessage(), taskResponse);
    }

    /**
     * 获取评定任务详情
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @GetMapping("/tasks/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取评定任务详情")
    public Result<EvaluationTaskResponse> getEvaluationTask(@PathVariable Long taskId) {
        EvaluationTaskResponse taskResponse = evaluationTaskService.getTaskById(taskId);
        if (taskResponse == null) {
            return Result.error("评定任务不存在");
        }
        return Result.success(taskResponse);
    }

    /**
     * 获取批次最新评定任务
     *
     * @param batchId 批次ID
     * @return 最新任务详情
     */
    @GetMapping("/tasks/latest/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取批次最新评定任务")
    public Result<EvaluationTaskResponse> getLatestEvaluationTask(@PathVariable Long batchId) {
        EvaluationTaskResponse taskResponse = evaluationTaskService.getLatestTaskByBatchId(batchId);
        return Result.success(taskResponse);
    }

    /**
     * 导出评定结果（同步，直接下载）
     *
     * @param batchId      可选，批次ID
     * @param academicYear 可选，学年
     * @param semester     可选，学期
     * @param response     HTTP响应
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "导出评定结果（同步）", description = "直接下载Excel文件")
    public void export(@RequestParam(required = false) Long batchId,
                       @RequestParam(required = false) String academicYear,
                       @RequestParam(required = false) Integer semester,
                       HttpServletResponse response) throws IOException {
        List<EvaluationResultExportVO> exportData = evaluationResultService.exportBatchResults(batchId, academicYear, semester, scholarshipProperties.getEvaluation().getExportMaxRows());

        // 设置响应头
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

        // 写入Excel
        EasyExcel.write(response.getOutputStream(), EvaluationResultExportVO.class)
                .sheet("evaluation_results")
                .doWrite(exportData);
    }

    /**
     * 提交异步导出任务
     * 导出任务在后台执行，返回任务ID用于查询进度
     *
     * @param batchId      可选，批次ID
     * @param academicYear 可选，学年
     * @param semester     可选，学期
     * @param maxRows      最大导出行数（默认5000）
     * @return 任务ID
     */
    @PostMapping("/export/submit")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "提交异步导出任务", description = "导出任务在后台执行，返回任务ID用于查询进度")
    public Result<Map<String, Object>> exportAsync(@RequestParam(required = false) Long batchId,
                                                   @RequestParam(required = false) String academicYear,
                                                   @RequestParam(required = false) Integer semester,
                                                   @RequestParam(defaultValue = "5000") int maxRows) {
        // 构建文件名
        String fileName = "evaluation_results"
                + (batchId != null ? "_" + batchId : "")
                + (academicYear != null && !academicYear.isBlank() ? "_" + academicYear : "")
                + (semester != null ? "_" + semester : "");

        // 限制最大行数
        int effectiveMaxRows = Math.min(maxRows, scholarshipProperties.getEvaluation().getExportMaxRows());

        // 提交异步任务
        String taskId = asyncTaskService.submitExport("evaluation-result", fileName, ctx -> {
            try {
                List<EvaluationResultExportVO> data = evaluationResultService.exportBatchResults(batchId, academicYear, semester, effectiveMaxRows);
                String filePath = ctx.buildFilePath("xlsx");
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    EasyExcel.write(fos, EvaluationResultExportVO.class)
                            .sheet("evaluation_results")
                            .doWrite(data);
                }
                ctx.markCompleted(fileName + ".xlsx", filePath);
            } catch (Exception e) {
                log.error("Async export failed: taskId={}", ctx.getTaskId(), e);
                ctx.markFailed(e.getMessage());
            }
        });

        return Result.success("导出任务已提交", Map.of("taskId", taskId));
    }

    /**
     * 查询异步导出任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/export/status/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "查询导出任务状态")
    public Result<Map<String, Object>> exportStatus(@PathVariable String taskId) {
        Map<String, Object> status = asyncTaskService.getExportStatus(taskId);
        if (status == null) {
            return Result.error("任务不存在或已过期");
        }
        return Result.success(status);
    }

    /**
     * 下载异步导出的文件
     *
     * @param taskId   任务ID
     * @param response HTTP响应
     */
    @GetMapping("/export/download/{taskId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "下载异步导出文件")
    public void exportDownload(@PathVariable String taskId, HttpServletResponse response) throws IOException {
        String filePath = asyncTaskService.getFilePath(taskId);
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

    /**
     * 校验是否允许同步评定接口
     * 如果配置中禁用了同步接口，抛出异常
     */
    private void ensureSyncEvaluationEndpointsAllowed() {
        if (!scholarshipProperties.getEvaluation().isAllowSyncEndpoints()) {
            throw new BusinessException("Synchronous evaluation endpoints are disabled. Use async tasks instead.");
        }
    }
}
