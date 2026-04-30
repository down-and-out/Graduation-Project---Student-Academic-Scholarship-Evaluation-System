package com.scholarship.controller;

import com.scholarship.common.result.Result;
import com.scholarship.common.util.PasswordValidator;
import com.scholarship.common.util.RsaUtils;
import com.scholarship.common.util.UsernameValidator;
import com.scholarship.config.JwtUtil;
import com.scholarship.dto.LoginRequest;
import com.scholarship.entity.SysUser;
import com.scholarship.security.LoginUser;
import com.scholarship.service.LoginAttemptService;
import com.scholarship.service.TokenBlacklistService;
import com.scholarship.vo.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "01-认证管理", description = "用户登录、登出、获取当前用户信息等认证相关接口")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码进行登录，成功后返回 JWT Token 和用户基本信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "401", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "429", description = "登录失败次数过多，账号或IP被锁定")
    })
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        UsernameValidator.validate(request.username());

        String username = request.username();
        String clientIp = getClientIp(http);
        if (loginAttemptService.isAccountLocked(username)) {
            long remainingTime = loginAttemptService.getAccountRemainingLockTime(username);
            log.warn("Login blocked by account lock, username={}, clientIp={}, remainingSeconds={}", username, clientIp, remainingTime);
            return Result.error(429, "账号已被临时锁定，请" + Math.max(1, remainingTime / 60) + "分钟后再试");
        }
        if (loginAttemptService.isIpLocked(clientIp)) {
            long remainingTime = loginAttemptService.getIpRemainingLockTime(clientIp);
            log.warn("Login blocked by ip lock, username={}, clientIp={}, remainingSeconds={}", username, clientIp, remainingTime);
            return Result.error(429, "当前IP请求过于频繁，请" + Math.max(1, remainingTime / 60) + "分钟后再试");
        }

        try {
            String rawPassword = RsaUtils.decryptPassword(request.password());
            PasswordValidator.validate(rawPassword);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, rawPassword);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            loginAttemptService.resetFailures(username, clientIp);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            SysUser sysUser = loginUser.getSysUser();
            String token = jwtUtil.generateToken(sysUser.getUsername(), sysUser.getId());
            log.info("Login success, username={}, userId={}, clientIp={}", sysUser.getUsername(), sysUser.getId(), clientIp);

            LoginResponse loginResponse = LoginResponse.builder()
                    .token(token)
                    .userId(sysUser.getId())
                    .username(sysUser.getUsername())
                    .realName(sysUser.getRealName())
                    .userType(sysUser.getUserType())
                    .avatar(sysUser.getAvatar())
                    .build();
            return Result.success("登录成功", loginResponse);
        } catch (BadCredentialsException e) {
            loginAttemptService.recordFailure(username, clientIp);
            log.warn("Login failed, username={}, clientIp={}", username, clientIp);
            return Result.error(401, "用户名或密码错误");
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录，将当前Token加入黑名单使其失效")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登出成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    public Result<Void> logout(HttpServletRequest http) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            log.info("Logout, username={}, userId={}", loginUser.getUsername(), loginUser.getUserId());
            String token = jwtUtil.parseToken(http.getHeader("Authorization"));
            if (token != null) {
                long expireTime = jwtUtil.extractExpiration(token).getTime() - System.currentTimeMillis();
                if (expireTime > 0) {
                    try {
                        tokenBlacklistService.addToBlacklist(token, expireTime / 1000);
                    } catch (Exception e) {
                        log.warn("Logout blacklist fallback triggered, username={}, userId={}, message={}",
                                loginUser.getUsername(), loginUser.getUserId(), e.getMessage(), e);
                    }
                }
            }
        }

        SecurityContextHolder.clearContext();
        return Result.success("登出成功");
    }

    @GetMapping("/current-user")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "401", description = "未登录")
    })
    public Result<LoginResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            return Result.error("未登录");
        }

        SysUser sysUser = loginUser.getSysUser();
        LoginResponse loginResponse = LoginResponse.builder()
                .userId(sysUser.getId())
                .username(sysUser.getUsername())
                .realName(sysUser.getRealName())
                .userType(sysUser.getUserType())
                .avatar(sysUser.getAvatar())
                .build();
        return Result.success(loginResponse);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip == null || ip.isBlank() ? "unknown" : ip;
    }
}
