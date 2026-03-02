package com.scholarship.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.CourseScore;
import com.scholarship.service.CourseScoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * 课程成绩控制器
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/course-score")
@RequiredArgsConstructor
@Tag(name = "03-课程成绩管理", description = "课程成绩的查询和录入接口")
public class CourseScoreController {

    private final CourseScoreService courseScoreService;

    /**
     * 分页查询成绩
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "分页查询成绩", description = "支持按学生、学年、学期筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<CourseScore>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID") @RequestParam(required = false) Long studentId,
            @Parameter(description = "学年") @RequestParam(required = false) String academicYear,
            @Parameter(description = "学期：1-第一学期 2-第二学期 3-夏季学期") @RequestParam(required = false) Integer semester) {

        Page<CourseScore> page = new Page<>(current, size);
        LambdaQueryWrapper<CourseScore> wrapper = new LambdaQueryWrapper<>();

        if (studentId != null) {
            wrapper.eq(CourseScore::getStudentId, studentId);
        }
        if (academicYear != null) {
            wrapper.eq(CourseScore::getAcademicYear, academicYear);
        }
        if (semester != null) {
            wrapper.eq(CourseScore::getSemester, semester);
        }

        IPage<CourseScore> result = courseScoreService.page(page, wrapper);
        return Result.success(result);
    }

    /**
     * 获取成绩详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取成绩详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "成绩不存在")
    })
    public Result<CourseScore> getById(@PathVariable Long id) {
        CourseScore score = courseScoreService.getById(id);
        if (score == null) {
            return Result.error("成绩不存在");
        }
        return Result.success(score);
    }

    /**
     * 录入成绩
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "录入成绩", description = "录入学生课程成绩")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "录入成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@RequestBody CourseScore score) {
        boolean success = courseScoreService.save(score);
        return success ? Result.success("录入成功") : Result.error("录入失败");
    }

    /**
     * 更新成绩
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "更新成绩", description = "修改课程成绩")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> update(@RequestBody CourseScore score) {
        boolean success = courseScoreService.updateById(score);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除成绩
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除成绩", description = "删除课程成绩记录")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = courseScoreService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 计算加权平均分
     */
    @GetMapping("/weighted-average/{studentId}")
    @Operation(summary = "计算加权平均分", description = "计算指定学生的加权平均分")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "计算成功")
    })
    public Result<BigDecimal> getWeightedAverage(
            @PathVariable Long studentId,
            @Parameter(description = "批次 ID") @RequestParam(required = false) Long batchId) {
        BigDecimal average = courseScoreService.calculateWeightedAverage(studentId, batchId);
        return Result.success(average);
    }

    /**
     * 计算总学分
     */
    @GetMapping("/total-credits/{studentId}")
    @Operation(summary = "计算总学分", description = "计算指定学生的总学分")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "计算成功")
    })
    public Result<BigDecimal> getTotalCredits(@PathVariable Long studentId) {
        BigDecimal credits = courseScoreService.calculateTotalCredits(studentId);
        return Result.success(credits);
    }

    /**
     * 计算平均绩点
     */
    @GetMapping("/average-gpa/{studentId}")
    @Operation(summary = "计算平均绩点", description = "计算指定学生的平均绩点")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "计算成功")
    })
    public Result<BigDecimal> getAverageGPA(@PathVariable Long studentId) {
        BigDecimal gpa = courseScoreService.calculateAverageGPA(studentId);
        return Result.success(gpa);
    }

    /**
     * 导出成绩为 Excel
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TUTOR')")
    @Operation(summary = "导出成绩", description = "导出成绩列表为 Excel 文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导出成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public void export(
            @Parameter(description = "学生 ID") @RequestParam(required = false) Long studentId,
            @Parameter(description = "学年") @RequestParam(required = false) String academicYear,
            HttpServletResponse response) throws IOException {

        log.info("导出成绩列表");

        LambdaQueryWrapper<CourseScore> wrapper = new LambdaQueryWrapper<>();
        if (studentId != null) {
            wrapper.eq(CourseScore::getStudentId, studentId);
        }
        if (academicYear != null) {
            wrapper.eq(CourseScore::getAcademicYear, academicYear);
        }

        List<CourseScore> list = courseScoreService.list(wrapper);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = java.net.URLEncoder.encode("成绩列表", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        EasyExcel.write(response.getOutputStream(), CourseScore.class)
            .sheet("成绩列表")
            .doWrite(list);
    }

    /**
     * 批量导入成绩
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "批量导入成绩", description = "从 Excel 文件批量导入成绩")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导入成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> importScores(@RequestParam("file") MultipartFile file) {
        log.info("批量导入成绩，文件名：{}", file.getOriginalFilename());
        // TODO: 实现 Excel 导入逻辑
        return Result.success("导入成功（待实现）");
    }
}
