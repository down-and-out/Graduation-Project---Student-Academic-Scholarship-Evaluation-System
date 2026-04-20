package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.entity.StudentInfo;
import com.scholarship.entity.SysUser;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.StudentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/student-info")
@RequiredArgsConstructor
@Tag(name = "02-研究生信息管理", description = "研究生学籍信息的查询、新增、修改、删除接口")
public class StudentInfoController {

    private final StudentInfoService studentInfoService;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/page")
    @Operation(summary = "分页查询研究生信息", description = "支持按关键字、院系、学籍状态筛选，院系和状态支持单个或多个值")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<StudentInfo>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "搜索关键字（姓名/学号）", example = "张三") @RequestParam(required = false) String keyword,
            @Parameter(description = "院系，支持单个或多个值", example = "计算机学院,软件学院") @RequestParam(required = false) List<String> department,
            @Parameter(description = "学籍状态，支持单个或多个值", example = "1,2") @RequestParam(required = false) List<String> status) {
        List<String> departments = parseStringParams(department);
        List<Integer> statuses = parseIntegerParams(status);
        IPage<StudentInfo> page = studentInfoService.pageStudents(current, size, keyword, departments, statuses);
        return Result.success(page);
    }

    @GetMapping("/my")
    @Operation(summary = "获取当前学生信息", description = "获取当前登录研究生的学籍信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "未找到学生信息")
    })
    public Result<StudentInfo> getMyInfo(@AuthenticationPrincipal LoginUser loginUser) {
        StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
        if (studentInfo == null) {
            return Result.error("未找到学生信息");
        }

        if (studentInfo.getTutorId() != null) {
            SysUser tutor = sysUserMapper.selectById(studentInfo.getTutorId());
            if (tutor != null) {
                studentInfo.setTutorName(tutor.getRealName());
            }
        }

        return Result.success(studentInfo);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据 ID 获取学生信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学生信息不存在")
    })
    public Result<StudentInfo> getById(@PathVariable Long id) {
        StudentInfo studentInfo = studentInfoService.getById(id);
        if (studentInfo == null) {
            return Result.error("学生信息不存在");
        }
        return Result.success(studentInfo);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "新增学生信息", description = "仅管理员可操作")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "新增成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@Valid @RequestBody StudentInfo studentInfo) {
        boolean success = studentInfoService.save(studentInfo);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新学生信息", description = "仅管理员可操作，同时同步更新用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> update(@Valid @RequestBody StudentInfo studentInfo) {
        boolean success = studentInfoService.updateStudentWithSync(studentInfo, true);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除学生信息", description = "仅管理员可操作，同时级联删除关联的用户账号")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> delete(@PathVariable Long id) {
        int result = studentInfoService.deleteWithCascade(id);

        switch (result) {
            case 0:
                return Result.success("删除成功");
            case 1:
                return Result.error("学生信息不存在");
            default:
                return Result.error("删除失败");
        }
    }

    @PutMapping("/my")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "学生更新自己的信息", description = "学生只能修改自己的电话、邮箱、研究方向、身份证、籍贯、家庭住址，同时同步到用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "403", description = "无权限"),
            @ApiResponse(responseCode = "404", description = "学生信息不存在")
    })
    public Result<Void> updateMyInfo(@RequestBody StudentInfo studentInfo,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        Long userId = loginUser.getUserId();
        boolean success = studentInfoService.updateByUserId(
                userId,
                studentInfo.getPhone(),
                studentInfo.getEmail(),
                studentInfo.getDirection(),
                studentInfo.getIdCard(),
                studentInfo.getNativePlace(),
                studentInfo.getAddress()
        );
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    private List<String> parseStringParams(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }

        Set<String> result = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            for (String value : rawValue.split(",")) {
                if (!value.isBlank()) {
                    result.add(value.trim());
                }
            }
        }
        return new ArrayList<>(result);
    }

    private List<Integer> parseIntegerParams(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }

        List<Integer> result = new ArrayList<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            for (String value : rawValue.split(",")) {
                if (value == null || value.isBlank()) {
                    continue;
                }
                result.add(Integer.parseInt(value.trim()));
            }
        }
        return result.stream().distinct().toList();
    }
}
