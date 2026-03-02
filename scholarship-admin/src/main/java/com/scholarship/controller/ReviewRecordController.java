package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.entity.ReviewRecord;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ReviewRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 评审记录控制器
 * <p>
 * 处理评审记录相关的请求，包括评审记录查询、添加等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/review-record")
@RequiredArgsConstructor
@Tag(name = "12-评审记录管理", description = "奖学金申请评审记录的查询和管理接口")
public class ReviewRecordController {

    private final ReviewRecordService reviewRecordService;

    /**
     * 根据申请 ID 查询评审记录
     *
     * @param applicationId 申请 ID
     * @return 评审记录列表
     */
    @GetMapping("/application/{applicationId}")
    @Operation(summary = "根据申请 ID 查询评审记录", description = "返回该申请的所有评审记录（按阶段排序）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<ReviewRecord>> listByApplicationId(@PathVariable Long applicationId) {
        List<ReviewRecord> records = reviewRecordService.listByApplicationId(applicationId);
        return Result.success(records);
    }

    /**
     * 根据评审人 ID 查询评审记录
     *
     * @param reviewerId 评审人 ID
     * @return 评审记录列表
     */
    @GetMapping("/reviewer/{reviewerId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "根据评审人 ID 查询评审记录", description = "返回该评审人的所有评审记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<List<ReviewRecord>> listByReviewerId(@PathVariable Long reviewerId) {
        List<ReviewRecord> records = reviewRecordService.listByReviewerId(reviewerId);
        return Result.success(records);
    }

    /**
     * 获取最新评审记录
     *
     * @param applicationId 申请 ID
     * @return 最新评审记录
     */
    @GetMapping("/latest/{applicationId}")
    @Operation(summary = "获取最新评审记录", description = "返回该申请的最新一条评审记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "未找到评审记录")
    })
    public Result<ReviewRecord> getLatestRecord(@PathVariable Long applicationId) {
        ReviewRecord record = reviewRecordService.getLatestRecord(applicationId);
        if (record == null) {
            return Result.error("未找到评审记录");
        }
        return Result.success(record);
    }

    /**
     * 添加评审记录
     *
     * @param applicationId 申请 ID
     * @param reviewStage 评审阶段（1-导师审核 2-院系审核 3-学校审核）
     * @param reviewResult 评审结果（1-通过 2-驳回 3-待定）
     * @param reviewScore 评审分数
     * @param reviewComment 评审意见
     * @param loginUser 当前登录用户
     * @return 是否成功
     */
    @PostMapping("/add/{applicationId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "添加评审记录", description = "为申请添加新的评审记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "添加成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
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

        log.info("添加评审记录，applicationId={}, stage={}, result={}", applicationId, reviewStage, reviewResult);

        boolean success = reviewRecordService.addReviewRecord(
            applicationId,
            reviewStage,
            loginUser.getUserId(),
            loginUser.getUsername(),
            reviewResult,
            reviewScore,
            reviewComment
        );

        return success ? Result.success("评审记录已添加") : Result.error("添加失败");
    }
}
