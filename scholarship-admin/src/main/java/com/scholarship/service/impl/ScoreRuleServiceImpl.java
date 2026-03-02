package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.ScoreRule;
import com.scholarship.mapper.ScoreRuleMapper;
import com.scholarship.service.ScoreRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评分规则服务实现类
 * <p>
 * 提供评分规则的基础 CRUD 操作，继承自 MyBatis-Plus 的 ServiceImpl
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
@Slf4j
@Service
@CacheConfig(cacheNames = "scoreRules")
public class ScoreRuleServiceImpl extends ServiceImpl<ScoreRuleMapper, ScoreRule> implements ScoreRuleService {

    @Override
    @Cacheable(key = "#id", unless = "#result == null")
    public ScoreRule getByIdWithCache(Long id) {
        log.debug("查询评分规则：id={}", id);
        return super.getById(id);
    }

    @Override
    @CacheEvict(key = "#entity.id")
    public boolean updateByIdWithCache(ScoreRule entity) {
        return super.updateById(entity);
    }

    @Override
    public List<ScoreRule> listByRuleType(Integer ruleType) {
        log.debug("根据规则类型查询规则：ruleType={}", ruleType);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getRuleType, ruleType)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<ScoreRule> listByCategoryId(Long categoryId) {
        log.debug("根据分类 ID 查询规则：categoryId={}", categoryId);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getCategoryId, categoryId)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<ScoreRule> listAvailableByRuleType(Integer ruleType) {
        log.debug("查询可用规则：ruleType={}", ruleType);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getRuleType, ruleType)
                .eq(ScoreRule::getIsAvailable, 1)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(List<ScoreRule> rules) {
        log.debug("批量保存规则，数量：{}", rules != null ? rules.size() : 0);
        return super.saveBatch(rules, rules != null ? rules.size() : 10);
    }

    @Override
    @CacheEvict(key = "#id")
    public boolean toggleAvailability(Long id) {
        log.debug("切换规则可用状态：id={}", id);
        ScoreRule rule = getById(id);
        if (rule == null) {
            return false;
        }
        rule.setIsAvailable(rule.getIsAvailable() == 1 ? 0 : 1);
        return updateById(rule);
    }
}
