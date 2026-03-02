package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.RuleCategory;
import com.scholarship.entity.ScoreRule;
import com.scholarship.mapper.RuleCategoryMapper;
import com.scholarship.service.RuleCategoryService;
import com.scholarship.service.ScoreRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则分类服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.1.0
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
        log.debug("查询分类树");

        // 查询所有启用的分类
        List<RuleCategory> allCategories = list(
            new LambdaQueryWrapper<RuleCategory>()
                .eq(RuleCategory::getStatus, 1)
                .orderByAsc(RuleCategory::getSortOrder)
        );

        // 构建树形结构（这里返回扁平列表，前端可根据 parentId 组装树）
        return allCategories;
    }

    @Override
    public List<RuleCategory> listTopCategories() {
        log.debug("查询顶级分类");

        return list(
            new LambdaQueryWrapper<RuleCategory>()
                .eq(RuleCategory::getParentId, 0L)
                .eq(RuleCategory::getStatus, 1)
                .orderByAsc(RuleCategory::getSortOrder)
        );
    }

    @Override
    public List<RuleCategory> listChildren(Long parentId) {
        log.debug("查询子分类，parentId={}", parentId);

        return list(
            new LambdaQueryWrapper<RuleCategory>()
                .eq(RuleCategory::getParentId, parentId)
                .eq(RuleCategory::getStatus, 1)
                .orderByAsc(RuleCategory::getSortOrder)
        );
    }

    @Override
    public List<ScoreRule> listRulesByCategory(Long categoryId) {
        log.debug("根据分类查询规则，categoryId={}", categoryId);

        return scoreRuleService.listByCategoryId(categoryId);
    }
}
