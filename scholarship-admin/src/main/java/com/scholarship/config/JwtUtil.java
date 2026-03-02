package com.scholarship.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT（JSON Web Token）工具类
 * <p>
 * 负责 JWT Token 的生成、解析和验证
 * JWT 用于前后端分离架构中的用户认证状态保持
 * </p>
 *
 * JWT 结构：Header.Payload.Signature
 * <ul>
 *   <li>Header: 算法和令牌类型</li>
 *   <li>Payload: 用户信息（用户 ID、用户名等）</li>
 *   <li>Signature: 签名（用于验证 Token 是否被篡改）</li>
 * </ul>
 *
 * 注意：生产环境必须配置 jwt.secret 和 jwt.expiration
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtUtil {

    /**
     * JWT 密钥（从配置文件中读取）
     * 默认值：仅用于开发环境，生产环境必须配置
     */
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentModeOnly32Chars}")
    private String secret;

    /**
     * Token 过期时间（从配置文件中读取，单位：毫秒）
     * 默认值：7 天（604800000 毫秒）
     */
    @Value("${jwt.expiration:604800000}")
    private Long expiration;

    /**
     * Token 前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 最小密钥长度要求（256 位 = 32 字节）
     */
    private static final int MIN_SECRET_LENGTH = 32;

    /**
     * 默认过期时间（7 天）
     */
    private static final long DEFAULT_EXPIRATION = 604800000L;

    /**
     * 初始化时验证配置
     */
    @PostConstruct
    public void init() {
        // 验证密钥强度
        if (secret.length() < MIN_SECRET_LENGTH) {
            log.warn("JWT 密钥长度不足 {} 字符，当前长度：{}。生产环境存在安全风险！",
                    MIN_SECRET_LENGTH, secret.length());
        }

        // 验证过期时间
        if (expiration <= 0) {
            log.warn("JWT 过期时间配置为无效值：{}，将使用默认值：{} 毫秒",
                    expiration, DEFAULT_EXPIRATION);
            expiration = DEFAULT_EXPIRATION;
        } else if (expiration > DEFAULT_EXPIRATION * 30) {
            log.warn("JWT 过期时间过长：{} 毫秒（约 {} 天），可能存在安全风险",
                    expiration, expiration / 86400000);
        }

        log.info("JWT 工具类初始化完成，密钥长度：{}，过期时间：{} 毫秒",
                secret.length(), expiration);
    }

    /**
     * 生成密钥
     * <p>
     * 使用配置的密钥字符串生成 SecretKey 对象
     * 密钥长度必须足够长以满足加密算法要求（HMAC-SHA 算法要求至少 256 位）
     * </p>
     *
     * @return SecretKey 对象
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 从 Token 中提取指定声明
     *
     * @param token           JWT Token
     * @param claimsResolver  声明解析函数
     * @return 声明的值
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 提取 Token 中的所有声明
     *
     * @param token JWT Token
     * @return Claims 对象
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取用户名
     *
     * @param token JWT Token
     * @return 用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从 Token 中提取用户 ID
     *
     * @param token JWT Token
     * @return 用户 ID
     */
    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从 Token 中提取过期时间
     *
     * @param token JWT Token
     * @return 过期时间
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 检查 Token 是否过期
     *
     * @param token JWT Token
     * @return true-已过期，false-未过期
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 生成 Token
     * <p>
     * 根据用户信息生成 JWT Token
     * Token 中包含：用户名（subject）、用户 ID、签发时间、过期时间
     * </p>
     *
     * @param username 用户名
     * @param userId   用户 ID
     * @return JWT Token
     */
    public String generateToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        return createToken(claims, username);
    }

    /**
     * 创建 Token
     * <p>
     * 使用 HS256 算法（HMAC-SHA256）对 Token 进行签名
     * </p>
     *
     * @param claims  声明信息
     * @param subject 主题（用户名）
     * @return JWT Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)           // 自定义声明
                .subject(subject)          // 主题（用户名）
                .issuedAt(now)             // 签发时间
                .expiration(expiryDate)    // 过期时间
                .signWith(getSigningKey()) // 签名
                .compact();
    }

    /**
     * 验证 Token 是否有效
     * <p>
     * 验证条件：
     * 1. Token 未过期（先检查，性能优化）
     * 2. Token 签名正确
     * 3. 用户名匹配
     * </p>
     *
     * @param token    JWT Token
     * @param username 用户名
     * @return true-有效，false-无效
     */
    public Boolean validateToken(String token, String username) {
        // 先检查过期时间（快速失败，避免不必要的解析操作）
        if (isTokenExpired(token)) {
            return false;
        }
        // 再验证用户名匹配
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username);
    }

    /**
     * 从请求头中解析 Token
     * <p>
     * 从 Authorization 请求头中提取 Token，并去掉"Bearer "前缀
     * </p>
     *
     * @param authHeader Authorization 请求头的值
     * @return 纯净的 Token（无前缀）
     */
    public String parseToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}
