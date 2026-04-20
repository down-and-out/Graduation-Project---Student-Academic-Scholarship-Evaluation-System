package com.scholarship.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.dto.param.EvaluationResultQueryParam;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.service.AwardAllocationService;
import com.scholarship.service.EvaluationCalculationService;
import com.scholarship.service.EvaluationRankService;
import com.scholarship.service.EvaluationResultService;
import com.scholarship.service.StudentInfoService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/evaluation-result")
@RequiredArgsConstructor
@Tag(name = "11-评定结果管理", description = "奖学金评定结果的查询接口")
public class EvaluationResultController {

    private final EvaluationResultService evaluationResultService;
    private final EvaluationCalculationService evaluationCalculationService;
    private final EvaluationRankService evaluationRankService;
    private final AwardAllocationService awardAllocationService;
    private final StudentInfoService studentInfoService;

    @GetMapping("/page")
    @Operation(summary = "分页查询评定结果", description = "支持按批次 ID、状态、学号、姓名筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<EvaluationResult>> page(@Valid @ModelAttribute EvaluationResultQueryParam queryParam,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        Long studentId = queryParam.getStudentId();
        if (loginUser != null && loginUser.getUserType() == 1) {
            StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
            if (studentInfo == null) {
                return Result.success(new Page<>(queryParam.getCurrent(), queryParam.getSize(), 0));
            }
            studentId = studentInfo.getId();
        }

        IPage<EvaluationResult> result = evaluationResultService.pageResults(
                queryParam.getCurrent(),
                queryParam.getSize(),
                queryParam.getBatchId(),
                studentId,
                queryParam.getStatus(),
                queryParam.getKeyword()
        );
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取评定结果详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "结果不存在")
    })
    public Result<EvaluationResult> getById(@PathVariable Long id) {
        EvaluationResult result = evaluationResultService.getById(id);
        if (result == null) {
            return Result.error("结果不存在");
        }
        return Result.success(result);
    }

    @GetMapping("/my-result")
    @Operation(summary = "获取我的评定结果", description = "获取当前登录学生的评定结果")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "401", description = "用户未登录"),
        @ApiResponse(responseCode = "404", description = "未找到学生信息或该批次暂无评定结果")
    })
    public Result<EvaluationResult> getMyResult(
            @Parameter(description = "批次 ID", example = "1") @RequestParam(required = false) Long batchId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("用户未登录");
        }

        StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
        if (studentInfo == null) {
            return Result.error("未找到学生信息");
        }

        EvaluationResult result = evaluationResultService.getStudentResult(studentInfo.getId(), batchId);
        if (result == null) {
            return Result.error("暂无评定结果");
        }

        return Result.success(result);
    }

    @PostMapping("/calculate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "计算批次评分", description = "计算某批次下所有申请的评分")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "计算成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Map<String, Object>> calculateBatch(@PathVariable Long batchId) {
        log.info("管理员触发批次评分计算，batchId={}", batchId);

        try {
            Map<Long, EvaluationResult> results = evaluationCalculationService.calculateBatchApplications(batchId);

            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("batchId", batchId);
            stats.put("calculatedCount", results.size());
            stats.put("results", results);

            return Result.success("评分计算完成", stats);
        } catch (Exception e) {
            log.error("批次评分计算失败，batchId={}", batchId, e);
            return Result.error("评分计算失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate-ranks/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "生成批次排名", description = "生成某批次下所有学生的院系排名和专业排名")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "生成成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Map<String, Object>> generateRanks(@PathVariable Long batchId) {
        log.info("管理员触发批次排名生成，batchId={}", batchId);

        try {
            Map<Long, EvaluationResult> rankResults = evaluationRankService.generateBatchRanks(batchId);

            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("batchId", batchId);
            stats.put("rankedCount", rankResults.size());

            return Result.success("排名生成完成", stats);
        } catch (Exception e) {
            log.error("批次排名生成失败，batchId={}", batchId, e);
            return Result.error("排名生成失败: " + e.getMessage());
        }
    }

    @PostMapping("/generate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "生成评定结果", description = "根据评分和排名自动分配奖项等级和奖学金金额")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "生成成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<AwardAllocationService.AwardAllocationResult> generateAwards(@PathVariable Long batchId) {
        log.info("管理员触发奖项分配，batchId={}", batchId);

        try {
            AwardAllocationService.AwardAllocationResult result = awardAllocationService.allocateAwards(batchId);
            return Result.success("奖项分配完成", result);
        } catch (Exception e) {
            log.error("奖项分配失败，batchId={}", batchId, e);
            return Result.error("奖项分配失败: " + e.getMessage());
        }
    }

    @PutMapping("/confirm/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "确认评定结果", description = "公示无异议后确认评定结果")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "确认成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "结果不存在")
    })
    public Result<Void> confirmResult(@PathVariable Long id) {
        log.info("确认评定结果，id={}", id);
        boolean success = evaluationResultService.confirmResult(id);
        return success ? Result.success("结果已确认") : Result.error("确认失败");
    }

    @PutMapping("/object/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "标记有异议", description = "将评定结果标记为有异议状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "结果不存在")
    })
    public Result<Void> objectResult(@PathVariable Long id) {
        log.info("标记有异议，id={}", id);
        boolean success = evaluationResultService.objectResult(id);
        return success ? Result.success("已标记有异议") : Result.error("操作失败");
    }

    @GetMapping("/batch/{batchId}/ranks")
    @Operation(summary = "获取批次排名列表", description = "支持按院系或专业查看排名")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<List<EvaluationResult>> getBatchRanks(
            @PathVariable Long batchId,
            @Parameter(description = "排名类型", example = "department")
            @RequestParam(defaultValue = "department") String type) {
        log.info("获取批次排名列表，batchId={}, type={}", batchId, type);
        List<EvaluationResult> results = evaluationResultService.getBatchRanks(batchId, type);
        return Result.success(results);
    }

    @PostMapping("/evaluate/{batchId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "一键完成评定", description = "依次执行评分计算、排名生成、奖项分配")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "评定完成"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "批次不存在")
    })
    public Result<Map<String, Object>> evaluateBatch(@PathVariable Long batchId) {
        log.info("一键完成评定，batchId={}", batchId);

        try {
            Map<String, Object> result = new java.util.HashMap<>();

            log.info("步骤 1: 计算评分");
            Map<Long, EvaluationResult> calcResults = evaluationCalculationService.calculateBatchApplications(batchId);
            result.put("calculatedCount", calcResults.size());

            log.info("步骤 2: 生成排名");
            Map<Long, EvaluationResult> rankResults = evaluationRankService.generateBatchRanks(batchId);
            result.put("rankedCount", rankResults.size());

            log.info("步骤 3: 分配奖项");
            AwardAllocationService.AwardAllocationResult awardResult = awardAllocationService.allocateAwards(batchId);
            result.put("awardResult", awardResult);

            result.put("batchId", batchId);
            result.put("status", "completed");

            return Result.success("评定完成", result);
        } catch (Exception e) {
            log.error("一键评定失败，batchId={}", batchId, e);
            return Result.error("评定失败: " + e.getMessage());
        }
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "导出评定结果", description = "导出某批次的评定结果为 Excel 文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导出成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public void export(@Parameter(description = "批次 ID")
                       @RequestParam(required = false) Long batchId,
                       HttpServletResponse response) {
        log.info("导出评定结果，batchId={}", batchId);

        try {
            List<EvaluationResultExportVO> exportData = evaluationResultService.exportBatchResults(batchId);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("评定结果" + (batchId != null ? "_" + batchId : ""), "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), EvaluationResultExportVO.class)
                .sheet("评定结果")
                .doWrite(exportData);

            log.info("评定结果导出成功，记录数={}", exportData.size());
        } catch (Exception e) {
            log.error("评定结果导出失败", e);
        }
    }
}
