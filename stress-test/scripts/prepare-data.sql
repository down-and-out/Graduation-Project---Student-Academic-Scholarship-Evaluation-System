-- ========================================
-- 奖学金评审系统 — 分场景压测数据准备脚本
-- 说明：
--   1. 生成 50~200 名测试学生（sys_user + student_info）
--   2. 预填 course_score / moral_performance 数据
--   3. 预填 evaluation_result 数据（供 page/export 压测）
--   4. 准备小批次(10条)、中批次(100条)、大批次(200条)的申请数据（供 evaluate 压测）
-- 幂等设计：重复执行不会重复插入
-- ========================================

SET NAMES utf8mb4;
USE `scholarship`;

-- ========================================
-- 0. 确保有一个管理员账号（用于 Token 生成）
-- ========================================
INSERT IGNORE INTO sys_user (username, password, real_name, user_type, status, version, deleted, create_time, update_time)
VALUES ('admin',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs',  -- BCrypt hash for 'a123456789'
        '系统管理员', 3, 1, 0, 0, NOW(), NOW());

-- ========================================
-- 1. 清理旧压测数据（基于特定 username 前缀）
-- ========================================
DELETE FROM moral_performance WHERE student_id IN (SELECT id FROM student_info WHERE student_no LIKE 'PT-%');
DELETE FROM course_score WHERE student_id IN (SELECT id FROM student_info WHERE student_no LIKE 'PT-%');
DELETE FROM evaluation_result WHERE student_no LIKE 'PT-%';
DELETE FROM scholarship_application WHERE application_no LIKE 'PT-%';
DELETE FROM student_info WHERE student_no LIKE 'PT-%';
DELETE FROM sys_user WHERE username LIKE 'pt_test_%';

-- ========================================
-- 2. 存储过程：批量生成测试学生
-- ========================================
DROP PROCEDURE IF EXISTS sp_generate_test_students;

DELIMITER //

CREATE PROCEDURE sp_generate_test_students(IN student_count INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_username VARCHAR(50);
    DECLARE v_student_no VARCHAR(20);
    DECLARE v_real_name VARCHAR(30);
    DECLARE v_user_id BIGINT;
    DECLARE v_student_id BIGINT;
    DECLARE v_department VARCHAR(50);
    DECLARE v_major VARCHAR(50);
    DECLARE v_bcrypt_pwd VARCHAR(200);

    -- BCrypt hash for 'a123456789'
    SET v_bcrypt_pwd = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EHs';

    WHILE i < student_count DO
        SET v_username = CONCAT('pt_test_', LPAD(i, 4, '0'));
        SET v_student_no = CONCAT('PT-', LPAD(i, 6, '0'));
        SET v_real_name = CONCAT('压测学生', LPAD(i, 4, '0'));
        SET v_department = CASE (i % 5)
            WHEN 0 THEN '计算机学院'
            WHEN 1 THEN '电子信息学院'
            WHEN 2 THEN '机械工程学院'
            WHEN 3 THEN '数学学院'
            ELSE '经济管理学院'
        END;
        SET v_major = CASE (i % 4)
            WHEN 0 THEN '计算机科学与技术'
            WHEN 1 THEN '电子信息工程'
            WHEN 2 THEN '机械工程'
            ELSE '应用数学'
        END;

        -- 创建 sys_user
        INSERT INTO sys_user (username, password, real_name, department, user_type, email, phone, status, version, deleted, create_time, update_time)
        VALUES (v_username, v_bcrypt_pwd, v_real_name, v_department, 1, CONCAT(v_username, '@test.com'), '13800138000', 1, 0, 0, NOW(), NOW());

        SET v_user_id = LAST_INSERT_ID();

        -- 创建 student_info
        INSERT INTO student_info (user_id, student_no, name, gender, id_card, enrollment_year, education_level, training_mode, department, major, class_name, direction, political_status, status, version, deleted, create_time, update_time)
        VALUES (v_user_id, v_student_no, v_real_name, (i % 2), CONCAT('420000', LPAD(i, 10, '0'), i % 10), 2023, 1, 1, v_department, v_major, CONCAT('研2023-', (i % 5) + 1, '班'), CONCAT(v_major, '方向'), '共青团员', 1, 0, 0, NOW(), NOW());

        SET v_student_id = LAST_INSERT_ID();

        -- 创建 course_score（每个学生 3~5 门课）
        INSERT INTO course_score (student_id, student_no, student_name, course_id, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
        VALUES
            (v_student_id, v_student_no, v_real_name, 1, CONCAT('课程A-', v_department), CONCAT('CS101-', i), 1, 3.0, 80 + (i % 20), 3.0 + (i % 10) * 0.1, '2024', 1, 0, NOW(), NOW()),
            (v_student_id, v_student_no, v_real_name, 2, CONCAT('课程B-', v_department), CONCAT('CS102-', i), 1, 4.0, 75 + (i % 25), 2.7 + (i % 8) * 0.1, '2024', 1, 0, NOW(), NOW()),
            (v_student_id, v_student_no, v_real_name, 3, CONCAT('课程C-', v_department), CONCAT('CS103-', i), 2, 2.0, 85 + (i % 15), 3.3 + (i % 7) * 0.1, '2024', 2, 0, NOW(), NOW());

        -- 70% 的学生再多加 1~2 门课
        IF (i % 10) < 7 THEN
            INSERT INTO course_score (student_id, student_no, student_name, course_id, course_name, course_code, course_type, credit, score, gpa, academic_year, semester, deleted, create_time, update_time)
            VALUES
                (v_student_id, v_student_no, v_real_name, 4, CONCAT('课程D-', v_department), CONCAT('CS104-', i), 2, 2.0, 70 + (i % 30), 2.3 + (i % 9) * 0.1, '2024', 2, 0, NOW(), NOW()),
                (v_student_id, v_student_no, v_real_name, 5, CONCAT('课程E-', v_department), CONCAT('CS105-', i), 3, 1.5, 90 + (i % 10), 3.7 + (i % 4) * 0.1, '2024', 1, 0, NOW(), NOW());
        END IF;

        -- 创建 moral_performance（每个学生 1~2 条）
        INSERT INTO moral_performance (student_id, student_no, student_name, performance_type, performance_name, description, level, score, verifier, verify_date, academic_year, semester, audit_status, deleted, create_time, update_time)
        VALUES (v_student_id, v_student_no, v_real_name, 1, '志愿服务', CONCAT(v_real_name, '志愿服务'), 3, 5.0 + (i % 15) * 0.5, '辅导员', '2024-06-01', '2024', 1, 1, 0, NOW(), NOW());

        IF (i % 3) = 0 THEN
            INSERT INTO moral_performance (student_id, student_no, student_name, performance_type, performance_name, description, level, score, verifier, verify_date, academic_year, semester, audit_status, deleted, create_time, update_time)
            VALUES (v_student_id, v_student_no, v_real_name, 3, '优秀学生干部', CONCAT(v_real_name, '获优秀学生干部'), 3, 8.0, '辅导员', '2024-12-01', '2024', 2, 1, 0, NOW(), NOW());
        END IF;

        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;

-- ========================================
-- 3. 执行存储过程：生成 200 名学生
-- ========================================
CALL sp_generate_test_students(200);

-- 验证
SELECT COUNT(*) AS student_count FROM student_info WHERE student_no LIKE 'PT-%';

-- ========================================
-- 4. 创建评定批次（供 evaluate 压测）
-- ========================================

-- 小批次（10 名学生参与）
INSERT INTO evaluation_batch (
    batch_name, batch_code, academic_year, semester,
    application_start_date, application_end_date,
    review_start_date, review_end_date,
    publicity_start_date, publicity_end_date,
    total_amount, winner_count, description, batch_status, deleted
)
SELECT
    '压测-小批次', 'PT-SMALL', '2024', 1,
    '2024-01-01', '2024-06-30',
    '2024-07-01', '2024-07-15',
    '2024-07-16', '2024-07-30',
    50000.00, 3, '小批次压测（10名学生）', 3, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'PT-SMALL');

-- 中批次（100 名学生参与）
INSERT INTO evaluation_batch (
    batch_name, batch_code, academic_year, semester,
    application_start_date, application_end_date,
    review_start_date, review_end_date,
    publicity_start_date, publicity_end_date,
    total_amount, winner_count, description, batch_status, deleted
)
SELECT
    '压测-中批次', 'PT-MEDIUM', '2024', 1,
    '2024-01-01', '2024-06-30',
    '2024-07-01', '2024-07-15',
    '2024-07-16', '2024-07-30',
    200000.00, 15, '中批次压测（100名学生）', 3, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'PT-MEDIUM');

-- 大批次（200 名学生参与）
INSERT INTO evaluation_batch (
    batch_name, batch_code, academic_year, semester,
    application_start_date, application_end_date,
    review_start_date, review_end_date,
    publicity_start_date, publicity_end_date,
    total_amount, winner_count, description, batch_status, deleted
)
SELECT
    '压测-大批次', 'PT-LARGE', '2024', 1,
    '2024-01-01', '2024-06-30',
    '2024-07-01', '2024-07-15',
    '2024-07-16', '2024-07-30',
    500000.00, 30, '大批次压测（200名学生）', 3, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'PT-LARGE');

-- 预填数据批次（已有评审结果，供 page/export 压测）
INSERT INTO evaluation_batch (
    batch_name, batch_code, academic_year, semester,
    application_start_date, application_end_date,
    review_start_date, review_end_date,
    publicity_start_date, publicity_end_date,
    total_amount, winner_count, description, batch_status, deleted
)
SELECT
    '压测-查询导出', 'PT-QUERY', '2023', 2,
    '2023-01-01', '2023-06-30',
    '2023-07-01', '2023-07-15',
    '2023-07-16', '2023-07-30',
    300000.00, 20, '预填评审结果批次（供page/export压测）', 5, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM evaluation_batch WHERE batch_code = 'PT-QUERY');

-- ========================================
-- 5. 为小/中/大批次创建 scholarship_application（申请记录）
-- ========================================
DROP PROCEDURE IF EXISTS sp_create_applications;

DELIMITER //

CREATE PROCEDURE sp_create_applications(
    IN batch_code_param VARCHAR(50),
    IN apply_count INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_batch_id BIGINT;
    DECLARE v_student_id BIGINT;
    DECLARE v_student_no VARCHAR(20);
    DECLARE v_student_name VARCHAR(30);

    SELECT id INTO v_batch_id FROM evaluation_batch WHERE batch_code = batch_code_param;

    IF v_batch_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '批次不存在';
    END IF;

    WHILE i < apply_count DO
        -- 依次取测试学生
        SELECT s.id, s.student_no, s.name
        INTO v_student_id, v_student_no, v_student_name
        FROM student_info s
        WHERE s.student_no LIKE 'PT-%'
        ORDER BY s.student_no
        LIMIT 1 OFFSET i;

        IF v_student_id IS NOT NULL THEN
            INSERT IGNORE INTO scholarship_application (
                batch_id, student_id, application_no, total_score, ranking, award_level,
                scholarship_amount, application_time, submit_time, status, version, deleted, create_time, update_time
            ) VALUES (
                v_batch_id, v_student_id,
                CONCAT('PT-', batch_code_param, '-', LPAD(i, 6, '0')),
                70 + (i % 30), NULL, NULL, NULL, NOW(), NOW(), 1, 0, 0, NOW(), NOW()
            );
        END IF;

        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;

CALL sp_create_applications('PT-SMALL', 10);
CALL sp_create_applications('PT-MEDIUM', 100);
CALL sp_create_applications('PT-LARGE', 200);

SELECT batch_code, COUNT(*) AS app_count FROM scholarship_application WHERE application_no LIKE 'PT-%' GROUP BY batch_code;

-- ========================================
-- 6. 为 PT-QUERY 批次预填 evaluation_result 数据（供 page/export 压测）
-- ========================================
DROP PROCEDURE IF EXISTS sp_prefill_results;

DELIMITER //

CREATE PROCEDURE sp_prefill_results(
    IN batch_code_param VARCHAR(50),
    IN result_count INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_batch_id BIGINT;
    DECLARE v_student_id BIGINT;
    DECLARE v_student_no VARCHAR(20);
    DECLARE v_student_name VARCHAR(30);
    DECLARE v_department VARCHAR(50);
    DECLARE v_major VARCHAR(50);
    DECLARE v_app_id BIGINT;

    SELECT id INTO v_batch_id FROM evaluation_batch WHERE batch_code = batch_code_param;

    WHILE i < result_count DO
        SELECT s.id, s.student_no, s.name, s.department, s.major
        INTO v_student_id, v_student_no, v_student_name, v_department, v_major
        FROM student_info s
        WHERE s.student_no LIKE 'PT-%'
        ORDER BY s.student_no
        LIMIT 1 OFFSET i;

        IF v_student_id IS NOT NULL THEN
            -- 查找或创建申请记录
            SELECT id INTO v_app_id FROM scholarship_application
            WHERE batch_id = v_batch_id AND student_id = v_student_id
            LIMIT 1;

            IF v_app_id IS NULL THEN
                INSERT INTO scholarship_application (
                    batch_id, student_id, application_no, total_score, ranking, award_level,
                    scholarship_amount, application_time, submit_time, status, version, deleted, create_time, update_time
                ) VALUES (
                    v_batch_id, v_student_id,
                    CONCAT('PT-', batch_code_param, '-', LPAD(i, 6, '0')),
                    70 + (i % 30), (i + 1), CASE WHEN (i % 30) < 10 THEN 1 WHEN (i % 30) < 20 THEN 2 WHEN (i % 30) < 30 THEN 3 ELSE 4 END,
                    CASE WHEN (i % 30) < 10 THEN 10000 WHEN (i % 30) < 20 THEN 5000 WHEN (i % 30) < 30 THEN 3000 ELSE 1000 END,
                    NOW(), NOW(), 4, 0, 0, NOW(), NOW()
                );
                SET v_app_id = LAST_INSERT_ID();
            END IF;

            INSERT IGNORE INTO evaluation_result (
                batch_id, application_id, student_id, student_name, student_no, department, major,
                course_score, research_score, competition_score, quality_score, total_score,
                department_rank, major_rank, award_level, award_amount,
                result_status, publicity_date, confirm_date, version, deleted, create_time, update_time
            ) VALUES (
                v_batch_id, v_app_id, v_student_id, v_student_name, v_student_no, v_department, v_major,
                75 + (i % 25), 10 + (i % 15), 5 + (i % 10), 8 + (i % 8), 85 + (i % 15),
                (i + 1), (i % 10) + 1,
                CASE WHEN (i % 30) < 10 THEN 1 WHEN (i % 30) < 20 THEN 2 WHEN (i % 30) < 30 THEN 3 ELSE 4 END,
                CASE WHEN (i % 30) < 10 THEN 10000 WHEN (i % 30) < 20 THEN 5000 WHEN (i % 30) < 30 THEN 3000 ELSE 1000 END,
                2, NOW(), NOW(), 0, 0, NOW(), NOW()
            );
        END IF;

        SET i = i + 1;
    END WHILE;
END //

DELIMITER ;

CALL sp_prefill_results('PT-QUERY', 200);

-- 验证
SELECT batch_id, COUNT(*) AS result_count FROM evaluation_result WHERE student_no LIKE 'PT-%' GROUP BY batch_id;

-- ========================================
-- 7. 清理存储过程（可选，保留也可）
-- ========================================
-- DROP PROCEDURE IF EXISTS sp_generate_test_students;
-- DROP PROCEDURE IF EXISTS sp_create_applications;
-- DROP PROCEDURE IF EXISTS sp_prefill_results;

SELECT '>>> 压测数据准备完成！' AS message;
SELECT COUNT(*) AS sys_user_count FROM sys_user WHERE username LIKE 'pt_test_%';
SELECT COUNT(*) AS student_info_count FROM student_info WHERE student_no LIKE 'PT-%';
SELECT COUNT(*) AS course_score_count FROM course_score WHERE student_no LIKE 'PT-%';
SELECT COUNT(*) AS moral_performance_count FROM moral_performance WHERE student_no LIKE 'PT-%';
SELECT COUNT(*) AS scholarship_application_count FROM scholarship_application WHERE application_no LIKE 'PT-%';
SELECT COUNT(*) AS evaluation_result_count FROM evaluation_result WHERE student_no LIKE 'PT-%';
