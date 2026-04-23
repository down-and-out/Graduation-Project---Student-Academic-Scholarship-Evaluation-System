package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.entity.SysNotification;
import com.scholarship.security.LoginUser;
import com.scholarship.service.SysNotificationService;
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

/**
 * 系统通知控制器。
 */
@Slf4j
@RestController
@RequestMapping("/sys/notification")
@RequiredArgsConstructor
@Tag(name = "系统通知管理", description = "系统通知查询和管理接口")
public class SysNotificationController {

    private final SysNotificationService sysNotificationService;

    @GetMapping("/page")
    @Operation(summary = "分页查询通知", description = "查询当前用户可见的通知列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<SysNotification>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "是否已读：0-未读 1-已读") @RequestParam(required = false) Integer isRead,
            @Parameter(description = "通知类型：1-系统 2-申请 3-评审 4-结果") @RequestParam(required = false) Integer type,
            @AuthenticationPrincipal LoginUser loginUser) {
        IPage<SysNotification> page = sysNotificationService.pageNotifications(
                current, size, loginUser.getUserId(), loginUser.getUserType(), isRead, type);
        return Result.success(page);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读通知数", description = "获取当前用户的未读通知数量")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<Integer> getUnreadCount(@AuthenticationPrincipal LoginUser loginUser) {
        int count = sysNotificationService.getUnreadCount(loginUser.getUserId(), loginUser.getUserType());
        return Result.success(count);
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新通知", description = "获取当前用户的最新通知列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<SysNotification>> getLatest(
            @Parameter(description = "数量限制", example = "5") @RequestParam(defaultValue = "5") Integer limit,
            @AuthenticationPrincipal LoginUser loginUser) {
        List<SysNotification> notifications = sysNotificationService.getLatestNotifications(
                loginUser.getUserId(), loginUser.getUserType(), limit);
        return Result.success(notifications);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 查询通知", description = "只允许查看当前用户可见的通知")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "通知不存在")
    })
    public Result<SysNotification> getById(@PathVariable Long id,
                                           @AuthenticationPrincipal LoginUser loginUser) {
        SysNotification notification = sysNotificationService.getAccessibleNotification(
                id, loginUser.getUserId(), loginUser.getUserType());
        if (notification == null) {
            return Result.error("通知不存在或无权查看");
        }
        return Result.success(notification);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "发送通知", description = "发送系统通知，仅管理员可用")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "发送成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> send(@RequestBody SysNotification notification,
                             @AuthenticationPrincipal LoginUser loginUser) {
        notification.setSenderId(loginUser.getUserId());
        notification.setSenderName(loginUser.getUsername());
        boolean success = sysNotificationService.sendNotification(notification);
        return success ? Result.success("发送成功") : Result.error("发送失败");
    }

    @PutMapping("/read/{id}")
    @Operation(summary = "标记为已读", description = "将通知标记为已读")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功"),
            @ApiResponse(responseCode = "404", description = "通知不存在")
    })
    public Result<Void> markAsRead(@PathVariable Long id,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = sysNotificationService.markAsRead(id, loginUser.getUserId(), loginUser.getUserType());
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    @PutMapping("/read-batch")
    @Operation(summary = "批量标记为已读", description = "将多个通知标记为已读")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "操作成功")
    })
    public Result<Void> markBatchAsRead(@RequestBody List<Long> ids,
                                        @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = sysNotificationService.markBatchAsRead(
                ids, loginUser.getUserId(), loginUser.getUserType());
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除通知", description = "删除指定通知，仅管理员可用")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = sysNotificationService.deleteNotification(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
