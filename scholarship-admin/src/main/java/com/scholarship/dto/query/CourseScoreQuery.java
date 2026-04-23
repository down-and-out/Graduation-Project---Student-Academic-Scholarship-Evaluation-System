package com.scholarship.dto.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 课程成绩查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CourseScoreQuery extends PageQuery {

    /**
     * 单个学生 ID。
     */
    private Long studentId;

    /**
     * 多个学生 ID，用于导师权限范围过滤。
     */
    private List<Long> studentIds;

    /**
     * 学年。
     */
    private String academicYear;

    /**
     * 学期：1-第一学期，2-第二学期，3-夏季学期。
     */
    private Integer semester;

    /**
     * 课程名称，模糊查询。
     */
    private String courseName;
}
