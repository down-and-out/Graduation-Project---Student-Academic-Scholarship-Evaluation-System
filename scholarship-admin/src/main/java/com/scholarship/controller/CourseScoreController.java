package com.scholarship.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.dto.query.CourseScoreQuery;
import com.scholarship.entity.CourseScore;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.service.CourseScoreService;
import com.scholarship.service.StudentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程成绩控制器。
 */
@Slf4j
@RestController
@RequestMapping("/course-score")
@RequiredArgsConstructor
@Tag(name = "03-课程成绩管理", description = "课程成绩的查询和录入接口")
public class CourseScoreController {

    private static final int USER_TYPE_TUTOR = 2;
    private static final int USER_TYPE_ADMIN = 3;

    private final CourseScoreService courseScoreService;
    private final StudentInfoService studentInfoService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "分页查询成绩", description = "支持按学生、学年、学期筛选，导师只能查询本人指导学生")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<CourseScore>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID") @RequestParam(required = false) Long studentId,
            @Parameter(description = "学年") @RequestParam(required = false) String academicYear,
            @Parameter(description = "学期：1-第一学期，2-第二学期，3-夏季学期") @RequestParam(required = false) Integer semester,
            @AuthenticationPrincipal LoginUser loginUser) {

        CourseScoreQuery query = new CourseScoreQuery();
        query.setCurrent(current);
        query.setSize(size);
        query.setStudentId(studentId);
        query.setAcademicYear(academicYear);
        query.setSemester(semester);

        if (!applyTutorScope(query, loginUser)) {
            return Result.success(new Page<>(current, size, 0));
        }

        return Result.success(courseScoreService.queryPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "获取成绩详情")
    public Result<CourseScore> getById(@PathVariable Long id, @AuthenticationPrincipal LoginUser loginUser) {
        CourseScore score = courseScoreService.getById(id);
        if (score == null) {
            return Result.error("成绩不存在");
        }
        if (!canAccessStudent(loginUser, score.getStudentId())) {
            return Result.error(403, "无权访问该学生数据");
        }
        return Result.success(score);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "录入成绩", description = "录入学生课程成绩，并自动补齐学生学号和姓名快照")
    public Result<Void> add(@RequestBody CourseScore score, @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessStudent(loginUser, score.getStudentId())) {
            return Result.error(403, "无权操作该学生数据");
        }
        boolean success = courseScoreService.save(score);
        return success ? Result.success("录入成功") : Result.error("录入失败");
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "更新成绩", description = "修改课程成绩，并自动补齐学生学号和姓名快照")
    public Result<Void> update(@RequestBody CourseScore score, @AuthenticationPrincipal LoginUser loginUser) {
        CourseScore oldScore = courseScoreService.getById(score.getId());
        if (oldScore == null) {
            return Result.error("成绩不存在");
        }
        Long targetStudentId = score.getStudentId() != null ? score.getStudentId() : oldScore.getStudentId();
        if (!canAccessStudent(loginUser, oldScore.getStudentId()) || !canAccessStudent(loginUser, targetStudentId)) {
            return Result.error(403, "无权操作该学生数据");
        }
        if (score.getStudentId() == null) {
            score.setStudentId(oldScore.getStudentId());
        }
        boolean success = courseScoreService.updateById(score);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除成绩", description = "删除课程成绩记录")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = courseScoreService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/weighted-average/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "计算加权平均分", description = "计算指定学生的加权平均分")
    public Result<BigDecimal> getWeightedAverage(
            @PathVariable Long studentId,
            @Parameter(description = "批次 ID") @RequestParam(required = false) Long batchId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessStudent(loginUser, studentId)) {
            return Result.error(403, "无权访问该学生数据");
        }
        return Result.success(courseScoreService.calculateWeightedAverage(studentId, batchId));
    }

    @GetMapping("/total-credits/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "计算总学分", description = "计算指定学生的总学分")
    public Result<BigDecimal> getTotalCredits(
            @PathVariable Long studentId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessStudent(loginUser, studentId)) {
            return Result.error(403, "无权访问该学生数据");
        }
        return Result.success(courseScoreService.calculateTotalCredits(studentId));
    }

    @GetMapping("/average-gpa/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "计算平均绩点", description = "计算指定学生的平均绩点")
    public Result<BigDecimal> getAverageGPA(
            @PathVariable Long studentId,
            @AuthenticationPrincipal LoginUser loginUser) {
        if (!canAccessStudent(loginUser, studentId)) {
            return Result.error(403, "无权访问该学生数据");
        }
        return Result.success(courseScoreService.calculateAverageGPA(studentId));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "导出成绩", description = "导出成绩列表为 Excel 文件")
    public void export(
            @Parameter(description = "学生 ID") @RequestParam(required = false) Long studentId,
            @Parameter(description = "学年") @RequestParam(required = false) String academicYear,
            @AuthenticationPrincipal LoginUser loginUser,
            HttpServletResponse response) throws IOException {

        CourseScoreQuery query = new CourseScoreQuery();
        query.setStudentId(studentId);
        query.setAcademicYear(academicYear);

        if (!applyTutorScope(query, loginUser)) {
            query.setStudentIds(List.of(-1L));
        }

        List<CourseScore> list = courseScoreService.queryForExport(query);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = java.net.URLEncoder.encode("成绩列表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), CourseScore.class)
            .sheet("成绩列表")
            .doWrite(list);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "批量导入成绩", description = "从 Excel 文件批量导入成绩")
    public Result<Void> importScores(@RequestParam("file") MultipartFile file) {
        log.warn("成绩导入暂未实现，fileName={}", file.getOriginalFilename());
        return Result.error("成绩导入暂未实现，请先使用单条录入或补齐真实导入逻辑");
    }

    private boolean applyTutorScope(CourseScoreQuery query, LoginUser loginUser) {
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
