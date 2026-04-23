package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.entity.ReviewRecord;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.ScholarshipApplicationMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ReviewRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评审记录控制器。
 */
@Slf4j
@RestController
@RequestMapping("/review-record")
@RequiredArgsConstructor
@Tag(name = "12-评审记录管理", description = "奖学金申请评审记录查询接口")
public class ReviewRecordController {

    private static final int USER_TYPE_STUDENT = 1;
    private static final int USER_TYPE_TUTOR = 2;
    private static final int USER_TYPE_ADMIN = 3;

    private final ReviewRecordService reviewRecordService;
    private final ScholarshipApplicationMapper applicationMapper;
    private final StudentInfoMapper studentInfoMapper;

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR', 'ROLE_STUDENT')")
    @Operation(summary = "根据申请 ID 查询评审记录", description = "学生只能看本人申请，导师只能看本人指导学生申请，管理员可看全部")
    public Result<List<ReviewRecord>> listByApplicationId(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessApplication(applicationId, loginUser)) {
            return Result.error(403, "无权查看该申请评审记录");
        }
        return Result.success(reviewRecordService.listByApplicationId(applicationId));
    }

    @GetMapping("/reviewer/{reviewerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "根据评审人 ID 查询评审记录", description = "导师只能查询自己的评审记录")
    public Result<List<ReviewRecord>> listByReviewerId(
            @PathVariable Long reviewerId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!isAdmin(loginUser) && !loginUser.getUserId().equals(reviewerId)) {
            return Result.error(403, "无权查看其他评审人的记录");
        }
        return Result.success(reviewRecordService.listByReviewerId(reviewerId));
    }

    @GetMapping("/latest/{applicationId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR', 'ROLE_STUDENT')")
    @Operation(summary = "获取最新评审记录")
    public Result<ReviewRecord> getLatestRecord(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessApplication(applicationId, loginUser)) {
            return Result.error(403, "无权查看该申请评审记录");
        }
        ReviewRecord record = reviewRecordService.getLatestRecord(applicationId);
        if (record == null) {
            return Result.error("未找到评审记录");
        }
        return Result.success(record);
    }

    @PostMapping("/add/{applicationId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "管理员补录评审记录", description = "正常评审记录应由申请审核主流程自动产生")
    public Result<Void> addReviewRecord(
            @PathVariable @NotNull(message = "申请 ID 不能为空") Long applicationId,
            @Parameter(description = "评审阶段", example = "1")
            @RequestParam @NotNull(message = "评审阶段不能为空") Integer reviewStage,
            @Parameter(description = "评审结果", example = "1")
            @RequestParam @NotNull(message = "评审结果不能为空") Integer reviewResult,
            @Parameter(description = "评审分数", example = "85.5")
            @RequestParam(required = false) BigDecimal reviewScore,
            @Parameter(description = "评审意见")
            @RequestParam(required = false) @Length(max = 500, message = "评审意见不能超过 500 字") String reviewComment,
            @AuthenticationPrincipal LoginUser loginUser) {

        log.info("管理员补录评审记录，applicationId={}, stage={}, result={}", applicationId, reviewStage, reviewResult);
        boolean success = reviewRecordService.addReviewRecord(
            applicationId,
            reviewStage,
            loginUser.getUserId(),
            loginUser.getRealName(),
            reviewResult,
            reviewScore,
            reviewComment
        );

        return success ? Result.success("评审记录已添加") : Result.error("添加失败");
    }

    private boolean canAccessApplication(Long applicationId, LoginUser loginUser) {
        if (loginUser == null || applicationId == null) {
            return false;
        }
        if (isAdmin(loginUser)) {
            return true;
        }
        ScholarshipApplication application = applicationMapper.selectById(applicationId);
        if (application == null) {
            return false;
        }
        StudentInfo student = studentInfoMapper.selectById(application.getStudentId());
        if (student == null) {
            return false;
        }
        if (Integer.valueOf(USER_TYPE_STUDENT).equals(loginUser.getUserType())) {
            return loginUser.getUserId().equals(student.getUserId());
        }
        if (Integer.valueOf(USER_TYPE_TUTOR).equals(loginUser.getUserType())) {
            return loginUser.getUserId().equals(student.getTutorId());
        }
        return false;
    }

    private boolean isAdmin(LoginUser loginUser) {
        return loginUser != null && Integer.valueOf(USER_TYPE_ADMIN).equals(loginUser.getUserType());
    }
}
