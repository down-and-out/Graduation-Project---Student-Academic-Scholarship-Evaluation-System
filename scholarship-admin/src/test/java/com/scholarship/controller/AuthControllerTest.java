package com.scholarship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarship.common.result.Result;
import com.scholarship.config.JwtUtil;
import com.scholarship.dto.LoginRequest;
import com.scholarship.entity.SysUser;
import com.scholarship.security.LoginUser;
import com.scholarship.service.LoginAttemptService;
import com.scholarship.service.TokenBlacklistService;
import com.scholarship.vo.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 认证控制器测试
 */
@DisplayName("AuthController 认证控制器测试")
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AuthController authController = new AuthController(
                authenticationManager,
                jwtUtil,
                loginAttemptService,
                tokenBlacklistService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();

        // 设置测试用的 SecurityContext
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("测试登录成功")
    void testLoginSuccess() throws Exception {
        // 准备测试数据
        LoginRequest request = new LoginRequest("testuser", "encryptedPassword123");
        SysUser sysUser = createTestSysUser();
        LoginUser loginUser = new LoginUser(sysUser);

        // 配置 Mock 行为
        when(loginAttemptService.isLocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("test-jwt-token");

        // 执行请求
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        // 验证交互
        verify(loginAttemptService).resetFailures(anyString());
        verify(jwtUtil).generateToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("测试登录失败 - 密码错误")
    void testLoginFailureBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "wrongPassword");

        when(loginAttemptService.isLocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("用户名或密码错误"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        verify(loginAttemptService).recordFailure(anyString());
    }

    @Test
    @DisplayName("测试登录失败 - 账户被锁定")
    void testLoginFailureLocked() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password");

        when(loginAttemptService.isLocked(anyString())).thenReturn(true);
        when(loginAttemptService.getRemainingLockTime(anyString())).thenReturn(1800L);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("测试登出成功")
    void testLogoutSuccess() throws Exception {
        SysUser sysUser = createTestSysUser();
        LoginUser loginUser = new LoginUser(sysUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        when(jwtUtil.parseToken(anyString())).thenReturn("test-token");
        when(jwtUtil.extractExpiration(anyString())).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("登出成功"));

        // 验证 loginAttemptService 没有被调用
        verify(loginAttemptService, never()).recordFailure(anyString());
    }

    @Test
    @DisplayName("测试获取当前用户信息 - 已登录")
    void testGetCurrentUserLoggedIn() throws Exception {
        SysUser sysUser = createTestSysUser();
        LoginUser loginUser = new LoginUser(sysUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loginUser);

        mockMvc.perform(get("/auth/current-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value(1L))
                .andExpect(jsonPath("$.data.username").value("testuser"))
                .andExpect(jsonPath("$.data.realName").value("Test User"));
    }

    @Test
    @DisplayName("测试获取当前用户信息 - 未登录")
    void testGetCurrentUserNotLoggedIn() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(null);

        mockMvc.perform(get("/auth/current-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("未登录"));
    }

    @Test
    @DisplayName("测试 RSA 解密后的登录流程")
    void testLoginWithRsaDecryption() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "encryptedLongPassword123");
        SysUser sysUser = createTestSysUser();
        LoginUser loginUser = new LoginUser(sysUser);

        when(loginAttemptService.isLocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("test-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * 创建测试用的 SysUser
     */
    private SysUser createTestSysUser() {
        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        sysUser.setUsername("testuser");
        sysUser.setRealName("Test User");
        sysUser.setUserType(1);
        sysUser.setStatus(1);
        return sysUser;
    }

    @Test
    @DisplayName("测试登录请求参数验证 - 用户名为空")
    void testLoginValidationEmptyUsername() throws Exception {
        LoginRequest request = new LoginRequest("", "password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试登录请求参数验证 - 密码为空")
    void testLoginValidationEmptyPassword() throws Exception {
        LoginRequest request = new LoginRequest("testuser", null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
