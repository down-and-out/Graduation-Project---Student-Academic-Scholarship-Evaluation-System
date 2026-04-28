package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评审阶段枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ReviewStageEnum {

    TUTOR(1, "导师审核", "导师审核阶段"),
    DEPARTMENT(2, "院系审核", "院系管理员审核阶段"),
    SCHOOL(3, "学校审核", "学校奖学金评审委员会审核阶段");

    /**
     * 评审阶段码
     */
    private final Integer code;

    /**
     * 评审阶段描述
     */
    private final String description;

    /**
     * 评审阶段详细信息
     */
    private final String message;

    /**
     * 根据阶段码获取枚举
     *
     * @param code 阶段码
     * @return 评审阶段枚举
     */
    public static ReviewStageEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReviewStageEnum stage : values()) {
            if (stage.getCode().equals(code)) {
                return stage;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的阶段码
     *
     * @param code 阶段码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return valueOfCode(code) != null;
    }
}
