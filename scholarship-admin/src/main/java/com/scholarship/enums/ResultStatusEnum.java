package com.scholarship.enums;

import lombok.Getter;

/**
 * 评定结果状态枚举
 * <p>
 * 对应 evaluation_result 表的 result_status 字段
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
public enum ResultStatusEnum {

    /**
     * 公示中
     */
    PUBLICITY(1, "公示中"),

    /**
     * 已确定
     */
    CONFIRMED(2, "已确定"),

    /**
     * 有异议
     */
    OBJECTED(3, "有异议");

    private final Integer code;
    private final String description;

    ResultStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举，找不到则返回 null
     */
    public static ResultStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ResultStatusEnum status : values()) {
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
        ResultStatusEnum status = getByCode(code);
        return status != null ? status.description : "";
    }
}
