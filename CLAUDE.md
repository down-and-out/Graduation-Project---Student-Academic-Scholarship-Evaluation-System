# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

研究生学业奖学金评定系统，前后端分离架构，支持研究生/导师/管理员三种角色。

## 常用命令

### 后端 (scholarship-admin/)

```bash
cd scholarship-admin

# 启动（开发环境，依赖 MySQL + Redis）
mvn spring-boot:run

# 指定环境启动
mvn spring-boot:run -Dspring.profiles.active=test

# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassName

# 编译
mvn compile

# 打包
mvn package -DskipTests
```

后端启动后：API 服务 `http://localhost:8080/api`，API 文档 `http://localhost:8080/api/doc.html`。

### 前端 (scholarship-web/)

```bash
cd scholarship-web

npm install
npm run dev          # 开发服务器 (localhost:3000)
npm run build        # 生产构建（含 vue-tsc 类型检查）
npm run lint         # ESLint 检查
npm run format       # Prettier 格式化
```

### 数据库

```bash
mysql -u root -p scholarship < docs/sql/scholarship.sql
mysql -u root -p scholarship < docs/sql/schema_index.sql
# 默认管理员：admin / 123456
```

## 高层架构

### 后端分层结构

```
controller/ → service/ (含 service/impl/) → mapper/ (+ XML 映射文件)
     ↕              ↕
  dto/param/query/vo/     entity/
```

- **统一响应**：所有 Controller 返回 `Result<T>`（`common/result/Result.java`）
- **异常处理**：业务异常抛出 `BusinessException`，全局异常处理器统一转换为 `Result`
- **DTO/VO 分离**：入参用 `dto/`（含 `param/`、`query/`），出参用 `vo/`
- **安全认证**：`SecurityConfig` 配置无状态 JWT 认证链，`JwtAuthenticationFilter` 前置，`EndpointRateLimitFilter` 后置
- **Redis 用途**：Token 黑名单、登录限流计数、Redisson 分布式锁、缓存

### 三角色 RBAC

- **student (1)**：科研成果管理、申请奖学金、查看评定结果
- **tutor (2)**：审核学生成果、查看指导学生
- **admin (3)**：用户管理、规则配置、批次管理、评定执行、结果管理

前端路由在 `router/index.ts` 中按 `role` meta 字段进行权限守卫，后端通过 `@PreAuthorize` 注解控制。

### 关键业务流程

1. **评定流程**：管理员创建评定批次 → 学生提交申请（含成果关联）→ 导师审核 → 管理员触发评定计算 → 结果公示 → 异议处理
2. **评定计算**：异步执行（`EvaluationTaskService`），按评分规则累加学业成绩、科研成果、综合素质等维度分数，生成排名
3. **成果管理**：论文、专利、项目、竞赛四类，需导师审核后方可关联到奖学金申请
4. **批量导入**：管理员通过 EasyExcel 批量导入学生信息（`BatchImportService`）

### 配置文件层级

- `application.yml`：公共配置（端口 8080、context-path `/api`、MyBatis-Plus、JWT）
- `application-dev.yml`：开发环境（用 `@ConfigurationProperties` 绑定到 `ScholarshipProperties`，评分/锁/线程池等业务配置）
- `application-stress.yml`：压测专用配置

### 前端项目结构

- `src/api/`：17 个 API 模块文件，对后端 API 的封装
- `src/views/`：按角色分目录 `student/`、`tutor/`、`admin/`，外加 `layout/` 和顶层页面
- `src/stores/`：Pinia 状态管理
- `src/utils/secureStorage.ts`：带加密前缀的 localStorage 封装，存储 token 和用户信息
- `vite.config.ts`：开发代理 `/api` → `localhost:8080`，生产构建 vendor chunk 分离

### 压测 (stress-test/)

JMeter 工具链，场景包括：申请提交（正常/冲突）、评定执行、结果分页、结果导出、混合负载。使用 `scripts/` 下的 shell 脚本进行 token 预生成、批次 ID 解析和结果分析。

## 开发约定

- 后端包路径：`com.scholarship`
- Mapper XML 位置：`resources/mapper/**/*Mapper.xml`
- MyBatis-Plus 逻辑删除字段：`deleted`（1=已删除，0=未删除）
- 主键策略：ASSIGN_ID（雪花算法）
- Jackson 序列化：非 null 字段不输出
- API 文档用 Knife4j 注解（`@Tag`、`@Operation`）
- 日期格式：`yyyy-MM-dd HH:mm:ss`，时区 GMT+8
- 前端用 Composition API + `<script setup>`
