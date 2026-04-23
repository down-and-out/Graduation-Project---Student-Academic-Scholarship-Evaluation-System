package com.scholarship.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.dto.query.MoralPerformanceQuery;
import com.scholarship.entity.MoralPerformance;
import com.scholarship.entity.StudentInfo;
import com.scholarship.enums.AuditStatusEnum;
import com.scholarship.security.LoginUser;
import com.scholarship.service.MoralPerformanceService;
import com.scholarship.service.StudentInfoService;
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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 德育表现控制器。
 */
@Slf4j
@RestController
@RequestMapping("/moral-performance")
@RequiredArgsConstructor
@Tag(name = "04-德育表现管理", description = "德育表现的查询、录入和审核接口")
public class MoralPerformanceController {

    private static final int USER_TYPE_TUTOR = 2;
    private static final int USER_TYPE_ADMIN = 3;

    private final MoralPerformanceService moralPerformanceService;
    private final StudentInfoService studentInfoService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "分页查询德育表现", description = "支持按学生、类型、学年筛选，导师只能查询本人指导学生")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<MoralPerformance>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID") @RequestParam(required = false) Long studentId,
            @Parameter(description = "表现类型") @RequestParam(required = false) Integer performanceType,
            @Parameter(description = "学年") @RequestParam(required = false) String academicYear,
            @Parameter(description = "审核状态：0-待审核，1-通过，2-驳回") @RequestParam(required = false) Integer auditStatus,
            @AuthenticationPrincipal LoginUser loginUser) {

        MoralPerformanceQuery query = new MoralPerformanceQuery();
        query.setCurrent(current);
        query.setSize(size);
        query.setStudentId(studentId);
        query.setPerformanceType(performanceType);
        query.setAcademicYear(academicYear);
        query.setAuditStatus(auditStatus);

        if (!applyTutorScope(query, loginUser)) {
            return Result.success(new Page<>(current, size, 0));
        }

        return Result.success(moralPerformanceService.queryPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "获取德育表现详情")
    public Result<MoralPerformance> getById(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        MoralPerformance performance = moralPerformanceService.getById(id);
        if (performance == null) {
            return Result.error("记录不存在");
        }
        if (!canAccessStudent(loginUser, performance.getStudentId())) {
            return Result.error(403, "无权访问该学生数据");
        }
        return Result.success(performance);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "录入德育表现", description = "录入学生德育表现记录，并自动补齐学生学号和姓名快照")
    public Result<Void> add(@RequestBody MoralPerformance performance, @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessStudent(loginUser, performance.getStudentId())) {
            return Result.error(403, "无权操作该学生数据");
        }
        performance.setAuditStatus(AuditStatusEnum.PENDING.getCode());
        boolean success = moralPerformanceService.save(performance);
        return success ? Result.success("录入成功") : Result.error("录入失败");
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "更新德育表现", description = "修改德育表现记录，并自动补齐学生学号和姓名快照")
    public Result<Void> update(@RequestBody MoralPerformance performance, @AuthenticationPrincipal LoginUser loginUser) {
        MoralPerformance oldPerformance = moralPerformanceService.getById(performance.getId());
        if (oldPerformance == null) {
            return Result.error("记录不存在");
        }
        Long targetStudentId = performance.getStudentId() != null
            ? performance.getStudentId()
            : oldPerformance.getStudentId();
        if (!canAccessStudent(loginUser, oldPerformance.getStudentId()) || !canAccessStudent(loginUser, targetStudentId)) {
            return Result.error(403, "无权操作该学生数据");
        }
        if (performance.getStudentId() == null) {
            performance.setStudentId(oldPerformance.getStudentId());
        }
        boolean success = moralPerformanceService.updateById(performance);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除德育表现", description = "删除德育表现记录")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = moralPerformanceService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/audit/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "审核德育表现", description = "导师只能审核本人指导学生的德育表现")
    public Result<Void> audit(
            @PathVariable Long id,
            @Parameter(description = "审核状态：1-通过，2-驳回") @RequestParam Integer auditStatus,
            @Parameter(description = "审核意见") @RequestParam(required = false) String auditComment,
            @AuthenticationPrincipal LoginUser loginUser) {

        MoralPerformance performance = moralPerformanceService.getById(id);
        if (performance == null) {
            return Result.error("记录不存在");
        }
        if (!canAccessStudent(loginUser, performance.getStudentId())) {
            return Result.error(403, "无权操作该学生数据");
        }

        log.info("审核德育表现，id={}, status={}", id, auditStatus);
        boolean success = moralPerformanceService.audit(id, auditStatus, auditComment, loginUser.getUserId());
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }

    @GetMapping("/total-score/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "获取德育总分", description = "计算指定学生的德育总分")
    public Result<BigDecimal> getTotalScore(
            @PathVariable Long studentId,
            @Parameter(description = "批次 ID") @RequestParam(required = false) Long batchId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessStudent(loginUser, studentId)) {
            return Result.error(403, "无权访问该学生数据");
        }
        return Result.success(moralPerformanceService.calculateTotalScore(studentId, batchId));
    }

    @GetMapping("/score-by-type/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "按类型统计分数", description = "按表现类型统计学生的德育分数")
    public Result<Map<String, BigDecimal>> getScoreByType(
            @PathVariable Long studentId,
            @Parameter(description = "批次 ID") @RequestParam(required = false) Long batchId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessStudent(loginUser, studentId)) {
            return Result.error(403, "无权访问该学生数据");
        }

        Map<String, BigDecimal> scoreMap = new HashMap<>();
        scoreMap.put("志愿服务", moralPerformanceService.calculateScoreByType(studentId, batchId, 1));
        scoreMap.put("社会实践", moralPerformanceService.calculateScoreByType(studentId, batchId, 2));
        scoreMap.put("荣誉称号", moralPerformanceService.calculateScoreByType(studentId, batchId, 3));
        scoreMap.put("学生干部", moralPerformanceService.calculateScoreByType(studentId, batchId, 4));
        scoreMap.put("其他", moralPerformanceService.calculateScoreByType(studentId, batchId, 5));

        return Result.success(scoreMap);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "获取学生德育表现列表", description = "获取指定学生的所有德育表现记录")
    public Result<List<MoralPerformance>> getByStudentId(
            @PathVariable Long studentId,
            @Parameter(description = "批次 ID") @RequestParam(required = false) Long batchId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessStudent(loginUser, studentId)) {
            return Result.error(403, "无权访问该学生数据");
        }
        return Result.success(moralPerformanceService.listByStudentId(studentId, batchId));
    }

    private boolean applyTutorScope(MoralPerformanceQuery query, LoginUser loginUser) {
        if (isAdmin(loginUser)) {
            return true;
        }
        if (!isTutor(loginUser)) {
            return false;
        }
        if (query.getStudentId() != null) {
            return canAccessStudent(loginUser, query.getStudentId());
        }
        List<Long> studentIds = ownStudentIds(loginUser.getUserId());
        if (studentIds.isEmpty()) {
            return false;
        }
        query.setStudentIds(studentIds);
        return true;
    }

    private List<Long> ownStudentIds(Long tutorUserId) {
        return studentInfoService.list(new LambdaQueryWrapper<StudentInfo>()
                .eq(StudentInfo::getTutorId, tutorUserId))
            .stream()
            .map(StudentInfo::getId)
            .toList();
    }

    private boolean canAccessStudent(LoginUser loginUser, Long studentId) {
        if (studentId == null || loginUser == null) {
            return false;
        }
        if (isAdmin(loginUser)) {
            return true;
        }
        if (!isTutor(loginUser)) {
            return false;
        }
        StudentInfo student = studentInfoService.getById(studentId);
        return student != null && loginUser.getUserId().equals(student.getTutorId());
    }

    private boolean isAdmin(LoginUser loginUser) {
        return loginUser != null && Integer.valueOf(USER_TYPE_ADMIN).equals(loginUser.getUserType());
    }

    private boolean isTutor(LoginUser loginUser) {
        return loginUser != null && Integer.valueOf(USER_TYPE_TUTOR).equals(loginUser.getUserType());
    }
}
