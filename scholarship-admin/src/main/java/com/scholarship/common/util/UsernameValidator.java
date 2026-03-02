package com.scholarship.common.util;

import com.scholarship.common.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * 用户名验证工具类
 * <p>
 * 用于验证用户名的合法性，防止 SQL 注入和 XSS 攻击
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public class UsernameValidator {

    /**
     * 用户名正则表达式：
     * - 只能包含字母、数字、下划线
     * - 长度 3-20 个字符
     * - 必须以字母开头
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]{2,19}$");

    /**
     * 验证用户名是否合法
     *
     * @param username 用户名
     * @throws BusinessException 用户名不合法时抛出
     */
    public static void validate(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }

        String trimmedUsername = username.trim();

        // 检查长度
        if (trimmedUsername.length() < 3 || trimmedUsername.length() > 20) {
            throw new BusinessException("用户名长度必须在 3-20 个字符之间");
        }

        // 白名单规则已足够安全，无需额外的敏感字符检测
        if (!USERNAME_PATTERN.matcher(trimmedUsername).matches()) {
            throw new BusinessException("用户名只能包含字母、数字和下划线，且必须以字母开头");
        }
    }

    /**
     * 检查用户名是否合法（返回布尔值）
     *
     * @param username 用户名
     * @return true-合法，false-非法
     */
    public static boolean isValid(String username) {
        try {
            validate(username);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }
}
