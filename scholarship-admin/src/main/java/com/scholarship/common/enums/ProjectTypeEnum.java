package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 项目类型枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ProjectTypeEnum {

    NATIONAL(1, "国家级项目", "国家科技重大专项、国家自然科学基金等"),
    MINISTERIAL(2, "省部级项目", "教育部、国家各部委、省科技计划项目"),
    LOCAL(3, "厅局级项目", "省厅局、市科技计划项目"),
    SCHOOL(4, "校级项目", "学校自主科研项目"),
    HORIZONTAL(5, "横向课题", "企业委托科研项目"),
    INTERNATIONAL(6, "国际合作项目", "国际合作科研项目"),
    INDUSTRY_UNIVERSITY(7, "产学研合作项目", "校企合作项目");

    /**
     * 项目类型码
     */
    private final Integer code;

    /**
     * 项目类型描述
     */
    private final String description;

    /**
     * 项目类型详细信息
     */
    private final String message;

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 项目类型枚举
     */
    public static ProjectTypeEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProjectTypeEnum type : values()) {
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
