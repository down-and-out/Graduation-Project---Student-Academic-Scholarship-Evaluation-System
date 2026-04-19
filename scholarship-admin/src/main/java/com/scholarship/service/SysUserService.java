package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.entity.SysUser;
import com.scholarship.vo.SysUserVO;

/**
 * 系统用户服务接口
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 分页查询用户
     *
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 搜索关键字（用户名或姓名）
     * @param userType 用户类型
     * @param status 状态
     * @return 分页结果
     */
    IPage<SysUser> pageUsers(Long current, Long size, String keyword, Integer userType, Integer status);

    /**
     * 分页查询用户（返回 VO 对象）
     *
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 搜索关键字（用户名或姓名）
     * @param userType 用户类型
     * @param status 状态
     * @return 分页结果（VO）
     */
    IPage<SysUserVO> pageUserVOs(Long current, Long size, String keyword, Integer userType, Integer status);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 创建用户
     *
     * @param user 用户信息
     * @param major 专业（仅学生类型需要）
     * @return 是否成功
     */
    boolean createUser(SysUser user, String major);

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUser(SysUser user);

    /**
     * 删除用户
     *
     * @param id 用户 ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 重置用户密码
     *
     * @param id 用户 ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 批量删除用户
     *
     * @param ids 用户 ID 列表
     * @return 是否成功
     */
    boolean deleteUsers(java.util.List<Long> ids);
}
