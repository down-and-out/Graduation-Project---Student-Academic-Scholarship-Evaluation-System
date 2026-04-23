package com.scholarship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.RuleCategory;
import com.scholarship.entity.ScoreRule;

import java.util.List;

/**
 * 规则分类服务接口。
 */
public interface RuleCategoryService extends IService<RuleCategory> {

    /**
     * 查询分类列表。
     */
    List<RuleCategory> listAsTree();

    /**
     * 查询顶级分类。
     */
    List<RuleCategory> listTopCategories();

    /**
     * 查询子分类。
     */
    List<RuleCategory> listChildren(Long parentId);

    /**
     * 根据分类查询规则。
     */
    List<ScoreRule> listRulesByCategory(Long categoryId);

    /**
     * 删除分类并校验是否仍有关联规则。
     */
    boolean deleteWithCheck(Long id);

    /**
     * 切换分类状态。
     */
    boolean toggleStatus(Long id);
}
