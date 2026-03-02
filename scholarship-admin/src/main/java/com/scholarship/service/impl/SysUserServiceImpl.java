package com.scholarship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scholarship.entity.SysUser;
import com.scholarship.mapper.SysUserMapper;
import com.scholarship.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 系统用户服务实现类
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public IPage<SysUser> pageUsers(Long current, Long size, String keyword, Integer userType, Integer status) {
        log.debug("分页查询用户，current={}, size={}, keyword={}, userType={}, status={}", current, size, keyword, userType, status);

        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getRealName, keyword));
        }
        if (userType != null) {
            wrapper.eq(SysUser::getUserType, userType);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public SysUser getByUsername(String username) {
        log.debug("根据用户名查询用户，username={}", username);

        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createUser(SysUser user) {
        log.info("创建用户，username={}, realName={}", user.getUsername(), user.getRealName());

        // 检查用户名是否存在
        SysUser existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 加密密码
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 默认状态为正常
        if (user.getStatus() == null) {
            user.setStatus(1);
        }

        return save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser user) {
        log.info("更新用户，id={}, username={}", user.getId(), user.getUsername());

        // 检查用户名是否存在（排除自己）
        SysUser existUser = getByUsername(user.getUsername());
        if (existUser != null && !existUser.getId().equals(user.getId())) {
            throw new RuntimeException("用户名已存在");
        }

        // 不允许直接修改密码，使用 resetPassword 方法
        user.setPassword(null);

        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        log.info("删除用户，id={}", id);
        return removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id, String newPassword) {
        log.info("重置用户密码，id={}", id);

        SysUser user = getById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUsers(List<Long> ids) {
        log.info("批量删除用户，ids={}", ids);
        return removeByIds(ids);
    }
}
