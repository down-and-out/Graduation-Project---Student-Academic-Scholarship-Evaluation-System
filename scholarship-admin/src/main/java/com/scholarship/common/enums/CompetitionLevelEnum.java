package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 竞赛等级枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum CompetitionLevelEnum {

    INTERNATIONAL(1, "国际级", "国际性竞赛"),
    NATIONAL(2, "国家级", "全国性竞赛"),
    PROVINCIAL(3, "省级", "省级竞赛"),
    SCHOOL(4, "校级", "学校级竞赛"),
    DEPARTMENT(5, "院级", "院系级竞赛");

    /**
     * 竞赛等级码
     */
    private final Integer code;

    /**
     * 竞赛等级描述
     */
    private final String description;

    /**
     * 竞赛等级详细信息
     */
    private final String message;

    /**
     * 根据等级码获取枚举
     *
     * @param code 等级码
     * @return 竞赛等级枚举
     */
    public static CompetitionLevelEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (CompetitionLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的等级码
     *
     * @param code 等级码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return valueOfCode(code) != null;
    }
}
