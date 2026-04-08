package com.scholarship.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 课程成绩查询参数
 * <p>
 * 用于分页查询课程成绩记录
 * </p>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseScoreQuery extends PageQuery {

    /**
     * 学生 ID
     */
    private Long studentId;

    /**
     * 学年
     */
    private String academicYear;

    /**
     * 学期：1-第一学期 2-第二学期 3-夏季学期
     */
    private Integer semester;

    /**
     * 课程名称（模糊查询）
     */
    private String courseName;
}
