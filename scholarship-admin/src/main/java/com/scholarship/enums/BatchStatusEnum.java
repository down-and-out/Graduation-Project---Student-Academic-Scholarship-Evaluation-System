package com.scholarship.enums;

import lombok.Getter;

@Getter
public enum BatchStatusEnum {

    NOT_STARTED(1, "未开始"),

    APPLYING(2, "申请中"),

    REVIEWING(3, "评审中"),

    PUBLICITY(4, "公示中"),

    COMPLETED(5, "已完成");

    private final Integer code;
    private final String description;

    BatchStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

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

    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    public static String getDescription(Integer code) {
        BatchStatusEnum status = getByCode(code);
        return status != null ? status.description : "";
    }

    public boolean canTransitionTo(BatchStatusEnum target) {
        if (target == null) {
            return false;
        }

        return switch (this) {
            case NOT_STARTED -> target == NOT_STARTED || target == APPLYING;
            case APPLYING -> target == REVIEWING;
            case REVIEWING -> target == PUBLICITY;
            case PUBLICITY -> target == COMPLETED;
            case COMPLETED -> false;
        };
    }
}
