import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAKeyValidation {
    public static void main(String[] args) throws Exception {
        // Using the key pair we generated earlier
        String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2rpd6ZboaKJZwZJ/k5PQ\n" +
                "DqjYITIdCCoD4pu1l/T2hKuOa3WVf0tW2XJBy5ZSXWrG5oygg214yAiTS4AeBVb2\n" +
                "ZNlxKRo2bqmAtszudNAnlwX6atElEB/RZ7h5Y+g9DXTXWuckJNgBToSx444H4dO7\n" +
                "QwkG8sIDPxDEqww0RfMqQljPhMy0IB2DSNuSgrcDDmqqSK5ZXB7aDgExBmZ+K/pM\n" +
                "Q5ug6lChD4ptueUSrS6gAm1eL/2l6LjoRbgmimF7Ua64SB0fpbc5vcpvEd8fl8GQ\n" +
                "MRTZ2sj1H8Ulk4htyk+wqd/e3J1WMX365z/9Qd04GfxRP8t642jPwa3vHahVGn4N\n" +
                "kQIDAQAB\n" +
                "-----END PUBLIC KEY-----";

        String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDaul3pluhoolnB\n" +
                "kn+Tk9AOqNghMh0IKgPim7WX9PaEq45rdZV/S1bZckHLllJdasbmjKCDbXjICJNL\n" +
                "gB4FVvZk2XEpGjZuqYC2zO500CeXBfpq0SUQH9FnuHlj6D0NdNda5yQk2AFOhLHj\n" +
                "jgfh07tDCQbywgM/EMSrDDRF8ypCWM+EzLQgHYNI25KCtwMOaqpIrllcHtoOATEG\n" +
                "Zn4r+kxDm6DqUKEPim255RKtLqACbV4v/aXouOhFuCaKYXtRrrhIHR+ltzm9ym8R\n" +
                "3x+XwZAxFNnayPUfxSWTiG3KT7Cp397cnVYxffrnP/1B3TgZ/FE/y3rjaM/Bre8d\n" +
                "qFUafg2RAgMBAAECggEACe16XiQIVnKplw/ULiZmXVP49o2OV2GFnZ9EDCqeyc5g\n" +
                "Zd/UMkJNv3ITkTvSCNI6ehpl6rE16zZgbevZvccafEdWS7xLcWH/wpkYnA3nZ7Zq\n" +
                "Weppcte1RHtROMfaUllvfbCO3NE+qSN8UEzEOzhfmbi3xQSQHlusR+s2fPpL4u9Z\n" +
                "haF94wzQTps32dV7hQfPMARI1HWD4cTQIM2KWrsyNqdf4l6SERknS9VjE3JVV5kI\n" +
                "xjWHNjH4WyK8WZr3G2Cle/yvpUCd+rcNu2m7v8WdBii1HoHLyTJ6j14d9AbaMMfW\n" +
                "6ZPB2oHrly8xFlkyQuyr+Rgs5aggsZ54STXstJ0lMQKBgQDswUZj1G29BqV1pMaa\n" +
                "8475ZqTiHfzRWIThYFjm/jVyYvx4cmBrlbIe3+qHxdUByEw0iH/P0+rRQqO9f55i\n" +
                "BO1tIYKa5IddqmOJ88Cq4FzGFQoqGCClVcnrxnr5vST8KHf14mB+Pwspii/JxtMi\n" +
                "Q96X8mog+a9K8V9+0/yskU1eJQKBgQDsgfYh6cqCTWfnPu/Kf5vDNkBEHAo3lpkv\n" +
                "aJCC/+tcJ12eXG2dhNvwt84Oa7ZtpHPafZ9HNS7hIMwyajq3IA73wPvwHRiEVTO8\n" +
                "VfNKXWSvwn3UrgKH9CECvApCNYvvuwNhtC1d8KS5r/6AJljh/9ynD15Sk+bKqoun\n" +
                "clNHpxEH/QKBgQDUwcMNx0JukeBy3gaUDLe5LCKUCLq/LjsLhdeejWdeSdH0M2Su\n" +
                "ibGtyS8rn/6RQXwp4VhqXVgxgR9AFcUw9Jigb3K82w1CgPI+cEv9wu3cG+Asf84m\n" +
                "hfeU56JvdygbagqTbkGkcecIB2R2gTQPIMqR5Yi15Ws/f7V/deUk9tZ9+QKBgCwV\n" +
                "rYN0yMKxGwUHvjrIvogMvL31XBIwiC1GGGnSulRbIiA6qlQKR9T+7fHSMhcN6Gp5\n" +
                "sA7d4vcj3ewn0PoBh8i2uD9xrPg9yX75pnfeIL6CSmIybVZaMy0HUAI3aPYurdqw\n" +
                "cyRnjszt+Up522eFAeRCARrzZrmwQIfdz5gzlAEhAoGBAJlksPvckkSyoPeSSt2i\n" +
                "ajJbP/5kONDGTGLHFx3wr+e6FCf5r4AAoVdQ49lyOmEXHgbeeFfzziB3zhJQ759I\n" +
                "gnXN0lsi91RcsR1BXEO2D3lAc4UgQHA3s9N2tNVxCR7lNkp4XfJ7KyyHtiI4Xb0I\n" +
                "zyCCgZ8QLbiY34RuntcJjsXp\n" +
                "-----END PRIVATE KEY-----";

        // Clean PEM format
        String publicKeyContent = publicKeyPEM
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        String privateKeyContent = privateKeyPEM
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        // Decode keys
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyContent);
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyContent);

        // Generate keys
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

        // Test encryption/decryption
        String originalText = "test_password_123";
        System.out.println("Original Text: " + originalText);

        // Encrypt with public key
        Cipher encryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedData = encryptCipher.doFinal(originalText.getBytes("UTF-8"));
        String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedData);
        System.out.println("Encrypted (Base64): " + encryptedBase64);

        // Decrypt with private key
        Cipher decryptCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedData = decryptCipher.doFinal(encryptedData);
        String decryptedText = new String(decryptedData, "UTF-8");
        System.out.println("Decrypted: " + decryptedText);

        if (originalText.equals(decryptedText)) {
            System.out.println("\nSUCCESS: Key pair validation passed! Encryption and decryption work correctly.");
        } else {
            System.out.println("\nFAILURE: Key pair validation failed! Encryption and decryption do not work properly.");
        }
    }
}