## 高并发优化清单 v1

### Summary
目标是优先提升 `申请提交`、`评定计算`、`结果查询/导出` 三类热点路径在高并发下的稳定性和吞吐，而不引入新的中间件复杂度。当前项目已经具备 Redis 锁、限流、异步线程池、唯一约束和索引基础，下一步应先做压测与定点优化，再评估是否需要消息队列。

### Key Changes
1. 压测先行，确认真实瓶颈
- 对 `POST /application/submit`、`POST /evaluation-result/evaluate/{batchId}`、`GET /evaluation-result/page`、`GET /evaluation-result/export` 做分场景压测。
- 记录 P95/P99、数据库慢 SQL、Redis RTT、线程池活跃数/队列长度、接口 429 比例。
- 先区分瓶颈属于数据库、Redis、JVM 线程池，还是单条业务链路中的同步计算。

2. 优先优化评定计算链路
- 重点检查 [EvaluationCalculationServiceImpl.java](/D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationCalculationServiceImpl.java:82) 的批量计算路径，确认批量预取是否已经覆盖所有子查询，避免隐藏的 N+1。
- 重点关注 `clearBatchResults -> 分页读取申请 -> 批量写结果 -> 排名 -> 分奖` 这条链路的总耗时和事务边界，避免大批次重算时长事务或大范围锁竞争。
- 评估 `readPageSize` 和 `writeBatchSize` 的最优值，当前配置在 [ScholarshipProperties.java](/D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/config/ScholarshipProperties.java:49)；优先通过压测调参，而不是改架构。
- 如果重算批次很大，优先把“清空旧结果后重写”改成更稳妥的分批覆盖/版本化写入方案，减少全表级抖动风险。

3. 优化查询和导出热点
- 检查 [EvaluationResultServiceImpl.java](/D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationResultServiceImpl.java:49) 的分页查询 SQL 执行计划，重点验证 `batchId`、`studentId`、`resultStatus`、`totalScore`、`studentNo/studentName like` 的组合过滤是否走到合适索引。
- `export` 当前仍是同步导出全量结果，见 [EvaluationResultController.java](/D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/controller/EvaluationResultController.java:235)。如果数据量上来，优先改成“创建导出任务 + 后台生成 + 前端轮询下载”，这比引入 MQ 更直接有效。
- 对“按学年/学期先查 batch，再查结果”的路径做专项压测，确认 `resolveBatchIds` 不会成为额外热点。

4. 收紧缓存与缓存失效策略
- 当前结果缓存失效采用 `SCAN + DELETE`，见 [CacheEvictionServiceImpl.java](/D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/CacheEvictionServiceImpl.java:31)。批次重算后会保守清很多分页缓存，这在并发高时可能放大 Redis 压力和缓存抖动。
- 优先改为更细粒度的 key 设计或版本号前缀方案，减少批量扫描删除。
- 重点验证高频查询接口的缓存命中率，不要只“有缓存”但命中低、失效重。

5. 巩固申请提交路径
- `submitApplication` 已有 Redis 锁 + 数据库唯一约束兜底，见 [ScholarshipApplicationServiceImpl.java](/D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/ScholarshipApplicationServiceImpl.java:122) 和 [add_application_concurrency_constraints.sql](/D:/learning/bishe_project/scholarship-admin/src/main/resources/sql/add_application_concurrency_constraints.sql:1)。
- 下一步不是换 MQ，而是验证锁 TTL、重复提交峰值下的数据库异常比例、以及申请附件/成果装配查询是否存在额外热点。
- 若提交洪峰集中在某几个时间点，优先通过限流、前端防抖、提交幂等令牌和业务排队提示处理。

6. 调整异步任务承载能力
- 当前评定、导出、导入线程池容量较小，见 [AsyncTaskConfig.java](/D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/config/AsyncTaskConfig.java:13)。优先补充线程池运行指标与拒绝策略告警。
- 根据压测结果调大评定任务线程池或隔离导出线程池，避免长任务互相挤占。
- 如果未来出现“任务大量堆积、需要跨实例稳定消费、需要失败重试和积压管理”，那时再考虑引入 MQ。

7. 数据库专项优化
- 验证现有并发索引脚本是否全部落库，尤其是 [add_concurrency_indexes.sql](/D:/learning/bishe_project/scholarship-admin/src/main/resources/sql/add_concurrency_indexes.sql:1) 和评定结果/任务相关索引。
- 对 `evaluation_result`、`scholarship_application`、`application_achievement`、`review_record` 做慢 SQL 排查和 `EXPLAIN`。
- 关注大分页、排序字段、模糊搜索和批量更新语句的执行计划，必要时增加覆盖索引或改查询方式。

8. 可观测性补齐
- 给申请提交、评定任务创建、评定任务执行、导出任务增加耗时、失败原因、重试次数、批次规模等指标。
- 为线程池、Redis、MySQL 连接池、缓存命中率、429 限流命中率补监控。
- 没有这些数据时，不建议做 MQ 或架构升级决策。

### Test Plan
- 申请提交压测：单批次同学生重复提交、同批次多学生并发提交、峰值突刺提交。
- 评定任务压测：小批次、中批次、大批次重算；重复触发同一批次；多管理员并发触发。
- 查询压测：结果分页、学生个人结果、排名列表、带关键字搜索。
- 导出压测：大批次全量导出，观察接口耗时、内存、线程池占用。
- 缓存验证：重算前后缓存命中率、缓存失效耗时、Redis CPU/慢日志。

### Assumptions
- 当前系统仍以单体 Spring Boot 应用为主，没有多服务事件驱动刚需。
- “高并发”主要指批量申请提交、管理员触发评定、结果查询高峰，而不是持续海量异步消息处理。
- 默认优先选择“调优现有 Redis/MySQL/线程池/缓存策略”，而不是新增 `RocketMQ` 基础设施。
