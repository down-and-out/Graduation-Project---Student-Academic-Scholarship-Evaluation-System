package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.enums.UserTypeEnum;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.entity.StudentInfo;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchPatentService;
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

@Slf4j
@RestController
@RequestMapping("/research-patent")
@RequiredArgsConstructor
@Tag(name = "04-科研专利管理", description = "科研专利的申请、查询和审核接口")
public class ResearchPatentController {

    private final ResearchPatentService researchPatentService;
    private final StudentInfoService studentInfoService;

    @GetMapping("/page")
    @Operation(summary = "分页查询专利", description = "支持按学生、审核状态和关键词筛选，并按当前角色限制数据范围")
    public Result<IPage<ResearchPatent>> page(
            @Parameter(description = "当前页", example = "1")
            @Min(value = 1, message = "页码不能小于 1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10")
            @Min(value = 1, message = "每页大小不能小于 1")
            @Max(value = 100, message = "每页大小不能大于 100")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生档案 ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "审核状态：0-待审核，1-通过，2-驳回", example = "1") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "关键词", example = "专利") @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(researchPatentService.pagePatents(
                new Page<>(current, size), studentId, auditStatus, keyword, loginUser));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取专利详情")
    public Result<ResearchPatent> getById(
            @Parameter(description = "专利 ID") @PathVariable @Min(value = 1, message = "专利 ID 无效") Long id,
            @AuthenticationPrincipal LoginUser loginUser) {
        ResearchPatent patent = researchPatentService.getPatentById(id, loginUser);
        return patent == null ? Result.error("专利不存在") : Result.success(patent);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "新增专利", description = "学生新增时自动绑定自己的学生档案 ID")
    public Result<Void> add(@Valid @RequestBody ResearchPatent patent,
                            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchPatentService.savePatent(patent, loginUser);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "更新专利", description = "学生只能更新自己的专利成果")
    public Result<Void> update(@Valid @RequestBody ResearchPatent patent,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchPatentService.updatePatent(patent, loginUser);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除专利", description = "仅管理员可操作")
    public Result<Void> delete(@PathVariable @Min(value = 1, message = "专利 ID 无效") Long id) {
        boolean success = researchPatentService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/count")
    @Operation(summary = "统计当前学生审核通过的专利数量")
    public Result<Long> count(@AuthenticationPrincipal LoginUser loginUser) {
        log.debug("统计专利数量, userId={}", loginUser.getUserId());
        StudentInfo studentInfo = studentInfoService.getByUserId(loginUser.getUserId());
        if (studentInfo == null) {
            log.debug("统计专利数量失败：学生信息不存在, userId={}", loginUser.getUserId());
            return Result.success(0L);
        }
        long count = researchPatentService.countByStudentId(studentInfo.getId());
        log.debug("统计专利数量结果, userId={}, studentId={}, count={}", loginUser.getUserId(), studentInfo.getId(), count);
        return Result.success(count);
    }

    @PutMapping("/audit/{id}")
    @PreAuthorize("hasAnyRole('ROLE_TUTOR', 'ROLE_ADMIN')")
    @Operation(summary = "审核专利", description = "导师或管理员对专利进行审核")
    public Result<Void> audit(
            @PathVariable @Min(value = 1, message = "专利 ID 无效") Long id,
            @RequestBody AuditRequest request,
            @AuthenticationPrincipal LoginUser loginUser) {
        try {
            boolean success = researchPatentService.audit(
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

    public record AuditRequest(Integer auditStatus, String auditComment) {
    }
}
