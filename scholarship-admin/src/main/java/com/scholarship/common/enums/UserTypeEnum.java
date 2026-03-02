package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {

    STUDENT(1, "研究生"),
    TUTOR(2, "导师"),
    ADMIN(3, "管理员");

    /**
     * 类型码
     */
    private final Integer code;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 用户类型枚举
     */
    public static UserTypeEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (UserTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
