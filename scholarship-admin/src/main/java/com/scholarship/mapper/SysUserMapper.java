package com.scholarship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.scholarship.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户Mapper接口
 * <p>
 * 数据访问层，负责对sys_user表进行CRUD操作
 * 继承MyBatis-Plus的BaseMapper，自动拥有单表CRUD能力
 * </p>
 *
 * BaseMapper提供的主要方法：
 * <ul>
 *   <li>insert(T entity): 插入单条记录</li>
 *   <li>deleteById(Serializable id): 根据ID删除</li>
 *   <li>updateById(T entity): 根据ID更新</li>
 *   <li>selectById(Serializable id): 根据ID查询</li>
 *   <li>selectList(Wrapper&lt;T&gt; wrapper): 条件查询列表</li>
 *   <li>selectPage(Page&lt;T&gt; page, Wrapper&lt;T&gt; wrapper): 分页查询</li>
 * </ul>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    // BaseMapper已提供基本的CRUD方法
    // 如需自定义SQL，可在此添加方法并在mapper/*.xml中编写SQL
}
