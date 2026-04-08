package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.dto.query.ScoreRuleQuery;
import com.scholarship.entity.ScoreRule;
import com.scholarship.service.ScoreRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评分规则控制器
 * <p>
 * 处理评分规则相关的请求（仅管理员可操作）
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/score-rule")
@RequiredArgsConstructor
@Tag(name = "08-评分规则管理", description = "奖学金评分规则的配置接口（仅管理员）")
public class ScoreRuleController {

    private final ScoreRuleService scoreRuleService;

    /**
     * 分页查询评分规则
     *
     * @param current  当前页
     * @param size     每页大小
     * @param ruleType 规则类型
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询评分规则", description = "支持按规则类型筛选")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<ScoreRule>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "规则类型", example = "1") @RequestParam(required = false) Integer ruleType) {

        ScoreRuleQuery query = new ScoreRuleQuery();
        query.setCurrent(current);
        query.setSize(size);
        query.setRuleType(ruleType);

        IPage<ScoreRule> result = scoreRuleService.queryPage(query);
        return Result.success(result);
    }

    /**
     * 获取评分规则详情
     *
     * @param id 规则 ID
     * @return 规则详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取评分规则详情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "规则不存在")
    })
    public Result<ScoreRule> getById(@PathVariable Long id) {
        ScoreRule rule = scoreRuleService.getById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        return Result.success(rule);
    }

    /**
     * 新增评分规则
     *
     * @param rule 规则信息
     * @return 是否成功
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "新增评分规则", description = "添加新的评分规则配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "新增成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@Valid @RequestBody ScoreRule rule) {
        boolean success = scoreRuleService.save(rule);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新评分规则
     *
     * @param rule 规则信息
     * @return 是否成功
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新评分规则", description = "修改评分规则配置")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "规则不存在")
    })
    public Result<Void> update(@Valid @RequestBody ScoreRule rule) {
        boolean success = scoreRuleService.updateById(rule);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除评分规则
     *
     * @param id 规则 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除评分规则", description = "仅管理员可操作")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "404", description = "规则不存在")
    })
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = scoreRuleService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 根据规则类型查询规则列表
     *
     * @param ruleType 规则类型（1-论文 2-专利 3-项目 4-竞赛 5-课程成绩 6-德育表现）
     * @return 规则列表
     */
    @GetMapping("/list")
    @Operation(summary = "根据规则类型查询规则列表", description = "返回该类型下所有规则（按排序号排序）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<ScoreRule>> listByType(
            @Parameter(description = "规则类型", required = true)
            @RequestParam Integer ruleType) {
        List<ScoreRule> rules = scoreRuleService.listByRuleType(ruleType);
        return Result.success(rules);
    }

    /**
     * 查询可用规则列表
     *
     * @param ruleType 规则类型
     * @return 可用规则列表
     */
    @GetMapping("/available")
    @Operation(summary = "查询可用规则列表", description = "仅返回已启用的规则")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<ScoreRule>> listAvailable(
            @Parameter(description = "规则类型", required = true)
            @RequestParam Integer ruleType) {
        List<ScoreRule> rules = scoreRuleService.listAvailableByRuleType(ruleType);
        return Result.success(rules);
    }

    /**
     * 切换规则可用状态
     *
     * @param id 规则 ID
     * @return 是否成功
     */
    @PutMapping("/toggle/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "切换规则可用状态", description = "启用/禁用评分规则")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "404", description = "规则不存在")
    })
    public Result<Void> toggleAvailability(@PathVariable Long id) {
        boolean success = scoreRuleService.toggleAvailability(id);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }

    /**
     * 批量导入规则
     *
     * @param rules 规则列表
     * @return 是否成功
     */
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "批量导入规则", description = "批量保存评分规则")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "导入成功"),
        @ApiResponse(responseCode = "400", description = "参数错误"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> batchImport(@Valid @RequestBody List<ScoreRule> rules) {
        boolean success = scoreRuleService.saveBatch(rules);
        return success ? Result.success("导入成功") : Result.error("导入失败");
    }
}
