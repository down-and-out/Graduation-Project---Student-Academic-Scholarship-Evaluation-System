package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.service.BasicDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 基础数据控制器
 * <p>
 * 提供院系、专业等基础数据的查询接口
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/basic-data")
@RequiredArgsConstructor
@Tag(name = "基础数据管理", description = "获取院系、专业等基础数据")
public class BasicDataController {

    private final BasicDataService basicDataService;

    /**
     * 获取院系列表
     */
    @GetMapping("/departments")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取院系列表", description = "从学生信息中去重获取所有院系")
    public Result<List<String>> getDepartments() {
        log.debug("获取院系列表");
        return Result.success(basicDataService.getDepartments());
    }

    /**
     * 获取专业列表
     *
     * @param department 院系（可选，按院系筛选）
     */
    @GetMapping("/majors")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "获取专业列表", description = "从学生信息中去重获取所有专业，可按院系筛选")
    public Result<List<String>> getMajors(
            @Parameter(description = "院系") @RequestParam(required = false) String department) {
        log.debug("获取专业列表，department={}", department);
        return Result.success(basicDataService.getMajors(department));
    }
}
