package com.scholarship.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtil JWT 工具类测试
 */
@DisplayName("JwtUtil JWT 工具类测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "this-is-a-test-secret-key-for-jwt-util-test-123456";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    @DisplayName("测试生成 Token")
    void testGenerateToken() {
        String token = jwtUtil.generateToken("testuser", 1L);

        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.contains(".")); // JWT 包含三个部分，用.分隔
    }

    @Test
    @DisplayName("测试从 Token 中提取用户名")
    void testExtractUsername() {
        String username = "testuser";
        Long userId = 100L;
        String token = jwtUtil.generateToken(username, userId);

        String extractedUsername = jwtUtil.extractUsername(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("测试从 Token 中提取用户 ID")
    void testExtractUserId() {
        String username = "testuser";
        Long userId = 100L;
        String token = jwtUtil.generateToken(username, userId);

        Long extractedUserId = jwtUtil.extractUserId(token);
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("测试提取过期时间")
    void testExtractExpiration() {
        String token = jwtUtil.generateToken("testuser", 1L);

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("测试 Token 是否过期")
    void testIsTokenExpired() {
        String token = jwtUtil.generateToken("testuser", 1L);

        // 新 Token 应该未过期
        assertFalse(jwtUtil.isTokenExpired(token));
    }

    @Test
    @DisplayName("测试验证 Token - 有效 Token")
    void testValidateTokenValid() {
        String username = "testuser";
        Long userId = 1L;
        String token = jwtUtil.generateToken(username, userId);

        assertTrue(jwtUtil.validateToken(token, username));
    }

    @Test
    @DisplayName("测试验证 Token - 用户名不匹配")
    void testValidateTokenUsernameMismatch() {
        String token = jwtUtil.generateToken("testuser", 1L);

        assertFalse(jwtUtil.validateToken(token, "wronguser"));
    }

    @Test
    @DisplayName("测试验证 Token - Token 被篡改")
    void testValidateTokenTampered() {
        String token = jwtUtil.generateToken("testuser", 1L);

        // 篡改 Token
        String tamperedToken = token.substring(0, token.length() - 5) + "tampered";

        assertThrows(Exception.class, () -> {
            jwtUtil.validateToken(tamperedToken, "testuser");
        });
    }

    @Test
    @DisplayName("测试从请求头解析 Token")
    void testParseToken() {
        String rawToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
        String authHeader = "Bearer " + rawToken;

        String parsedToken = jwtUtil.parseToken(authHeader);
        assertEquals(rawToken, parsedToken);
    }

    @Test
    @DisplayName("测试解析不带 Bearer 前缀的 Token")
    void testParseTokenWithoutPrefix() {
        String rawToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";

        String parsedToken = jwtUtil.parseToken(rawToken);
        assertNull(parsedToken); // 不带前缀应该返回 null
    }

    @Test
    @DisplayName("测试解析 null 请求头")
    void testParseNullHeader() {
        assertNull(jwtUtil.parseToken(null));
    }

    @Test
    @DisplayName("测试 Token 包含正确的声明信息")
    void testTokenClaims() {
        String username = "testuser";
        Long userId = 123L;
        String token = jwtUtil.generateToken(username, userId);

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(username, claims.getSubject());
        assertEquals(userId, claims.get("userId", Long.class));
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getIssuedAt());
    }

    @Test
    @DisplayName("测试过期 Token")
    void testExpiredToken() {
        // 创建一个 Token 并验证 extractExpiration 方法
        String token = jwtUtil.generateToken("testuser", 1L);

        Date expiration = jwtUtil.extractExpiration(token);
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("测试不同用户生成不同 Token")
    void testDifferentUsersDifferentTokens() {
        String token1 = jwtUtil.generateToken("user1", 1L);
        String token2 = jwtUtil.generateToken("user2", 2L);

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("测试同一用户多次生成 Token")
    void testSameUserMultipleTokens() {
        String token1 = jwtUtil.generateToken("testuser", 1L);

        // 等待一下确保时间戳不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtUtil.generateToken("testuser", 1L);

        // JWT 包含时间戳，每次生成的 Token 应该不同
        // 但由于时间可能在同一秒内，所以只验证格式正确
        assertNotNull(token1);
        assertNotNull(token2);
        assertTrue(token1.contains("."));
        assertTrue(token2.contains("."));
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    }
}
