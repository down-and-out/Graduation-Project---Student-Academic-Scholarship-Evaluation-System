package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.dto.query.ScoreRuleQuery;
import com.scholarship.entity.ScoreRule;

import java.util.List;

/**
 * 评分规则服务接口
 *
 * @author Scholarship Development Team
 * @version 1.1.0
 */
public interface ScoreRuleService extends IService<ScoreRule> {

    /**
     * 根据 ID 查询评分规则（带缓存）
     * @param id 规则 ID
     * @return 评分规则
     */
    ScoreRule getByIdWithCache(Long id);

    /**
     * 更新评分规则（清除缓存）
     * @param entity 规则实体
     * @return 是否成功
     */
    boolean updateByIdWithCache(ScoreRule entity);

    /**
     * 根据规则类型查询可用规则列表
     * @param ruleType 规则类型（1-论文 2-专利 3-项目 4-竞赛 5-课程成绩 6-德育表现）
     * @return 规则列表
     */
    List<ScoreRule> listByRuleType(Integer ruleType);

    /**
     * 根据分类 ID 查询规则列表
     * @param categoryId 分类 ID
     * @return 规则列表
     */
    List<ScoreRule> listByCategoryId(Long categoryId);

    /**
     * 查询某类型的所有可用规则（按排序号排序）
     * @param ruleType 规则类型
     * @return 规则列表
     */
    List<ScoreRule> listAvailableByRuleType(Integer ruleType);

    /**
     * 批量保存规则列表
     * @param rules 规则列表
     * @return 是否成功
     */
    boolean saveBatch(List<ScoreRule> rules);

    /**
     * 切换规则可用状态
     * @param id 规则 ID
     * @return 是否成功
     */
    boolean toggleAvailability(Long id);

    /**
     * 分页查询评分规则
     * @param query 查询参数
     * @return 分页结果
     */
    IPage<ScoreRule> queryPage(ScoreRuleQuery query);
}
