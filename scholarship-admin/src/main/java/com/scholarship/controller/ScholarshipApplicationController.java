package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ScholarshipApplicationService;
import com.scholarship.service.StudentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
@Tag(name = "10-奖学金申请管理", description = "奖学金申请的提交、查询、审核接口")
public class ScholarshipApplicationController {

    private final ScholarshipApplicationService applicationService;
    private final StudentInfoService studentInfoService;

    @GetMapping("/page")
    @Operation(summary = "分页查询申请记录", description = "支持按批次 ID、学生 ID、状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<ScholarshipApplication>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "批次 ID", example = "1") @RequestParam(required = false) Long batchId,
            @Parameter(description = "学生 ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "状态", example = "1") @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser != null && loginUser.getUserType() == 1) {
            StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
            if (studentInfo == null) {
                return Result.success(new Page<>(current, size, 0));
            }
            studentId = studentInfo.getId();
        }

        IPage<ScholarshipApplication> page = applicationService.pageApplications(current, size, batchId, studentId, status);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取申请详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "申请不存在")
    })
    public Result<ScholarshipApplication> getById(@PathVariable Long id) {
        ScholarshipApplication application = applicationService.getById(id);
        if (application == null) {
            return Result.error("申请不存在");
        }
        return Result.success(application);
    }

    @PostMapping("/submit")
    @Operation(summary = "提交奖学金申请", description = "研究生提交奖学金申请，系统自动生成申请编号")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "提交成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public Result<Void> submit(
            @Valid @RequestBody ScholarshipApplication application,
            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = applicationService.submitApplication(application, loginUser.getUserId());
        return success ? Result.success("提交成功") : Result.error("提交失败");
    }

    @PutMapping("/review/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TUTOR', 'ROLE_ADMIN')")
    @Operation(summary = "导师审核申请", description = "导师对奖学金申请进行审核并填写意见")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "审核成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "申请不存在")
    })
    public Result<Void> review(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "审核参数")
            @RequestBody ReviewApplicationRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        Long tutorId = loginUser.getUserId();
        boolean success = applicationService.tutorReview(id, request.opinion(), tutorId);
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    public record ReviewApplicationRequest(
        String opinion
    ) {}
}
