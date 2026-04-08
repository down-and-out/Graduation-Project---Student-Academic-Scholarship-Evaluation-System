package com.scholarship.enums;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 获奖等级枚举
 * <p>
 * 对应 evaluation_result 表的 award_level 字段
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
public enum AwardLevelEnum {

    /**
     * 特等奖学金
     */
    SPECIAL(1, "特等奖学金", new BigDecimal("20000")),

    /**
     * 一等奖学金
     */
    FIRST(2, "一等奖学金", new BigDecimal("10000")),

    /**
     * 二等奖学金
     */
    SECOND(3, "二等奖学金", new BigDecimal("6000")),

    /**
     * 三等奖学金
     */
    THIRD(4, "三等奖学金", new BigDecimal("3000")),

    /**
     * 未获奖
     */
    NONE(5, "未获奖", BigDecimal.ZERO);

    private final Integer code;
    private final String description;
    private final BigDecimal defaultAmount;

    AwardLevelEnum(Integer code, String description, BigDecimal defaultAmount) {
        this.code = code;
        this.description = description;
        this.defaultAmount = defaultAmount;
    }

    /**
     * 根据状态码获取枚举
     *
     * @param code 状态码
     * @return 对应的枚举，找不到则返回 null
     */
    public static AwardLevelEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (AwardLevelEnum level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        return null;
    }

    /**
     * 判断等级码是否有效
     *
     * @param code 等级码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }

    /**
     * 获取等级名称
     *
     * @param code 等级码
     * @return 等级名称，找不到返回空字符串
     */
    public static String getDescription(Integer code) {
        AwardLevelEnum level = getByCode(code);
        return level != null ? level.description : "";
    }

    /**
     * 获取默认奖学金金额
     *
     * @param code 等级码
     * @return 默认金额，找不到返回 0
     */
    public static BigDecimal getDefaultAmount(Integer code) {
        AwardLevelEnum level = getByCode(code);
        return level != null ? level.defaultAmount : BigDecimal.ZERO;
    }
}
