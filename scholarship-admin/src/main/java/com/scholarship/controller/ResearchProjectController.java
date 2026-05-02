package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ResearchProject;
import com.scholarship.entity.StudentInfo;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchProjectService;
import com.scholarship.service.StudentInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

/**
 * 科研项目管理控制器
 * 功能：项目登记、查询、审核、删除
 *
 * 业务流程：
 * 1. 学生/管理员新增项目 -> 状态变为待审核
 * 2. 导师/管理员审核项目 -> 通过/驳回
 * 3. 审核通过的项目可关联到奖学金申请
 *
 * 权限说明：
 * - 新增/更新：学生和管理员均可
 * - 审核：仅导师和管理员
 * - 删除：仅管理员
 */
@Slf4j
@RestController
@RequestMapping("/research-project")
@RequiredArgsConstructor
@Tag(name = "05-科研项目管理", description = "科研项目的登记、查询和审核接口")
public class ResearchProjectController {

    private final ResearchProjectService researchProjectService;
    private final StudentInfoService studentInfoService;

    /**
     * 分页查询项目
     * 根据角色自动过滤数据范围，学生仅能查看自己的项目
     *
     * @param current      当前页码
     * @param size         每页条数
     * @param studentId    筛选指定学生的项目（管理员/导师用）
     * @param auditStatus  审核状态：0-待审核，1-通过，2-驳回
     * @param keyword      关键词筛选
     * @param loginUser    当前登录用户
     * @return 分页后的项目列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询项目", description = "支持按学生、审核状态和关键词筛选，并按当前角色限制数据范围")
    public Result<IPage<ResearchProject>> page(
            @Parameter(description = "当前页", example = "1")
            @Min(value = 1, message = "页码不能小于 1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10")
            @Min(value = 1, message = "每页大小不能小于 1")
            @Max(value = 100, message = "每页大小不能大于 100")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生档案 ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "审核状态：0-待审核，1-通过，2-驳回", example = "1") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "关键词", example = "项目") @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(researchProjectService.pageProjects(
                new Page<>(current, size), studentId, auditStatus, keyword, loginUser));
    }

    /**
     * 获取项目详情
     *
     * @param id        项目ID
     * @param loginUser 当前登录用户
     * @return 项目详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取项目详情")
    public Result<ResearchProject> getById(
            @Parameter(description = "项目 ID") @PathVariable @Min(value = 1, message = "项目 ID 无效") Long id,
            @AuthenticationPrincipal LoginUser loginUser) {
        ResearchProject project = researchProjectService.getProjectById(id, loginUser);
        return project == null ? Result.error("项目不存在") : Result.success(project);
    }

    /**
     * 新增项目
     * 学生新增时自动绑定自己的学生档案ID
     *
     * @param project   项目信息
     * @param loginUser 当前登录用户
     * @return 操作结果
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "新增项目", description = "学生新增时自动绑定自己的学生档案 ID")
    public Result<Void> add(@Valid @RequestBody ResearchProject project,
                            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchProjectService.saveProject(project, loginUser);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新项目
     * 学生只能更新自己的项目成果
     *
     * @param project   更新后的项目信息
     * @param loginUser 当前登录用户
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "更新项目", description = "学生只能更新自己的项目成果")
    public Result<Void> update(@Valid @RequestBody ResearchProject project,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchProjectService.updateProject(project, loginUser);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 审核项目
     * 导师或管理员审核项目，通过后可用于奖学金申请关联
     *
     * @param id        项目ID
     * @param request   审核参数（状态、审核意见）
     * @param loginUser 当前登录用户
     * @return 操作结果
     */
    @PutMapping("/audit/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TUTOR', 'ROLE_ADMIN')")
    @Operation(summary = "审核项目", description = "导师或管理员审核项目成果")
    public Result<Void> audit(
            @PathVariable @Min(value = 1, message = "项目 ID 无效") Long id,
            @RequestBody AuditRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        try {
            boolean success = researchProjectService.audit(
                    id,
                    request.auditStatus(),
                    request.auditComment(),
                    loginUser.getUserId(),
                    UserTypeEnum.isAdmin(loginUser.getUserType())
            );
            return success ? Result.success("审核成功") : Result.error("审核失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除项目（仅管理员）
     *
     * @param id 项目ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除项目", description = "仅管理员可操作")
    public Result<Void> delete(@PathVariable @Min(value = 1, message = "项目 ID 无效") Long id) {
        boolean success = researchProjectService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 统计当前学生审核通过的项目数量
     *
     * @param loginUser 当前登录学生
     * @return 通过审核的项目数量
     */
    @GetMapping("/count")
    @Operation(summary = "统计当前学生审核通过的项目数量")
    public Result<Long> count(@AuthenticationPrincipal LoginUser loginUser) {
        log.debug("统计项目数量, userId={}", loginUser.getUserId());
        StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
        if (studentInfo == null) {
            log.debug("统计项目数量失败：学生信息不存在, userId={}", loginUser.getUserId());
            return Result.success(0L);
        }
        long count = researchProjectService.countOwnedByStudentId(studentInfo.getId());
        log.debug("统计项目数量结果, userId={}, studentId={}, count={}", loginUser.getUserId(), studentInfo.getId(), count);
        return Result.success(count);
    }

    /**
     * 审核请求体
     * @param auditStatus   审核状态（1=通过，2=驳回）
     * @param auditComment  审核意见
     */
    public record AuditRequest(Integer auditStatus, String auditComment) {
    }
}
