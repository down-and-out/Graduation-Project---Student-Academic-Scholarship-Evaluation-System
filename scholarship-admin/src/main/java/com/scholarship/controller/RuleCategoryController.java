package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.entity.RuleCategory;
import com.scholarship.entity.ScoreRule;
import com.scholarship.service.RuleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则分类控制器
 * <p>
 * 处理评分规则分类相关的请求
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/rule-category")
@RequiredArgsConstructor
@Tag(name = "09-规则分类管理", description = "评分规则分类的管理接口（仅管理员）")
public class RuleCategoryController {

    private final RuleCategoryService ruleCategoryService;

    /**
     * 查询分类树
     *
     * @return 分类树列表
     */
    @GetMapping("/tree")
    @Operation(summary = "查询分类树", description = "返回所有启用的分类（前端可根据 parentId 组装树形结构）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<RuleCategory>> listAsTree() {
        List<RuleCategory> categories = ruleCategoryService.listAsTree();
        return Result.success(categories);
    }

    /**
     * 查询顶级分类
     *
     * @return 顶级分类列表
     */
    @GetMapping("/top")
    @Operation(summary = "查询顶级分类", description = "返回所有一级分类")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    public Result<List<RuleCategory>> listTopCategories() {
        List<RuleCategory> categories = ruleCategoryService.listTopCategories();
        return Result.success(categories);
    }

    /**
     * 查询子分类
     *
     * @param parentId 父分类 ID
     * @return 子分类列表
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "查询子分类", description = "返回指定父分类下的所有子分类")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "分类不存在")
    })
    public Result<List<RuleCategory>> listChildren(@PathVariable Long parentId) {
        List<RuleCategory> categories = ruleCategoryService.listChildren(parentId);
        return Result.success(categories);
    }

    /**
     * 根据分类查询规则
     *
     * @param categoryId 分类 ID
     * @return 规则列表
     */
    @GetMapping("/{categoryId}/rules")
    @Operation(summary = "根据分类查询规则", description = "返回指定分类下的所有评分规则")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功"),
        @ApiResponse(responseCode = "404", description = "分类不存在")
    })
    public Result<List<ScoreRule>> listRulesByCategory(@PathVariable Long categoryId) {
        List<ScoreRule> rules = ruleCategoryService.listRulesByCategory(categoryId);
        return Result.success(rules);
    }

    /**
     * 新增分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "新增分类", description = "添加新的规则分类")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "新增成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> add(@RequestBody RuleCategory category) {
        category.setStatus(1); // 默认为启用状态
        boolean success = ruleCategoryService.save(category);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "更新分类", description = "修改规则分类信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "403", description = "无权限")
    })
    public Result<Void> update(@RequestBody RuleCategory category) {
        boolean success = ruleCategoryService.updateById(category);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除分类
     *
     * @param id 分类 ID
     * @return 是否成功
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "删除分类", description = "删除指定的规则分类")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "删除成功"),
        @ApiResponse(responseCode = "403", description = "无权限"),
        @ApiResponse(responseCode = "400", description = "该分类下存在规则，无法删除")
    })
    public Result<Void> delete(@PathVariable Long id) {
        // 检查分类下是否有规则
        List<ScoreRule> rules = ruleCategoryService.listRulesByCategory(id);
        if (!rules.isEmpty()) {
            return Result.error("该分类下存在规则，无法删除");
        }

        boolean success = ruleCategoryService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 切换分类状态
     *
     * @param id 分类 ID
     * @return 是否成功
     */
    @PutMapping("/toggle/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Operation(summary = "切换分类状态", description = "启用/禁用规则分类")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "操作成功"),
        @ApiResponse(responseCode = "404", description = "分类不存在")
    })
    public Result<Void> toggleStatus(@PathVariable Long id) {
        RuleCategory category = ruleCategoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        category.setStatus(category.getStatus() == 1 ? 0 : 1);
        boolean success = ruleCategoryService.updateById(category);
        return success ? Result.success("操作成功") : Result.error("操作失败");
    }
}
