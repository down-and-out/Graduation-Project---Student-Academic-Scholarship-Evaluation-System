package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.common.util.ParamParserUtil;
import com.scholarship.entity.StudentInfo;
import com.scholarship.entity.SysUser;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.security.LoginUser;
import com.scholarship.service.StudentInfoService;
import com.scholarship.vo.TutorStudentVO;
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

import java.util.List;

/**
 * 研究生信息管理控制器
 *
 * 功能说明：
 * - 分页查询研究生信息（支持关键字、院系、入学年份、学籍状态筛选）
 * - 导师分页查询指导学生（含成果统计）
 * - 获取当前学生信息
 * - 根据 ID 获取学生信息
 * - 新增学生信息（仅管理员）
 * - 更新学生信息（仅管理员，同时同步到用户表）
 * - 删除学生信息（仅管理员，级联删除用户账号）
 * - 学生更新自己的信息（仅学生，只能修改部分字段）
 *
 * 权限说明：
 * - 分页查询、新增、更新、删除仅管理员可访问
 * - 导师查询指导学生仅导师可访问
 * - 学生查询自己的信息和更新自己的信息仅学生可访问
 */
@Slf4j
@RestController
@RequestMapping("/student-info")
@RequiredArgsConstructor
@Tag(name = "02-研究生信息管理", description = "研究生学籍信息的查询、新增、修改、删除接口")
public class StudentInfoController {

    private final StudentInfoService studentInfoService;
    private final SysUserMapper sysUserMapper;

    /**
     * 分页查询研究生信息
     * 支持按关键字（学号/姓名）、院系、入学年份、学籍状态筛选
     *
     * @param current        当前页码
     * @param size           每页条数
     * @param keyword        搜索关键字（匹配学号或姓名）
     * @param department     院系列表（支持单个或多个值）
     * @param enrollmentYear 入学年份（按学号前4位筛选）
     * @param status         学籍状态列表
     * @return 分页后的研究生信息列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "分页查询研究生信息", description = "支持按关键字、院系、入学年份、学籍状态筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<StudentInfo>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "搜索关键字（姓名/学号）", example = "张三") @RequestParam(required = false) String keyword,
            @Parameter(description = "院系，支持单个或多个值", example = "计算机学院,软件学院") @RequestParam(required = false) List<String> department,
            @Parameter(description = "入学年份，按学号前4位筛选", example = "2024") @RequestParam(required = false) String enrollmentYear,
            @Parameter(description = "学籍状态，支持单个或多个值", example = "1,2") @RequestParam(required = false) List<String> status) {
        List<String> departments = ParamParserUtil.parseStringParams(department);
        List<Integer> statuses = ParamParserUtil.parseIntegerParams(status);
        IPage<StudentInfo> page = studentInfoService.pageStudents(current, size, keyword, departments, enrollmentYear, statuses);
        return Result.success(page);
    }

    /**
     * 导师分页查询指导学生
     * 仅返回当前导师名下学生，并附带论文/专利/项目成果统计
     *
     * @param current    当前页码
     * @param size       每页条数
     * @param keyword    搜索关键字（匹配学号或姓名）
     * @param grade      年级（筛选同一年级学生）
     * @param loginUser  当前登录导师
     * @return 分页后的指导学生列表（含成果统计）
     */
    @GetMapping("/tutor/page")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "导师分页查询指导学生", description = "仅返回当前导师名下学生，并附带成果统计")
    public Result<IPage<TutorStudentVO>> pageTutorStudents(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade,
            @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(studentInfoService.pageTutorStudents(
                loginUser.getUserId(),
                current,
                size,
                keyword,
                grade
        ));
    }

    /**
     * 获取导师名下学生年级列表
     * 返回当前导师名下学生的去重年级列表，按年份倒序排列
     *
     * @param loginUser 当前登录导师
     * @return 去重后的年级列表
     */
    @GetMapping("/tutor/grades")
    @PreAuthorize("hasRole('ROLE_TUTOR')")
    @Operation(summary = "获取导师名下学生年级列表", description = "返回当前导师名下学生的去重年级列表，按年份倒序")
    public Result<List<String>> getTutorGrades(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(studentInfoService.listTutorGrades(loginUser.getUserId()));
    }

    /**
     * 获取当前登录学生的学籍信息
     * 如果有导师，同时返回导师姓名
     *
     * @param loginUser 当前登录学生
     * @return 学生学籍信息
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
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

        // 如果有导师，查询导师姓名
        if (studentInfo.getTutorId() != null) {
            SysUser tutor = sysUserMapper.selectById(studentInfo.getTutorId());
            if (tutor != null) {
                studentInfo.setTutorName(tutor.getRealName());
            }
        }

        return Result.success(studentInfo);
    }

    /**
     * 根据 ID 获取学生信息（带权限校验）
     * admin/student/tutor 均可访问，但数据域受角色限制
     *
     * @param id        学生ID
     * @param loginUser 当前登录用户
     * @return 学生信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_STUDENT','ROLE_TUTOR','ROLE_ADMIN')")
    @Operation(summary = "根据 ID 获取学生信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "学生信息不存在")
    })
    public Result<StudentInfo> getById(@PathVariable Long id,
                                       @AuthenticationPrincipal LoginUser loginUser) {
        StudentInfo studentInfo = studentInfoService.getStudentDetailById(id, loginUser);
        if (studentInfo == null) {
            return Result.error("学生信息不存在");
        }
        return Result.success(studentInfo);
    }

    /**
     * 新增学生信息（仅管理员）
     *
     * @param studentInfo 学生信息
     * @return 操作结果
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
     * 更新学生信息（仅管理员）
     * 更新学生信息，同时同步到用户表（姓名、院系、电话、邮箱）
     *
     * @param studentInfo 学生信息（含ID）
     * @return 操作结果
     */
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

    /**
     * 删除学生信息（仅管理员）
     * 删除学生信息，同时级联删除关联的用户账号
     *
     * @param id 学生ID
     * @return 操作结果
     */
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

    /**
     * 学生更新自己的信息（仅学生）
     * 学生只能修改电话、邮箱、研究方向、身份证、籍贯、家庭住址
     * 同时同步到用户信息表
     *
     * @param studentInfo 只能包含可修改的字段
     * @param loginUser   当前登录学生
     * @return 操作结果
     */
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
}
