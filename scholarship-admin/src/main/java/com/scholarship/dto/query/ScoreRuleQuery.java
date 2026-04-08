package com.scholarship.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评分规则查询参数
 * <p>
 * 用于分页查询评分规则
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreRuleQuery extends PageQuery {

    /**
     * 规则类型：1-论文 2-专利 3-项目 4-竞赛 5-课程成绩 6-德育表现
     */
    private Integer ruleType;

    /**
     * 规则名称（模糊查询）
     */
    private String ruleName;

    /**
     * 是否启用：0-禁用 1-启用
     */
    private Integer available;
}
