package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.entity.RuleCategory;
import com.scholarship.entity.ScoreRule;
import com.scholarship.service.RuleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则分类控制器。
 */
@Slf4j
@RestController
@RequestMapping("/rule-category")
@RequiredArgsConstructor
@Tag(name = "规则分类管理", description = "评分规则分类管理接口")
public class RuleCategoryController {

    private final RuleCategoryService ruleCategoryService;

    @GetMapping("/tree")
    @Operation(summary = "查询分类列表", description = "当前库结构为平铺分类表，返回全部启用分类")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<RuleCategory>> listAsTree() {
        return Result.success(ruleCategoryService.listAsTree());
    }

    @GetMapping("/top")
    @Operation(summary = "查询顶级分类", description = "当前库结构不区分父子层级，返回全部启用分类")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<RuleCategory>> listTopCategories() {
        return Result.success(ruleCategoryService.listTopCategories());
    }

    @GetMapping("/children/{parentId}")
    @Operation(summary = "查询子分类", description = "当前库结构不支持父子层级，固定返回空列表")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<RuleCategory>> listChildren(@PathVariable Long parentId) {
        return Result.success(ruleCategoryService.listChildren(parentId));
    }

    @GetMapping("/{categoryId}/rules")
    @Operation(summary = "根据分类查询规则", description = "返回指定分类下的所有评分规则")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<ScoreRule>> listRulesByCategory(@PathVariable Long categoryId) {
        return Result.success(ruleCategoryService.listRulesByCategory(categoryId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "新增分类", description = "添加新的规则分类")
    public Result<Void> add(@Valid @RequestBody RuleCategory category) {
        category.setStatus(1);
        boolean success = ruleCategoryService.save(category);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新分类", description = "修改规则分类信息")
    public Result<Void> update(@Valid @RequestBody RuleCategory category) {
        boolean success = ruleCategoryService.updateById(category);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除分类", description = "删除指定规则分类")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = ruleCategoryService.deleteWithCheck(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @PutMapping("/toggle/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "切换分类状态", description = "启用或禁用规则分类")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        boolean success = ruleCategoryService.toggleStatus(id);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }
}
