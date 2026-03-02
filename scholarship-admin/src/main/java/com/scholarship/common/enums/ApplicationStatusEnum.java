package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 奖学金申请状态枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum ApplicationStatusEnum {

    DRAFT(0, "草稿"),
    SUBMITTED(1, "已提交"),
    TUTOR_REVIEWING(2, "导师审核中"),
    TUTOR_PASSED(3, "导师审核通过"),
    TUTOR_REJECTED(4, "导师审核不通过"),
    ADMIN_REVIEWING(5, "院系审核中"),
    ADMIN_REJECTED(6, "院系审核不通过"),
    APPROVED(7, "审核通过"),
    APPEALED(8, "已申诉");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 状态枚举
     */
    public static ApplicationStatusEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApplicationStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
