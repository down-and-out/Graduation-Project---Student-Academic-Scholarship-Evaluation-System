package com.scholarship.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 专利类型枚举
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Getter
@AllArgsConstructor
public enum PatentTypeEnum {

    INVENTION(1, "发明专利", "对产品、方法或其改进提出的新技术方案"),
    UTILITY_MODEL(2, "实用新型专利", "对产品形状、结构或其结合提出的新技术方案"),
    DESIGN(3, "外观设计专利", "对产品形状、图案或其结合以及色彩与形状、图案的结合所作出的富有美感的新设计"),
    SOFTWARE_COPYRIGHT(4, "软件著作权", "计算机软件的著作权登记"),
    INTEGRATED_CIRCUIT(5, "集成电路布图设计", "集成电路布图设计的专利保护"),
    PLANT_VARIETY(6, "植物新品种", "植物新品种的专利保护");

    /**
     * 专利类型码
     */
    private final Integer code;

    /**
     * 专利类型描述
     */
    private final String description;

    /**
     * 专利类型详细信息
     */
    private final String message;

    /**
     * 根据类型码获取枚举
     *
     * @param code 类型码
     * @return 专利类型枚举
     */
    public static PatentTypeEnum valueOfCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (PatentTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断是否为有效的类型码
     *
     * @param code 类型码
     * @return 是否有效
     */
    public static boolean isValid(Integer code) {
        return valueOfCode(code) != null;
    }
}
