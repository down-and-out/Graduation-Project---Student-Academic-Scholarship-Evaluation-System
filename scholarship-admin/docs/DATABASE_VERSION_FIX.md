# 数据库 version 字段批量修复说明

## 问题描述
使用导师账号登录，点击"科研成果审核"时，报错：
```
### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'version' in 'field list'
### SQL: SELECT id,student_id,paper_title,...,version,deleted,... FROM research_paper WHERE deleted=0 ORDER BY publication_date DESC LIMIT ?
### Cause: java.sql.SQLSyntaxErrorException: Unknown column 'version' in 'field list'
```

## 问题原因
MyBatis-Plus 的乐观锁插件（`OptimisticLockerInnerInterceptor`）需要所有实体表都有 `version` 字段，但数据库中只有部分表有此字段。

## 解决方案

### 执行 SQL 批量添加 version 字段

```sql
-- 为所有业务表添加 version 字段（乐观锁版本号）
ALTER TABLE research_paper ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER indexing;
ALTER TABLE research_patent ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER status;
ALTER TABLE research_project ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER status;
ALTER TABLE competition_award ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER status;
ALTER TABLE application_achievement ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER application_id;
ALTER TABLE scholarship_application ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER status;
ALTER TABLE evaluation_result ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER status;
ALTER TABLE result_appeal ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER status;
ALTER TABLE review_record ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER review_time;
ALTER TABLE student_info ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER status;
ALTER TABLE evaluation_batch ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER batch_year;
ALTER TABLE rule_category ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER sort_order;
ALTER TABLE score_rule ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER is_available;
ALTER TABLE sys_notification ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER is_read;
ALTER TABLE sys_operation_log ADD COLUMN version INT DEFAULT 1 NOT NULL AFTER status;
```

### 验证字段已添加

```sql
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'scholarship'
  AND COLUMN_NAME = 'version'
ORDER BY TABLE_NAME;
```

## 修复结果

### 已添加 version 字段的表（15 个）

| 序号 | 表名 | 说明 |
|------|------|------|
| 1 | application_achievement | 申请成果 |
| 2 | competition_award | 竞赛获奖 |
| 3 | evaluation_batch | 评审批次 |
| 4 | evaluation_result | 评审结果 |
| 5 | research_paper | 论文成果 |
| 6 | research_patent | 专利成果 |
| 7 | research_project | 科研项目 |
| 8 | result_appeal | 结果申诉 |
| 9 | review_record | 评审记录 |
| 10 | rule_category | 规则分类 |
| 11 | scholarship_application | 奖学金申请 |
| 12 | score_rule | 评分规则 |
| 13 | student_info | 学生信息 |
| 14 | sys_notification | 系统通知 |
| 15 | sys_operation_log | 操作日志 |
| 16 | sys_user | 系统用户（已修复） |

### version 字段说明

- **类型**: `INT`
- **默认值**: `1`
- **约束**: `NOT NULL`
- **作用**: MyBatis-Plus 乐观锁版本号，每次更新自动递增

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

### 3. 测试功能
1. 使用导师账号登录
2. 点击"科研成果审核"
3. 验证页面是否正常加载

## 相关文件

### SQL 脚本
- `scholarship-admin/src/main/resources/sql/add_version_columns.sql`

### 实体类
所有使用 `@Version` 注解的实体类：
- `ResearchPaper.java`
- `ResearchPatent.java`
- `ResearchProject.java`
- `CompetitionAward.java`
- `ScholarshipApplication.java`
- ...

### 配置文件
- `application-dev.yml` - MyBatis-Plus 插件配置

## 注意事项

1. **生产环境执行**: 在生产环境执行前，请先备份数据
2. **现有数据**: 现有数据的 version 字段默认值为 1，不影响业务
3. **乐观锁机制**: 更新时会自动比较 version，防止并发修改

## 完成时间
2026-02-22
