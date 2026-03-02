package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ResearchProject;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 科研项目控制器
 * <p>
 * 处理科研项目相关的请求，包括项目登记、审核等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/research-project")
@RequiredArgsConstructor
@Tag(name = "05-科研项目管理", description = "科研项目的登记、查询、审核接口")
public class ResearchProjectController {

    private final ResearchProjectService researchProjectService;

    /**
     * 分页查询项目
     *
     * @param current       当前页
     * @param size          每页大小
     * @param studentId     学生 ID
     * @param auditStatus   审核状态
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询项目", description = "支持按学生 ID、审核状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<ResearchProject>> page(
            @Parameter(description = "当前页", example = "1")
            @Min(value = 1, message = "页码不能小于 1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10")
            @Min(value = 1, message = "每页大小不能小于 1")
            @Max(value = 100, message = "每页大小不能大于 100")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "审核状态", example = "1") @RequestParam(required = false) Integer auditStatus) {
        Page<ResearchProject> page = new Page<>(current, size);
        IPage<ResearchProject> result = researchProjectService.page(page);
        return Result.success(result);
    }

    /**
     * 获取项目详情
     *
     * @param id 项目 ID
     * @return 项目详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取项目详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "项目不存在")
    })
    public Result<ResearchProject> getById(
            @Parameter(description = "项目 ID")
            @PathVariable @Min(value = 1, message = "项目 ID 无效") Long id) {
        ResearchProject project = researchProjectService.getById(id);
        if (project == null) {
            return Result.error("项目不存在");
        }
        return Result.success(project);
    }

    /**
     * 新增项目
     *
     * @param project 项目信息
     * @return 是否成功
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "新增项目", description = "登记科研项目信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "新增成功"),
        @ApiResponse(responseCode = "400", description = "参数错误")
    })
    public Result<Void> add(@Valid @RequestBody ResearchProject project,
                            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchProjectService.saveProject(project, loginUser.getUserId());
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新项目
     *
     * @param project 项目信息
     * @return 是否成功
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "更新项目", description = "更新科研项目信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "项目不存在"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> update(@Valid @RequestBody ResearchProject project,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchProjectService.updateProject(project, loginUser.getUserId());
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除项目
     *
     * @param id 项目 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除项目", description = "仅管理员可操作")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "项目不存在")
    })
    public Result<Void> delete(
            @Parameter(description = "项目 ID")
            @PathVariable @Min(value = 1, message = "项目 ID 无效") Long id) {
        boolean success = researchProjectService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
