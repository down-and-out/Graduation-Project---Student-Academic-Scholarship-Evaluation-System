# RSA 密钥对更新说明

## 问题原因
登录时出现错误：`RSA 私钥未加载，请检查配置`

原因是后端没有配置 RSA 私钥，导致无法解密前端加密的密码。

## 解决方案

### 1. 生成新的 RSA 密钥对
使用 `RsaKeyPairGenerator` 生成新的 2048 位 RSA 密钥对。

### 2. 配置公钥到前端
**文件**: `scholarship-web/.env`

```
VITE_RSA_PUBLIC_KEY=-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7FT7XYFx5VrpxVI2efr8
Ojp8UdtIVYtDJWib9KMCV7ERNFpA8rt3TuF76IjhZy92/aiXpywJNSZsaMAqgQev
8BwJYodJohxWIpDcR8UFgblRhEnvosFBNXkJ81oL49ZaUQz0laH7VolJOI1FVJoa
OXljQWNIb6fUriQnS5JNFTacWmutc+YkZZrk07f53AyDdACW7fWXP7KUVf2YJ/Fq
DMngfzdzcTJGFM11R6jYqpMqEaYxWY474PlhS2Blh87Di4VeHVBpT9vucNu04uMc
Y8B85fCu5F/bBdgx4XSbYcDUu74hU815OkGylq6XS0JvHr2aRyJnAoAY+PQ8mc5y
GwIDAQAB
-----END PUBLIC KEY-----
```

### 3. 配置私钥到后端
**文件**: `scholarship-admin/src/main/resources/application-dev.yml`

```yaml
rsa:
  private-key: |
    -----BEGIN PRIVATE KEY-----
    MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDsVPtdgXHlWunF
    UjZ5+vw6OnxR20hVi0MlaJv0owJXsRE0WkDyu3dO4XvoiOFnL3b9qJenLAk1Jmxo
    wCqBB6/wHAlih0miHFYikNxHxQWBuVGESe+iwUE1eQnzWgvj1lpRDPSVoftWiUk4
    jUVUmho5eWNBY0hvp9SuJCdLkk0VNpxaa61z5iRlmuTTt/ncDIN0AJbt9Zc/spRV
    /Zgn8WoMyeB/N3NxMkYUzXVHqNiqkyoRpjFZjjvg+WFLYGWHzsOLhV4dUGlP2+5w
    27Ti4xxjwHzl8K7kX9sF2DHhdJthwNS7viFTzXk6QbKWrpdLQm8evZpHImcCgBj4
    9DyZznIbAgMBAAECggEAYDgnUUYjE5DErdBPmrE7oQ9vzzn6xM1azK+/E0aM7RwL
    16dJWypFJIn8U0vrjYHPLe5GtErjAs8+gxV+GKODF7yqe4F+UrC7xv34mYXXPJ4x
    WK1rkfWhsX4ytW/7eEss4WPnbsaQ2IYzmAE4cX3+YFkSNqyP8NnBZxcUIpoRpSkb
    AcYwQS+lLT2NvqI5m8yRjpNaPn+Ow1/xwnS7Ypzzs97FVsX52rv+LsqOpUH6Xcm1
    fnKGjC1BefEBeKiIBK1bS8qh9sVrjpxz1owfGxDTTr8fFZmXZAcCAowke2DQXkL4
    DZVy1UC9PHi9sgB5LMXj+XxfJxOZSKxK83FlTREEmQKBgQD7AIE0rH0mbZx9f590
    +KEOp9d77SgeADdASwmJbKiHvxXXql6dx3B12iXXvyCaj7bdOocroAzHzj5+S2aj
    4z2eXxCI0WyeIMZE8c2vVg61KMhCetiMwo8PhLecL3bln/5MNzatGsbxwJS5FHTT
    7GRlQ2Xgj6R6XRU5jG7bmB8F+QKBgQDxCbIw+y6UFkuPqAbrur41X3tiFrkEHJ0z
    HvKtb+6/TiKgQ0AcfHptgrcbIRv1yQojl8jBVtvjSdcCKllMsiTkl82MnGWQJl47
    4GiXJsaX7PCaM3DaGM/0gBW4Cu1qn4drdL8Y0hjdMFPT27qxy9a8H/yRkx9NSrFo
    rEbRIrStswKBgQCpVxrUpba9kV/tz4eODBvk3lnj8wmIzA56ouVpQNSm2MVsvjWd
    byuxZx724qylemYd9VmylgVpW/0PieFSmoI88dge5mPIf0Ykx5pEO+QqVrxdFbI5
    rR2Fk72ocuFdZEbVtr59pT4pZgswntt/CtJk+0albWPDZj9mlxdHsebkSQKBgBer
    wBgXUsewONNuyN81g6ByNAe5+4lv1fBDRcnKiEO5RKVAdXdWsh83CwxbAAfvKlO8
    gDacdROpGLhZuFNT21OnpMP+R+sUGYT/0MWnRTF+T1KVYqvIbAOy4G8mg5JJAF+J
    I4sodtCiAll35qm3PutSqNcS/6vzfeCtqna3Vc8JAoGBAKl4PDs+4niwkfruMPyw
    9cLkc8JxwmHsL4FBm6ZNLRvMxOXbuZymfUX1z6vUuLgTf7tNDcHcaYGht1OefSAw
    oZI5VH3tEhoC1Z4FWOdPAMa+EmxNpjpQgwRebZ0A+KMSBfw0Ldde7FjJRZT1yaLr
    ZzcCnhtOliXRu5bJcYcb/+Ck
    -----END PRIVATE KEY-----
```

## 验证步骤

### 1. 重启后端服务
```bash
cd scholarship-admin
mvn spring-boot:run
```

### 2. 检查日志
查看日志中是否显示 `RSA 私钥加载成功`

### 3. 重启前端服务
```bash
cd scholarship-web
npm run dev
```

### 4. 测试登录
使用以下账号测试登录：
- 用户名：`admin`
- 密码：`a123456789`

## 密钥信息

### 密钥规格
- 密钥长度：2048 位
- 加密算法：RSA/ECB/PKCS1Padding
- 公钥格式：PEM (X.509)
- 私钥格式：PEM (PKCS#8)

### 公钥指纹 (SHA256)
用于验证公钥是否正确配置

### 私钥安全
- 私钥仅配置在开发环境
- 生产环境应使用环境变量配置
- 定期更换密钥对

## 修改文件清单

### 前端文件
- `scholarship-web/.env` - 更新 RSA 公钥

### 后端文件
- `scholarship-admin/src/main/resources/application-dev.yml` - 添加 RSA 私钥配置

## 注意事项

1. **前后端密钥配对**: 公钥和私钥必须是一对生成的，不能混用
2. **重启服务**: 修改配置后必须重启服务才能生效
3. **生产环境**: 生产环境应使用环境变量配置私钥，不要直接写在配置文件中
4. **密钥安全**: 私钥文件不应提交到版本控制系统

## 完成时间
2026-02-22
