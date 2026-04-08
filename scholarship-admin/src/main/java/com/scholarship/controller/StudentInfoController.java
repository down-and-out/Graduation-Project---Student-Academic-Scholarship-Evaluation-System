package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.entity.StudentInfo;
import com.scholarship.security.LoginUser;
import com.scholarship.service.StudentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 研究生信息控制器
 * <p>
 * 处理研究生学籍信息相关的请求，包括信息查询、维护等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/student-info")
@RequiredArgsConstructor
@Tag(name = "02-研究生信息管理", description = "研究生学籍信息的查询、新增、修改、删除接口")
public class StudentInfoController {

    private final StudentInfoService studentInfoService;

    /**
     * 分页查询研究生信息
     *
     * @param current   当前页
     * @param size      每页大小
     * @param keyword   搜索关键字（姓名/学号）
     * @param department 院系
     * @param status    学籍状态（0-休学 1-在读 2-毕业 3-退学）
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询研究生信息", description = "支持按关键字、院系、学籍状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<StudentInfo>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "搜索关键字（姓名/学号）", example = "张三") @RequestParam(required = false) String keyword,
            @Parameter(description = "院系", example = "计算机学院") @RequestParam(required = false) String department,
            @Parameter(description = "学籍状态", example = "1") @RequestParam(required = false) Integer status) {
        IPage<StudentInfo> page = studentInfoService.pageStudents(current, size, keyword, department, status);
        return Result.success(page);
    }

    /**
     * 获取当前登录学生的信息
     *
     * @param loginUser 当前登录用户
     * @return 学生信息
     */
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
        return Result.success(studentInfo);
    }

    /**
     * 根据 ID 获取研究生信息
     *
     * @param id 学生 ID
     * @return 学生信息
     */
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

    /**
     * 新增研究生信息
     * <p>仅管理员可操作</p>
     *
     * @param studentInfo 学生信息
     * @return 是否成功
     */
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

    /**
     * 更新研究生信息
     * <p>仅管理员可操作，同时同步更新 sys_user 表</p>
     *
     * @param studentInfo 学生信息
     * @return 是否成功
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新学生信息", description = "仅管理员可操作，同时同步更新用户信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> update(@Valid @RequestBody StudentInfo studentInfo) {
        // 使用同步更新方法，保持两表数据一致
        boolean success = studentInfoService.updateStudentWithSync(studentInfo, true);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除研究生信息
     * <p>仅管理员可操作，同时级联删除关联的用户账号</p>
     *
     * @param id 学生 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除学生信息", description = "仅管理员可操作，同时级联删除关联的用户账号（原子操作）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> delete(@PathVariable Long id) {
        // 使用原子操作方法，事务保证两表同时删除成功或同时失败
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

    /**
     * 学生更新自己的信息
     * <p>仅允许修改：电话、邮箱、研究方向，同时同步更新 sys_user</p>
     *
     * @param studentInfo 学生信息（只需包含允许修改的字段）
     * @return 是否成功
     */
    @PutMapping("/my")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @Operation(summary = "学生更新自己的信息", description = "学生只能修改自己的电话、邮箱、研究方向，同时同步到用户信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "学生信息不存在")
    })
    public Result<Void> updateMyInfo(@RequestBody StudentInfo studentInfo,
                                     @AuthenticationPrincipal LoginUser loginUser) {
        // 获取当前登录用户ID
        Long userId = loginUser.getUserId();

        // 使用同步更新方法
        boolean success = studentInfoService.updateByUserId(userId, studentInfo.getPhone(), studentInfo.getEmail(), studentInfo.getDirection());

        return success ? Result.success("更新成功") : Result.error("更新失败");
    }
}
