package com.scholarship.common.util;

import java.security.*;
import java.util.Base64;

/**
 * RSA 密钥对生成工具
 *
 * <p>使用方法：</p>
 * <ol>
 *   <li>运行 main 方法生成密钥对</li>
 *   <li>将公钥配置到前端的 rsa.js 中</li>
 *   <li>将私钥配置到后端的 RsaUtils 类中</li>
 * </ol>
 *
 * <p>注意：</p>
 * <ul>
 *   <li>密钥长度：2048 位</li>
 *   <li>生产环境应定期更换密钥</li>
 *   <li>私钥必须保密，不能泄露</li>
 * </ul>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public class RsaKeyPairGenerator {

    /**
     * 密钥长度
     */
    private static final int KEY_SIZE = 2048;

    public static void main(String[] args) throws Exception {
        System.out.println("========================================");
        System.out.println("  RSA 密钥对生成工具");
        System.out.println("========================================\n");

        // 生成密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // 获取公钥（Base64 编码）
        String publicKeyBase64 = Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());

        // 获取私钥（Base64 编码）
        String privateKeyBase64 = Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());

        // 格式化输出
        String publicKeyPEM = formatKey(publicKeyBase64, "PUBLIC KEY");
        String privateKeyPEM = formatKey(privateKeyBase64, "PRIVATE KEY");

        System.out.println("===== 公钥（配置到前端 rsa.js） =====\n");
        System.out.println(publicKeyPEM);
        System.out.println("\n===== 私钥（配置到后端 RsaUtils） =====\n");
        System.out.println(privateKeyPEM);
        System.out.println("\n========================================");
        System.out.println("提示：");
        System.out.println("1. 将公钥复制到 src/utils/rsa.js 的 PUBLIC_KEY 常量");
        System.out.println("2. 将私钥配置到 RsaUtils 类的 PRIVATE_KEY 常量");
        System.out.println("3. 生产环境请定期更换密钥");
        System.out.println("========================================");
    }

    /**
     * 格式化 PEM 格式的密钥
     */
    private static String formatKey(String key, String type) {
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN ").append(type).append("-----\n");

        // 每行 64 个字符
        for (int i = 0; i < key.length(); i += 64) {
            int end = Math.min(i + 64, key.length());
            sb.append(key.substring(i, end)).append("\n");
        }

        sb.append("-----END ").append(type).append("-----");
        return sb.toString();
    }
}
