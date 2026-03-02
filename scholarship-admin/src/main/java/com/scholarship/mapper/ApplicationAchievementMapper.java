package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.ApplicationAchievement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 申请成果关联Mapper接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Mapper
public interface ApplicationAchievementMapper extends BaseMapper<ApplicationAchievement> {
}
