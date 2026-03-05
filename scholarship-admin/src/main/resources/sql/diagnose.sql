-- 数据库诊断脚本
USE `scholarship`;

-- 查看现有评定批次
SELECT '=== evaluation_batch 表 ===' AS info;
SELECT id, batch_name, batch_code, status FROM evaluation_batch;

-- 查看表结构
SELECT '=== evaluation_batch 结构 ===' AS info;
SHOW COLUMNS FROM evaluation_batch;

SELECT '=== course_score 结构 ===' AS info;
SHOW COLUMNS FROM course_score;

SELECT '=== moral_performance 结构 ===' AS info;
SHOW COLUMNS FROM moral_performance;

SELECT '=== scholarship_application 结构 ===' AS info;
SHOW COLUMNS FROM scholarship_application;

-- 查看现有学生
SELECT '=== 学生信息 ===' AS info;
SELECT id, student_no, name FROM student_info LIMIT 10;