package com.scholarship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scholarship.config.JwtUtil;
import com.scholarship.dto.LoginRequest;
import com.scholarship.entity.SysUser;
import com.scholarship.security.LoginUser;
import com.scholarship.service.LoginAttemptService;
import com.scholarship.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController tests")
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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("login success")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "encryptedPassword123");
        SysUser sysUser = createTestSysUser();
        LoginUser loginUser = new LoginUser(sysUser);

        when(loginAttemptService.isAccountLocked(anyString())).thenReturn(false);
        when(loginAttemptService.isIpLocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(loginUser);
        when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("test-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(loginAttemptService).resetFailures(anyString(), anyString());
        verify(jwtUtil).generateToken(anyString(), anyLong());
    }

    @Test
    @DisplayName("login failure - bad credentials")
    void testLoginFailureBadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "wrongPass123");

        when(loginAttemptService.isAccountLocked(anyString())).thenReturn(false);
        when(loginAttemptService.isIpLocked(anyString())).thenReturn(false);
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("用户名或密码错误"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("用户名或密码错误"));

        verify(loginAttemptService).recordFailure(anyString(), anyString());
    }

    @Test
    @DisplayName("login failure - account locked")
    void testLoginFailureAccountLocked() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password");

        when(loginAttemptService.isAccountLocked(anyString())).thenReturn(true);
        when(loginAttemptService.getAccountRemainingLockTime(anyString())).thenReturn(1800L);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(429))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("login failure - ip locked")
    void testLoginFailureIpLocked() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "password");

        when(loginAttemptService.isAccountLocked(anyString())).thenReturn(false);
        when(loginAttemptService.isIpLocked(anyString())).thenReturn(true);
        when(loginAttemptService.getIpRemainingLockTime(anyString())).thenReturn(600L);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(429));
    }

    @Test
    @DisplayName("logout success")
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

        verify(loginAttemptService, never()).recordFailure(anyString(), anyString());
    }

    @Test
    @DisplayName("get current user - logged in")
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
    @DisplayName("get current user - not logged in")
    void testGetCurrentUserNotLoggedIn() throws Exception {
        when(securityContext.getAuthentication()).thenReturn(null);

        mockMvc.perform(get("/auth/current-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("未登录"));
    }

    private SysUser createTestSysUser() {
        SysUser sysUser = new SysUser();
        sysUser.setId(1L);
        sysUser.setUsername("testuser");
        sysUser.setRealName("Test User");
        sysUser.setUserType(1);
        sysUser.setStatus(1);
        return sysUser;
    }
}
