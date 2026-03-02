package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.CourseScore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程成绩 Mapper 接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Mapper
public interface CourseScoreMapper extends BaseMapper<CourseScore> {
}
