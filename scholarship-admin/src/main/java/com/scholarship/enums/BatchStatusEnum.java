package com.scholarship.enums;

import lombok.Getter;

/**
 * 批次状态枚举
 * <p>
 * 对应 evaluation_batch 表的 batch_status 字段
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
public enum BatchStatusEnum {

    /**
     * 未开始（已发布）
     */
    NOT_STARTED(1, "未开始"),

    /**
     * 申请中
     */
    APPLYING(2, "申请中"),

    /**
     * 评审中
     */
    REVIEWING(3, "评审中"),

    /**
     * 公示中
     */
    PUBLICITY(4, "公示中"),

    /**
     * 已完成
     */
    COMPLETED(5, "已完成");

    private final Integer code;
    private final String description;

    BatchStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举，找不到则返回 null
     */
    public static BatchStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (BatchStatusEnum status : values()) {
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
        BatchStatusEnum status = getByCode(code);
        return status != null ? status.description : "";
    }
}
