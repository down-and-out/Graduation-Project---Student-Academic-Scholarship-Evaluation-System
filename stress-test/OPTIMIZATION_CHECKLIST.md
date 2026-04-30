# 压测后的代码级优化清单

本文基于现有压测结果整理，重点对应到后端代码中的具体入口，方便后续按模块推进优化。

## 1. 导出链路优先改造

当前最明确的瓶颈是评定结果导出接口。在压测结果里，`phase5` 错误率接近 `49.97%`，说明该接口在并发下已经明显进入保护或拥塞状态。

相关代码位置：

- [EvaluationResultController.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/controller/EvaluationResultController.java)
- [EvaluationResultServiceImpl.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationResultServiceImpl.java)

当前实现的问题：

- 导出接口是同步接口。
- `exportBatchResults(...)` 会先 `list(wrapper)` 一次性查出整批结果。
- 之后会在内存中构建完整的 `List<EvaluationResultExportVO>`。
- 最后才统一 `EasyExcel.write(...).doWrite(exportData)`。

这意味着在大数据量和并发访问下，这条链路会同时放大：

- 数据库读取压力
- JVM 堆内存占用
- HTTP 请求持有时间
- Excel 写出耗时

建议优化：

1. 优先改成“创建导出任务 + 后台异步生成文件 + 轮询下载”的模式。
2. 如果短期不改接口模型，至少要改成分批查询、分批写出，不要先组完整 `List`。
3. 导出应视为重任务，不应继续依赖同步请求线程承担全部成本。

## 2. 导出线程池与限流策略需要重新定义

相关代码位置：

- [ScholarshipProperties.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/config/ScholarshipProperties.java)
- [AsyncTaskConfig.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/config/AsyncTaskConfig.java)
- [EndpointRateLimitFilter.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/config/EndpointRateLimitFilter.java)

当前配置特征：

- 导出线程池：`core=1, max=2, queue=10`
- 导出限流：`ipLimit=3/60s, actorLimit=2/60s`

这套配置适合保护系统，但也意味着：

- 导出接口很容易在压测中被限流策略主导
- 目前的压测结果更像“保护阈值验证”，而不是“真实导出能力评估”

建议优化：

1. 如果导出仍然保留同步接口，就明确把它定义成严格限流接口，不再把它和普通查询吞吐一起比较。
2. 如果改成异步导出，应把限流前移到“导出任务创建接口”，而不是下载接口本身。
3. 导出线程池后续可以单独扩容，但前提是先完成异步化，否则扩容只是延后问题暴露。

## 3. 申请提交链路重点是验证真实写入成本

相关代码位置：

- [ScholarshipApplicationServiceImpl.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/ScholarshipApplicationServiceImpl.java)

当前 `submitApplication(...)` 的并发保护其实已经比较完整，包括：

- Redis 提交锁
- 提交前主动查重
- 数据库唯一约束兜底
- 捕获唯一约束异常后走幂等返回

从压测现象看，后端短期不一定要大改，但有两个值得重点检查的地方：

1. `generateApplicationNo()` 是否会在高峰时形成串行热点。
2. `replaceByApplicationId(...)` 在成果较多时是否会产生较高事务成本和多次 SQL 写入。

建议优化：

1. 检查申请编号生成逻辑是否依赖单 Redis 计数器，确认是否需要做更轻量的号段或更低竞争方案。
2. 对 `replaceByApplicationId(...)` 做一次 SQL 次数和耗时分析，确认它是不是提交路径真正的重成本点。
3. 下轮压测时确保更多请求命中“首次创建”，不要主要测到幂等返回。

## 4. 缓存失效策略过于保守

相关代码位置：

- [CacheEvictionServiceImpl.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/CacheEvictionServiceImpl.java)

当前缓存失效策略的特点是：重算或写入后大量使用 `SCAN + DELETE` 保守清理 Redis Key。

比较典型的地方：

- 批次重算后全量清理评定结果分页缓存
- 申请写入后全量清理申请分页缓存

这会带来两个问题：

1. 重算完成时刻容易形成 Redis 抖动。
2. 高并发下缓存命中率可能被“整批失效”拖低。

建议优化：

1. 中期改成基于版本号的缓存 key 设计，减少 `SCAN + DELETE`。
2. 至少先记录每次批次重算后的删除 key 数量，确认它是否已经成为热点。
3. 对评定结果分页缓存和申请分页缓存分别观察命中率，不要只看“是否启用了缓存”。

## 5. 评定执行链路要拆分“重算”和“快返回”

相关代码位置：

- [EvaluationResultController.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/controller/EvaluationResultController.java)
- [EvaluationTaskServiceImpl.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationTaskServiceImpl.java)

当前评定触发接口的设计方向是对的：

- 请求先创建任务
- 再异步执行任务
- 同批次重复触发时复用活跃任务或快速跳过

但这也意味着压测里看到的高吞吐，并不等于“真实重计算能力很强”，因为大量请求可能压到的是：

- 活跃任务复用
- 非 `PENDING` 状态快速跳过
- 分布式锁保护下的轻路径

建议优化：

1. 压测结论里把“首次触发评定”和“重复触发同批次”完全拆开。
2. 关注 `executeTaskAsync(...)` 真实触发了多少次，而不是只看接口 TPS。
3. 重点观察 `executor.evaluation.queue.size`，因为它更能反映后台评定承载能力。

## 6. 评定线程池容量需要结合真实重算场景评估

相关代码位置：

- [ScholarshipProperties.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/config/ScholarshipProperties.java)
- [AsyncTaskConfig.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/config/AsyncTaskConfig.java)

当前评定线程池配置：

- `core=2, max=4, queue=20`

这套配置偏保守，适合单体项目稳定运行，但如果大批次评定重算频繁触发，就可能出现：

- 队列堆积
- 长任务等待
- 其他后台任务受挤压

建议优化：

1. 在真实“首次重算”场景下观察线程池 `active/queue/completed` 指标。
2. 如果队列持续增长，再考虑适度扩容，而不是先凭感觉调大。
3. 如果后续引入异步导出，也要确保导出与评定线程池隔离，不互相争抢资源。

## 7. 评定计算主链路继续优化“先删后写”

相关代码位置：

- [EvaluationCalculationServiceImpl.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationCalculationServiceImpl.java)

目前这个类已经做得不错的一点是：批次计算时已经预加载了学生信息、论文、专利、项目、竞赛、课程成绩和德育分，避免了明显的 N+1 查询。

但仍有两个后续可优化点：

1. `calculateBatchApplications(...)` 每次都会先 `clearBatchResults(batchId)`，然后再重新 `saveBatchResults(...)`。
2. `calculateTotalScore(...)` 在循环中被频繁调用，虽然读取权重设置大概率会命中缓存，但仍然可以进一步前置。

建议优化：

1. 评估是否可以把“整批先删后写”改成更稳的分批覆盖或版本化结果写入。
2. 把权重设置、规则集合等批次级配置尽量提前读取一次，不要让单条记录重复参与配置解析。
3. 重点配合慢 SQL 和数据库写入量观察，确认大批次重算时的真实成本。

## 8. 分页查询当前不是第一优先级

相关代码位置：

- [EvaluationResultServiceImpl.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationResultServiceImpl.java)
- [ScholarshipApplicationServiceImpl.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/service/impl/ScholarshipApplicationServiceImpl.java)
- [CursorPageHelper.java](D:/learning/bishe_project/scholarship-admin/src/main/java/com/scholarship/common/support/CursorPageHelper.java)

当前从压测结果看，结果分页接口整体表现比较稳定，暂时没有导出或评定链路那么急。

现阶段更适合做的是：

1. 校验索引是否和查询条件匹配。
2. 持续观察慢 SQL。
3. 保留深分页保护，不急着重写分页模型。

不建议此时优先投入大量时间去改分页架构。

## 建议的实施顺序

按投入产出比排序，建议这样推进：

1. 先处理导出链路：异步化优先。
2. 再处理评定计算：重点看“先删后写”和真实重算成本。
3. 然后处理缓存失效：减少 `SCAN + DELETE`。
4. 最后回头检查申请提交路径里的编号生成和成果替换写入成本。
