package com.scholarship.controller;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 学科竞赛获奖控制器
 * <p>
 * 处理学科竞赛获奖相关的请求，包括获奖登记、审核等
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/competition-award")
@RequiredArgsConstructor
@Tag(name = "06-学科竞赛获奖管理", description = "学科竞赛获奖的登记、查询、审核接口")
public class CompetitionAwardController {

    private final CompetitionAwardService competitionAwardService;

    /**
     * 分页查询竞赛获奖
     *
     * @param current       当前页
     * @param size          每页大小
     * @param studentId     学生 ID
     * @param auditStatus   审核状态
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询竞赛获奖", description = "支持按学生 ID、审核状态筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<IPage<CompetitionAward>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "学生 ID", example = "1") @RequestParam(required = false) Long studentId,
            @Parameter(description = "审核状态", example = "1") @RequestParam(required = false) Integer auditStatus) {
        Page<CompetitionAward> page = new Page<>(current, size);
        IPage<CompetitionAward> result = competitionAwardService.page(page);
        return Result.success(result);
    }

    /**
     * 获取竞赛获奖详情
     *
     * @param id 获奖 ID
     * @return 竞赛获奖详情
     */
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

    /**
     * 新增竞赛获奖
     *
     * @param award 获奖信息
     * @return 是否成功
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "新增竞赛获奖", description = "登记学科竞赛获奖信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "新增成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@Valid @RequestBody CompetitionAward award,
                            @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = competitionAwardService.saveAward(award, loginUser.getUserId());
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新竞赛获奖
     *
     * @param award 获奖信息
     * @return 是否成功
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_STUDENT', 'ROLE_ADMIN')")
    @Operation(summary = "更新竞赛获奖", description = "更新学科竞赛获奖信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "记录不存在"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> update(@Valid @RequestBody CompetitionAward award,
                               @AuthenticationPrincipal LoginUser loginUser) {
        boolean success = competitionAwardService.updateAward(award, loginUser.getUserId());
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除竞赛获奖
     *
     * @param id 获奖 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除竞赛获奖", description = "仅管理员可操作")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "记录不存在")
    })
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = competitionAwardService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
