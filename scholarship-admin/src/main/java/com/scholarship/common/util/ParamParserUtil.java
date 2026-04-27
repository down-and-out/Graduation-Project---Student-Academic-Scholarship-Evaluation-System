package com.scholarship.common.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 参数解析工具类
 * <p>
 * 统一处理 Controller 层多值筛选参数（逗号分隔或重复参数）的解析，
 * 去重保留顺序，返回可变列表。
 * </p>
 */
public class ParamParserUtil {

    /**
     * 解析字符串类型参数列表
     * <p>
     * 将原始字符串列表（可能包含逗号分隔的多值）解析为去重后的字符串列表。
     * 例如：["1,2", "3"] → ["1", "2", "3"]
     * </p>
     *
     * @param rawValues 原始参数值列表，可能为 null 或包含逗号分隔的值
     * @return 解析后的去重字符串列表（可变），不会为 null
     */
    public static List<String> parseStringParams(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> result = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            for (String value : rawValue.split(",")) {
                if (!value.isBlank()) {
                    result.add(value.trim());
                }
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * 解析整数类型参数列表
     * <p>
     * 将原始字符串列表（可能包含逗号分隔的多值）解析为去重后的整数列表。
     * 例如：["1,2", "3"] → [1, 2, 3]
     * </p>
     *
     * @param rawValues 原始参数值列表，可能为 null 或包含逗号分隔的值
     * @return 解析后的去重整数列表（可变），不会为 null
     */
    public static List<Integer> parseIntegerParams(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Integer> result = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            for (String value : rawValue.split(",")) {
                if (!value.isBlank()) {
                    result.add(Integer.parseInt(value.trim()));
                }
            }
        }
        return new ArrayList<>(result);
    }

    private ParamParserUtil() {
        // 工具类，防止实例化
    }
}
