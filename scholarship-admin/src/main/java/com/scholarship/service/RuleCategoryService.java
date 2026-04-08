package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.RuleCategory;

import java.util.List;

/**
 * 规则分类服务接口
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
public interface RuleCategoryService extends IService<RuleCategory> {

    /**
     * 查询分类树（支持父子层级）
     *
     * @return 分类树列表
     */
    List<RuleCategory> listAsTree();

    /**
     * 查询顶级分类
     *
     * @return 顶级分类列表
     */
    List<RuleCategory> listTopCategories();

    /**
     * 查询子分类
     *
     * @param parentId 父分类 ID
     * @return 子分类列表
     */
    List<RuleCategory> listChildren(Long parentId);

    /**
     * 根据分类查询规则
     *
     * @param categoryId 分类 ID
     * @return 规则列表
     */
    List<com.scholarship.entity.ScoreRule> listRulesByCategory(Long categoryId);

    /**
     * 删除分类（带检查）
     * <p>
     * 如果分类下存在规则，则抛出异常
     *
     * @param id 分类 ID
     * @return 是否成功
     */
    boolean deleteWithCheck(Long id);

    /**
     * 切换分类状态
     *
     * @param id 分类 ID
     * @return 是否成功
     */
    boolean toggleStatus(Long id);
}
