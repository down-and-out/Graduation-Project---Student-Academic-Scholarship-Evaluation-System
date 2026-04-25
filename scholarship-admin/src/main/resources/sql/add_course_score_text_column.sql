ALTER TABLE course_score
    ADD COLUMN score_text VARCHAR(32) NULL COMMENT '成绩显示文本' AFTER score;
