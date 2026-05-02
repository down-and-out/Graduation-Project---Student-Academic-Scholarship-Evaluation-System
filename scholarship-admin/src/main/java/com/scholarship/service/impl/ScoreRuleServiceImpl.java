package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.support.CacheConstants;
import com.scholarship.dto.query.ScoreRuleQuery;
import com.scholarship.entity.ScoreRule;
import com.scholarship.mapper.ScoreRuleMapper;
import com.scholarship.common.event.CacheEvictionEvent;
import com.scholarship.common.event.CacheEvictionOperation;
import com.scholarship.service.ScoreRuleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评分规则服务实现类
 *
 * 功能说明：
 * - 分页查询评分规则（支持规则类型筛选）
 * - 带缓存查询规则详情
 * - 根据规则类型查询规则列表
 * - 根据分类ID查询规则列表
 * - 查询可用规则列表（仅启用状态）
 * - 新增/更新/删除评分规则（操作后清除缓存）
 * - 切换规则可用状态
 * - 批量导入规则
 *
 * 缓存说明：
 * - 规则详情使用 @Cacheable 缓存（key: 规则ID）
 * - 可用规则列表使用 @Cacheable 缓存（key: 规则类型）
 * - 新增、更新、删除操作后发布缓存清除事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = CacheConstants.RULE_DETAIL)
public class ScoreRuleServiceImpl extends ServiceImpl<ScoreRuleMapper, ScoreRule> implements ScoreRuleService {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 带缓存查询规则详情
     * 缓存键：规则ID
     */
    @Override
    @Cacheable(key = "#id")
    public ScoreRule getByIdWithCache(Long id) {
        log.debug("查询评分规则, id={}", id);
        return super.getById(id);
    }

    /**
     * 更新规则（更新后清除缓存）
     */
    @Override
    public boolean updateByIdWithCache(ScoreRule entity) {
        return updateById(entity);
    }

    /**
     * 更新评分规则
     * 更新后发布缓存清除事件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(ScoreRule entity) {
        boolean success = super.updateById(entity);
        if (success) {
            // 发布缓存清除事件
            eventPublisher.publishEvent(new CacheEvictionEvent(this, CacheEvictionOperation.EVICT_RULE_CACHES, null));
        }
        return success;
    }

    /**
     * 根据规则类型查询规则列表
     * 按排序号正序排列
     *
     * @param ruleType 规则类型
     * @return 规则列表
     */
    @Override
    public List<ScoreRule> listByRuleType(Integer ruleType) {
        log.debug("根据规则类型查询规则, ruleType={}", ruleType);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getRuleType, ruleType)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    /**
     * 根据分类ID查询规则列表
     * 按排序号正序排列
     *
     * @param categoryId 分类ID
     * @return 规则列表
     */
    @Override
    public List<ScoreRule> listByCategoryId(Long categoryId) {
        log.debug("根据分类 ID 查询规则, categoryId={}", categoryId);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getCategoryId, categoryId)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    /**
     * 查询可用规则列表（仅启用状态）
     * 按排序号正序排列
     * 缓存键：CacheConstants.ruleAvailableKey(ruleType)
     *
     * @param ruleType 规则类型
     * @return 可用规则列表
     */
    @Override
    @Cacheable(value = CacheConstants.RULE_AVAILABLE,
            key = "T(com.scholarship.common.support.CacheConstants).ruleAvailableKey(#ruleType)",
            unless = "#result == null || #result.isEmpty()")
    public List<ScoreRule> listAvailableByRuleType(Integer ruleType) {
        log.debug("查询可用规则, ruleType={}", ruleType);
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<ScoreRule>()
                .eq(ScoreRule::getRuleType, ruleType)
                .eq(ScoreRule::getIsAvailable, 1)
                .orderByAsc(ScoreRule::getSortOrder);
        return list(wrapper);
    }

    /**
     * 新增评分规则
     * 保存前校验分类ID不能为空
     * 保存后发布缓存清除事件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(ScoreRule entity) {
        requireCategoryId(entity);
        boolean success = super.save(entity);
        if (success) {
            // 发布缓存清除事件
            eventPublisher.publishEvent(new CacheEvictionEvent(this, CacheEvictionOperation.EVICT_RULE_CACHES, null));
        }
        return success;
    }

    /**
     * 批量保存评分规则
     * 保存前校验每条规则的分类ID不能为空
     * 保存后发布缓存清除事件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(List<ScoreRule> rules) {
        log.debug("批量保存规则，数量：{}", rules != null ? rules.size() : 0);
        if (rules != null) {
            for (ScoreRule rule : rules) {
                requireCategoryId(rule);
            }
        }
        boolean success = super.saveBatch(rules, rules != null ? rules.size() : 10);
        if (success) {
            // 发布缓存清除事件
            eventPublisher.publishEvent(new CacheEvictionEvent(this, CacheEvictionOperation.EVICT_RULE_CACHES, null));
        }
        return success;
    }

    /**
     * 切换规则可用状态
     * 在启用(1)和禁用(0)之间切换
     */
    @Override
    public boolean toggleAvailability(Long id) {
        log.debug("切换规则可用状态：id={}", id);
        ScoreRule rule = getById(id);
        if (rule == null) {
            return false;
        }
        rule.setIsAvailable(rule.getIsAvailable() == 1 ? 0 : 1);
        return updateById(rule);
    }

    /**
     * 分页查询评分规则
     *
     * @param query 查询条件（包含分页参数、规则类型、规则名称、可用状态）
     * @return 分页后的评分规则列表
     */
    @Override
    public IPage<ScoreRule> queryPage(ScoreRuleQuery query) {
        log.debug("分页查询评分规则，query={}", query);

        Page<ScoreRule> page = new Page<>(query.getCurrent(), query.getSize());
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<>();

        // 规则类型筛选（支持多个类型）
        List<Integer> ruleTypes = query.getRuleTypes();
        if (ruleTypes != null && !ruleTypes.isEmpty()) {
            if (ruleTypes.size() == 1) {
                wrapper.eq(ScoreRule::getRuleType, ruleTypes.get(0));
            } else {
                wrapper.in(ScoreRule::getRuleType, ruleTypes);
            }
        } else if (query.getRuleType() != null) {
            // 单个规则类型（兼容旧接口）
            wrapper.eq(ScoreRule::getRuleType, query.getRuleType());
        }

        // 规则名称模糊搜索
        if (query.getRuleName() != null) {
            wrapper.like(ScoreRule::getRuleName, query.getRuleName());
        }

        // 可用状态筛选
        if (query.getAvailable() != null) {
            wrapper.eq(ScoreRule::getIsAvailable, query.getAvailable());
        }

        // 按排序号正序
        wrapper.orderByAsc(ScoreRule::getSortOrder);
        return page(page, wrapper);
    }

    /**
     * 校验规则分类ID不能为空
     */
    private void requireCategoryId(ScoreRule rule) {
        if (rule == null) {
            throw new BusinessException("评分规则不能为空");
        }
        if (rule.getCategoryId() == null) {
            throw new BusinessException("评分规则分类不能为空，请选择真实分类");
        }
    }
}
