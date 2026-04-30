package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.result.Result;
import com.scholarship.dto.ScholarshipApplicationSubmitResponse;
import com.scholarship.dto.param.ScholarshipApplicationSubmitRequest;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ScholarshipApplicationService;
import com.scholarship.service.StudentInfoService;
import com.scholarship.vo.ApplicationAchievementVO;
import com.scholarship.vo.ScholarshipApplicationDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
@Tag(name = "奖学金申请管理", description = "奖学金申请提交、查询和审核接口")
public class ScholarshipApplicationController {

    private final ScholarshipApplicationService applicationService;
    private final StudentInfoService studentInfoService;

    @GetMapping("/page")
    @Operation(summary = "分页查询申请记录", description = "支持按批次ID、学生ID、状态筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<ScholarshipApplication>> page(
            @Parameter(description = "当前页", example = "1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "批次ID", example = "1")
            @RequestParam(required = false) Long batchId,
            @Parameter(description = "学生ID", example = "1")
            @RequestParam(required = false) Long studentId,
            @Parameter(description = "状态", example = "1")
            @RequestParam(required = false) Integer status,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (loginUser != null && UserTypeEnum.isStudent(loginUser.getUserType())) {
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
    public Result<ScholarshipApplicationDetailVO> getById(@PathVariable Long id) {
        ScholarshipApplicationDetailVO application = applicationService.getDetailById(id);
        if (application == null) {
            return Result.error("申请不存在");
        }
        return Result.success(application);
    }

    @GetMapping("/available-achievements")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "获取当前学生可选成果", description = "返回当前登录学生已审核通过、可关联到奖学金申请的成果")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功")
    })
    public Result<List<ApplicationAchievementVO>> listAvailableAchievements(
            @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(applicationService.listAvailableAchievements(loginUser.getUserId()));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交奖学金申请", description = "研究生提交奖学金申请并保存成果关联")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "提交成功"),
            @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public Result<ScholarshipApplicationSubmitResponse> submit(
            @Valid @RequestBody ScholarshipApplicationSubmitRequest application,
            @AuthenticationPrincipal LoginUser loginUser) {
        ScholarshipApplicationSubmitResponse response = applicationService.submitApplication(
                application,
                loginUser.getUserId()
        );
        return Result.success(response.getMessage(), response);
    }

    @PutMapping("/review/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TUTOR', 'ROLE_ADMIN')")
    @Operation(summary = "审核申请", description = "导师或管理员审核奖学金申请并记录审核意见")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "审核成功"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "申请不存在")
    })
    public Result<Void> review(
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "审核参数")
            @Valid @RequestBody ReviewApplicationRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = applicationService.reviewApplication(
                id,
                request.opinion(),
                loginUser.getUserId(),
                loginUser.getUsername(),
                loginUser.getUserType()
        );
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    public record ReviewApplicationRequest(@NotBlank(message = "审核意见不能为空") String opinion) {
    }
}
