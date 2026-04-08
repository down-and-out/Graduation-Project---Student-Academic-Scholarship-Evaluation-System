package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.SysSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 系统设置 Mapper
 * <p>
 * 提供系统设置的数据访问能力
 * </p>
 */
@Mapper
public interface SysSettingMapper extends BaseMapper<SysSetting> {

    /**
     * 根据设置键和版本号查询
     *
     * @param settingKey 设置键
     * @param version    版本号
     * @return 设置实体
     */
    @Select("SELECT * FROM sys_setting WHERE setting_key = #{key} AND version = #{version}")
    SysSetting selectByKeyAndVersion(@Param("key") String settingKey, @Param("version") Integer version);

    /**
     * 查询当前生效的设置
     *
     * @param settingKey 设置键
     * @return 设置实体
     */
    @Select("SELECT * FROM sys_setting WHERE setting_key = #{key} AND is_active = 1 LIMIT 1")
    SysSetting selectActiveByKey(@Param("key") String settingKey);
}
