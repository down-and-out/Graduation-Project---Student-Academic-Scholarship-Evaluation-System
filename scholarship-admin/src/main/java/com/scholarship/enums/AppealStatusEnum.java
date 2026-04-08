package com.scholarship.enums;

import lombok.Getter;

/**
 * 异议状态枚举
 * <p>
 * 对应 result_appeal 表的 appeal_status 字段
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
public enum AppealStatusEnum {

    /**
     * 待处理
     */
    PENDING(1, "待处理"),

    /**
     * 处理中
     */
    PROCESSING(2, "处理中"),

    /**
     * 已处理
     */
    PROCESSED(3, "已处理");

    private final Integer code;
    private final String description;

    AppealStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举，找不到则返回 null
     */
    public static AppealStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AppealStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断状态码是否有效
     *
     * @param code 状态码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 获取状态名称
     *
     * @param code 状态码
     * @return 状态名称，找不到返回空字符串
     */
    public static String getDescription(Integer code) {
        AppealStatusEnum status = getByCode(code);
        return status != null ? status.description : "";
    }
}
