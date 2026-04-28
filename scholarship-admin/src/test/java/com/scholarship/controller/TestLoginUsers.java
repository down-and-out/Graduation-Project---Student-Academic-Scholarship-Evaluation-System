package com.scholarship.controller;

import com.scholarship.entity.SysUser;
import com.scholarship.security.LoginUser;

final class TestLoginUsers {

    private TestLoginUsers() {
    }

    static LoginUser admin() {
        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        sysUser.setUsername("admin");
        sysUser.setRealName("Admin User");
        sysUser.setUserType(3);
        sysUser.setStatus(1);
        return new LoginUser(sysUser);
    }
}
