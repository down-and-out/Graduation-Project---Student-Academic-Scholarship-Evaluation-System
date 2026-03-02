package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 审核意见枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ReviewOpinionEnum {

    PASS("通过", true),
    REJECT("不通过", false),
    REFUSE("拒绝", false),
    AGREE("同意", true),
    DISAGREE("不同意", false),
    PENDING("待审核", null);

    /**
     * 意见文本
     */
    private final String text;

    /**
     * 是否通过（null 表示待审核）
     */
    private final Boolean passed;

    /**
     * 根据意见文本获取枚举
     *
     * @param text 意见文本
     * @return 意见枚举
     */
    public static ReviewOpinionEnum valueOfText(String text) {
        if (text == null) {
            return PENDING;
        }
        return Arrays.stream(values())
                .filter(op -> op.getText().equals(text))
                .findFirst()
                .orElse(PENDING);
    }

    /**
     * 判断是否为通过意见
     *
     * @param text 意见文本
     * @return true-通过，false-不通过，null-待审核
     */
    public static Boolean isPassed(String text) {
        return valueOfText(text).getPassed();
    }
}
