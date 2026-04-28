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
 * 结果异议控制器。
 */
@Slf4j
@RestController
@RequestMapping("/result-appeal")
@RequiredArgsConstructor
@Tag(name = "结果异议管理", description = "评定结果异议提交和处理接口")
public class ResultAppealController {

    private final ResultAppealService resultAppealService;
    private final StudentInfoMapper studentInfoMapper;

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
            if (studentId != null) {
                wrapper.eq(ResultAppeal::getStudentId, studentId);
            }
        } else {
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

    @PostMapping
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "提交异议", description = "学生对本人评定结果提交异议")
    public Result<Void> submit(@Valid @RequestBody ResultAppeal appeal,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = resultAppealService.submitAppeal(appeal, loginUser);
        return success ? Result.success("提交成功") : Result.error("提交失败");
    }

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

    public record HandleAppealRequest(String handleOpinion) {
    }
}
