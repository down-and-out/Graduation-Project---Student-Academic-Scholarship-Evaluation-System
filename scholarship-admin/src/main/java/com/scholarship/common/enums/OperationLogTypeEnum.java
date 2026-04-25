package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作日志类型枚举。
 */
@Getter
@AllArgsConstructor
public enum OperationLogTypeEnum {

    LOGIN(1, "登录"),
    USER(2, "用户管理"),
    EVALUATION(3, "评定管理"),
    SYSTEM(4, "系统设置");

    private final Integer code;

    private final String label;

    public static OperationLogTypeEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OperationLogTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getLabelByCode(Integer code) {
        OperationLogTypeEnum value = fromCode(code);
        return value != null ? value.getLabel() : null;
    }
}
