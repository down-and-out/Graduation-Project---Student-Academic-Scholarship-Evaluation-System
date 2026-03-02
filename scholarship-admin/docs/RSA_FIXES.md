# RSA 登录问题修复完成总结

## 问题描述
用户登录时出现错误：
```
RSA 私钥未加载，请检查配置
RSA 解密失败，拒绝处理请求
```

登录日志显示：
```
用户登录：username=admin, passwordLength=344
RSA 解密开始，加密数据长度：344
RSA 私钥未加载，请检查配置
```

## 问题原因
1. **后端未配置 RSA 私钥**：后端的 `application-dev.yml` 中没有配置 `rsa.private-key`
2. **前后端密钥不配对**：前端有公钥配置，但后端私钥缺失或未正确配置

## 解决方案

### 1. 生成新的 RSA 密钥对
执行 `RsaKeyPairGenerator` 生成新的 2048 位 RSA 密钥对：
```bash
cd scholarship-admin
mvn exec:java -Dexec.mainClass="com.scholarship.common.util.RsaKeyPairGenerator"
```

### 2. 更新前端公钥配置
**文件**: `scholarship-web/.env`

```env
VITE_RSA_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7FT7XYFx5VrpxVI2efr8\nOjp8UdtIVYtDJWib9KMCV7ERNFpA8rt3TuF76IjhZy92/aiXpywJNSZsaMAqgQev\n8BwJYodJohxWIpDcR8UFgblRhEnvosFBNXkJ81oL49ZaUQz0laH7VolJOI1FVJoa\nOXljQWNIb6fUriQnS5JNFTacWmutc+YkZZrk07f53AyDdACW7fWXP7KUVf2YJ/Fq\nDMngfzdzcTJGFM11R6jYqpMqEaYxWY474PlhS2Blh87Di4VeHVBpT9vucNu04uMc\nY8B85fCu5F/bBdgx4XSbYcDUu74hU815OkGylq6XS0JvHr2aRyJnAoAY+PQ8mc5y\nGwIDAQAB\n-----END PUBLIC KEY-----
```

### 3. 更新后端私钥配置
**文件**: `scholarship-admin/src/main/resources/application-dev.yml`

```yaml
# RSA 配置 - 私钥（PEM 格式，使用 | 多行文本）
rsa:
  private-key: |
    -----BEGIN PRIVATE KEY-----
    MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDsVPtdgXHlWunF
    ...（完整私钥）...
    -----END PRIVATE KEY-----
```

### 4. 重启服务
- 后端服务：运行在 `http://localhost:8080`
- 前端服务：运行在 `http://localhost:3001`

## 验证结果

### 后端日志
```
2026-02-22T17:35:51.017+08:00  INFO 33464 --- [scholarship-admin] [main] com.scholarship.common.util.RsaUtils : RSA 私钥加载成功
2026-02-22T17:35:52.177+08:00  INFO 33464 --- [scholarship-admin] [main] com.scholarship.ScholarshipApplication : Started ScholarshipApplication in 4.282 seconds
```

### 服务状态
- ✅ 后端服务：运行中（PID 33464）
- ✅ 前端服务：运行中（PID 35600）

### 测试账号
- 用户名：`admin`
- 密码：`a123456789`

## 修改文件清单

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `scholarship-web/.env` | 更新 RSA 公钥 | ✅ |
| `scholarship-admin/src/main/resources/application-dev.yml` | 添加 RSA 私钥配置 | ✅ |
| `scholarship-admin/RSA_KEY_UPDATE.md` | 新增密钥更新说明 | ✅ |
| `scholarship-admin/RSA_FIXES.md` | 新增修复总结（本文档） | ✅ |

## 技术细节

### RSA 加密流程
1. 前端使用公钥加密密码（RSA 2048 位）
2. 加密后的密码通过 HTTPS 发送到后端
3. 后端使用私钥解密密码
4. 解密后的密码与数据库中的 BCrypt 哈希进行验证

### 密钥规格
- 密钥长度：2048 位
- 加密算法：RSA/ECB/PKCS1Padding
- 公钥格式：PEM (X.509)
- 私钥格式：PEM (PKCS#8)

### 安全建议
1. 生产环境应使用环境变量配置私钥
2. 定期更换密钥对（建议每 6-12 个月）
3. 私钥文件不应提交到版本控制系统
4. 建议使用 HTTPS 保护传输安全

## 完成时间
2026-02-22

## 验证人
系统自动验证
