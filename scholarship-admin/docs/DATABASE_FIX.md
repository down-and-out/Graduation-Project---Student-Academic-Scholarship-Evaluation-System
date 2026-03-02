# sys_user 表缺失字段修复说明

## 问题描述
登录后端报错：
```
### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'version' in 'field list'
### SQL: SELECT id,username,password,real_name,user_type,email,phone,avatar,status,version,deleted,create_time,update_time,remark FROM sys_user WHERE deleted=0 AND (username = ?)
```

## 问题原因
数据库表 `sys_user` 缺少 `version` 字段（乐观锁版本号），这是 MyBatis-Plus 的乐观锁插件需要的字段。

## 解决方案

### 1. 执行 SQL 添加字段
```sql
-- 添加 version 字段（乐观锁版本号）
ALTER TABLE sys_user
ADD COLUMN version INT DEFAULT 1 NOT NULL COMMENT '乐观锁版本号' AFTER status;
```

### 2. 验证字段已添加
```sql
DESC sys_user;
```

输出应包含：
```
version    int    NO        1
```

## 执行结果

### 修复前表结构
```
id            bigint       PRI     auto_increment
username      varchar(50)  UNI
password      varchar(200)
real_name     varchar(50)
user_type     tinyint      MUL     1
email         varchar(100)
phone         varchar(20)
avatar        varchar(500)
status        tinyint      MUL     1
deleted       tinyint              0
create_time   datetime             CURRENT_TIMESTAMP
update_time   datetime             CURRENT_TIMESTAMP
remark        varchar(500)
```

### 修复后表结构
```
id            bigint       PRI     auto_increment
username      varchar(50)  UNI
password      varchar(200)
real_name     varchar(50)
user_type     tinyint      MUL     1
email         varchar(100)
phone         varchar(20)
avatar        varchar(500)
status        tinyint      MUL     1
version       int          NOT NULL    1    ← 新增字段
deleted       tinyint              0
create_time   datetime             CURRENT_TIMESTAMP
update_time   datetime             CURRENT_TIMESTAMP
remark        varchar(500)
```

## 验证步骤

### 1. 重启后端服务
```bash
cd scholarship-admin
mvn spring-boot:run
```

### 2. 检查日志
查看日志中是否显示：
```
Started ScholarshipApplication in X.XXX seconds
```

### 3. 测试登录
使用以下账号测试登录：
- 用户名：`admin`
- 密码：`a123456789`

## 相关文件

### SQL 脚本
- `scholarship-admin/src/main/resources/sql/fix_sys_user_table.sql`

### 实体类
- `scholarship-admin/src/main/java/com/scholarship/entity/SysUser.java`

### 配置文件
- `scholarship-admin/src/main/resources/application-dev.yml`

## 注意事项

1. **version 字段作用**: MyBatis-Plus 乐观锁插件需要此字段来实现乐观锁机制
2. **默认值**: version 字段默认值为 1，每次更新时自动递增
3. **其他表**: 如果其他表也使用了乐观锁插件，同样需要添加 version 字段

## 完成时间
2026-02-22
