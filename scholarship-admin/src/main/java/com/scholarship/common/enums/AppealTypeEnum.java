package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 异议类型枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum AppealTypeEnum {

    SCORE_ERROR(1, "分数错误", "您提交的成果分数计算有误，请核实后重新申诉"),
    OMISSION(2, "漏算成果", "您的成果未被正确计入总分，请补充材料"),
    RANKING_ERROR(3, "排名错误", "您的排名计算有误，请核实排名规则"),
    MATERIAL_NOT_REVIEWED(4, "材料未审核", "您的材料尚未完成审核，请等待审核完成"),
    OTHER(5, "其他原因", "其他异议原因，请详细描述");

    /**
     * 异议类型码
     */
    private final Integer code;

    /**
     * 异议类型描述
     */
    private final String description;

    /**
     * 异议类型的错误提示信息
     */
    private final String message;

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 异议类型枚举
     */
    public static AppealTypeEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AppealTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的类型码
     *
     * @param code 类型码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return valueOfCode(code) != null;
    }
}
