package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.RuleCategory;
import com.scholarship.entity.ScoreRule;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.mapper.RuleCategoryMapper;
import com.scholarship.service.RuleCategoryService;
import com.scholarship.service.ScoreRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 规则分类服务实现。
 */
@Slf4j
@Service
public class RuleCategoryServiceImpl extends ServiceImpl<RuleCategoryMapper, RuleCategory>
        implements RuleCategoryService {

    private final ScoreRuleService scoreRuleService;

    public RuleCategoryServiceImpl(ScoreRuleService scoreRuleService) {
        this.scoreRuleService = scoreRuleService;
    }

    @Override
    public List<RuleCategory> listAsTree() {
        log.debug("查询分类列表");
        return listActiveCategories();
    }

    @Override
    public List<RuleCategory> listTopCategories() {
        log.debug("查询顶级分类列表");
        // 当前表结构没有 parent_id，顶级分类接口退化为返回全部启用分类。
        return listActiveCategories();
    }

    @Override
    public List<RuleCategory> listChildren(Long parentId) {
        log.debug("查询子分类，parentId={}", parentId);
        // 当前表结构不支持父子层级，返回空列表避免查询不存在的列。
        return List.of();
    }

    @Override
    public List<ScoreRule> listRulesByCategory(Long categoryId) {
        log.debug("根据分类查询规则，categoryId={}", categoryId);
        return scoreRuleService.listByCategoryId(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithCheck(Long id) {
        log.info("删除分类（带检查），id={}", id);

        RuleCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        List<ScoreRule> rules = listRulesByCategory(id);
        if (!rules.isEmpty()) {
            throw new BusinessException("该分类下存在规则，无法删除");
        }

        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleStatus(Long id) {
        log.info("切换分类状态，id={}", id);

        RuleCategory category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        category.setStatus(category.getStatus() == 1 ? 0 : 1);
        return updateById(category);
    }

    private List<RuleCategory> listActiveCategories() {
        return list(new LambdaQueryWrapper<RuleCategory>()
                .eq(RuleCategory::getStatus, 1)
                .orderByAsc(RuleCategory::getSortOrder)
                .orderByAsc(RuleCategory::getId));
    }
}
