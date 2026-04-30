package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EvaluationTaskStatusEnum {

    PENDING(0, "排队中"),
    RUNNING(1, "执行中"),
    SUCCESS(2, "执行成功"),
    FAILED(3, "执行失败"),
    CANCELLED(4, "已取消");

    private final Integer code;
    private final String description;

    public static EvaluationTaskStatusEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (EvaluationTaskStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static boolean isValid(Integer code) {
        return valueOfCode(code) != null;
    }

    public static String getDescription(Integer code) {
        EvaluationTaskStatusEnum status = valueOfCode(code);
        return status != null ? status.description : "";
    }

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED;
    }
}
