package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.result.Result;
import com.scholarship.common.util.ParamParserUtil;
import com.scholarship.dto.ResetPasswordRequest;
import com.scholarship.dto.StudentCreateFields;
import com.scholarship.dto.UserCreateRequest;
import com.scholarship.entity.StudentInfo;
import com.scholarship.entity.SysUser;
import com.scholarship.service.StudentInfoService;
import com.scholarship.service.SysUserService;
import com.scholarship.vo.SysUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 系统用户管理控制器
 *
 * 功能说明：
 * - 分页查询用户列表（支持关键词、角色、院系、状态筛选）
 * - 根据 ID 查询用户详情（含学生信息）
 * - 新增用户（同时自动创建学生档案）
 * - 更新用户信息（同时同步到学生档案）
 * - 删除用户（级联删除学生档案）
 * - 批量删除用户
 * - 重置用户密码
 *
 * 权限说明：所有接口仅管理员可访问（需要 ROLE_ADMIN 角色）
 */
@Slf4j
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
@Tag(name = "01-系统用户管理", description = "系统用户的增删改查接口（仅管理员）")
public class SysUserController {

    private final SysUserService sysUserService;
    private final StudentInfoService studentInfoService;

    /**
     * 分页查询用户列表
     *
     * @param current    当前页码（从1开始）
     * @param size       每页条数
     * @param keyword    搜索关键字（匹配用户名或姓名）
     * @param department  院系列表（支持单个或多个值筛选）
     * @param userType   用户类型列表（1=研究生，2=导师，3=管理员）
     * @param status     状态列表（0=禁用，1=正常）
     * @return 分页后的用户列表（包含学生信息）
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "分页查询用户", description = "支持按用户名、姓名、院系、单个或多个用户类型、单个或多个状态筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<SysUserVO>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "院系/部门，支持单个值、逗号分隔多值或重复参数")
            @RequestParam(required = false) List<String> department,
            @Parameter(description = "用户类型：支持单个值、逗号分隔多值或重复参数，1-研究生 2-导师 3-管理员")
            @RequestParam(required = false) List<String> userType,
            @Parameter(description = "状态：支持单个值、逗号分隔多值或重复参数，0-禁用 1-正常")
            @RequestParam(required = false) List<String> status) {
        return Result.success(sysUserService.pageUserVOs(
                current,
                size,
                keyword,
                normalizeStringFilterValues(department),
                normalizeIntegerFilterValues(userType),
                normalizeIntegerFilterValues(status)
        ));
    }

    /**
     * 根据用户 ID 查询用户详情
     * 如果用户类型为学生，同时返回关联的学生档案信息
     *
     * @param id 用户ID
     * @return 用户详情（含学生信息）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "根据 ID 查询用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<SysUserVO> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        StudentInfo studentInfo = null;
        // 如果是学生类型，同时查询学生档案
        if (UserTypeEnum.isStudent(user.getUserType())) {
            studentInfo = studentInfoService.getByUserId(user.getId());
        }
        return Result.success(SysUserVO.fromEntity(user, studentInfo));
    }

    /**
     * 新增用户
     * 创建新用户，如果用户类型为学生，同时自动创建学生档案
     *
     * @param request 用户创建请求（包含用户基本信息 + 学生专属字段）
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "新增用户", description = "创建新的系统用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "新增成功"),
            @ApiResponse(responseCode = "400", description = "用户名已存在"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@Valid @RequestBody UserCreateRequest request) {
        StudentCreateFields studentFields = new StudentCreateFields(request);

        boolean success = sysUserService.createUser(
                request.getUser(),
                request.getMajor(),
                studentFields
        );
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新用户信息
     * 修改用户基本信息，同时同步到学生档案（姓名、院系、电话、邮箱）
     *
     * @param user 用户信息（含ID）
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新用户", description = "修改用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "400", description = "用户名已存在"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> update(@Valid @RequestBody SysUser user) {
        boolean success = sysUserService.updateUser(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除用户
     * 删除指定用户，同时级联删除关联的学生档案
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除用户", description = "删除指定的系统用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = sysUserService.deleteUser(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除用户
     * 批量删除指定用户，同时级联删除关联的学生档案
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "批量删除用户", description = "批量删除系统用户")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> deleteBatch(@RequestBody List<Long> ids) {
        boolean success = sysUserService.deleteUsers(ids);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 重置用户密码
     * 将用户密码恢复为系统默认密码（或指定新密码）
     *
     * @param id       用户ID
     * @param request   重置密码请求（可选的新密码）
     * @return 操作结果
     */
    @PutMapping("/reset-password/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "重置密码", description = "重置用户密码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "重置成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request) {
        boolean success = sysUserService.resetPassword(id, request.getPassword());
        return success ? Result.success("重置成功") : Result.error("重置失败");
    }

    /**
     * 将字符串类型的筛选参数解析为 Integer 列表
     * 用于解析 userType、status 等枚举值参数
     */
    private List<Integer> normalizeIntegerFilterValues(List<String> values) {
        List<Integer> parsed = ParamParserUtil.parseIntegerParams(values);
        return parsed.isEmpty() ? null : parsed;
    }

    /**
     * 将字符串类型的筛选参数解析为 String 列表
     * 用于解析 department 等文本参数
     */
    private List<String> normalizeStringFilterValues(List<String> values) {
        List<String> parsed = ParamParserUtil.parseStringParams(values);
        return parsed.isEmpty() ? null : parsed;
    }
}
