package com.scholarship.enums;

import lombok.Getter;

/**
 * 审核状态枚举
 * <p>
 * 用于德育表现、科研论文、专利、项目、竞赛等需要审核的模块
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
public enum AuditStatusEnum {

    /**
     * 待审核/未审核
     */
    PENDING(0, "待审核"),

    /**
     * 审核通过
     */
    APPROVED(1, "审核通过"),

    /**
     * 审核驳回
     */
    REJECTED(2, "审核驳回");

    private final Integer code;
    private final String description;

    AuditStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举，找不到则返回 null
     */
    public static AuditStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AuditStatusEnum status : values()) {
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
        AuditStatusEnum status = getByCode(code);
        return status != null ? status.description : "";
    }
}
