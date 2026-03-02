package com.scholarship.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RsaUtils RSA 解密工具类测试
 */
@DisplayName("RsaUtils RSA 解密工具类测试")
class RsaUtilsTest {

    @AfterEach
    @DisplayName("清理测试私钥")
    void tearDown() {
        // 清理测试设置的私钥，避免影响其他测试
        ReflectionTestUtils.setField(RsaUtils.class, "privateKeyPem", null);
        ReflectionTestUtils.setField(RsaUtils.class, "privateKey", null);
    }

    @Test
    @DisplayName("测试 decryptPassword 方法 - 短密码直接返回")
    void testDecryptPasswordShortPassword() {
        // 短密码（长度<100）直接返回，不需要 RSA 解密
        assertEquals("password123", RsaUtils.decryptPassword("password123"));
        assertEquals("short", RsaUtils.decryptPassword("short"));
    }

    @Test
    @DisplayName("测试 decryptPassword 方法 - null 输入")
    void testDecryptPasswordNull() {
        assertNull(RsaUtils.decryptPassword(null));
    }

    @Test
    @DisplayName("测试 decryptPassword 方法 - 空字符串")
    void testDecryptPasswordEmpty() {
        assertEquals("", RsaUtils.decryptPassword(""));
    }

    @Test
    @DisplayName("测试 decrypt 方法 - null 输入")
    void testDecryptNull() {
        assertNull(RsaUtils.decrypt(null));
    }

    @Test
    @DisplayName("测试 decrypt 方法 - 空字符串")
    void testDecryptEmpty() {
        assertEquals("", RsaUtils.decrypt(""));
    }

    @Test
    @DisplayName("测试 decrypt 方法 - 边界长度数据（长度 99 视为明文）")
    void testBoundaryLengthData() {
        setupTestPrivateKey();

        // 长度 99 的字符串（短于 100，视为明文）
        String ninetyNineChars = "a".repeat(99);
        assertEquals(ninetyNineChars, RsaUtils.decrypt(ninetyNineChars));
    }

    @Test
    @DisplayName("测试 decrypt 方法 - 边界长度 100 抛出异常")
    void testBoundaryLength100ThrowsException() {
        setupTestPrivateKey();

        // 长度 100 的字符串会尝试 RSA 解密，但数据不是有效加密数据，应抛出异常
        String hundredChars = "a".repeat(100);
        assertThrows(IllegalArgumentException.class, () -> RsaUtils.decrypt(hundredChars));
    }

    @Test
    @DisplayName("测试 decrypt 方法 - 无效 Base64 数据抛出异常")
    void testDecryptInvalidBase64() {
        setupTestPrivateKey();

        // 长度>=100 但不是有效 Base64 的数据，会抛出异常
        String invalidBase64 = "a".repeat(150);  // 长度超过 100，会尝试 Base64 解码
        assertThrows(IllegalArgumentException.class, () -> RsaUtils.decrypt(invalidBase64));
    }

    @Test
    @DisplayName("测试解密失败抛出异常 - 密钥不匹配")
    void testDecryptWithWrongKey() {
        // 生成一个密钥对用于设置私钥
        setupTestPrivateKey();

        // 用另一个密钥对的公钥加密数据（密钥不匹配）
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair wrongKeyPair = keyGen.generateKeyPair();

            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, wrongKeyPair.getPublic());
            byte[] encryptedBytes = encryptCipher.doFinal("testPassword".getBytes());
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);

            // 密钥不匹配，解密应失败
            assertThrows(IllegalArgumentException.class, () -> RsaUtils.decrypt(encryptedBase64));
        } catch (Exception e) {
            fail("测试准备失败：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试完整加密解密流程")
    void testFullEncryptionDecryption() {
        try {
            // 生成测试密钥对
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            // 使用公钥加密
            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] encryptedBytes = encryptCipher.doFinal("testPassword123".getBytes());
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);

            // 设置私钥
            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                    "\n-----END PRIVATE KEY-----";
            ReflectionTestUtils.setField(RsaUtils.class, "privateKeyPem", privateKeyPem);

            // 解密验证
            String decrypted = RsaUtils.decryptPassword(encryptedBase64);
            assertEquals("testPassword123", decrypted);
        } catch (Exception e) {
            fail("加密解密测试失败：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试解密流程 - 中文密码")
    void testDecryptChinesePassword() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            encryptCipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] encryptedBytes = encryptCipher.doFinal("中文密码测试 123".getBytes());
            String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);

            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                    "\n-----END PRIVATE KEY-----";
            ReflectionTestUtils.setField(RsaUtils.class, "privateKeyPem", privateKeyPem);

            String decrypted = RsaUtils.decryptPassword(encryptedBase64);
            assertEquals("中文密码测试 123", decrypted);
        } catch (Exception e) {
            fail("中文密码解密测试失败：" + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试 isAvailable 方法 - 未设置私钥时返回 false")
    void testIsAvailableWithoutKey() {
        ReflectionTestUtils.setField(RsaUtils.class, "privateKey", null);
        ReflectionTestUtils.setField(RsaUtils.class, "privateKeyPem", null);
        assertFalse(RsaUtils.isAvailable());
    }

    @Test
    @DisplayName("测试 isAvailable 方法 - 设置私钥后返回 true")
    void testIsAvailableWithKey() {
        setupTestPrivateKey();
        // 需要手动触发私钥加载，因为测试中直接设置字段不会触发 @Value 注解
        setupTestPrivateKeyAndLoad();
        assertTrue(RsaUtils.isAvailable());
    }

    @Test
    @DisplayName("测试 RSA 私钥未加载时解密抛出异常")
    void testDecryptWithoutKeyThrowsException() {
        ReflectionTestUtils.setField(RsaUtils.class, "privateKey", null);
        ReflectionTestUtils.setField(RsaUtils.class, "privateKeyPem", null);

        // 长度>=100 的数据会尝试 RSA 解密，但未配置私钥
        String longData = "a".repeat(200);
        assertThrows(IllegalStateException.class, () -> RsaUtils.decrypt(longData));
    }

    /**
     * 设置测试用的私钥（仅设置 privateKeyPem 字段）
     */
    private void setupTestPrivateKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                    "\n-----END PRIVATE KEY-----";

            ReflectionTestUtils.setField(RsaUtils.class, "privateKeyPem", privateKeyPem);
        } catch (Exception e) {
            fail("生成测试密钥失败：" + e.getMessage());
        }
    }

    /**
     * 设置测试用的私钥并加载（同时设置 privateKeyPem 和 privateKey 字段）
     */
    private void setupTestPrivateKeyAndLoad() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded()) +
                    "\n-----END PRIVATE KEY-----";

            ReflectionTestUtils.setField(RsaUtils.class, "privateKeyPem", privateKeyPem);

            // 从 PEM 解析私钥
            String privateKeyContent = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
            java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            java.security.KeyFactory factory = java.security.KeyFactory.getInstance("RSA");
            java.security.PrivateKey privateKey = factory.generatePrivate(spec);

            ReflectionTestUtils.setField(RsaUtils.class, "privateKey", privateKey);
        } catch (Exception e) {
            fail("生成测试密钥失败：" + e.getMessage());
        }
    }
}
