package com.scholarship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.scholarship.common.result.Result;
import com.scholarship.common.util.ParamParserUtil;
import com.scholarship.dto.query.ScoreRuleQuery;
import com.scholarship.entity.ScoreRule;
import com.scholarship.service.ScoreRuleService;
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

@Slf4j
@RestController
@RequestMapping("/score-rule")
@RequiredArgsConstructor
@Tag(name = "08-评分规则管理", description = "奖学金评分规则的配置接口（仅管理员）")
public class ScoreRuleController {

    private final ScoreRuleService scoreRuleService;

    @GetMapping("/page")
    @Operation(summary = "分页查询评分规则", description = "支持按规则类型筛选，兼容单个或多个类型")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<IPage<ScoreRule>> page(
            @Parameter(description = "当前页", example = "1") @RequestParam(defaultValue = "1") Long current,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Long size,
            @Parameter(description = "规则类型，支持单个或多个值", example = "1,2") @RequestParam(required = false) List<String> ruleType) {

        ScoreRuleQuery query = new ScoreRuleQuery();
        query.setCurrent(current);
        query.setSize(size);

        List<Integer> ruleTypes = ParamParserUtil.parseIntegerParams(ruleType);
        if (!ruleTypes.isEmpty()) {
            query.setRuleTypes(ruleTypes);
            query.setRuleType(ruleTypes.size() == 1 ? ruleTypes.get(0) : null);
        }

        IPage<ScoreRule> result = scoreRuleService.queryPage(query);
        return Result.success(result);
    }

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
