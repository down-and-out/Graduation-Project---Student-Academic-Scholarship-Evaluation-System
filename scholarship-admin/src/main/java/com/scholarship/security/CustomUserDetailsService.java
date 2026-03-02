package com.scholarship.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scholarship.common.exception.BusinessException;
import com.scholarship.common.result.ResultCode;
import com.scholarship.entity.SysPermission;
import com.scholarship.entity.SysRole;
import com.scholarship.entity.SysUser;
import com.scholarship.mapper.SysPermissionMapper;
import com.scholarship.mapper.SysRoleMapper;
import com.scholarship.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义UserDetailsService实现类
 * <p>
 * 实现Spring Security的UserDetailsService接口
 * 负责从数据库中加载用户信息，用于认证和授权
 * </p>
 *
 * 工作流程：
 * <ol>
 *   <li>用户登录或请求时，Spring Security调用此类的loadUserByUsername方法</li>
 *   <li>根据用户名从数据库查询用户信息</li>
 *   <li>如果用户不存在，抛出UsernameNotFoundException异常</li>
 *   <li>如果用户存在但被禁用，后续会在认证时抛出异常</li>
 *   <li>将用户信息封装成LoginUser对象返回</li>
 * </ol>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 根据用户名加载用户信息
     * <p>
     * 此方法在以下情况会被调用：
     * 1. 用户登录时（验证用户名和密码）
     * 2. JWT认证时（根据Token中的用户名加载用户信息）
     * </p>
     *
     * @param username 用户名
     * @return UserDetails对象（LoginUser实例）
     * @throws UsernameNotFoundException 用户不存在时抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: username={}", username);

        // 从数据库查询用户信息
        SysUser sysUser = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );

        // 用户不存在
        if (sysUser == null) {
            log.warn("用户不存在: username={}", username);
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        // 用户被禁用
        if (sysUser.getStatus() == 0) {
            log.warn("用户已被禁用: username={}", username);
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 加载用户角色和权限
        List<SimpleGrantedAuthority> authorities = loadUserAuthorities(sysUser.getId());

        // 封装成 LoginUser 对象返回
        LoginUser loginUser = new LoginUser(sysUser);
        loginUser.setAuthorities(authorities);
        return loginUser;
    }

    /**
     * 加载用户权限列表
     * <p>
     * 根据用户 ID 查询用户关联的角色和权限
     * </p>
     *
     * @param userId 用户 ID
     * @return 权限列表
     */
    private List<SimpleGrantedAuthority> loadUserAuthorities(Long userId) {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // 查询用户关联的角色
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        for (SysRole role : roles) {
            // 添加角色权限（ROLE_前缀）
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleCode()));
        }

        // 查询用户关联的权限
        List<SysPermission> permissions = sysPermissionMapper.selectPermissionsByUserId(userId);
        for (SysPermission permission : permissions) {
            // 添加具体权限
            authorities.add(new SimpleGrantedAuthority(permission.getPermissionCode()));
        }

        log.debug("用户 [userId={}] 的权限列表：{}", userId,
                authorities.stream().map(SimpleGrantedAuthority::getAuthority).collect(Collectors.toList()));

        return authorities;
    }
}
