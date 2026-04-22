package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.entity.ApplicationAchievement;
import com.scholarship.service.ApplicationAchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 申请成果关联控制器
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/application-achievement")
@Tag(name = "申请成果关联", description = "奖学金申请与成果关联管理接口")
public class ApplicationAchievementController {

    private final ApplicationAchievementService applicationAchievementService;

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "按申请查询成果关联")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    public Result<List<ApplicationAchievement>> listByApplicationId(
            @Parameter(description = "申请 ID")
            @PathVariable @NotNull Long applicationId) {
        return Result.success(applicationAchievementService.listByApplicationId(applicationId));
    }

    @PutMapping("/application/{applicationId}")
    @Operation(summary = "覆盖保存申请成果关联")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    public Result<Void> replaceByApplicationId(
            @Parameter(description = "申请 ID")
            @PathVariable @NotNull Long applicationId,
            @RequestBody List<@Valid ApplicationAchievement> achievements) {
        log.info("覆盖保存申请成果关联，applicationId={}, size={}",
                applicationId, achievements == null ? 0 : achievements.size());
        boolean success = applicationAchievementService.replaceByApplicationId(applicationId, achievements);
        return success ? Result.success("保存成功") : Result.error("保存失败");
    }

    @DeleteMapping("/application/{applicationId}")
    @Operation(summary = "删除申请下的全部成果关联")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    public Result<Void> removeByApplicationId(
            @Parameter(description = "申请 ID")
            @PathVariable @NotNull Long applicationId) {
        log.info("删除申请成果关联，applicationId={}", applicationId);
        boolean success = applicationAchievementService.removeByApplicationId(applicationId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
