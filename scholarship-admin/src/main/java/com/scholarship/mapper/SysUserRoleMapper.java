package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
