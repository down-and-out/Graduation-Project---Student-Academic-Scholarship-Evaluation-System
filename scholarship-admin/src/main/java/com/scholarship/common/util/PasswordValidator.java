package com.scholarship.common.util;

import com.scholarship.common.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * 密码验证工具类
 * <p>
 * 用于验证密码的合法性，确保密码符合安全要求
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public class PasswordValidator {

    /**
     * 密码正则表达式：
     * - 长度至少 10 个字符
     * - 必须包含数字
     * - 必须包含字母（大小写均可）
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9]{10,}$");

    /**
     * 最小密码长度
     */
    private static final int MIN_PASSWORD_LENGTH = 10;

    /**
     * 验证密码是否合法
     *
     * @param password 密码
     * @throws BusinessException 密码不合法时抛出
     */
    public static void validate(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }

        String trimmedPassword = password.trim();

        // 使用正则表达式检查密码合法性（长度、数字、字母）
        if (!PASSWORD_PATTERN.matcher(trimmedPassword).matches()) {
            throw new BusinessException("密码长度必须至少为 " + MIN_PASSWORD_LENGTH + " 个字符，且必须包含数字和字母");
        }
    }

    /**
     * 检查密码是否合法（返回布尔值）
     *
     * @param password 密码
     * @return true-合法，false-非法
     */
    public static boolean isValid(String password) {
        try {
            validate(password);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }
}
