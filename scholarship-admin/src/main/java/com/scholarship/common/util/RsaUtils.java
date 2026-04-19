package com.scholarship.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 解密工具类
 *
 * <p>用于解密前端 RSA 加密的密码</p>
 *
 * 注意：生产环境应避免记录敏感信息日志
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class RsaUtils {

    /**
     * 加密数据最小长度阈值（短于此长度的数据视为明文）
     * jsencrypt 加密后的数据长度通常在 344 字符左右（对应 2048 位密钥）
     */
    private static final int ENCRYPTED_DATA_MIN_LENGTH = 100;

    /**
     * RSA 私钥（从环境变量或配置文件读取）
     * 生产环境必须配置 RSA_PRIVATE_KEY 环境变量
     */
    private static String privateKeyPem;

    private static PrivateKey privateKey;

    /**
     * 设置私钥 PEM 字符串（通过 Spring 注入）
     * @param keyPem PEM 格式的私钥
     */
    @Value("${rsa.private-key:}")
    public void setPrivateKeyPem(String keyPem) {
        privateKeyPem = keyPem;
        if (privateKeyPem != null && !privateKeyPem.isEmpty() && !"${rsa.private-key}".equals(keyPem)) {
            try {
                privateKey = getPrivateKeyFromPEM(privateKeyPem);
                log.info("RSA 私钥加载成功");
            } catch (Exception e) {
                log.error("加载 RSA 私钥失败，RSA 解密功能将不可用", e);
            }
        } else {
            log.warn("未配置 RSA 私钥（rsa.private-key），请设置环境变量 RSA_PRIVATE_KEY");
        }
    }

    /**
     * 静态方法获取私钥 - 兼容旧代码调用
     */
    private static PrivateKey getPrivateKey() {
        if (privateKey == null && privateKeyPem != null && !privateKeyPem.isEmpty()) {
            try {
                privateKey = getPrivateKeyFromPEM(privateKeyPem);
            } catch (Exception e) {
                log.error("获取 RSA 私钥失败", e);
            }
        }
        return privateKey;
    }

    /**
     * 从 PEM 格式字符串获取私钥
     */
    private static PrivateKey getPrivateKeyFromPEM(String pem) throws Exception {
        // 移除 PEM 头尾和换行
        String privateKeyContent = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        // Base64 解码
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);

        // 生成私钥
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /**
     * RSA 解密
     *
     * @param encryptedData 加密的数据（Base64 字符串）
     * @return 解密后的明文
     * @throws IllegalArgumentException 当解密失败时抛出异常
     */
    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }

        // 尝试 Base64 解码来判断是否为加密数据
        // jsencrypt 加密后的数据是 Base64 格式，长度通常在 344 字符左右（对应2048位密钥）
        if (encryptedData.length() < ENCRYPTED_DATA_MIN_LENGTH) {
            // 太短，不可能是 RSA 加密的数据，直接返回
            log.debug("数据长度较短，视为明文：{}", encryptedData.length());
            return encryptedData;
        }

        PrivateKey key = getPrivateKey();
        if (key == null) {
            log.error("RSA 私钥未加载，请检查配置");
            throw new IllegalStateException("RSA 私钥未加载，请检查配置");
        }

        // 尝试 Base64 解码
        byte[] encryptedBytes;
        try {
            encryptedBytes = Base64.getDecoder().decode(encryptedData);
        } catch (Exception e) {
            log.error("加密数据 Base64 解码失败：{}", e.getMessage());
            throw new IllegalArgumentException("加密数据格式错误", e);
        }

        log.debug("RSA 解密开始，加密数据长度：{} 字节", encryptedBytes.length);

        // 输出调试信息：密钥相关信息
        if (key instanceof java.security.interfaces.RSAPrivateKey) {
            java.security.interfaces.RSAPrivateKey rsaPrivateKey = (java.security.interfaces.RSAPrivateKey) key;
            log.debug("RSA 密钥信息 - 模数位长: {} bits", rsaPrivateKey.getModulus().bitLength());

            // 验证密文长度与密钥长度的兼容性
            int keyLengthInBytes = rsaPrivateKey.getModulus().bitLength() / 8;
            log.debug("密钥模数长度: {} 字节, 加密数据长度: {} 字节", keyLengthInBytes, encryptedBytes.length);
        }

        // 使用标准的 PKCS1Padding 方式进行解密（与 jsencrypt 兼容）
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            String decrypted = new String(decryptedBytes);

            log.debug("RSA 解密成功，解密后长度：{}", decrypted.length());
            return decrypted;

        } catch (Exception e) {
            log.error("RSA 解密失败：{}", e.getMessage());
            // 注意：不能记录原始加密数据日志，避免泄露敏感信息

            // 严格模式：当解密失败时抛出异常，这样可以准确反映问题
            throw new IllegalArgumentException("RSA 解密失败：请检查加密数据是否正确（可能原因是前后端密钥不匹配）", e);
        }
    }


    /**
     * 解密密码
     *
     * @param encryptedPassword 加密的密码
     * @return 解密后的密码
     * @throws IllegalArgumentException 当解密失败时抛出异常
     */
    public static String decryptPassword(String encryptedPassword) {
        return decrypt(encryptedPassword);
    }

    /**
     * 检查 RSA 是否可用
     *
     * @return true-可用，false-不可用
     */
    public static boolean isAvailable() {
        return privateKey != null;
    }
}
