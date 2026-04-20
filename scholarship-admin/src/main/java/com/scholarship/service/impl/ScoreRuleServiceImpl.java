package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.dto.query.ScoreRuleQuery;
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

@Slf4j
@Service
@CacheConfig(cacheNames = "scoreRules")
public class ScoreRuleServiceImpl extends ServiceImpl<ScoreRuleMapper, ScoreRule> implements ScoreRuleService {

    @Override
    @Cacheable(key = "#id", unless = "#result == null")
    public ScoreRule getByIdWithCache(Long id) {
        log.debug("查询评分规则, id={}", id);
        return super.getById(id);
    }

    @Override
    @CacheEvict(key = "#entity.id")
    public boolean updateByIdWithCache(ScoreRule entity) {
        return super.updateById(entity);
    }

    @Override
    public List<ScoreRule> listByRuleType(Integer ruleType) {
        log.debug("根据规则类型查询规则, ruleType={}", ruleType);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getRuleType, ruleType)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<ScoreRule> listByCategoryId(Long categoryId) {
        log.debug("根据分类 ID 查询规则, categoryId={}", categoryId);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getCategoryId, categoryId)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<ScoreRule> listAvailableByRuleType(Integer ruleType) {
        log.debug("查询可用规则, ruleType={}", ruleType);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getRuleType, ruleType)
                .eq(ScoreRule::getIsAvailable, 1)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    public boolean save(ScoreRule entity) {
        if (entity.getCategoryId() == null && entity.getRuleType() != null) {
            entity.setCategoryId(Long.valueOf(entity.getRuleType()));
        }
        return super.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(allEntries = true)
    public boolean saveBatch(List<ScoreRule> rules) {
        log.debug("批量保存规则，数量：{}", rules != null ? rules.size() : 0);
        if (rules != null) {
            for (ScoreRule rule : rules) {
                if (rule.getCategoryId() == null && rule.getRuleType() != null) {
                    rule.setCategoryId(Long.valueOf(rule.getRuleType()));
                }
            }
        }
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

    @Override
    public IPage<ScoreRule> queryPage(ScoreRuleQuery query) {
        log.debug("分页查询评分规则，query={}", query);

        Page<ScoreRule> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<>();

        List<Integer> ruleTypes = query.getRuleTypes();
        if (ruleTypes != null && !ruleTypes.isEmpty()) {
            if (ruleTypes.size() == 1) {
                wrapper.eq(ScoreRule::getRuleType, ruleTypes.get(0));
            } else {
                wrapper.in(ScoreRule::getRuleType, ruleTypes);
            }
        } else if (query.getRuleType() != null) {
            wrapper.eq(ScoreRule::getRuleType, query.getRuleType());
        }

        if (query.getRuleName() != null) {
            wrapper.like(ScoreRule::getRuleName, query.getRuleName());
        }
        if (query.getAvailable() != null) {
            wrapper.eq(ScoreRule::getIsAvailable, query.getAvailable());
        }

        wrapper.orderByAsc(ScoreRule::getSortOrder);
        return page(page, wrapper);
    }
}
