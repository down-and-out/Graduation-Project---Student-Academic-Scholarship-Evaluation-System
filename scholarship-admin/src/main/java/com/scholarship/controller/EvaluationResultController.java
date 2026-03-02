package com.scholarship.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.EvaluationResult;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.service.*;
import com.scholarship.vo.EvaluationResultExportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 评定结果控制器
 * <p>
 * 处理奖学金评定结果相关的请求，包括结果查询、公示等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/evaluation-result")
@RequiredArgsConstructor
@Tag(name = "11-评定结果管理", description = "奖学金评定结果的查询接口")
public class EvaluationResultController {

    private final EvaluationResultService evaluationResultService;
    private final StudentInfoService studentInfoService;
    private final EvaluationCalculationService evaluationCalculationService;
    private final EvaluationRankService evaluationRankService;
    private final AwardAllocationService awardAllocationService;

    /**
     * 分页查询评定结果
     *
     * @param current   当前页
     * @param size      每页大小
     * @param batchId   批次 ID
     * @param studentId 学生 ID
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询评定结果", description = "支持按批次 ID、学生 ID 筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<EvaluationResult>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "批次 ID", example = "1") @RequestParam(required = false) Long batchId,
            @Parameter(description = "学生 ID", example = "1") @RequestParam(required = false) Long studentId) {
        Page<EvaluationResult> page = new Page<>(current, size);
        IPage<EvaluationResult> result = evaluationResultService.page(page);
        return Result.success(result);
    }

    /**
     * 获取结果详情
     *
     * @param id 结果 ID
     * @return 结果详情
     */
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

    /**
     * 获取我的评定结果
     *
     * @param batchId 批次 ID（可选，不传则获取最新的评定结果）
     * @return 评定结果
     */
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
        // 从当前登录用户获取学生 ID
        if (loginUser == null || loginUser.getUserId() == null) {
            return Result.error("用户未登录");
        }

        // 根据用户 ID 查询学生信息
        StudentInfo studentInfo = studentInfoService.getOne(
            new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getUserId, loginUser.getUserId())
        );

        if (studentInfo == null) {
            return Result.error("未找到学生信息");
        }

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<EvaluationResult>()
            .eq(EvaluationResult::getStudentId, studentInfo.getId());

        if (batchId != null) {
            wrapper.eq(EvaluationResult::getBatchId, batchId);
        } else {
            // 如果没有传入 batchId，获取最新的评定结果
            wrapper.orderByDesc(EvaluationResult::getBatchId);
        }

        EvaluationResult result = evaluationResultService.getOne(wrapper);

        if (result == null) {
            return Result.error("暂无评定结果");
        }

        return Result.success(result);
    }

    /**
     * 计算某批次所有申请的评分
     *
     * @param batchId 批次 ID
     * @return 计算结果统计
     */
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

            // 构建统计信息
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("batchId", batchId);
            stats.put("calculatedCount", results.size());
            stats.put("results", results);

            return Result.success("评分计算完成", stats);
        } catch (Exception e) {
            log.error("批次评分计算失败，batchId={}", batchId, e);
            return Result.error("评分计算失败：" + e.getMessage());
        }
    }

    /**
     * 生成某批次的排名
     *
     * @param batchId 批次 ID
     * @return 排名结果统计
     */
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

            // 构建统计信息
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("batchId", batchId);
            stats.put("rankedCount", rankResults.size());

            return Result.success("排名生成完成", stats);
        } catch (Exception e) {
            log.error("批次排名生成失败，batchId={}", batchId, e);
            return Result.error("排名生成失败：" + e.getMessage());
        }
    }

    /**
     * 生成评定结果（分配奖项）
     *
     * @param batchId 批次 ID
     * @return 奖项分配结果
     */
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
            return Result.error("奖项分配失败：" + e.getMessage());
        }
    }

    /**
     * 确认评定结果
     *
     * @param id 结果 ID
     * @return 是否成功
     */
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

        EvaluationResult result = evaluationResultService.getById(id);
        if (result == null) {
            return Result.error("评定结果不存在");
        }

        result.setResultStatus(2); // 2-已确定
        boolean success = evaluationResultService.updateById(result);

        return success ? Result.success("结果已确认") : Result.error("确认失败");
    }

    /**
     * 标记有异议
     *
     * @param id 结果 ID
     * @return 是否成功
     */
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

        EvaluationResult result = evaluationResultService.getById(id);
        if (result == null) {
            return Result.error("评定结果不存在");
        }

        result.setResultStatus(3); // 3-有异议
        boolean success = evaluationResultService.updateById(result);

        return success ? Result.success("已标记有异议") : Result.error("操作失败");
    }

    /**
     * 获取批次排名列表
     *
     * @param batchId 批次 ID
     * @param type 排名类型（department-院系排名，major-专业排名）
     * @return 排名列表
     */
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

        LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<EvaluationResult>()
            .eq(EvaluationResult::getBatchId, batchId)
            .orderByAsc(EvaluationResult::getTotalScore);

        if ("department".equals(type)) {
            wrapper.orderByAsc(EvaluationResult::getDepartmentRank);
        } else {
            wrapper.orderByAsc(EvaluationResult::getMajorRank);
        }

        List<EvaluationResult> results = evaluationResultService.list(wrapper);
        return Result.success(results);
    }

    /**
     * 一键完成评定（计算 + 排名 + 奖项分配）
     *
     * @param batchId 批次 ID
     * @return 评定结果统计
     */
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

            // 1. 计算评分
            log.info("步骤 1: 计算评分");
            Map<Long, EvaluationResult> calcResults = evaluationCalculationService.calculateBatchApplications(batchId);
            result.put("calculatedCount", calcResults.size());

            // 2. 生成排名
            log.info("步骤 2: 生成排名");
            Map<Long, EvaluationResult> rankResults = evaluationRankService.generateBatchRanks(batchId);
            result.put("rankedCount", rankResults.size());

            // 3. 分配奖项
            log.info("步骤 3: 分配奖项");
            AwardAllocationService.AwardAllocationResult awardResult = awardAllocationService.allocateAwards(batchId);
            result.put("awardResult", awardResult);

            result.put("batchId", batchId);
            result.put("status", "completed");

            return Result.success("评定完成", result);
        } catch (Exception e) {
            log.error("一键评定失败，batchId={}", batchId, e);
            return Result.error("评定失败：" + e.getMessage());
        }
    }

    /**
     * 导出评定结果为 Excel
     *
     * @param batchId 批次 ID（可选，不传则导出所有）
     * @param response HTTP 响应
     */
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
            // 查询数据
            LambdaQueryWrapper<EvaluationResult> wrapper = new LambdaQueryWrapper<>();
            if (batchId != null) {
                wrapper.eq(EvaluationResult::getBatchId, batchId);
            }
            wrapper.orderByDesc(EvaluationResult::getTotalScore);

            List<EvaluationResult> results = evaluationResultService.list(wrapper);

            // 转换为导出 VO
            List<EvaluationResultExportVO> exportData = new java.util.ArrayList<>();
            int index = 1;
            for (EvaluationResult result : results) {
                EvaluationResultExportVO vo = new EvaluationResultExportVO();
                vo.setIndex(index++);
                vo.setStudentNo(result.getStudentNo());
                vo.setStudentName(result.getStudentName());
                vo.setDepartment(result.getDepartment());
                vo.setMajor(result.getMajor());
                vo.setCourseScore(result.getCourseScore());
                vo.setResearchScore(result.getResearchScore());
                vo.setCompetitionScore(result.getCompetitionScore());
                vo.setQualityScore(result.getQualityScore());
                vo.setTotalScore(result.getTotalScore());
                vo.setDepartmentRank(result.getDepartmentRank());
                vo.setMajorRank(result.getMajorRank());
                vo.setAwardLevelName(getAwardLevelName(result.getAwardLevel()));
                vo.setAwardAmount(result.getAwardAmount());
                vo.setResultStatusName(getResultStatusName(result.getResultStatus()));
                exportData.add(vo);
            }

            // 设置 Excel 响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("评定结果" + (batchId != null ? "_" + batchId : ""), "UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            // 使用 EasyExcel 写入
            com.alibaba.excel.EasyExcel.write(response.getOutputStream(), EvaluationResultExportVO.class)
                .sheet("评定结果")
                .doWrite(exportData);

            log.info("评定结果导出成功，记录数={}", exportData.size());
        } catch (Exception e) {
            log.error("评定结果导出失败", e);
        }
    }

    /**
     * 获取获奖等级名称
     */
    private String getAwardLevelName(Integer awardLevel) {
        if (awardLevel == null) return "";
        return switch (awardLevel) {
            case 1 -> "特等奖学金";
            case 2 -> "一等奖学金";
            case 3 -> "二等奖学金";
            case 4 -> "三等奖学金";
            case 5 -> "未获奖";
            default -> "";
        };
    }

    /**
     * 获取结果状态名称
     */
    private String getResultStatusName(Integer resultStatus) {
        if (resultStatus == null) return "";
        return switch (resultStatus) {
            case 1 -> "公示中";
            case 2 -> "已确定";
            case 3 -> "有异议";
            default -> "";
        };
    }
}
