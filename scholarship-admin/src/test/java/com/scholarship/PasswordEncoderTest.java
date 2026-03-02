package com.scholarship;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码编码器测试类
 */
public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后密码: " + encodedPassword);
        System.out.println("密码长度: " + encodedPassword.length());
        
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("新密码验证结果: " + matches);
        
        String dbPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
        boolean dbMatches = encoder.matches(rawPassword, dbPassword);
        System.out.println("数据库密码验证结果: " + dbMatches);
    }
}
