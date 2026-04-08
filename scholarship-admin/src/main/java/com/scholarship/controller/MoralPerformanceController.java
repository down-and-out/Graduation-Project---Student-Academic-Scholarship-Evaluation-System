package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.dto.query.MoralPerformanceQuery;
import com.scholarship.entity.MoralPerformance;
import com.scholarship.enums.AuditStatusEnum;
import com.scholarship.security.LoginUser;
import com.scholarship.service.MoralPerformanceService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 德育表现控制器
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/moral-performance")
@RequiredArgsConstructor
@Tag(name = "04-德育表现管理", description = "德育表现的查询、录入和审核接口")
public class MoralPerformanceController {

    private final MoralPerformanceService moralPerformanceService;

    /**
     * 分页查询德育表现
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "分页查询德育表现", description = "支持按学生、类型、学年筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<MoralPerformance>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID") @RequestParam(required = false) Long studentId,
            @Parameter(description = "表现类型：1-志愿服务 2-社会实践 3-荣誉称号 4-学生干部 5-其他") @RequestParam(required = false) Integer performanceType,
            @Parameter(description = "学年") @RequestParam(required = false) String academicYear,
            @Parameter(description = "审核状态：0-待审核 1-通过 2-驳回") @RequestParam(required = false) Integer auditStatus) {

        MoralPerformanceQuery query = new MoralPerformanceQuery();
        query.setCurrent(current);
        query.setSize(size);
        query.setStudentId(studentId);
        query.setPerformanceType(performanceType);
        query.setAcademicYear(academicYear);
        query.setAuditStatus(auditStatus);

        IPage<MoralPerformance> result = moralPerformanceService.queryPage(query);
        return Result.success(result);
    }

    /**
     * 获取德育表现详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取德育表现详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "记录不存在")
    })
    public Result<MoralPerformance> getById(@PathVariable Long id) {
        MoralPerformance performance = moralPerformanceService.getById(id);
        if (performance == null) {
            return Result.error("记录不存在");
        }
        return Result.success(performance);
    }

    /**
     * 录入德育表现
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "录入德育表现", description = "录入学生德育表现记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "录入成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@RequestBody MoralPerformance performance) {
        performance.setAuditStatus(AuditStatusEnum.PENDING.getCode()); // 使用枚举
        boolean success = moralPerformanceService.save(performance);
        return success ? Result.success("录入成功") : Result.error("录入失败");
    }

    /**
     * 更新德育表现
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "更新德育表现", description = "修改德育表现记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> update(@RequestBody MoralPerformance performance) {
        boolean success = moralPerformanceService.updateById(performance);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除德育表现
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除德育表现", description = "删除德育表现记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = moralPerformanceService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 审核德育表现
     */
    @PutMapping("/audit/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "审核德育表现", description = "审核德育表现记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "审核成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> audit(
            @PathVariable Long id,
            @Parameter(description = "审核状态：1-通过 2-驳回") @RequestParam Integer auditStatus,
            @Parameter(description = "审核意见") @RequestParam(required = false) String auditComment,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("审核德育表现，id={}, status={}", id, auditStatus);

        boolean success = moralPerformanceService.audit(id, auditStatus, auditComment, loginUser.getUserId());
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    /**
     * 获取德育总分
     */
    @GetMapping("/total-score/{studentId}")
    @Operation(summary = "获取德育总分", description = "计算指定学生的德育总分")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "计算成功")
    })
    public Result<BigDecimal> getTotalScore(
            @PathVariable Long studentId,
            @Parameter(description = "批次 ID") @RequestParam(required = false) Long batchId) {
        BigDecimal score = moralPerformanceService.calculateTotalScore(studentId, batchId);
        return Result.success(score);
    }

    /**
     * 按类型统计分数
     */
    @GetMapping("/score-by-type/{studentId}")
    @Operation(summary = "按类型统计分数", description = "按表现类型统计学生的德育分数")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "统计成功")
    })
    public Result<Map<String, BigDecimal>> getScoreByType(
            @PathVariable Long studentId,
            @Parameter(description = "批次 ID") @RequestParam(required = false) Long batchId) {

        Map<String, BigDecimal> scoreMap = new HashMap<>();
        scoreMap.put("志愿服务", moralPerformanceService.calculateScoreByType(studentId, batchId, 1));
        scoreMap.put("社会实践", moralPerformanceService.calculateScoreByType(studentId, batchId, 2));
        scoreMap.put("荣誉称号", moralPerformanceService.calculateScoreByType(studentId, batchId, 3));
        scoreMap.put("学生干部", moralPerformanceService.calculateScoreByType(studentId, batchId, 4));
        scoreMap.put("其他", moralPerformanceService.calculateScoreByType(studentId, batchId, 5));

        return Result.success(scoreMap);
    }

    /**
     * 获取学生德育表现列表
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "获取学生德育表现列表", description = "获取指定学生的所有德育表现记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<MoralPerformance>> getByStudentId(
            @PathVariable Long studentId,
            @Parameter(description = "批次 ID") @RequestParam(required = false) Long batchId) {
        List<MoralPerformance> list = moralPerformanceService.listByStudentId(studentId, batchId);
        return Result.success(list);
    }
}
