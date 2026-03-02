package com.scholarship;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * BCrypt 密码生成器
 * <p>
 * 用于生成符合 BCrypt 标准的密码哈希值
 * 可直接用于数据库中密码字段的更新
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public class BCryptGenerator {

    /**
     * BCrypt 密码编码器
     */
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 生成 BCrypt 密码哈希
     *
     * @param rawPassword 原始密码
     * @return BCrypt 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码是否匹配
     *
     * @param rawPassword 原始密码
     * @param encodedPassword BCrypt 加密后的密码
     * @return true-匹配，false-不匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    /**
     * 主方法 - 生成测试密码的 BCrypt 哈希值
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 生成符合新规则的密码：a123456789
        // 规则：字母 + 数字，长度至少 10 位
        String[] testPasswords = {
            "a123456789",  // 默认密码
            "A123456789",  // 大写字母开头
            "Password@123", // 复杂密码（仅供参考）
            "test1234567",  // 测试密码
            "admin12345"    // 管理员密码
        };

        System.out.println("========================================");
        System.out.println("BCrypt 密码生成器");
        System.out.println("========================================\n");

        for (String password : testPasswords) {
            String encoded = encode(password);
            boolean verified = matches(password, encoded);

            System.out.println("原始密码：" + password);
            System.out.println("BCrypt 哈希：" + encoded);
            System.out.println("长度：" + encoded.length());
            System.out.println("验证结果：" + verified);
            System.out.println("----------------------------------------");
        }

        // 生成默认密码的 BCrypt 哈希（用于数据库更新）
        String defaultPassword = "a123456789";
        String defaultEncoded = encode(defaultPassword);

        System.out.println("\n========================================");
        System.out.println("默认密码 BCrypt 哈希（可直接用于 SQL 更新）");
        System.out.println("========================================");
        System.out.println("原始密码：" + defaultPassword);
        System.out.println("BCrypt 哈希：" + defaultEncoded);
        System.out.println("\nSQL 更新语句：");
        System.out.println("UPDATE sys_user SET password = '" + defaultEncoded + "' WHERE deleted = 0;");
    }
}
