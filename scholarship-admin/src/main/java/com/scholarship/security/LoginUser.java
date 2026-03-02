package com.scholarship.security;

import com.scholarship.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 登录用户信息类
 * <p>
 * 实现Spring Security的UserDetails接口，封装用户认证和授权所需的信息
 * 在认证成功后，该对象会被存储在SecurityContext中，可以通过SecurityContextHolder获取
 * </p>
 *
 * 主要用途：
 * <ul>
 *   <li>存储用户基本信息（ID、用户名、密码等）</li>
 *   <li>存储用户权限列表（用于授权判断）</li>
 *   <li>判断账户状态（是否过期、是否锁定等）</li>
 * </ul>
 *
 * @author Scholarship Development Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 系统用户实体
     * 包含用户的基本信息
     */
    private SysUser sysUser;

    /**
     * 用户权限列表
     * 存储用户拥有的所有权限
     */
    private List<SimpleGrantedAuthority> authorities;

    /**
     * 构造函数：仅传入用户对象
     * 权限列表初始化为空集合（后续可根据需要从数据库加载）
     *
     * @param sysUser 系统用户实体
     */
    public LoginUser(SysUser sysUser) {
        this.sysUser = sysUser;
        this.authorities = Collections.emptyList();
    }

    // ==================== UserDetails接口实现 ====================

    /**
     * 获取用户权限列表
     * <p>
     * 用于授权判断，比如@PreAuthorize("hasAuthority('user:add')")
     * </p>
     *
     * @return 权限列表
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * 获取用户密码
     * <p>
     * 用于密码验证，Spring Security会自动比对用户输入的密码和此方法返回的密码
     * </p>
     *
     * @return 用户密码（加密后的）
     */
    @Override
    public String getPassword() {
        return sysUser.getPassword();
    }

    /**
     * 获取用户名
     * <p>
     * 用于识别用户身份
     * </p>
     *
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    /**
     * 账户是否未过期
     * <p>
     * true: 账户未过期，可以正常使用
     * false: 账户已过期，无法使用
     * </p>
     *
     * @return true-未过期，false-已过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     * <p>
     * true: 账户未锁定，可以正常使用
     * false: 账户已锁定，无法使用
     * </p>
     *
     * @return true-未锁定，false-已锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证（密码）是否未过期
     * <p>
     * true: 凭证未过期，可以正常使用
     * false: 凭证已过期，需要修改密码
     * </p>
     *
     * @return true-未过期，false-已过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     * <p>
     * 与sysUser.status字段关联
     * true: 账户启用，可以正常使用
     * false: 账户禁用，无法使用
     * </p>
     *
     * @return true-启用，false-禁用
     */
    @Override
    public boolean isEnabled() {
        return sysUser.getStatus() == 1;
    }

    // ==================== 便捷方法 ====================

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public Long getUserId() {
        return sysUser.getId();
    }

    /**
     * 获取用户真实姓名
     *
     * @return 真实姓名
     */
    public String getRealName() {
        return sysUser.getRealName();
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型（1-研究生 2-导师 3-管理员）
     */
    public Integer getUserType() {
        return sysUser.getUserType();
    }
}
