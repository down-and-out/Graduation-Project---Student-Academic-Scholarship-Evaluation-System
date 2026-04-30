# Stress Test Guide

这套压测方案面向当前奖学金评审系统的 4 类热点链路：

- 学生申请提交 `POST /application/submit`
- 管理员触发评定 `POST /evaluation-result/evaluate/{batchId}`
- 结果分页查询 `GET /evaluation-result/page`
- 结果导出 `GET /evaluation-result/export`

新版方案重点解决 5 个问题：

- 学生链路和管理员链路使用不同 token 数据源
- 批次 ID 自动解析，不再依赖硬编码默认值
- 单接口专项压测与混合流量压测分开执行
- 导出接口单独评估，不与核心吞吐结论混算
- 压测完成后自动汇总结果并给出瓶颈提示

## 目录结构

```text
stress-test/
  README.md
  data/
    tokens-admin.csv
    tokens-student.csv
  jmeter/
    application-submit-normal.jmx
    application-submit-conflict.jmx
    evaluation-execute.jmx
    mixed-workload.jmx
    result-export.jmx
    result-page.jmx
  results/
    <timestamp>/
  scripts/
    analyze-results.sh
    generate-admin-tokens.sh
    generate-student-tokens.sh
    generate-tokens.sh
    monitor.sh
    preflight-check.sh
    prepare-data.sql
    resolve-batch-ids.sh
    run-benchmark.sh
    slow-sql-monitor.sh
```

## 前置条件

- 后端服务已启动，默认地址为 `http://localhost:8080/api`
- MySQL 中已导入测试数据，且存在 `PT-SMALL`、`PT-MEDIUM`、`PT-LARGE`、`PT-QUERY`
- JMeter 已安装，且 `jmeter` 命令可执行
- `mysql`、`curl`、`bash` 可执行
- `python3` 可选。
  没有 `python3` 时，`analyze-results.sh` 会自动退化为 shell 简版报告，仍会生成 `summary-report.md` 和 `summary-report.json`
- `jq` 可选。
  有 `jq` 时，token 脚本优先用它解析登录返回 JSON
- `redis-cli` 可选。
  没有 `redis-cli` 时，监控脚本会把 `redis_rtt_ms` 记为 `NA`
- `jps`、`ps` 可选。
  缺失时 `monitor.sh` 会把 `cpu_pct`、`mem_pct` 记为 `NA`

## 第一步：准备测试数据

执行：

```bash
mysql -u root -p < stress-test/scripts/prepare-data.sql
```

这一步会准备：

- 压测学生账号 `pt_test_0000` 起的一批学生用户
- 评定批次 `PT-SMALL`、`PT-MEDIUM`、`PT-LARGE`、`PT-QUERY`
- 对应的申请、评定结果、课程成绩、德育数据

## 第二步：生成 token

管理员 token：

```bash
bash stress-test/scripts/generate-admin-tokens.sh
```

学生 token：

```bash
bash stress-test/scripts/generate-student-tokens.sh
```

默认输出文件：

- [tokens-admin.csv](/D:/learning/bishe_project/stress-test/data/tokens-admin.csv)
- [tokens-student.csv](/D:/learning/bishe_project/stress-test/data/tokens-student.csv)
- [tokens-admin-failures.csv](/D:/learning/bishe_project/stress-test/data/tokens-admin-failures.csv)
- [tokens-student-failures.csv](/D:/learning/bishe_project/stress-test/data/tokens-student-failures.csv)

说明：

- `application-submit-normal.jmx` 使用学生 token
- `application-submit-conflict.jmx` 使用学生 token
- `evaluation-execute.jmx`、`result-page.jmx`、`result-export.jmx` 使用管理员 token
- 冲突提交场景会在运行时自动抽取单学生 token，专门测试同一学生重复提交
- token 脚本现在默认基于脚本目录解析输出路径，不再依赖当前执行目录
- 如果登录失败，会把失败详情写入 `*-failures.csv`，便于区分 `401`、`429` 和响应结构异常

## 第三步：运行前校验

执行：

```bash
bash stress-test/scripts/preflight-check.sh
```

会检查：

- 管理员和学生 token 文件是否存在且不为空
- `jmeter` 是否可执行
- `curl` 是否可执行
- `/actuator/health` 是否可访问
- `/druid/datasource.json` 是否可访问
- 关键 Actuator 指标是否存在
- `PT-*` 批次 ID 是否能从数据库解析出来
- 检查结果会按 `PASS/FAIL` 汇总输出，而不是遇到第一项失败就中断

## 第四步：自动解析批次 ID

单独查看批次 ID 时可执行：

```bash
bash stress-test/scripts/resolve-batch-ids.sh
```

输出类似：

```bash
BATCH_SMALL=7
BATCH_MEDIUM=8
BATCH_LARGE=9
BATCH_QUERY=10
```

## 第五步：执行完整压测

执行：

```bash
bash stress-test/scripts/run-benchmark.sh
```

结果会输出到：

```text
stress-test/results/<YYYYMMDD_HHMMSS>/
```

脚本额外行为：

- 自动生成 `manifest.csv`，记录每个 phase 的开始时间、结束时间、状态、退出码和对应结果文件
- 通过 `trap` 自动清理后台 `monitor.sh` 进程，减少 phase 失败后的遗留监控进程
- 支持通过环境变量筛选场景：

```bash
ONLY_PHASES=phase5_result_export bash stress-test/scripts/run-benchmark.sh
SKIP_PHASES=phase6_mixed_workload,phase7_stability_mixed bash stress-test/scripts/run-benchmark.sh
```

## 当前场景编排

完整脚本按下面顺序执行：

1. `phase1_submit_normal`
2. `phase2_submit_conflict`
3. `phase3_evaluate`
4. `phase4_result_page`
5. `phase5_result_export`
6. `phase6_mixed_workload`
7. `phase7_stability_mixed`

### 申请提交场景

提交压测已经拆成两个独立计划：

- `application-submit-normal.jmx`
  目标是模拟不同学生对同一批次的正常并发提交
- `application-submit-conflict.jmx`
  目标是模拟同一学生对同一批次的重复提交，验证 Redis 锁、唯一约束和幂等响应

单独运行正常提交：

```bash
jmeter -n -t stress-test/jmeter/application-submit-normal.jmx \
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-student.csv \
  -Jbatch_submit=9 \
  -l stress-test/results/submit-normal.jtl
```

单独运行冲突提交：

```bash
jmeter -n -t stress-test/jmeter/application-submit-conflict.jmx \
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-student.csv \
  -Jbatch_submit=9 \
  -l stress-test/results/submit-conflict.jtl
```

### 评定执行场景

`evaluation-execute.jmx` 已拆成两类重点压力：

- `baseline-small-medium`
  小批次和中批次的基线触发
- `multi-batch-concurrency`
  小、中、大批次并发触发，观察异步线程池和数据库压力
- `same-batch-retrigger`
  同一大批次重复触发，观察任务复用和防重逻辑

单独运行：

```bash
jmeter -n -t stress-test/jmeter/evaluation-execute.jmx \
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-admin.csv \
  -Jbatch_small=7 -Jbatch_medium=8 -Jbatch_large=9 \
  -l stress-test/results/evaluate.jtl
```

### 结果分页场景

`result-page.jmx` 包含：

- `cache-hit`
- `cache-miss-random`
- `deep-page`

注意：

- 已修正为后端真实参数 `current/size`
- 默认使用 `PT-QUERY` 对应批次

单独运行：

```bash
jmeter -n -t stress-test/jmeter/result-page.jmx \
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-admin.csv \
  -Jbatch_query=10 \
  -l stress-test/results/result-page.jtl
```

### 导出专项场景

`result-export.jmx` 是独立专项，不参与核心吞吐排名。

包含：

- `single-export`
- `export-rate-limit`
- `export-concurrency`

单独运行：

```bash
jmeter -n -t stress-test/jmeter/result-export.jmx \
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-admin.csv \
  -Jbatch_query=10 \
  -l stress-test/results/result-export.jtl
```

### 混合流量场景

`mixed-workload.jmx` 用于逼近系统真实高峰，默认线程比例为：

- 分页查询 `70`
- 学生提交 `15`
- 评定触发 `5`
- 导出调用 `10`

默认执行 300 秒。

单独运行：

```bash
jmeter -n -t stress-test/jmeter/mixed-workload.jmx \
  -Jtoken_csv=D:/learning/bishe_project/stress-test/data/tokens-admin.csv \
  -Jstudent_token_csv=D:/learning/bishe_project/stress-test/data/tokens-student.csv \
  -Jbatch_submit=9 -Jbatch_query=10 -Jbatch_small=7 -Jbatch_medium=8 \
  -Jrun_seconds=300 \
  -Jpage_threads=70 -Jsubmit_threads=15 -Jevaluate_threads=5 -Jexport_threads=10 \
  -l stress-test/results/mixed-workload.jtl
```

## 监控与结果文件

每个 phase 会自动产生：

- `monitor_<phase>.csv`
- `<phase>_<scenario>.jtl`
- `logs/*.log`
- `manifest.csv`
- `slow-sql.json`
- `summary-report.md`
- `summary-report.json`

监控文件关键字段：

- `druid_active`
- `druid_wait`
- `redis_rtt_ms`
- `exec_eval_active`
- `exec_eval_queue`
- `exec_export_active`
- `exec_export_queue`
- `tomcat_busy`
- `cpu_pct`
- `mem_pct`
- `gc_pause_count`

压测结束后，`run-benchmark.sh` 会自动执行：

```bash
bash stress-test/scripts/analyze-results.sh stress-test/results/<timestamp>
```

它会把 `.jtl`、监控 CSV 和慢 SQL 自动汇总成：

- `summary-report.md`
- `summary-report.json`

补充说明：

- 有 `python3` 时，生成完整版汇总，包含 `RPS / Avg / P50 / P95 / P99 / Max`
- 没有 `python3` 时，自动退化为 shell 简版汇总，至少保留 `Samples / Error% / 429%`
- `monitor.sh` 中的 `NA` 值会被分析脚本安全忽略，不会导致报告生成失败

## 结果解读建议

### 先看单接口专项

优先判断：

- 正常提交是否稳定
- 冲突提交是否大量报错或锁等待飙升
- 同批次重复触发是否被正确复用
- 分页缓存命中和未命中 RT 差异是否明显
- 导出是否主要受限流影响

### 再看混合流量

混合流量更适合回答：

- 数据库连接池是否被查询和后台任务同时打满
- 评定线程池队列是否持续堆积
- 导出是否拖垮整体查询体验
- Redis RTT 是否在多链路同时运行时抖动

### 不要把导出和核心吞吐混为一谈

导出是同步重操作，适合单独做专项容量评估，不适合作为系统整体 QPS 指标代表。

### 结果汇总脚本会给出哪些判定

`analyze-results.sh` 会自动汇总：

- 每个 `.jtl` 的 `Samples / Error% / 429% / RPS / Avg / P50 / P95 / P99 / Max`
- 每个监控 CSV 的 `druid_active_max / druid_wait_max / redis_rtt_p95 / eval_queue_max / export_queue_max / tomcat_busy_max / cpu_p95 / mem_p95`
- 慢 SQL Top 10

并根据规则给出瓶颈提示，例如：

- `druid_wait_max > 0`
  更像数据库连接池或慢 SQL 瓶颈
- `exec_eval_queue_max` 高
  更像评定线程池堆积
- `exec_export_queue_max` 高
  更像导出专项排队
- `redis_rtt_ms_p95 > 5`
  更像 Redis 或锁热点问题
- `429%` 高
  更像限流保护生效，不能直接按“系统崩了”解读

## 常见问题

### 1. `preflight-check.sh` 报 metric unavailable

说明当前环境没有暴露对应 Actuator 指标，先确认：

- 应用是否使用了正确 profile
- 指标是否真的注册到了 `/actuator/metrics`

### 2. 学生 token 生成失败

先确认：

- `prepare-data.sql` 是否已经执行
- `pt_test_0000` 等学生账号是否真的存在
- 登录限流是否触发
- 查看 `stress-test/data/*-failures.csv` 中记录的 `http_code` 和返回体

### 3. 批次 ID 解析失败

先确认：

- `evaluation_batch` 中是否存在 `PT-SMALL`、`PT-MEDIUM`、`PT-LARGE`、`PT-QUERY`
- 当前连接的 MySQL 库是否是压测库

### 4. 混合流量场景中导出大量 429

这通常说明限流生效，不一定是系统崩溃。需要结合：

- `druid_wait`
- `tomcat_busy`
- `exec_export_queue`
- 整体错误率

一起判断。

### 5. `summary-report.md` 只有简版字段

这说明当前机器上没有可用的 `python3`，脚本已自动降级为 shell 汇总。

这不会阻止压测完成，但报告中不会包含：

- `P50 / P95 / P99`
- 监控 CSV 聚合
- 慢 SQL Top 10 结构化分析

## 建议执行顺序

推荐你每次都按这个顺序来：

1. 执行 `prepare-data.sql`
2. 生成管理员 token
3. 生成学生 token
4. 跑 `preflight-check.sh`
5. 先跑单接口专项
6. 再跑 `run-benchmark.sh` 看混合流量
7. 打开结果目录里的 `summary-report.md` 先看瓶颈提示

这样出来的结果最容易判断真实瓶颈。

## 压测后清理

如果你希望把压测留下的数据和缓存状态清掉，建议按下面顺序执行。

### 清理 MySQL 压测数据

执行：

```bash
mysql -u root -p < stress-test/scripts/cleanup-data.sql
```

对应文件：

- [cleanup-data.sql](/D:/learning/bishe_project/stress-test/scripts/cleanup-data.sql:1)

它会只清理压测范围内的数据，主要依据：

- 批次：`PT-SMALL`、`PT-MEDIUM`、`PT-LARGE`、`PT-QUERY`
- 学生/用户：`PT-*`、`pt_test_*`

不会按整表粗暴清空正常业务数据。

### 清理 Redis 压测遗留键

执行：

```bash
bash stress-test/scripts/cleanup-redis.sh
```

对应文件：

- [cleanup-redis.sh](/D:/learning/bishe_project/stress-test/scripts/cleanup-redis.sh:1)

默认会按模式清理这类键：

- `rate-limit:*`
- `login:attempt:*`
- `login:lock:*`
- `app:no:counter:*`
- `lock:application:*`
- `lock:evaluation:*`
- `scholarship:app:*`
- `scholarship:eval:*`
- `scholarship:batch:*`
- `scholarship:rule:*`
- `scholarship:sys-setting:*`
- `token:blacklist:*`

如果 Redis 有密码或不是默认库，可以这样执行：

```bash
REDIS_HOST=127.0.0.1 REDIS_PORT=6379 REDIS_PASSWORD=your_password REDIS_DB=0 \
bash stress-test/scripts/cleanup-redis.sh
```

### 推荐的压测后收尾顺序

建议每次压测后按这个顺序做：

1. 先保存 `results/<timestamp>/` 目录
2. 查看 `summary-report.md`
3. 执行 `cleanup-data.sql`
4. 执行 `cleanup-redis.sh`
5. 如有需要，再重新执行 `prepare-data.sql` 回到下一轮压测初始状态
