package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.dto.ScholarshipApplicationSubmitResponse;
import com.scholarship.dto.param.ScholarshipApplicationSubmitRequest;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ScholarshipApplicationService;
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

/**
 * 奖学金申请控制器
 * 功能：申请提交、查询、审核
 *
 * 业务流程：
 * 1. 学生查看当前可用评定批次（申请时间内）
 * 2. 学生选择已审核通过的科研成果作为关联项，填写自我评价后提交
 * 3. 导师/管理员审核申请并填写审核意见
 * 4. 审核通过后进入评定环节
 *
 * 权限说明：
 * - 提交/查看我的申请：仅学生
 * - 审核：仅导师和管理员
 * - 分页查询：所有角色可访问（数据域受限制）
 */
@Slf4j
@RestController
@RequestMapping("/application")
@RequiredArgsConstructor
@Tag(name = "奖学金申请管理", description = "奖学金申请提交、查询和审核接口")
public class ScholarshipApplicationController {

    private final ScholarshipApplicationService applicationService;

    /**
     * 分页查询申请记录
     * 根据角色自动过滤数据范围：
     * - 管理员：可查看所有申请
     * - 导师：仅能查看指导学生的申请
     * - 学生：仅能查看自己的申请
     *
     * @param current    当前页码
     * @param size       每页条数
     * @param batchId    筛选指定批次的申请
     * @param studentId  筛选指定学生的申请
     * @param status     申请状态筛选
     * @param loginUser  当前登录用户
     * @return 分页后的申请记录列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT','ROLE_TUTOR','ROLE_ADMIN')")
    @Operation(summary = "分页查询申请记录", description = "支持按批次ID、学生ID、状态筛选，数据域受角色限制")
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
        // 数据域过滤下沉到 Service 层，由 DataScopeHelper 统一处理
        IPage<ScholarshipApplication> page = applicationService.pageApplications(current, size, batchId, studentId, status, loginUser);
        return Result.success(page);
    }

    /**
     * 获取申请详情
     * 返回申请信息及关联的成果列表
     *
     * @param id        申请ID
     * @param loginUser 当前登录用户
     * @return 申请详情（含成果列表）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_TUTOR', 'ROLE_ADMIN')")
    @Operation(summary = "获取申请详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "申请不存在")
    })
    public Result<ScholarshipApplicationDetailVO> getById(@PathVariable Long id,
                                                          @AuthenticationPrincipal LoginUser loginUser) {
        ScholarshipApplicationDetailVO application = applicationService.getDetailById(id, loginUser);
        if (application == null) {
            return Result.error("申请不存在");
        }
        return Result.success(application);
    }

    /**
     * 获取当前学生可选成果列表
     * 返回已通过导师审核的科研成果（论文/专利/项目/竞赛），可用于关联到奖学金申请
     *
     * @param loginUser 当前登录学生
     * @return 可选成果列表（含成果ID、类型、标题、得分等）
     */
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

    /**
     * 提交奖学金申请
     * 包含自我评价、补充说明、关联成果列表
     *
     * @param application 申请数据（批次ID、自我评价、成果关联列表）
     * @param loginUser   当前登录学生
     * @return 提交结果（含申请编号）
     */
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

    /**
     * 审核奖学金申请
     * 导师或管理员对申请进行审核并填写意见
     *
     * @param id        申请ID
     * @param request   审核意见
     * @param loginUser 当前登录用户
     * @return 操作结果
     */
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

    /**
     * 审核请求体
     * @param opinion 审核意见（必填）
     */
    public record ReviewApplicationRequest(@NotBlank(message = "审核意见不能为空") String opinion) {
    }
}
