package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统权限Mapper接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据用户 ID 查询用户关联的权限
     * @param userId 用户 ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByUserId(@Param("userId") Long userId);
}
