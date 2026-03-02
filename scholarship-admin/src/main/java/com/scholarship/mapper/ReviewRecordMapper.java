package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.ReviewRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评审记录Mapper接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Mapper
public interface ReviewRecordMapper extends BaseMapper<ReviewRecord> {
}
