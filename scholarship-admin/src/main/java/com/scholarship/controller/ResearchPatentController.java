package com.scholarship.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.ResearchPatent;
import com.scholarship.exception.BusinessException;
import com.scholarship.security.LoginUser;
import com.scholarship.service.ResearchPatentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/research-patent")
@RequiredArgsConstructor
@Tag(name = "04-科研专利管理", description = "科研专利的申请、查询、审核接口")
public class ResearchPatentController {

    private final ResearchPatentService researchPatentService;

    @GetMapping("/page")
    @Operation(summary = "分页查询专利", description = "支持按学生、审核状态和关键字筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<ResearchPatent>> page(
            @Parameter(description = "当前页", example = "1")
            @Min(value = 1, message = "页码不能小于 1")
            @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10")
            @Min(value = 1, message = "每页大小不能小于 1")
            @Max(value = 100, message = "每页大小不能大于 100")
            @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "审核状态", example = "1") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "关键字", example = "专利") @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        Page<ResearchPatent> page = new Page<>(current, size);
        LambdaQueryWrapper<ResearchPatent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(studentId != null, ResearchPatent::getStudentId, studentId)
                .eq(auditStatus != null, ResearchPatent::getAuditStatus, auditStatus)
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(ResearchPatent::getPatentName, keyword)
                        .or().like(ResearchPatent::getPatentNo, keyword)
                        .or().like(ResearchPatent::getInventors, keyword)
                        .or().like(ResearchPatent::getApplicant, keyword))
                .orderByDesc(ResearchPatent::getCreateTime);

        if (loginUser != null && Integer.valueOf(1).equals(loginUser.getUserType())) {
            wrapper.eq(ResearchPatent::getStudentId, loginUser.getUserId());
        }

        return Result.success(researchPatentService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取专利详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "专利不存在")
    })
    public Result<ResearchPatent> getById(
            @Parameter(description = "专利ID")
            @PathVariable @Min(value = 1, message = "专利 ID 无效") Long id) {
        ResearchPatent patent = researchPatentService.getById(id);
        if (patent == null) {
            return Result.error("专利不存在");
        }
        return Result.success(patent);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "新增专利", description = "研究生提交专利成果")
    public Result<Void> add(@Valid @RequestBody ResearchPatent patent,
                            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchPatentService.savePatent(patent, loginUser);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "更新专利", description = "更新专利成果信息")
    public Result<Void> update(@Valid @RequestBody ResearchPatent patent,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = researchPatentService.updatePatent(patent, loginUser);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除专利", description = "仅管理员可操作")
    public Result<Void> delete(
            @Parameter(description = "专利ID")
            @PathVariable @Min(value = 1, message = "专利 ID 无效") Long id) {
        boolean success = researchPatentService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/audit/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "审核专利", description = "管理员对专利进行审核")
    public Result<Void> audit(
            @PathVariable @Min(value = 1, message = "专利 ID 无效") Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "审核参数")
            @RequestBody AuditRequest request) {
        try {
            boolean success = researchPatentService.audit(id, request.auditStatus(), request.auditComment());
            return success ? Result.success("审核成功") : Result.error("审核失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    public record AuditRequest(
            Integer auditStatus,
            String auditComment
    ) {}
}
