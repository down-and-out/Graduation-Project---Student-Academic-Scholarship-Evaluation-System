package com.scholarship.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.scholarship.common.result.Result;
import com.scholarship.entity.CompetitionAward;
import com.scholarship.security.LoginUser;
import com.scholarship.service.CompetitionAwardService;
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
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/competition-award")
@RequiredArgsConstructor
@Tag(name = "06-学科竞赛获奖管理", description = "学科竞赛获奖的登记、查询、审核接口")
public class CompetitionAwardController {

    private final CompetitionAwardService competitionAwardService;

    @GetMapping("/page")
    @Operation(summary = "分页查询竞赛获奖", description = "支持按学生、审核状态和关键字筛选")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<CompetitionAward>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "审核状态", example = "1") @RequestParam(required = false) Integer auditStatus,
            @Parameter(description = "关键字", example = "竞赛") @RequestParam(required = false) String keyword) {
        Page<CompetitionAward> page = new Page<>(current, size);
        LambdaQueryWrapper<CompetitionAward> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(studentId != null, CompetitionAward::getStudentId, studentId)
                .eq(auditStatus != null, CompetitionAward::getAuditStatus, auditStatus)
                .and(keyword != null && !keyword.isBlank(), w -> w
                        .like(CompetitionAward::getCompetitionName, keyword)
                        .or().like(CompetitionAward::getInstructor, keyword)
                        .or().like(CompetitionAward::getIssuingUnit, keyword)
                        .or().like(CompetitionAward::getOrganizer, keyword))
                .orderByDesc(CompetitionAward::getCreateTime);
        return Result.success(competitionAwardService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取竞赛获奖详情")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "404", description = "记录不存在")
    })
    public Result<CompetitionAward> getById(@PathVariable Long id) {
        CompetitionAward award = competitionAwardService.getById(id);
        if (award == null) {
            return Result.error("记录不存在");
        }
        return Result.success(award);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "新增竞赛获奖", description = "登记学科竞赛获奖信息")
    public Result<Void> add(@Valid @RequestBody CompetitionAward award,
                            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = competitionAwardService.saveAward(award, loginUser.getUserId());
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "更新竞赛获奖", description = "更新学科竞赛获奖信息")
    public Result<Void> update(@Valid @RequestBody CompetitionAward award,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = competitionAwardService.updateAward(award, loginUser.getUserId());
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除竞赛获奖", description = "仅管理员可操作")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = competitionAwardService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
