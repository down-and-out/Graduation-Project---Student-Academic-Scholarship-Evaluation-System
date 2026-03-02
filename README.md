# 研究生学业奖学金评定系统

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.0-cyan.svg)](https://vuejs.org/)

一个基于 Web 的研究生学业奖学金评定系统，实现了奖学金评定全过程的信息化管理。系统采用前后端分离架构，支持研究生、导师和管理员三种角色，提供用户权限管理、学生信息管理、科研成果管理、评分规则配置、奖学金申请与评审、结果公示与异议处理等功能。

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [功能模块](#功能模块)
- [API 接口](#api 接口)
- [数据库设计](#数据库设计)
- [安全特性](#安全特性)
- [开发规范](#开发规范)
- [测试指南](#测试指南)
- [常见问题](#常见问题)

## 功能特性

- **完整的用户权限管理**：基于 RBAC 模型，支持研究生、导师和管理员三种角色
- **科研成果管理**：支持论文、专利、项目和学科竞赛四种成果类型
- **灵活的评分规则配置**：支持多级规则分类和自定义评分权重
- **全流程评定管理**：从申请提交、导师审核到院系评审的完整流程
- **结果公示与异议处理**：支持结果查询、公示和异议申请
- **操作日志记录**：完整的操作日志追踪
- **安全可靠**：JWT 认证、登录限流、Token 黑名单、RSA 密码加密

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| **框架** | Spring Boot 3.2.0 | Web 应用框架 |
| **JDK** | 17 | Java 运行环境 |
| **ORM** | MyBatis-Plus 3.5.7 | 数据持久层 |
| **数据库** | MySQL 8.0+ | 关系型数据库 |
| **缓存** | Redis 7.x | 缓存/会话管理 |
| **连接池** | Druid 1.2.23 | 数据库连接池 |
| **认证** | Spring Security + JWT | 安全认证 |
| **API 文档** | Knife4j 4.5.0 | OpenAPI/Swagger UI |
| **数据库迁移** | Flyway | 数据库版本管理 |
| **工具库** | Hutool 5.8.29 | Java 工具类 |
| **JSON** | FastJSON2 2.0.50 | JSON 处理 |
| **Lombok** | - | 代码简化 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| **框架** | Vue 3.4.0 | 前端框架 |
| **构建工具** | Vite 5.0.0 | 开发服务器/打包 |
| **UI 组件** | Element Plus 2.5.0 | UI 组件库 |
| **状态管理** | Pinia 2.1.7 | 全局状态管理 |
| **路由** | Vue Router 4.2.5 | 前端路由 |
| **HTTP 客户端** | Axios 1.6.2 | HTTP 请求 |
| **加密** | jsencrypt 3.3.2 | RSA 密码加密 |
| **CSS 预处理器** | Sass 1.69.5 | SCSS 样式 |
| **TypeScript** | 5.4.0 | 类型系统 |

## 项目结构

```
bishe_project/
├── scholarship-admin/              # 后端项目 (Spring Boot)
│   ├── src/main/java/com/scholarship/
│   │   ├── controller/             # 控制层 (11 个 Controller)
│   │   ├── service/                # 业务逻辑层
│   │   ├── mapper/                 # 数据访问层 (MyBatis)
│   │   ├── entity/                 # 实体类 (20 个)
│   │   ├── dto/                    # 数据传输对象
│   │   ├── vo/                     # 视图对象
│   │   ├── config/                 # 配置类
│   │   ├── common/                 # 公共模块
│   │   └── security/               # 安全模块
│   ├── src/main/resources/
│   │   ├── mapper/                 # MyBatis XML 映射文件
│   │   ├── application.yml         # 主配置文件
│   │   ├── application-dev.yml     # 开发环境配置
│   │   ├── application-test.yml    # 测试环境配置
│   │   └── application-prod.yml    # 生产环境配置
│   └── pom.xml                     # Maven 配置
│
├── scholarship-web/                # 前端项目 (Vue 3)
│   ├── src/
│   │   ├── api/                    # API 接口封装 (11 个模块)
│   │   ├── assets/                 # 静态资源
│   │   ├── components/             # 公共组件
│   │   ├── views/                  # 页面视图
│   │   │   ├── layout/             # 布局组件
│   │   │   ├── student/            # 研究生端页面
│   │   │   ├── tutor/              # 导师端页面
│   │   │   └── admin/              # 管理员端页面
│   │   ├── router/                 # Vue Router 路由配置
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── utils/                  # 工具函数
│   │   ├── App.vue                 # 根组件
│   │   └── main.js                 # 应用入口
│   ├── package.json                # NPM 依赖配置
│   └── vite.config.js              # Vite 构建配置
│
├── docs/
│   └── sql/                        # 数据库脚本
│       ├── scholarship.sql         # 主数据库脚本
│       ├── schema_index.sql        # 索引创建脚本
│       └── init_test_data.sql      # 测试数据脚本
│
├── TEST_GUIDE.md                   # 测试指南
└── README.md                       # 项目文档
```

## 快速开始

### 环境要求

| 组件 | 版本要求 | 必需 |
|------|---------|------|
| JDK | 17+ | ✅ |
| Node.js | 18+ | ✅ |
| MySQL | 8.0+ | ✅ |
| Redis | 7.x | ✅ |
| Maven | 3.8+ | ✅ |

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS scholarship DEFAULT CHARACTER SET utf8mb4;"

# 导入数据库脚本
mysql -u root -p scholarship < docs/sql/scholarship.sql
mysql -u root -p scholarship < docs/sql/schema_index.sql
```

### 2. 后端启动

```bash
# 修改数据库配置（可选）
# 编辑 scholarship-admin/src/main/resources/application-dev.yml

# 启动后端服务
cd scholarship-admin
mvn spring-boot:run
```

启动成功后访问：
- API 服务：http://localhost:8080/api
- API 文档：http://localhost:8080/api/doc.html
- Druid 监控：http://localhost:8080/druid/index.html

### 3. 前端启动

```bash
# 安装依赖
cd scholarship-web
npm install

# 启动开发服务器
npm run dev
```

启动成功后访问：http://localhost:3000

### 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | 123456 | 系统管理员 |

## 功能模块

### 1. 用户与权限管理

- 用户登录/登出（JWT 认证）
- 角色权限管理（研究生/导师/管理员）
- 用户信息管理
- 操作日志记录

### 2. 研究生信息管理

- 个人信息维护
- 学籍信息管理
- 学生档案查询

### 3. 科研成果管理

- **论文管理**：期刊论文、会议论文录入与审核
- **专利管理**：发明专利、实用新型专利录入与审核
- **项目管理**：科研项目参与情况记录
- **竞赛获奖**：学科竞赛获奖情况录入与审核
- 成果审核流程：导师在线审核学生提交的成果

### 4. 评分规则管理

- 规则分类管理（学业成绩、科研成果、综合素质等）
- 评分规则配置（分值计算方式）
- 规则权重设置

### 5. 奖学金评定

- 申请批次管理（按学年/学期创建评定批次）
- 学生在线申请
- 导师审核
- 院系评审
- 结果计算与排名

### 6. 结果管理

- 评定结果查询
- 结果公示
- 异议处理
- 证书导出

## API 接口

### 认证接口

| 接口 | 方法 | 描述 |
|------|------|------|
| `/auth/login` | POST | 用户登录 |
| `/auth/logout` | POST | 用户登出 |
| `/auth/current-user` | GET | 获取当前用户信息 |

### 业务接口

| Controller | 功能描述 |
|------------|----------|
| `StudentInfoController` | 学生信息管理 |
| `ResearchPaperController` | 论文成果管理 |
| `ResearchPatentController` | 专利成果管理 |
| `ResearchProjectController` | 项目管理 |
| `CompetitionAwardController` | 竞赛获奖管理 |
| `EvaluationBatchController` | 申请批次管理 |
| `ScoreRuleController` | 评分规则管理 |
| `ScholarshipApplicationController` | 奖学金申请管理 |
| `EvaluationResultController` | 评定结果管理 |
| `ReviewRecordController` | 审核记录管理 |
| `ResultAppealController` | 结果异议管理 |

详细 API 文档请访问：http://localhost:8080/api/doc.html

## 数据库设计

系统共设计了 **20 张核心数据表**：

### 用户权限表（5 张）
- `sys_user` - 系统用户表
- `sys_role` - 角色表
- `sys_permission` - 权限表
- `sys_user_role` - 用户角色关联表
- `sys_role_permission` - 角色权限关联表

### 学生信息表（1 张）
- `student_info` - 学生信息表

### 科研成果表（4 张）
- `research_paper` - 论文表
- `research_patent` - 专利表
- `research_project` - 项目表
- `competition_award` - 竞赛获奖表

### 评定管理表（5 张）
- `evaluation_batch` - 评定批次表
- `scholarship_application` - 奖学金申请表
- `application_achievement` - 申请成果表
- `review_record` - 审核记录表
- `evaluation_result` - 评定结果表

### 规则配置表（2 张）
- `rule_category` - 规则分类表
- `score_rule` - 评分规则表

### 系统辅助表（3 张）
- `result_appeal` - 结果异议表
- `sys_notification` - 系统通知表
- `sys_operation_log` - 操作日志表

## 安全特性

### 1. JWT 认证
- Token 有效期 2 小时
- 支持 Token 黑名单（登出后失效）

### 2. 登录限流
- 5 次失败后锁定账户 30 分钟
- 基于 IP 和用户名的组合限制

### 3. RSA 加密
- 前端使用 RSA 公钥加密密码
- 后端使用私钥解密

### 4. 用户名验证
- 防止 SQL 注入
- 只能包含字母、数字和下划线，且必须以字母开头

### 5. Spring Security
- 基于角色的访问控制（RBAC）
- 接口级别的权限校验

## 开发规范

### 后端开发

1. 统一使用 `Result<T>` 封装接口返回值
2. 使用 `BusinessException` 抛出业务异常
3. Service 层使用 `@Transactional` 控制事务
4. 使用 Knife4j 注解编写 API 文档

### 前端开发

1. 使用 Composition API 编写组件
2. 使用 Pinia 进行状态管理
3. 使用 Element Plus 组件库
4. 遵循 Vue 3 官方风格指南

### 代码检查

```bash
# 后端代码检查
cd scholarship-admin
mvn checkstyle:check

# 前端代码检查
cd scholarship-web
npm run lint
npm run format:check
```

## 测试指南

详细测试指南请参考 [TEST_GUIDE.md](TEST_GUIDE.md)

### 快速测试

```bash
# 测试登录限流
for i in {1..6}; do
  curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"wrong"}'
done

# 测试 Token 注销
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' | jq -r '.data.token')

# 使用 Token 访问
curl -X GET http://localhost:8080/api/auth/current-user \
  -H "Authorization: Bearer $TOKEN"

# 登出
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"

# 再次访问（应失败）
curl -X GET http://localhost:8080/api/auth/current-user \
  -H "Authorization: Bearer $TOKEN"
```

## 常见问题

### Q: 登录限流不生效？

1. 检查 Redis 是否启动：`redis-cli ping` 应返回 `PONG`
2. 检查 Redis 配置是否正确
3. 查看后端日志是否有相关错误

### Q: Token 注销后仍可访问？

1. 检查 Redis 中 Token 黑名单是否写入
2. 确认 `JwtAuthenticationFilter` 是否正确配置

### Q: 前端密码未加密？

1. 检查 `package.json` 中是否安装 `jsencrypt`
2. 检查登录组件中是否正确导入加密函数

### Q: 数据库连接失败？

1. 确认 MySQL 服务已启动
2. 检查 `application-dev.yml` 中的数据库配置
3. 确认数据库 `scholarship` 已创建

## 更新日志

### v1.0.0
- 初始版本发布
- 实现完整的奖学金评定流程
- 支持三种用户角色
- 提供完善的 API 文档

## License

MIT License
