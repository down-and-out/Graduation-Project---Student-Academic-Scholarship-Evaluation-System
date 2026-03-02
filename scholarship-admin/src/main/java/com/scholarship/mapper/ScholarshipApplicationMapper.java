package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.ScholarshipApplication;
import org.apache.ibatis.annotations.Mapper;

/**
 * 奖学金申请Mapper接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Mapper
public interface ScholarshipApplicationMapper extends BaseMapper<ScholarshipApplication> {
}
