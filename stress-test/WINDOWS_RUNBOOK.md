# Windows 压测执行清单

这份清单按你当前项目路径编写：

- 项目根目录：`D:\learning\bishe_project`
- 压测目录：`D:\learning\bishe_project\stress-test`

适合在 Windows 的 PowerShell 中直接执行。

## 0. 前置准备

先确认这些工具可用：

- `mysql`
- `jmeter`
- `curl`
- `bash`
- `java`（JDK 17+）
- `python3`（可选，缺失时报告会降级）
- `redis-cli`（可选，但建议有）

在 PowerShell 里可以这样检查：

```powershell
mysql --version
jmeter --version
bash --version
java -version
python3 --version
redis-cli --version
```

如果 `jmeter` 没配到环境变量，也可以后面改用它的绝对路径。

建议环境变量准备方式：

```powershell
$env:BASE_URL="http://localhost:8080/api"
$env:JMETER_BIN="D:\learning\apache-jmeter-5.6.3\bin\jmeter.bat"
# 如果数据库或 Redis 需要密码，再补这些
# $env:MYSQL_PASSWORD="your_mysql_password"
# $env:REDIS_PASSWORD="your_redis_password"
```

### 启动后端（推荐使用压测专用 profile）

压测时建议使用 `stress` profile 启动后端，它会自动关闭限流、放宽登录限制、扩大线程池：

```powershell
$env:SPRING_PROFILES_ACTIVE="stress"
cd scholarship-admin
mvn spring-boot:run
```

如果不需要 stress profile（保持 dev 默认配置），正常启动即可：

```powershell
cd scholarship-admin
mvn spring-boot:run
```

> **说明**：`application-stress.yml` 继承 `application-dev.yml`，仅覆盖压测相关配置：
> - `rate-limit.enabled: false` — 关闭接口限流
> - `security.login.max-attempts: 99999` — 放宽登录锁定阈值
> - 线程池（evaluation/export/batch-import）适度扩大

## 1. 进入项目目录

```powershell
Set-Location D:\learning\bishe_project
```

## 2. 准备压测数据

执行前会提示输入 MySQL 密码：

```powershell
mysql -u root -p < stress-test\scripts\prepare-data.sql
```

这一步会创建压测学生、压测批次和对应测试数据。

## 3. 生成管理员 token

```powershell
bash stress-test/scripts/generate-admin-tokens.sh
```

生成后检查文件是否存在：

```powershell
Get-ChildItem stress-test\data\tokens-admin.csv
```

## 4. 生成学生 token

```powershell
bash stress-test/scripts/generate-student-tokens.sh
```

生成后检查文件是否存在：

```powershell
Get-ChildItem stress-test\data\tokens-student.csv
```

## 5. 运行前校验

```powershell
bash stress-test/scripts/preflight-check.sh
```

preflight-check 使用三级体系（**CRITICAL** / **WARN** / **PASS**）：

- **CRITICAL**（阻断执行）：JMeter 不存在、Token CSV 为空、Token CSV 文件缺失
- **WARN**（仅提示，不阻断）：`/actuator/health` 不可达、个别 metric 不可用、Druid 不可达、批次解析失败
- 只有 CRITICAL 级别的问题才会阻止后续压测

如果这一步报告 CRITICAL 问题，先修复后再跑压测：

- token 文件缺失 → 重新执行步骤 3、4
- JMeter 命令不可用 → 检查环境变量或使用绝对路径

## 6. 执行完整压测

```powershell
bash stress-test/scripts/run-benchmark.sh
```

执行完成后，结果会生成到：

```text
stress-test\results\<时间戳>\
```

重点先看：

- `summary-report.md`
- `summary-report.json`
- `slow-sql.json`（统合报告）
- `slow-sql_phase*.json`（每个 Phase 独立采集）
- `monitor_*.csv`
- `manifest.csv`

> **Phase 6/7 说明**：Phase 6（混合流量）失败不会阻断 Phase 7（稳定性测试），两者均可独立执行。
> JMeter 默认堆内存已设为 `-Xms512m -Xmx2g`，避免高并发时 JVM segfault。

如果你只想跑某几个阶段，可以在 PowerShell 里先设置环境变量：

```powershell
$env:ONLY_PHASES="phase5_result_export"
bash stress-test/scripts/run-benchmark.sh
```

如果你想跳过高风险阶段，例如混合流量：

```powershell
$env:SKIP_PHASES="phase6_mixed_workload,phase7_stability_mixed"
bash stress-test/scripts/run-benchmark.sh
```

## 7. 打开结果目录

可以先列出最新结果目录：

```powershell
Get-ChildItem stress-test\results
```

如果你想直接看 Markdown 报告内容：

```powershell
Get-Content stress-test\results\<时间戳>\summary-report.md
```

## 8. 单独执行某个场景

### 8.1 单独跑申请提交

```powershell
jmeter -n -t stress-test/jmeter/application-submit-normal.jmx `
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-student.csv `
  -Jbatch_submit=9 `
  -l stress-test/results/submit-normal.jtl
```

### 8.1.1 单独跑冲突提交

```powershell
jmeter -n -t stress-test/jmeter/application-submit-conflict.jmx `
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-student.csv `
  -Jbatch_submit=9 `
  -l stress-test/results/submit-conflict.jtl
```

### 8.2 单独跑评定执行

```powershell
jmeter -n -t stress-test/jmeter/evaluation-execute.jmx `
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-admin.csv `
  -Jbatch_small=7 `
  -Jbatch_medium=8 `
  -Jbatch_large=9 `
  -l stress-test/results/evaluate.jtl
```

### 8.3 单独跑结果分页

```powershell
jmeter -n -t stress-test/jmeter/result-page.jmx `
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-admin.csv `
  -Jbatch_query=10 `
  -l stress-test/results/result-page.jtl
```

### 8.4 单独跑导出专项

```powershell
jmeter -n -t stress-test/jmeter/result-export.jmx `
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-admin.csv `
  -Jbatch_query=10 `
  -l stress-test/results/result-export.jtl
```

### 8.5 单独跑混合流量

```powershell
jmeter -n -t stress-test/jmeter/mixed-workload.jmx `
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-admin.csv `
  -Jstudent_token_csv=D:/learning/bishe_project/stress-test/data/tokens-student.csv `
  -Jbatch_submit=9 `
  -Jbatch_query=10 `
  -Jbatch_small=7 `
  -Jbatch_medium=8 `
  -Jrun_seconds=300 `
  -Jpage_threads=70 `
  -Jsubmit_threads=15 `
  -Jevaluate_threads=5 `
  -Jexport_threads=10 `
  -l stress-test/results/mixed-workload.jtl
```

## 9. 压测后清理 MySQL

```powershell
mysql -u root -p < stress-test\scripts\cleanup-data.sql
```

## 10. 压测后清理 Redis

```powershell
bash stress-test/scripts/cleanup-redis.sh
```

如果 Redis 有密码：

```powershell
$env:REDIS_PASSWORD="your_password"
bash stress-test/scripts/cleanup-redis.sh
```

## 11. 推荐完整执行顺序

按顺序执行：

```powershell
Set-Location D:\learning\bishe_project
mysql -u root -p < stress-test\scripts\prepare-data.sql
bash stress-test/scripts/generate-admin-tokens.sh
bash stress-test/scripts/generate-student-tokens.sh
bash stress-test/scripts/preflight-check.sh
bash stress-test/scripts/run-benchmark.sh
mysql -u root -p < stress-test\scripts\cleanup-data.sql
bash stress-test/scripts/cleanup-redis.sh
```

## 12. 常见问题

### `bash` 命令不可用

说明你本机没有可用的 Git Bash / WSL Bash。

可以先检查：

```powershell
bash --version
```

如果没有，需要先安装 Git for Windows 或 WSL。

### `jmeter` 命令不可用

用绝对路径代替，例如：

```powershell
C:\tools\apache-jmeter-5.6.3\bin\jmeter.bat --version
```

然后把上面的 `jmeter` 全部替换成完整路径。

### `preflight-check.sh` 失败

优先检查：

- CRITICAL 级别问题（阻止执行）：JMeter 不存在、Token CSV 为空
- WARN 级别问题（不阻止执行）：`/actuator/health` 不可达、Druid 不可达、批次不存在
- 如果用 stress profile 启动后端，`/actuator/health` 已放行匿名访问，无需 Token

### 登录限流问题

如果 Token 生成过程中遇到 429 限流：

```powershell
# 方案 A：使用 stress profile 重启后端（推荐，一劳永逸）
$env:SPRING_PROFILES_ACTIVE="stress"
cd scholarship-admin
mvn spring-boot:run

# 方案 B：手动设置环境变量（dev profile）
$env:RATE_LIMIT_ENABLED="false"
$env:LOGIN_MAX_ATTEMPTS="99999"
$env:LOGIN_IP_MAX_ATTEMPTS="99999"
bash stress-test/scripts/cleanup-redis.sh
```

### 不知道 `PT-*` 批次真实 ID

直接执行：

```powershell
bash stress-test/scripts/resolve-batch-ids.sh
```

## 13. 报告查看建议

压测结束后优先看：

1. `summary-report.md`
2. `slow-sql.json`（统合）以及各 Phase 独立报告 `slow-sql_phase*.json`
3. `monitor_*.csv`

如果 `summary-report.md` 里出现：

- `druid_wait_max > 0`
  先查数据库连接池和慢 SQL
- `exec_eval_queue_max` 高
  先查评定线程池
- `redis_rtt_ms_p95 > 5`
  先查 Redis
- `429%` 高
  先区分是限流保护，还是系统真扛不住
