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
- `python3`
- `redis-cli`（可选，但建议有）

在 PowerShell 里可以这样检查：

```powershell
mysql --version
jmeter --version
python3 --version
redis-cli --version
```

如果 `jmeter` 没配到环境变量，也可以后面改用它的绝对路径。

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

如果这一步失败，先不要继续跑压测，先修复：

- token 文件缺失
- Actuator 指标不可访问
- Druid 不可访问
- `PT-*` 批次不存在

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
- `slow-sql.json`
- `monitor_*.csv`

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

- 后端服务是否已启动
- `/actuator/health` 是否可访问
- `/druid/datasource.json` 是否可访问
- token 文件是否真的生成成功

### 不知道 `PT-*` 批次真实 ID

直接执行：

```powershell
bash stress-test/scripts/resolve-batch-ids.sh
```

## 13. 报告查看建议

压测结束后优先看：

1. `summary-report.md`
2. `slow-sql.json`
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
