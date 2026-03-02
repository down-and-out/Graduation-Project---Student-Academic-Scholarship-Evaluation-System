package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.StudentInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 研究生信息Mapper接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Mapper
public interface StudentInfoMapper extends BaseMapper<StudentInfo> {
}
