# 学生页面代码审查与优化建议

## 审查范围

- 前端学生端页面
  - `scholarship-web/src/views/student/Profile.vue`
  - `scholarship-web/src/views/student/Achievements.vue`
  - `scholarship-web/src/views/student/CourseScores.vue`
  - `scholarship-web/src/views/student/Application.vue`
  - `scholarship-web/src/views/student/Result.vue`
- 前端相关接口
  - `scholarship-web/src/api/student.ts`
  - `scholarship-web/src/api/application.ts`
  - `scholarship-web/src/api/courseScore.ts`
  - `scholarship-web/src/api/result.ts`
  - `scholarship-web/src/api/appeal.ts`
  - `scholarship-web/src/api/project.ts`
  - `scholarship-web/src/api/competition.ts`
- 后端相关控制器/服务
  - `scholarship-admin/src/main/java/com/scholarship/controller/StudentInfoController.java`
  - `scholarship-admin/src/main/java/com/scholarship/controller/ScholarshipApplicationController.java`
  - `scholarship-admin/src/main/java/com/scholarship/controller/EvaluationResultController.java`
  - `scholarship-admin/src/main/java/com/scholarship/controller/CourseScoreController.java`
  - `scholarship-admin/src/main/java/com/scholarship/controller/ResultAppealController.java`
  - `scholarship-admin/src/main/java/com/scholarship/service/impl/StudentInfoServiceImpl.java`
  - `scholarship-admin/src/main/java/com/scholarship/service/impl/ScholarshipApplicationServiceImpl.java`
  - `scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationResultServiceImpl.java`
  - `scholarship-admin/src/main/java/com/scholarship/service/impl/ResultAppealServiceImpl.java`

## 主要问题

### 1. 奖学金申请详情接口存在越权风险

- 位置：
  - `scholarship-admin/src/main/java/com/scholarship/controller/ScholarshipApplicationController.java:77`
  - `scholarship-admin/src/main/java/com/scholarship/controller/ScholarshipApplicationController.java:83`
  - `scholarship-admin/src/main/java/com/scholarship/service/impl/ScholarshipApplicationServiceImpl.java:193`
- 问题描述：
  - `GET /application/{id}` 未看到 `@PreAuthorize` 保护。
  - `getDetailById` 仅按 `applicationId` 查详情，没有校验当前登录人是否为申请所属学生，也没有限制导师/管理员的可见范围。
  - 这意味着只要知道或枚举申请 ID，就可能访问到他人的申请详情，包括自评、备注、导师意见等敏感数据。
- 影响：
  - 属于真实的权限边界问题，优先级最高。
  - 学生端当前虽然通常只会请求自己的记录，但接口本身不安全，后续被脚本或其他前端入口调用时会直接暴露数据。
- 建议：
  - 为该接口补充权限注解。
  - 在服务层或控制器层增加 owner 校验：
    - 学生只能查看自己的申请。
    - 导师只能查看自己指导学生的申请。
    - 管理员可查看全部。
  - 不要只依赖前端路由或页面逻辑来限制访问。

### 2. 评定结果“当前结果”查询在多批次场景下可能直接报错

- 位置：
  - `scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationResultServiceImpl.java:120`
  - `scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationResultServiceImpl.java:130`
  - `scholarship-admin/src/main/java/com/scholarship/service/impl/EvaluationResultServiceImpl.java:133`
  - `scholarship-web/src/views/student/Result.vue:243`
- 问题描述：
  - `getStudentResult(studentId, batchId)` 在 `batchId == null` 时，只做了 `orderByDesc(batchId)`，随后调用 `getOne(wrapper)`。
  - 当某学生存在多个批次评定结果时，`getOne` 很容易抛出多条结果异常。
  - 学生端 `Result.vue` 正是无参调用 `getMyResult()`，因此这一问题会直接影响页面打开。
- 影响：
  - 学生只要历史上有多批次结果，页面就可能进入报错流。
  - 属于高风险线上故障。
- 建议：
  - 后端改为显式取“最新一条”而不是 `getOne`。
  - 更推荐后端明确接口语义：
    - 要么必须传 `batchId`。
    - 要么专门返回“当前生效批次/最近批次”的唯一记录。
  - 如果保留“最近一条”逻辑，应使用 `limit 1` 或分页查询而不是依赖 `getOne`。

### 3. 申请页把“有历史申请”误判成“当前批次已申请”

- 位置：
  - `scholarship-web/src/views/student/Application.vue:274`
  - `scholarship-web/src/views/student/Application.vue:343`
  - `scholarship-web/src/views/student/Application.vue:346`
  - `scholarship-web/src/views/student/Application.vue:374`
  - `scholarship-web/src/views/student/Application.vue:417`
- 问题描述：
  - 页面通过 `getApplicationPage({ current: 1, size: 1 })` 只取第一页第一条申请记录。
  - 然后直接用 `myApplication !== null` 判断 `hasApplied`。
  - 这并没有按当前批次 `batchInfo.id` 过滤。
  - 如果学生上一批次申请过、当前批次尚未申请，页面仍可能把他判定为“已申请”。
- 影响：
  - 学生会被错误阻止提交当前批次申请。
  - 点击“查看申请”时还可能打开旧批次申请详情，造成业务混乱。
- 建议：
  - `loadMyApplication` 应改为带 `batchId` 查询当前批次申请。
  - `hasApplied` 应与当前批次绑定，而不是与“是否存在任意申请记录”绑定。
  - 页面初始化顺序也应注意：
    - 先确定当前批次。
    - 再查询该批次下的我的申请记录。

### 4. 学生成果页与课程成绩页分页行为存在缺陷

- 位置：
  - `scholarship-web/src/views/student/Achievements.vue:153`
  - `scholarship-web/src/views/student/Achievements.vue:951`
  - `scholarship-web/src/views/student/Achievements.vue:952`
  - `scholarship-web/src/views/student/CourseScores.vue:102`
  - `scholarship-web/src/views/student/CourseScores.vue:174`
  - `scholarship-web/src/views/student/CourseScores.vue:195`
- 问题描述：
  - 两个页面都把分页器 `@current-change` 绑定到了查询函数。
  - 但查询函数内部又会把 `queryParams.current = 1` 重置为第一页。
  - 结果是用户点击第 2 页后，请求很快又被重置回第 1 页，实际无法翻页。
- 影响：
  - 一旦数据量超过一页，用户基本无法正常浏览后续记录。
  - 这是明显的交互逻辑缺陷。
- 建议：
  - 分离三个动作：
    - 搜索：显式重置到第一页。
    - 切页：保留用户点击后的页码。
    - 改页大小：重置到第一页。
  - 建议拆成：
    - `handleSearch`
    - `handlePageChange`
    - `handleSizeChange`
  - 避免在通用查询函数里无条件重置页码。

### 5. 学生成果页前后端能力边界不一致，接口契约存在漂移

- 位置：
  - `scholarship-web/src/views/student/Achievements.vue:661`
  - `scholarship-web/src/views/student/Achievements.vue:1010`
  - `scholarship-web/src/views/student/Achievements.vue:1100`
  - `scholarship-web/src/views/student/Achievements.vue:1116`
  - `scholarship-web/src/views/student/Achievements.vue:1155`
  - `scholarship-web/src/views/student/Achievements.vue:1169`
  - `scholarship-admin/src/main/java/com/scholarship/controller/ResearchProjectController.java:70`
  - `scholarship-admin/src/main/java/com/scholarship/controller/CompetitionAwardController.java:59`
- 问题描述：
  - 前端 `isTypeEditable` 只允许论文、专利可编辑，项目、竞赛在 UI 上被禁用。
  - 但同一文件中又保留了项目、竞赛的创建与提交逻辑。
  - 更关键的是，项目/竞赛/论文新增 payload 中仍带了 `studentId: 0` 这种占位值。
  - 后端项目与竞赛控制器实际上是允许学生新增和更新的。
- 影响：
  - 页面行为、接口能力、请求结构三者语义不一致。
  - 现在也许因为服务层会覆盖 studentId 才没出事，但这属于“脆弱可用”。
  - 后续只要后端校验稍微严格一点，就可能因 `studentId: 0` 失败。
- 建议：
  - 明确产品规则：
    - 如果学生可以维护项目/竞赛，则前端应完整开放并统一行为。
    - 如果学生不该维护，则删除对应创建/编辑残留逻辑。
  - 前端不要再传 `studentId: 0` 这种脏占位字段。
  - 学生身份下的 `studentId` 应完全由后端根据当前登录人推导。

### 6. 结果页把“暂无结果”当成异常，用户体验不佳

- 位置：
  - `scholarship-web/src/views/student/Result.vue:241`
  - `scholarship-admin/src/main/java/com/scholarship/controller/EvaluationResultController.java:118`
- 问题描述：
  - 后端在“没有评定结果”时返回业务错误。
  - 前端会进入 catch 分支并弹出“加载评定结果失败”。
  - 但页面本身又有“暂无评定结果”的空态卡片。
- 影响：
  - 对学生来说，“尚未出结果”的正常业务状态会被误导成系统异常。
- 建议：
  - 后端对“无数据”场景返回成功响应加空数据。
  - 前端根据空数据渲染空态，不弹错误提示。

## 其他观察

### 1. 课程成绩年份筛选选项通过大分页全量拉取

- 位置：
  - `scholarship-web/src/views/student/CourseScores.vue:150`
  - `scholarship-web/src/constants/index.ts:22`
- 说明：
  - 页面用 `size = 1000` 全量拉取当前学生成绩，再从记录里提取学年选项。
  - 这在数据量小的时候能工作，但不够稳健。
- 建议：
  - 更适合单独提供“我的成绩学年列表”接口，避免为筛选项加载整页业务数据。

### 2. 申请页当前批次状态映射过于粗糙

- 位置：
  - `scholarship-web/src/views/student/Application.vue:311`
- 说明：
  - 当前逻辑几乎把批次状态压缩成“active/closed”两类。
  - 如果后续有“待开始、申请中、审核中、公示中、已结束”等更细状态，页面展示会失真。
- 建议：
  - 前后端统一批次状态枚举，避免学生页再做二次猜测映射。

### 3. 申请页当前批次与我的申请加载顺序耦合不清晰

- 位置：
  - `scholarship-web/src/views/student/Application.vue:433`
- 说明：
  - 页面初始化时 `loadBatchInfo()` 与 `loadMyApplication()` 并行执行。
  - 但“我的申请”其实依赖“当前批次”来判断是否已申请当前批次。
- 建议：
  - 如果改成按批次查询申请，则应串行：
    - 先加载当前批次。
    - 再加载该批次下的我的申请。

## 优先级建议

### 第一优先级

- 修复申请详情接口越权问题。
- 修复 `getMyResult()` 在多结果场景下的取数逻辑。
- 修复申请页“当前批次是否已申请”的判断逻辑。

### 第二优先级

- 修复成果页、课程成绩页分页逻辑。
- 统一学生成果页前后端能力边界。

### 第三优先级

- 优化“暂无结果”响应语义。
- 收敛筛选项查询方式与状态枚举设计。

## 推荐整改方向

### 前端层面

- 统一“查询”“切页”“换页大小”三个动作的状态处理。
- 所有“当前批次相关页面”都显式围绕 `batchId` 建模。
- 减少“取第一页第一条”这种隐式业务假设。
- 清理失效逻辑、占位字段、重复状态来源。

### 后端层面

- 所有学生可访问的详情接口必须做 owner 校验。
- 对“单条接口”与“最新一条接口”做语义区分，不要混用 `getOne`。
- 学生身份相关字段尽量由服务端根据 `loginUser` 自动回填，不信任前端传值。
- 对“无数据”与“异常”做清晰区分，方便前端渲染正确空态。

## 结论

学生页面整体功能链路已经基本成型，但目前存在几类比较典型的问题：

- 权限边界未收紧。
- 当前批次语义不稳定。
- 分页交互有明显缺陷。
- 部分前后端接口契约已经出现漂移。

其中最需要优先处理的是权限问题和结果/申请取数逻辑，这两类问题更容易演变成线上事故。其余问题虽然不一定立刻导致系统不可用，但会持续增加维护成本，并影响学生端实际体验。
