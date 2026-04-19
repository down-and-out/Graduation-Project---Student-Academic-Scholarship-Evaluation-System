-- 为 moral_performance 表添加缺失字段
-- 该字段在实体类中定义但数据库表中缺失

-- 添加 performance_type 字段
ALTER TABLE moral_performance
ADD COLUMN IF NOT EXISTS performance_type INT DEFAULT NULL COMMENT '表现类型：1-志愿服务 2-社会实践 3-荣誉称号 4-学生干部 5-其他'
AFTER student_name;

-- 添加 performance_name 字段
ALTER TABLE moral_performance
ADD COLUMN IF NOT EXISTS performance_name VARCHAR(200) DEFAULT NULL COMMENT '表现名称'
AFTER performance_type;