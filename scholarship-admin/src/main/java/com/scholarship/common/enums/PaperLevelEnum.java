package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 论文等级枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PaperLevelEnum {

    SCI_Q1(1, "SCI一区", "国际顶级期刊"),
    SCI_Q2(2, "SCI二区", "国际高水平期刊"),
    SCI_Q3(3, "SCI三区", "国际知名期刊"),
    SCI_Q4(4, "SCI四区", "国际一般期刊"),
    EI(5, "EI", "工程索引期刊"),
    CORE(6, "核心期刊", "国内核心期刊"),
    GENERAL(7, "普通期刊", "国内普通期刊"),
    ISTP(8, "ISTP", "会议论文");

    /**
     * 论文等级码
     */
    private final Integer code;

    /**
     * 论文等级描述
     */
    private final String description;

    /**
     * 论文等级详细信息
     */
    private final String message;

    /**
     * 根据等级码获取枚举
     *
     * @param code 等级码
     * @return 论文等级枚举
     */
    public static PaperLevelEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PaperLevelEnum level : values()) {
            if (level.getCode().equals(code)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的等级码
     *
     * @param code 等级码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return valueOfCode(code) != null;
    }
}
