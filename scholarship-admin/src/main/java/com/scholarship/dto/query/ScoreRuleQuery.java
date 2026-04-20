package com.scholarship.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScoreRuleQuery extends PageQuery {

    private Integer ruleType;

    private List<Integer> ruleTypes;

    private String ruleName;

    private Integer available;
}
