package com.scholarship.controller;

import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ApplicationAchievement;
import com.scholarship.entity.ScholarshipApplication;
import com.scholarship.entity.StudentInfo;
import com.scholarship.mapper.ScholarshipApplicationMapper;
import com.scholarship.mapper.StudentInfoMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ApplicationAchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * 申请成果关联控制器。
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/application-achievement")
@Tag(name = "申请成果关联", description = "奖学金申请与成果关联查询接口")
public class ApplicationAchievementController {

    private final ApplicationAchievementService applicationAchievementService;
    private final ScholarshipApplicationMapper applicationMapper;
    private final StudentInfoMapper studentInfoMapper;

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "按申请查询成果关联")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR', 'ROLE_STUDENT')")
    public Result<List<ApplicationAchievement>> listByApplicationId(
            @Parameter(description = "申请 ID")
            @PathVariable @NotNull Long applicationId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessApplication(applicationId, loginUser)) {
            return Result.error(403, "无权查看该申请成果");
        }
        return Result.success(applicationAchievementService.listByApplicationId(applicationId));
    }

    @PutMapping("/application/{applicationId}")
    @Operation(summary = "覆盖保存申请成果关联")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> replaceByApplicationId(
            @Parameter(description = "申请 ID")
            @PathVariable @NotNull Long applicationId,
            @RequestBody List<@Valid ApplicationAchievement> achievements) {
        log.warn("拒绝通过独立接口覆盖申请成果关联，applicationId={}, size={}",
                applicationId, achievements == null ? 0 : achievements.size());
        return Result.error("申请成果只能通过奖学金申请主流程提交，禁止独立覆盖保存");
    }

    @DeleteMapping("/application/{applicationId}")
    @Operation(summary = "删除申请下的全部成果关联")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Result<Void> removeByApplicationId(
            @Parameter(description = "申请 ID")
            @PathVariable @NotNull Long applicationId) {
        log.warn("拒绝通过独立接口删除申请成果关联，applicationId={}", applicationId);
        return Result.error("申请成果只能随申请主流程维护，禁止独立删除");
    }

    private boolean canAccessApplication(Long applicationId, LoginUser loginUser) {
        if (loginUser == null || applicationId == null) {
            return false;
        }
        if (UserTypeEnum.isAdmin(loginUser.getUserType())) {
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
        if (UserTypeEnum.isStudent(loginUser.getUserType())) {
            return loginUser.getUserId().equals(student.getUserId());
        }
        if (UserTypeEnum.isTutor(loginUser.getUserType())) {
            return loginUser.getUserId().equals(student.getTutorId());
        }
        return false;
    }
}
