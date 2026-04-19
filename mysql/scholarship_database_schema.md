# 研究生学业奖学金评定系统 - 数据库表结构详细文档

**数据库名称**: `scholarship`
**数据库类型**: MySQL
**字符集**: utf8mb4_unicode_ci
**文档生成日期**: 2026-03-05
**最后更新**: 根据实际数据库结构同步更新

---

## 目录

1. [核心业务表](#1-核心业务表)
   - [student_info - 学生信息表](#11-student_info---学生信息表)
   - [scholarship_application - 奖学金申请表](#12-scholarship_application---奖学金申请表)
   - [evaluation_batch - 评审批次表](#13-evaluation_batch---评审批次表)
   - [evaluation_result - 评审结果表](#14-evaluation_result---评审结果表)
   - [application_achievement - 申请成果表](#15-application_achievement---申请成果表)
2. [学业与德育表](#2-学业与德育表)
   - [course_score - 课程成绩表](#21-course_score---课程成绩表)
   - [moral_performance - 德育表现表](#22-moral_performance---德育表现表)
3. [成果/奖项表](#3-成果奖项表)
   - [competition_award - 学科竞赛表](#31-competition_award---学科竞赛表)
   - [research_paper - 研究论文表](#32-research_paper---研究论文表)
   - [research_patent - 研究专利表](#33-research_patent---研究专利表)
   - [research_project - 研究项目表](#34-research_project---研究项目表)
4. [评审与规则表](#4-评审与规则表)
   - [review_record - 评审记录表](#41-review_record---评审记录表)
   - [result_appeal - 结果申诉表](#42-result_appeal---结果申诉表)
   - [score_rule - 评分规则表](#43-score_rule---评分规则表)
   - [rule_category - 规则分类表](#44-rule_category---规则分类表)
5. [系统管理表](#5-系统管理表)
   - [sys_user - 系统用户表](#51-sys_user---系统用户表)
   - [sys_role - 系统角色表](#52-sys_role---系统角色表)
   - [sys_permission - 系统权限表](#53-sys_permission---系统权限表)
   - [sys_user_role - 用户角色关系表](#54-sys_user_role---用户角色关系表)
   - [sys_role_permission - 角色权限关系表](#55-sys_role_permission---角色权限关系表)
   - [sys_notification - 系统通知表](#56-sys_notification---系统通知表)
   - [sys_operation_log - 操作日志表](#57-sys_operation_log---操作日志表)
6. [表关系图](#6-表关系图)
7. [已知问题](#7-已知问题)

---

## 1. 核心业务表

### 1.1 student_info - 学生信息表

**表说明**: 存储研究生的基本信息，包括学籍、专业、导师等信息。

| 字段名             | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                 |
|:-------------------|:--------------|:-----|:------|:------------------|:-----------------|:-------------------------------------|
| id                 | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                       |
| user_id            | bigint        | NO   | UNI   | NULL              |                  | 关联系统用户 ID，与 sys_user 表关联      |
| student_no         | varchar(20)   | NO   | UNI   | NULL              |                  | 学号，唯一                             |
| name               | varchar(50)   | NO   |       | NULL              |                  | 姓名                                   |
| gender             | tinyint       | NO   |       | NULL              |                  | 性别：0-女，1-男                        |
| id_card            | varchar(18)   | YES  |       | NULL              |                  | 身份证号                               |
| enrollment_year    | int           | NO   |       | NULL              |                  | 入学年份，如 2024                        |
| education_level    | tinyint       | NO   |       | NULL              |                  | 学历层次：1-硕士，2-博士                 |
| training_mode      | tinyint       | NO   |       | NULL              |                  | 培养方式：1-全日制，2-非全日制           |
| department         | varchar(100)  | NO   | MUL   | NULL              |                  | 院系名称                               |
| major              | varchar(100)  | NO   |       | NULL              |                  | 专业名称                               |
| class_name         | varchar(50)   | YES  |       | NULL              |                  | 班级名称                               |
| tutor_id           | bigint        | YES  | MUL   | NULL              |                  | 导师 ID，关联 sys_user 表（导师用户）     |
| direction          | varchar(100)  | YES  |       | NULL              |                  | 研究方向                               |
| political_status   | varchar(50)   | YES  |       | NULL              |                  | 政治面貌（如党员、团员等）               |
| nation             | varchar(50)   | YES  |       | NULL              |                  | 民族                                   |
| native_place       | varchar(100)  | YES  |       | NULL              |                  | 籍贯                                   |
| address            | varchar(200)  | YES  |       | NULL              |                  | 家庭住址                               |
| phone              | varchar(20)   | YES  |       | NULL              |                  | 电话                                   |
| email              | varchar(100)  | YES  |       | NULL              |                  | 邮箱                                   |
| status             | tinyint       | NO   | MUL   | 1                 |                  | 学生状态：0-休学，1-在读，2-毕业，3-退学  |
| version            | int           | NO   |       | 1                 |                  | 乐观锁版本号                           |
| deleted            | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除             |
| create_time        | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                               |
| update_time        | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                               |
| remark             | varchar(500)  | YES  |       | NULL              |                  | 备注                                   |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (student_no)
- UNIQUE KEY (user_id)
- KEY (tutor_id)
- KEY (department)
- KEY (status)

---

### 1.2 scholarship_application - 奖学金申请表

**表说明**: 存储学生提交的奖学金申请信息，包括申请状态、审核意见、排名等。

| 字段名                | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                              |
|:----------------------|:--------------|:-----|:------|:------------------|:-----------------|:--------------------------------------------------|
| id                    | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                                    |
| batch_id              | bigint        | NO   | MUL   | NULL              |                  | 评审批次 ID，关联 evaluation_batch 表                |
| student_id            | bigint        | NO   | MUL   | NULL              |                  | 学生 ID，关联 student_info 表                       |
| application_no        | varchar(50)   | NO   | UNI   | NULL              |                  | 申请编号，唯一                                     |
| total_score           | decimal(6,2)  | NO   |       | 0.00              |                  | 申请总分数                                         |
| ranking               | int           | YES  | MUL   | NULL              |                  | 排名                                               |
| award_level           | tinyint       | YES  |       | NULL              |                  | 获奖等级：1-一等奖学金，2-二等奖学金，3-三等奖学金，4-未获奖 |
| scholarship_amount    | decimal(10,2) | YES  |       | NULL              |                  | 奖学金金额（元）                                   |
| application_time      | datetime      | NO   |       | NULL              |                  | 申请时间                                           |
| submit_time           | datetime      | YES  |       | NULL              |                  | 提交时间                                           |
| status                | tinyint       | NO   | MUL   | 0                 |                  | 状态：0-草稿，1-已提交，2-审核中，3-审核通过，4-已公示，5-已发放，6-已拒绝 |
| version               | int           | NO   |       | 1                 |                  | 乐观锁版本号                                       |
| tutor_opinion         | varchar(500)  | YES  |       | NULL              |                  | 导师意见                                           |
| tutor_id              | bigint        | YES  |       | NULL              |                  | 导师审核 ID，关联 sys_user 表                       |
| tutor_review_time     | datetime      | YES  |       | NULL              |                  | 导师审核时间                                       |
| college_opinion       | varchar(500)  | YES  |       | NULL              |                  | 院系审核意见                                       |
| college_reviewer_id   | bigint        | YES  |       | NULL              |                  | 院系审核人 ID                                       |
| college_review_time   | datetime      | YES  |       | NULL              |                  | 院系审核时间                                       |
| deleted               | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除                        |
| create_time           | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                                           |
| update_time           | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                                           |
| remark                | varchar(500)  | YES  |       | NULL              |                  | 备注                                               |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (application_no)
- KEY (batch_id)
- KEY (student_id)
- KEY (status)
- KEY (ranking)

---

### 1.3 evaluation_batch - 评审批次表

**表说明**: 存储奖学金评审批次的信息，如学年、申请时间、评审时间、名额等。

| 字段名                   | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                            |
|:-------------------------|:--------------|:-----|:------|:------------------|:-----------------|:--------------------------------|
| id                       | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                  |
| batch_name               | varchar(100)  | NO   |       | NULL              |                  | 批次名称，如"2024 年秋季学期奖学金评审" |
| batch_code               | varchar(50)   | YES  |       | NULL              |                  | 批次编码                        |
| academic_year            | varchar(50)   | YES  |       | NULL              |                  | 学年，如"2024-2025"              |
| semester                 | tinyint       | YES  |       | NULL              |                  | 学期：1-第一学期，2-第二学期，3-全年  |
| application_start_date   | date          | YES  |       | NULL              |                  | 申请开始日期                    |
| application_end_date     | date          | YES  |       | NULL              |                  | 申请截止日期                    |
| batch_year               | int           | NO   | MUL   | NULL              |                  | 批次年份                        |
| version                  | int           | NO   |       | 1                 |                  | 乐观锁版本号                    |
| batch_type               | tinyint       | NO   |       | NULL              |                  | 批次类型：1-春季奖学金，2-秋季奖学金，3-专项奖学金 |
| start_date               | date          | NO   |       | NULL              |                  | 申请开始日期（冗余字段）         |
| end_date                 | date          | NO   |       | NULL              |                  | 申请截止日期（冗余字段）         |
| review_start_date        | date          | NO   |       | NULL              |                  | 评审开始日期                    |
| review_end_date          | date          | NO   |       | NULL              |                  | 评审截止日期                    |
| total_quota              | int           | YES  |       | NULL              |                  | 评审名额总数                    |
| scholarship_amount       | decimal(10,2) | YES  |       | NULL              |                  | 奖学金总额（实体类中对应 totalAmount） |
| description              | varchar(500)  | YES  |       | NULL              |                  | 批次说明                        |
| status                   | tinyint       | NO   | MUL   | 0                 |                  | 批次状态（实体类中对应 batchStatus）0-未开始，1-进行中，2-评审中，3-已结束，4-已取消 |
| deleted                  | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除     |
| create_time              | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                        |
| update_time              | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                        |

**索引**:
- PRIMARY KEY (id)
- KEY (batch_year)
- KEY (status)

---

### 1.4 evaluation_result - 评审结果表

**表说明**: 存储每个学生在每个评审批次中的最终评审结果，包括各项分数、排名、获奖等级等。

| 字段名            | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                     |
|:------------------|:--------------|:-----|:------|:------------------|:-----------------|:-----------------------------------------|
| id                | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                           |
| batch_id          | bigint        | NO   | MUL   | NULL              |                  | 评审批次 ID，关联 evaluation_batch 表      |
| student_id        | bigint        | NO   |       | NULL              |                  | 学生 ID，关联 student_info 表             |
| student_name      | varchar(50)   | YES  |       | NULL              |                  | 学生姓名（冗余字段）                      |
| student_no        | varchar(20)   | YES  |       | NULL              |                  | 学号（冗余字段）                          |
| department        | varchar(100)  | YES  |       | NULL              |                  | 院系（冗余字段）                          |
| major             | varchar(100)  | YES  |       | NULL              |                  | 专业（冗余字段）                          |
| application_id    | bigint        | NO   | MUL   | NULL              |                  | 申请表 ID，关联 scholarship_application 表 |
| total_score       | decimal(6,2)  | NO   |       | NULL              |                  | 总分                                     |
| academic_score    | decimal(6,2)  | YES  |       | 0.00              |                  | 学业成绩分数                             |
| research_score    | decimal(6,2)  | YES  |       | 0.00              |                  | 科研成果分数                             |
| competition_score | decimal(6,2)  | YES  |       | 0.00              |                  | 竞赛获奖分数                             |
| quality_score     | decimal(6,2)  | YES  |       | 0.00              |                  | 综合素质分数                             |
| course_score      | decimal(6,2)  | YES  |       | 0.00              |                  | 课程成绩分数                             |
| moral_score       | decimal(6,2)  | YES  |       | 0.00              |                  | 德育分数                                 |
| ranking           | int           | NO   | MUL   | NULL              |                  | 总排名                                   |
| department_rank   | int           | YES  |       | NULL              |                  | 院系排名                                 |
| major_rank        | int           | YES  |       | NULL              |                  | 专业排名                                 |
| award_level       | tinyint       | NO   | MUL   | NULL              |                  | 获奖等级：0-未获奖，1-一等奖学金，2-二等奖学金，3-三等奖学金 |
| award_amount      | decimal(10,2) | YES  |       | 0.00              |                  | 奖学金金额（实体类对应 awardAmount）     |
| publicity_date    | datetime      | YES  |       | NULL              |                  | 公示日期时间                             |
| confirm_date      | datetime      | YES  |       | NULL              |                  | 确定日期                                 |
| result_status     | tinyint       | YES  |       | 1                 |                  | 结果状态（实体类对应 resultStatus）：1-待公示，2-已确认，3-有异议 |
| deleted           | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除              |
| create_time       | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                                 |
| update_time       | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                                 |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (batch_id, student_id) - 同一学生在同批次中只能有一条结果
- KEY (application_id)
- KEY (ranking)
- KEY (award_level)

---

### 1.5 application_achievement - 申请成果表

**表说明**: 存储申请中填写的成果信息，是申请与具体成果（论文、专利、项目、竞赛）的关联表。

| 字段名           | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                  |
|:-----------------|:--------------|:-----|:------|:------------------|:-----------------|:--------------------------------------|
| id               | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                        |
| application_id   | bigint        | NO   | MUL   | NULL              |                  | 申请表 ID，关联 scholarship_application 表 |
| version          | int           | NO   |       | 1                 |                  | 乐观锁版本号                          |
| achievement_type | tinyint       | NO   | MUL   | NULL              |                  | 成果类型：1-论文，2-专利，3-项目，4-竞赛  |
| achievement_id   | bigint        | NO   |       | NULL              |                  | 成果 ID，根据类型关联对应表            |
| score            | decimal(6,2)  | NO   |       | NULL              |                  | 得分                                  |
| score_comment    | varchar(500)  | YES  |       | NULL              |                  | 评分说明                              |
| rule_id          | bigint        | YES  |       | NULL              |                  | 使用的评分规则 ID，关联 score_rule 表   |
| create_time      | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                              |

**索引**:
- PRIMARY KEY (id)
- KEY (application_id)
- KEY (achievement_type, achievement_id)

---

## 2. 学业与德育表

### 2.1 course_score - 课程成绩表

**表说明**: 存储学生的课程成绩信息，包括学分、绩点、成绩等。

| 字段名         | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                  |
|:---------------|:--------------|:-----|:------|:------------------|:-----------------|:--------------------------------------|
| id             | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                        |
| student_id     | bigint        | YES  | MUL   | NULL              |                  | 学生 ID，关联 student_info 表          |
| student_no     | varchar(20)   | YES  |       | NULL              |                  | 学号                                  |
| student_name   | varchar(50)   | YES  |       | NULL              |                  | 姓名                                  |
| course_id      | bigint        | YES  | MUL   | NULL              |                  | 课程 ID                               |
| course_name    | varchar(200)  | YES  |       | NULL              |                  | 课程名称                              |
| course_code    | varchar(50)   | YES  |       | NULL              |                  | 课程代码                              |
| course_type    | tinyint       | YES  |       | NULL              |                  | 课程类型：1-必修，2-选修，3-实践        |
| credit         | decimal(4,1)  | YES  |       | NULL              |                  | 学分                                  |
| score          | decimal(5,2)  | YES  |       | NULL              |                  | 成绩                                  |
| gpa            | decimal(3,1)  | YES  |       | NULL              |                  | 绩点                                  |
| academic_year  | varchar(20)   | YES  |       | NULL              |                  | 学年                                  |
| semester       | tinyint       | YES  |       | NULL              |                  | 学期：1-第一学期，2-第二学期            |
| exam_date      | date          | YES  |       | NULL              |                  | 考试日期                              |
| remark         | varchar(500)  | YES  |       | NULL              |                  | 备注                                  |
| deleted        | tinyint       | YES  | MUL   | 0                 |                  | 逻辑删除：0-未删除，1-已删除           |
| create_time    | datetime      | YES  |       | CURRENT_TIMESTAMP |                  | 创建时间                              |
| update_time    | datetime      | YES  |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                              |

**索引**:
- PRIMARY KEY (id)
- KEY (student_id)
- KEY (course_id)
- KEY (deleted)

---

### 2.2 moral_performance - 德育表现表

**表说明**: 存储学生的德育表现信息，包括志愿服务、社会实践、荣誉称号等。

| 字段名            | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                  |
|:------------------|:--------------|:-----|:------|:------------------|:-----------------|:--------------------------------------|
| id                | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                        |
| student_id        | bigint        | YES  | MUL   | NULL              |                  | 学生 ID，关联 student_info 表          |
| student_no        | varchar(20)   | YES  |       | NULL              |                  | 学号                                  |
| student_name      | varchar(50)   | YES  |       | NULL              |                  | 姓名                                  |
| performance_type  | int           | YES  |       | NULL              |                  | 表现类型：1-志愿服务，2-社会实践，3-荣誉称号，4-学生干部，5-其他 |
| performance_name  | varchar(200)  | YES  |       | NULL              |                  | 表现名称                              |
| description       | varchar(500)  | YES  |       | NULL              |                  | 表现描述                              |
| level             | int           | YES  |       | NULL              |                  | 级别：1-国家级，2-省级，3-校级，4-院级   |
| moral_score       | decimal(6,2)  | YES  |       | NULL              |                  | 道德品质分数                          |
| honor_title       | varchar(200)  | YES  |       | NULL              |                  | 荣誉称号                              |
| honor_level       | tinyint       | YES  |       | NULL              |                  | 荣誉级别                              |
| honor_date        | date          | YES  |       | NULL              |                  | 获奖日期                              |
| volunteer_hours   | int           | YES  |       | NULL              |                  | 志愿时长（小时）                      |
| social_work       | varchar(500)  | YES  |       | NULL              |                  | 社会工作                              |
| score             | decimal(6,2)  | YES  |       | NULL              |                  | 获得分数                              |
| verifier          | varchar(50)   | YES  |       | NULL              |                  | 证明人                                |
| verify_date       | date          | YES  |       | NULL              |                  | 证明日期                              |
| academic_year     | varchar(20)   | YES  |       | NULL              |                  | 学年                                  |
| semester          | int           | YES  |       | NULL              |                  | 学期：1-第一学期，2-第二学期，3-全年     |
| audit_status         | tinyint       | YES  |       | 0                 |                  | 审核状态：0-待审核，1-审核通过，2-审核驳回（实体类中对应 auditStatus） |
| auditor_id        | bigint        | YES  |       | NULL              |                  | 审核人 ID                             |
| audit_time        | datetime      | YES  |       | NULL              |                  | 审核时间                              |
| audit_comment     | varchar(500)  | YES  |       | NULL              |                  | 审核意见                              |
| proof_materials   | varchar(500)  | YES  |       | NULL              |                  | 证明材料路径                          |
| remark            | varchar(500)  | YES  |       | NULL              |                  | 备注                                  |
| deleted           | tinyint       | YES  | MUL   | 0                 |                  | 逻辑删除：0-未删除，1-已删除           |
| create_time       | datetime      | YES  |       | CURRENT_TIMESTAMP |                  | 创建时间                              |
| update_time       | datetime      | YES  |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                              |

**索引**:
- PRIMARY KEY (id)
- KEY (student_id)
- KEY (deleted)

---

## 3. 成果/奖项表

### 3.1 competition_award - 学科竞赛表

**表说明**: 存储学生参加的学科竞赛获奖信息。

| 字段名         | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                      |
|:---------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------------------|
| id             | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                            |
| student_id     | bigint        | NO   | MUL   | NULL              |                  | 学生 ID，关联 student_info 表              |
| competition_name| varchar(200) | NO   |       | NULL              |                  | 竞赛名称                                  |
| award_level    | tinyint       | NO   |       | NULL              |                  | 获奖级别：1-国家级，2-省级，3-校级          |
| award_rank     | tinyint       | NO   |       | NULL              |                  | 获奖等级：1-特等奖，2-一等奖，3-二等奖，4-三等奖，5-优秀奖 |
| award_date     | date          | YES  |       | NULL              |                  | 获奖日期                                  |
| organizer      | varchar(200)  | YES  |       | NULL              |                  | 主办单位                                  |
| team_members   | varchar(500)  | YES  |       | NULL              |                  | 团队成员列表，逗号分隔                     |
| attachment_url | varchar(500)  | YES  |       | NULL              |                  | 附件 URL（获奖证书等）                     |
| status         | tinyint       | NO   | MUL   | 0                 |                  | 审核状态：0-待审核，1-导师审核通过，2-院系审核通过，3-审核不通过 |
| version        | int           | NO   |       | 1                 |                  | 乐观锁版本号                              |
| review_comment | varchar(500)  | YES  |       | NULL              |                  | 审核意见                                  |
| reviewer_id    | bigint        | YES  |       | NULL              |                  | 审核人 ID                                 |
| review_time    | datetime      | YES  |       | NULL              |                  | 审核时间                                  |
| deleted        | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除               |
| create_time    | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                                  |
| update_time    | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                                  |
| remark         | varchar(500)  | YES  |       | NULL              |                  | 备注                                      |

**索引**:
- PRIMARY KEY (id)
- KEY (student_id)
- KEY (status)

---

### 3.2 research_paper - 研究论文表

**表说明**: 存储学生发表的研究论文信息。

| 字段名           | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                          |
|:-----------------|:--------------|:-----|:------|:------------------|:-----------------|:----------------------------------------------|
| id               | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                                |
| student_id       | bigint        | NO   | MUL   | NULL              |                  | 学生 ID，关联 student_info 表                  |
| paper_title      | varchar(200)  | NO   |       | NULL              |                  | 论文标题                                      |
| authors          | varchar(500)  | NO   |       | NULL              |                  | 作者列表，逗号分隔                             |
| author_rank      | tinyint       | NO   |       | NULL              |                  | 学生作者排名：1-第一作者，2-第二作者，3-通讯作者 |
| journal_name     | varchar(200)  | NO   |       | NULL              |                  | 期刊名称                                      |
| journal_level    | tinyint       | NO   |       | NULL              |                  | 期刊级别：1-SCI 一区，2-SCI 二区，3-SCI 三区，4-SCI 四区，5-EI，6-核心期刊，7-普通期刊 |
| impact_factor    | decimal(5,2)  | YES  |       | NULL              |                  | 影响因子                                      |
| publication_date | date          | YES  | MUL   | NULL              |                  | 发表日期                                      |
| volume           | varchar(50)   | YES  |       | NULL              |                  | 卷号                                          |
| issue            | varchar(50)   | YES  |       | NULL              |                  | 期号                                          |
| pages            | varchar(50)   | YES  |       | NULL              |                  | 页码范围                                      |
| doi              | varchar(100)  | YES  |       | NULL              |                  | DOI 号                                        |
| indexing         | varchar(200)  | YES  |       | NULL              |                  | 收录情况（SCI/EI/ISTP 等）                    |
| attachment_url   | varchar(500)  | YES  |       | NULL              |                  | 附件 URL（论文 PDF）                          |
| status           | tinyint       | NO   | MUL   | 0                 |                  | 审核状态：0-待审核，1-导师审核通过，2-院系审核通过，3-审核不通过 |
| version          | int           | NO   |       | 1                 |                  | 乐观锁版本号                                  |
| review_comment   | varchar(500)  | YES  |       | NULL              |                  | 审核意见                                      |
| reviewer_id      | bigint        | YES  |       | NULL              |                  | 审核人 ID                                     |
| review_time      | datetime      | YES  |       | NULL              |                  | 审核时间                                      |
| deleted          | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除                   |
| create_time      | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                                      |
| update_time      | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                                      |
| remark           | varchar(500)  | YES  |       | NULL              |                  | 备注                                          |

**索引**:
- PRIMARY KEY (id)
- KEY (student_id)
- KEY (status)
- KEY (publication_date)

---

### 3.3 research_patent - 研究专利表

**表说明**: 存储学生申请的研究专利信息。

| 字段名               | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                          |
|:---------------------|:--------------|:-----|:------|:------------------|:-----------------|:----------------------------------------------|
| id                   | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                                |
| student_id           | bigint        | NO   | MUL   | NULL              |                  | 学生 ID，关联 student_info 表                  |
| patent_name          | varchar(200)  | NO   |       | NULL              |                  | 专利名称                                      |
| patent_type          | tinyint       | NO   |       | NULL              |                  | 专利类型：1-发明专利，2-实用新型，3-外观设计    |
| applicant_rank       | int           | YES  |       | NULL              |                  | 申请人排名                                    |
| patent_no            | varchar(50)   | YES  |       | NULL              |                  | 专利号                                        |
| inventors            | varchar(500)  | NO   |       | NULL              |                  | 发明人列表                                    |
| inventor_rank        | tinyint       | NO   |       | NULL              |                  | 学生发明人排名                                |
| applicant            | varchar(200)  | NO   |       | NULL              |                  | 申请人（单位）                                |
| application_date     | date          | YES  | MUL   | NULL              |                  | 申请日期                                      |
| authorization_date   | date          | YES  |       | NULL              |                  | 授权日期                                      |
| patent_status        | tinyint       | NO   |       | 1                 |                  | 专利状态：1-实质审查，2-已授权，3-已失效       |
| audit_status         | tinyint       | YES  |       | 0                 |                  | 审核状态：0-待审核，1-审核通过，2-审核驳回（实体类中对应 auditStatus） |
| auditor_id           | bigint        | YES  |       | NULL              |                  | 审核人 ID                                     |
| audit_time           | datetime      | YES  |       | NULL              |                  | 审核时间                                      |
| audit_comment        | varchar(500)  | YES  |       | NULL              |                  | 审核意见                                      |
| proof_materials      | varchar(500)  | YES  |       | NULL              |                  | 证明材料路径                                  |
| score                | decimal(6,2)  | YES  |       | NULL              |                  | 获得分数                                      |
| deleted              | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除                   |
| create_time          | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                                      |
| update_time          | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                                      |
| remark               | varchar(500)  | YES  |       | NULL              |                  | 备注                                          |

**索引**:
- PRIMARY KEY (id)
- KEY (student_id)
- KEY (audit_status)
- KEY (application_date)

---

### 3.4 research_project - 研究项目表

**表说明**: 存储学生参与的研究项目信息。

| 字段名           | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                          |
|:-----------------|:--------------|:-----|:------|:------------------|:-----------------|:----------------------------------------------|
| id               | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                                |
| student_id       | bigint        | NO   | MUL   | NULL              |                  | 学生 ID，关联 student_info 表                  |
| project_name     | varchar(200)  | NO   |       | NULL              |                  | 项目名称                                      |
| project_no       | varchar(50)   | YES  |       | NULL              |                  | 项目编号                                      |
| project_level    | tinyint       | NO   |       | NULL              |                  | 项目级别：1-国家级，2-省部级，3-市厅级，4-校级项目 |
| project_type     | tinyint       | NO   |       | NULL              |                  | 项目类型：1-基础研究，2-应用研究，3-开发研究    |
| project_source   | varchar(200)  | YES  |       | NULL              |                  | 项目来源                                      |
| leader_id        | bigint        | YES  |       | NULL              |                  | 项目负责人 ID                                 |
| leader_name      | varchar(50)   | YES  |       | NULL              |                  | 项目负责人姓名                       |
| member_rank      | int           | YES  |       | NULL              |                  | 成员排名                                      |
| project_role     | tinyint       | NO   |       | NULL              |                  | 学生角色：1-项目负责人，2-主要参与人，3-一般参与人 |
| leader           | varchar(50)   | NO   |       | NULL              |                  | 项目负责人                                    |
| participants     | varchar(500)  | YES  |       | NULL              |                  | 参与人员列表                                  |
| start_date       | date          | YES  | MUL   | NULL              |                  | 开始日期                                      |
| end_date         | date          | YES  |       | NULL              |                  | 结束日期                                      |
| project_status   | tinyint       | YES  |       | 1                 |                  | 项目状态：1-在研，2-已结题，3-已终止           |
| audit_status     | tinyint       | YES  |       | 0                 |                  | 审核状态：0-待审核，1-审核通过，2-审核驳回（实体类中对应 auditStatus） |
| auditor_id       | bigint        | YES  |       | NULL              |                  | 审核人 ID                                     |
| audit_time       | datetime      | YES  |       | NULL              |                  | 审核时间                                      |
| audit_comment    | varchar(500)  | YES  |       | NULL              |                  | 审核意见                                      |
| proof_materials  | varchar(500)  | YES  |       | NULL              |                  | 证明材料路径                                  |
| score            | decimal(6,2)  | YES  |       | NULL              |                  | 获得分数                                      |
| funding          | decimal(12,2) | YES  |       | NULL              |                  | 项目经费（元）                                |
| deleted          | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除                   |
| create_time      | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                                      |
| update_time      | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                                      |
| remark           | varchar(500)  | YES  |       | NULL              |                  | 备注                                          |

**索引**:
- PRIMARY KEY (id)
- KEY (student_id)
- KEY (audit_status)
- KEY (start_date)

---

## 4. 评审与规则表

### 4.1 review_record - 评审记录表

**表说明**: 存储每笔申请的各级评审记录，包括导师审核、院系审核、学校审核等阶段。

| 字段名           | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                  |
|:-----------------|:--------------|:-----|:------|:------------------|:-----------------|:--------------------------------------|
| id               | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                        |
| application_id   | bigint        | NO   | MUL   | NULL              |                  | 申请表 ID，关联 scholarship_application 表 |
| review_stage     | tinyint       | YES  |       | 1                 |                  | 评审阶段：1-导师审核，2-院系审核，3-学校审核 |
| review_result    | tinyint       | YES  |       | 1                 |                  | 评审结果：1-通过，2-驳回，3-待定       |
| review_score     | decimal(6,2)  | YES  |       | NULL              |                  | 评审分数                              |
| review_comment   | varchar(500)  | YES  |       | NULL              |                  | 评审意见                              |
| reviewer_id      | bigint        | NO   | MUL   | NULL              |                  | 评审人 ID，关联 sys_user 表            |
| reviewer_name    | varchar(50)   | NO   |       | NULL              |                  | 评审人姓名                            |
| score            | decimal(6,2)  | NO   |       | NULL              |                  | 评审得分                              |
| opinion          | varchar(500)  | YES  |       | NULL              |                  | 评审意见                              |
| review_time      | datetime      | NO   |       | NULL              |                  | 评审时间                              |
| version          | int           | NO   |       | 1                 |                  | 乐观锁版本号                          |
| create_time      | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                              |

**索引**:
- PRIMARY KEY (id)
- KEY (application_id)
- KEY (reviewer_id)

---

### 4.2 result_appeal - 结果申诉表

**表说明**: 存储学生对评审结果有异议时的申诉信息。

| 字段名           | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                  |
|:-----------------|:--------------|:-----|:------|:------------------|:-----------------|:--------------------------------------|
| id               | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                        |
| result_id        | bigint        | NO   | MUL   | NULL              |                  | 评审结果 ID，关联 evaluation_result 表 |
| batch_id         | bigint        | YES  |       | NULL              |                  | 批次 ID，关联 evaluation_batch 表      |
| student_id       | bigint        | NO   | MUL   | NULL              |                  | 学生 ID，关联 student_info 表         |
| student_name     | varchar(50)   | YES  |       | NULL              |                  | 学生姓名                              |
| appeal_type      | tinyint       | YES  |       | 1                 |                  | 申诉类型：1-分数错误，2-材料遗漏，3-计算错误，4-其他 |
| appeal_title     | varchar(200)  | YES  |       | NULL              |                  | 申诉标题                              |
| appeal_reason    | varchar(500)  | NO   |       | NULL              |                  | 申诉原因                              |
| appeal_content   | text          | NO   |       | NULL              |                  | 申诉内容                              |
| attachment_path  | varchar(500)  | YES  |       | NULL              |                  | 附件路径                              |
| appeal_status    | tinyint       | YES  |       | 1                 |                  | 申诉状态：1-待处理，2-处理中，3-已处理，4-已驳回 |
| attachment_url   | varchar(500)  | YES  |       | NULL              |                  | 附件 URL                              |
| status           | tinyint       | NO   | MUL   | 0                 |                  | 处理状态：0-处理中，1-已处理，2-已答复，3-已关闭 |
| version          | int           | NO   |       | 1                 |                  | 乐观锁版本号                          |
| handle_opinion   | varchar(500)  | YES  |       | NULL              |                  | 处理意见                              |
| handler_id       | bigint        | YES  |       | NULL              |                  | 处理人 ID                             |
| handler_name     | varchar(50)   | YES  |       | NULL              |                  | 处理人姓名                            |
| handle_time      | datetime      | YES  |       | NULL              |                  | 处理时间                              |
| deleted          | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除           |
| create_time      | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                              |
| update_time      | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                              |

**索引**:
- PRIMARY KEY (id)
- KEY (result_id)
- KEY (student_id)
- KEY (status)

---

### 4.3 score_rule - 评分规则表

**表说明**: 存储评分规则，定义各类成果类型、级别对应的分数。

| 字段名         | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                                  |
|:---------------|:--------------|:-----|:------|:------------------|:-----------------|:--------------------------------------|
| id             | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                        |
| category_id    | bigint        | NO   | MUL   | NULL              |                  | 分类 ID，关联 rule_category 表         |
| rule_code      | varchar(50)   | NO   | UNI   | NULL              |                  | 规则编码，唯一                         |
| rule_name      | varchar(100)  | NO   |       | NULL              |                  | 规则名称                              |
| rule_type      | tinyint       | NO   | MUL   | NULL              |                  | 规则类型：1-论文，2-专利，3-项目，4-竞赛，5-课程成绩，6-综合素质 |
| score          | decimal(6,2)  | NO   |       | NULL              |                  | 分值                                  |
| max_score      | decimal(6,2)  | YES  |       | NULL              |                  | 最高分，NULL 表示无上限                |
| level          | varchar(50)   | YES  |       | NULL              |                  | 等级要求（SCI 一区、国家级等）         |
| condition      | varchar(500)  | YES  |       | NULL              |                  | 条件详细说明                          |
| is_available   | tinyint       | NO   |       | 1                 |                  | 是否可用：0-不可用，1-可用             |
| sort_order     | int           | NO   |       | 0                 |                  | 排序号                                |
| deleted        | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除           |
| create_time    | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                              |
| update_time    | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                              |
| remark         | varchar(500)  | YES  |       | NULL              |                  | 备注                                  |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (rule_code)
- KEY (category_id)
- KEY (rule_type)

---

### 4.4 rule_category - 规则分类表

**表说明**: 存储评分规则的分类信息。

| 字段名        | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                          |
|:--------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------|
| id            | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                |
| category_code | varchar(50)   | NO   | UNI   | NULL              |                  | 分类编码，唯一                 |
| category_name | varchar(50)   | NO   |       | NULL              |                  | 分类名称                       |
| description   | varchar(200)  | YES  |       | NULL              |                  | 分类描述                       |
| sort_order    | int           | NO   |       | 0                 |                  | 排序号                         |
| version       | int           | NO   |       | 1                 |                  | 乐观锁版本号                   |
| status        | tinyint       | NO   |       | 1                 |                  | 状态：0-禁用，1-启用            |
| deleted       | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除    |
| create_time   | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                       |
| update_time   | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                       |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (category_code)

---

## 5. 系统管理表

### 5.1 sys_user - 系统用户表

**表说明**: 存储系统用户的基本信息，包括学生、教师、管理员等所有用户类型。

| 字段名        | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                          |
|:--------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------|
| id            | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                |
| username      | varchar(50)   | NO   | UNI   | NULL              |                  | 用户名（登录账号），唯一        |
| password      | varchar(200)  | NO   |       | NULL              |                  | 密码（BCrypt 加密）             |
| real_name     | varchar(50)   | NO   |       | NULL              |                  | 真实姓名                       |
| user_type     | tinyint       | NO   | MUL   | 1                 |                  | 用户类型：1-研究生，2-导师，3-管理员 |
| email         | varchar(100)  | YES  |       | NULL              |                  | 联系邮箱                       |
| department    | varchar(100)  | YES  |       | NULL              |                  | 院系/部门                       |
| phone         | varchar(20)   | YES  |       | NULL              |                  | 联系电话                       |
| avatar        | varchar(500)  | YES  |       | NULL              |                  | 头像 URL                       |
| status        | tinyint       | NO   | MUL   | 1                 |                  | 状态：0-禁用，1-正常            |
| version       | int           | NO   |       | 1                 |                  | 乐观锁版本号                   |
| deleted       | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除    |
| create_time   | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                       |
| update_time   | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                       |
| remark        | varchar(500)  | YES  |       | NULL              |                  | 备注                           |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (username)
- KEY (user_type)
- KEY (status)

---

### 5.2 sys_role - 系统角色表

**表说明**: 存储系统角色信息，如学生角色、教师角色、管理员角色等。

| 字段名        | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                          |
|:--------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------|
| id            | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                |
| role_code     | varchar(50)   | NO   | UNI   | NULL              |                  | 角色编码，唯一                 |
| role_name     | varchar(50)   | NO   |       | NULL              |                  | 角色名称                       |
| description   | varchar(200)  | YES  |       | NULL              |                  | 角色描述                       |
| sort_order    | int           | NO   |       | 0                 |                  | 排序号                         |
| status        | tinyint       | NO   |       | 1                 |                  | 状态：0-禁用，1-正常            |
| deleted       | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除    |
| create_time   | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                       |
| update_time   | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                       |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (role_code)

---

### 5.3 sys_permission - 系统权限表

**表说明**: 存储系统权限（菜单/按钮）信息，采用树形结构组织。

| 字段名            | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                          |
|:------------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------|
| id                | bigint        | NO   | PRI   | NULL              | auto_increment   | **权限 ID 主键**，自增             |
| parent_id         | bigint        | NO   | MUL   | 0                 |                  | 父权限 ID，0 为顶级节点           |
| permission_code   | varchar(100)  | NO   | UNI   | NULL              |                  | 权限编码，唯一                 |
| permission_name   | varchar(50)   | NO   |       | NULL              |                  | 权限名称                       |
| permission_type   | tinyint       | NO   |       | NULL              |                  | 权限类型：1-菜单，2-按钮         |
| path              | varchar(200)  | YES  |       | NULL              |                  | 路由路径                       |
| component         | varchar(200)  | YES  |       | NULL              |                  | 组件路径                       |
| icon              | varchar(100)  | YES  |       | NULL              |                  | 图标                           |
| sort_order        | int           | NO   |       | 0                 |                  | 排序号                         |
| status            | tinyint       | NO   |       | 1                 |                  | 状态：0-禁用，1-启用            |
| deleted           | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除    |
| create_time       | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                       |
| update_time       | datetime      | NO   |       | CURRENT_TIMESTAMP | ON UPDATE        | 更新时间                       |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (permission_code)
- KEY (parent_id)

---

### 5.4 sys_user_role - 用户角色关系表

**表说明**: 存储用户与角色的关联关系（多对多）。

| 字段名        | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                          |
|:--------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------|
| id            | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                |
| user_id       | bigint        | NO   | MUL   | NULL              |                  | 用户 ID，关联 sys_user 表       |
| role_id       | bigint        | NO   | MUL   | NULL              |                  | 角色 ID，关联 sys_role 表       |
| create_time   | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                       |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (user_id, role_id) - 同一用户同一角色只能有一条记录
- KEY (role_id)

---

### 5.5 sys_role_permission - 角色权限关系表

**表说明**: 存储角色与权限的关联关系（多对多）。

| 字段名        | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                          |
|:--------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------|
| id            | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                |
| role_id       | bigint        | NO   | MUL   | NULL              |                  | 角色 ID，关联 sys_role 表       |
| permission_id | bigint        | NO   | MUL   | NULL              |                  | 权限 ID，关联 sys_permission 表 |
| create_time   | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                       |

**索引**:
- PRIMARY KEY (id)
- UNIQUE KEY (role_id, permission_id) - 同一角色同一权限只能有一条记录
- KEY (permission_id)

---

### 5.6 sys_notification - 系统通知表

**表说明**: 存储系统通知消息，支持发送给特定用户、特定角色或全体用户。

| 字段名            | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                          |
|:------------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------|
| id                | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                |
| title             | varchar(200)  | NO   |       | NULL              |                  | 通知标题                       |
| content           | text          | NO   |       | NULL              |                  | 通知内容                       |
| notification_type | tinyint       | NO   |       | NULL              |                  | 通知类型：1-系统通知，2-审核通知，3-结果通知 |
| receiver_type     | tinyint       | NO   |       | NULL              |                  | 接收者类型：1-全体用户，2-指定用户，3-指定角色 |
| business_id       | bigint        | YES  |       | NULL              |                  | 关联业务 ID                    |
| receiver_id       | bigint        | YES  | MUL   | NULL              |                  | 接收者 ID（指定用户时使用）     |
| role_id           | bigint        | YES  |       | NULL              |                  | 角色 ID（指定角色时使用）       |
| is_read           | tinyint       | NO   | MUL   | 0                 |                  | 是否已读：0-未读，1-已读        |
| sender_id         | bigint        | YES  |       | NULL              |                  | 发送者 ID                      |
| sender_name       | varchar(50)   | YES  |       | NULL              |                  | 发送者姓名                     |
| version           | int           | NO   |       | 1                 |                  | 乐观锁版本号                   |
| read_time         | datetime      | YES  |       | NULL              |                  | 阅读时间                       |
| publisher_id      | bigint        | NO   |       | NULL              |                  | 发布者 ID                      |
| deleted           | tinyint       | NO   |       | 0                 |                  | 逻辑删除：0-未删除，1-已删除    |
| create_time       | datetime      | NO   |       | CURRENT_TIMESTAMP |                  | 创建时间                       |

**索引**:
- PRIMARY KEY (id)
- KEY (receiver_id)
- KEY (is_read)

---

### 5.7 sys_operation_log - 操作日志表

**表说明**: 存储用户在系统中的操作日志，用于审计和追踪。

| 字段名         | 数据类型      | 可空 | 键    | 默认值            | 额外             | 说明                          |
|:---------------|:--------------|:-----|:------|:------------------|:-----------------|:------------------------------|
| id             | bigint        | NO   | PRI   | NULL              | auto_increment   | **ID 主键**，自增                |
| operator_id    | bigint        | YES  | MUL   | NULL              |                  | 操作人 ID                      |
| operator_name  | varchar(50)   | YES  |       | NULL              |                  | 操作人姓名                     |
| module         | varchar(100)  | YES  |       | NULL              |                  | 操作模块                       |
| operation_type | tinyint       | YES  |       | 1                 |                  | 操作类型：1-查询，2-新增，3-修改，4-删除，5-审核，6-导出 |
| description    | varchar(200)  | YES  |       | NULL              |                  | 操作描述                       |
| operation      | varchar(100)  | NO   |       | NULL              |                  | 操作名称                       |
| method         | varchar(200)  | NO   |       | NULL              |                  | 请求方法                       |
| params         | text          | YES  |       | NULL              |                  | 请求参数                       |
| operator_ip    | varchar(50)   | YES  |       | NULL              |                  | 操作 IP                        |
| request_url    | varchar(500)  | YES  |       | NULL              |                  | 请求 URL                       |
| response_data  | text          | YES  |       | NULL              |                  | 返回结果                       |
| location       | varchar(100)  | YES  |       | NULL              |                  | IP 归属地                      |
| browser        | varchar(50)   | YES  |       | NULL              |                  | 浏览器                         |
| os             | varchar(50)   | YES  |       | NULL              |                  | 操作系统                       |
| status         | tinyint       | NO   |       | NULL              |                  | 状态：0-失败，1-成功            |
| error_msg      | varchar(500)  | YES  |       | NULL              |                  | 错误信息                       |
| execution_time | bigint        | YES  |       | NULL              |                  | 执行时长（毫秒）               |
| create_time    | datetime      | NO   | MUL   | CURRENT_TIMESTAMP |                  | 创建时间                       |

**索引**:
- PRIMARY KEY (id)
- KEY (operator_id)
- KEY (create_time)

---

## 6. 表关系图

### 核心业务流程关系

```
sys_user (用户)
    │
    └── 1:1 ──> student_info (学生信息)
                    │
                    │ 1:N
                    ├───────────────────────────────────────┐
                    │                                       │
                    ▼                                       ▼
            course_score (课程成绩)              moral_performance (德育表现)
                    │                                       │
                    └──────────────────┬────────────────────┘
                                       │
                                       ▼
                            scholarship_application (申请)
                                       │
                        ┌──────────────┼──────────────┐
                        │              │              │
                        ▼              ▼              ▼
                evaluation_result  review_record  application_achievement
                        │                                │
                        │         ┌─────────┬─────────┬──┴────┐
                        │         │         │         │       │
                        ▼         ▼         ▼         ▼       ▼
                competition_award  research_paper  research_patent  research_project

evaluation_batch (批次) ──> evaluation_result

rule_category (规则分类) ──> score_rule (评分规则) ──> application_achievement
```

### 系统权限关系

```
sys_user (用户)
    │
    │ N:M
    ▼
sys_user_role (用户角色关系)
    │
    ▼
sys_role (角色)
    │
    │ N:M
    ▼
sys_role_permission (角色权限关系)
    │
    ▼
sys_permission (权限/菜单)
```

### 通知与日志

```
sys_user (用户)
    │
    ├── N ──> sys_notification (通知)
    │
    └── N ──> sys_operation_log (操作日志)
```

---

## 7. 已知问题

### 7.1 字段清理历史记录

#### V1.8 迁移（2026-03-05）已清理字段：
| 表名 | 已删除字段 | 原因 |
|:-----|:-----------|:-----|
| research_patent | authorization_date_new | 注释乱码，代码未定义此属性 |
| research_project | leader_name_new | 注释乱码，代码未定义此属性 |
| evaluation_result | publish_date | 代码未定义 publishDate，使用 publicity_date |
| result_appeal | status | 代码使用 appeal_status |
| result_appeal | attachment_url | 代码使用 attachment_path |
| sys_operation_log | operator_id | 代码使用 operatorId 映射到 user_id |
| sys_operation_log | execute_time | 代码使用 executionTime 映射到 response_time |

#### V1.9 迁移（2026-03-05）已重命名字段：
| 表名 | 旧字段名 | 新字段名 | 说明 |
|:-----|:---------|:---------|:-----|
| evaluation_batch | status | batch_status | 与实体类 batchStatus 一致 |
| evaluation_batch | scholarship_amount | total_amount | 与实体类 totalAmount 一致 |
| sys_operation_log | user_id | operator_id | 与实体类 operatorId 一致 |
| sys_operation_log | username | operator_name | 与实体类 operatorName 一致 |
| sys_operation_log | request_uri | request_url | 与实体类 requestUrl 一致 |
| sys_operation_log | response_time | execution_time | 与实体类 executionTime 一致 |
| sys_operation_log | ip_address | operator_ip | 与实体类 operatorIp 一致 |

#### V2.0 迁移（2026-03-05）已删除冗余字段：
| 表名 | 已删除字段 | 原因 |
|:-----|:-----------|:-----|
| evaluation_result | status | 实体类未定义，使用 result_status |
| evaluation_result | version | 实体类未定义 |
| evaluation_result | scholarship_amount | 实体类使用 award_amount |
| research_patent | status | 实体类使用 audit_status |
| research_patent | version | 实体类未定义 |
| research_patent | authorization_date | 与 grant_date 重复 |
| research_patent | grant_date | 与 authorization_date 重复，保留 authorization_date |
| research_patent | review_comment | 实体类使用 audit_comment |
| research_patent | reviewer_id | 实体类使用 auditor_id |
| research_patent | review_time | 实体类使用 audit_time |
| research_project | status | 实体类使用 audit_status |
| research_project | version | 实体类未定义 |
| research_project | review_comment | 实体类使用 audit_comment |
| research_project | reviewer_id | 实体类使用 auditor_id |
| research_project | review_time | 实体类使用 audit_time |

### 7.2 当前仍需关注的问题

| 表名 | 问题描述 | 建议 |
|:-----|:---------|:-----|
| evaluation_batch | application_start_date, application_end_date vs start_date, end_date | 申请日期字段重复定义，建议统一 |
| review_record | review_score, score | 评审分数字段重复 |
| review_record | review_comment, opinion | 评审意见字段重复 |

### 7.3 字符集不一致

| 表名 | 当前字符集 | 建议 |
|:-----|:-----------|:-----|
| course_score | utf8mb4_0900_ai_ci | 建议统一为 utf8mb4_unicode_ci |
| moral_performance | utf8mb4_0900_ai_ci | 建议统一为 utf8mb4_unicode_ci |

### 7.4 实体类与数据库字段映射说明

本项目使用 MyBatis-Plus 进行 ORM 映射。经过 V1.9 迁移后，数据库字段名已与 Java 实体类字段名保持一致（驼峰命名转下划线命名）：

| 实体类 | Java 字段名 | 数据库字段名 | 映射方式 |
|:-------|:------------|:-------------|:---------|
| EvaluationBatch | batchStatus | batch_status | 默认映射（驼峰转下划线） |
| EvaluationBatch | totalAmount | total_amount | 默认映射（驼峰转下划线） |
| EvaluationResult | awardAmount | award_amount | 默认映射（驼峰转下划线） |
| EvaluationResult | resultStatus | result_status | 默认映射（驼峰转下划线） |
| SysOperationLog | operatorId | operator_id | 默认映射（驼峰转下划线） |
| SysOperationLog | operatorName | operator_name | 默认映射（驼峰转下划线） |
| SysOperationLog | requestUrl | request_url | 默认映射（驼峰转下划线） |
| SysOperationLog | executionTime | execution_time | 默认映射（驼峰转下划线） |
| SysOperationLog | operatorIp | operator_ip | 默认映射（驼峰转下划线） |
| ResearchPatent | auditStatus | audit_status | 默认映射（驼峰转下划线） |
| ResearchProject | auditStatus | audit_status | 默认映射（驼峰转下划线） |

**注意**：
1. 大部分字段使用 MyBatis-Plus 默认的驼峰转下划线映射
2. 少数字段（如 `create_time`, `update_time`）需要显式使用 `@TableField` 注解指定

### 7.5 建议后续优化

1. **统一字符集**：将所有表统一为 utf8mb4_unicode_ci
2. **添加外键约束**：建议在关键关联字段上添加外键约束，保证数据一致性

---

## 附录：通用字段说明

以下字段在多个表中出现，含义统一：

| 字段名        | 说明                              |
|:--------------|:----------------------------------|
| id            | 主键 ID，自增                      |
| version       | 乐观锁版本号，用于并发控制          |
| deleted       | 逻辑删除标志：0-未删除，1-已删除    |
| create_time   | 记录创建时间                       |
| update_time   | 记录最后更新时间                   |
| status        | 状态字段，具体含义因表而异          |
| remark        | 备注说明                           |

---

**文档结束**
