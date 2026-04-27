package com.scholarship.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.scholarship.dto.StudentCreateFields;
import com.scholarship.entity.SysUser;
import com.scholarship.vo.SysUserVO;

import java.util.List;

/**
 * 系统用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    IPage<SysUser> pageUsers(Long current, Long size, String keyword, List<String> departments, List<Integer> userTypes, List<Integer> statuses);

    IPage<SysUserVO> pageUserVOs(Long current, Long size, String keyword, List<String> departments, List<Integer> userTypes, List<Integer> statuses);

    SysUser getByUsername(String username);

    boolean createUser(SysUser user, String major);

    boolean createUser(SysUser user, String major, StudentCreateFields studentFields);

    boolean updateUser(SysUser user);

    boolean deleteUser(Long id);

    boolean resetPassword(Long id, String newPassword);

    boolean deleteUsers(List<Long> ids);
}
