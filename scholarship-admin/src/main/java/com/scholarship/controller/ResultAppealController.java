package com.scholarship.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ResultAppeal;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResultAppealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 结果异议控制器
 * 功能：学生对评定结果提出异议，管理员处理异议
 *
 * 业务流程：
 * 1. 学生对本人评定结果（公示期内）提交异议申请
 * 2. 管理员审核异议并填写处理意见
 * 3. 如需调整结果，可通过评定结果调整接口修改奖项等级
 *
 * 权限说明：
 * - 提交异议：仅学生（只能对自己已评定结果提出）
 * - 分页查询：管理员查看全部，学生只能查看自己的异议
 * - 处理异议：仅管理员
 */
@Slf4j
@RestController
@RequestMapping("/result-appeal")
@RequiredArgsConstructor
@Tag(name = "结果异议管理", description = "评定结果异议提交和处理接口")
public class ResultAppealController {

    private final ResultAppealService resultAppealService;
    private final StudentInfoMapper studentInfoMapper;

    /**
     * 分页查询异议
     * 根据角色过滤数据范围：管理员查看全部，学生只能查看自己的异议
     *
     * @param current      当前页码
     * @param size         每页条数
     * @param studentId    筛选指定学生（管理员用）
     * @param appealStatus 异议状态筛选
     * @param loginUser    当前登录用户
     * @return 分页后的异议列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_STUDENT')")
    @Operation(summary = "分页查询异议", description = "管理员可查看全部，学生只能查看自己的异议")
    public Result<IPage<ResultAppeal>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "异议状态", example = "1") @RequestParam(required = false) Integer appealStatus,
            @AuthenticationPrincipal LoginUser loginUser) {

        Page<ResultAppeal> page = new Page<>(current, size);
        LambdaQueryWrapper<ResultAppeal> wrapper = new LambdaQueryWrapper<>();

        if (isAdmin(loginUser)) {
            // 管理员可按学生ID筛选
            if (studentId != null) {
                wrapper.eq(ResultAppeal::getStudentId, studentId);
            }
        } else {
            // 学生只能查看自己的异议
            StudentInfo student = studentInfoMapper.selectOne(new LambdaQueryWrapper<StudentInfo>()
                    .eq(StudentInfo::getUserId, loginUser.getUserId())
                    .last("LIMIT 1"));
            if (student == null) {
                return Result.success(page);
            }
            wrapper.eq(ResultAppeal::getStudentId, student.getId());
        }

        if (appealStatus != null) {
            wrapper.eq(ResultAppeal::getAppealStatus, appealStatus);
        }
        wrapper.orderByDesc(ResultAppeal::getCreateTime);

        return Result.success(resultAppealService.page(page, wrapper));
    }

    /**
     * 提交异议
     * 学生对本人评定结果提交异议，包含异议理由和详细说明
     *
     * @param appeal   异议信息（结果ID、理由、详细说明）
     * @param loginUser 当前登录学生
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "提交异议", description = "学生对本人评定结果提交异议")
    public Result<Void> submit(@Valid @RequestBody ResultAppeal appeal,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = resultAppealService.submitAppeal(appeal, loginUser);
        return success ? Result.success("提交成功") : Result.error("提交失败");
    }

    /**
     * 处理异议
     * 管理员审核异议并填写处理意见
     *
     * @param id        异议ID
     * @param request   处理意见
     * @param loginUser 当前登录管理员
     * @return 操作结果
     */
    @PutMapping("/handle/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "处理异议", description = "管理员处理异议并填写处理意见")
    public Result<Void> handle(
            @PathVariable Long id,
            @RequestBody HandleAppealRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = resultAppealService.handleAppeal(id, request.handleOpinion(), loginUser);
        return success ? Result.success("处理成功") : Result.error("处理失败");
    }

    private boolean isAdmin(LoginUser loginUser) {
        return loginUser != null && UserTypeEnum.isAdmin(loginUser.getUserType());
    }

    /**
     * 处理异议请求体
     * @param handleOpinion 处理意见（必填）
     */
    public record HandleAppealRequest(String handleOpinion) {
    }
}
